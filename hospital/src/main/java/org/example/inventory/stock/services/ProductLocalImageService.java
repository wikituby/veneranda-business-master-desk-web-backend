package org.example.inventory.stock.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Stores product images on the PC under a dedicated folder and serves them via
 * {@code /stock-item/local-images/{fileName}}.
 */
@ApplicationScoped
public class ProductLocalImageService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductLocalImageService.class);
    public static final String LOCAL_IMAGE_URL_PREFIX = "/stock-item/local-images/";

    @ConfigProperty(name = "vbm.product-images.dir", defaultValue = "__AUTO__")
    String configuredDir;

    private Path imagesDir;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @PostConstruct
    void init() {
        try {
            imagesDir = resolveImagesDir();
            Files.createDirectories(imagesDir);
            LOG.info("Product local images folder: {}", imagesDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Could not create product images folder", e);
        }
    }

    public Path getImagesDir() {
        return imagesDir;
    }

    /**
     * Writes raw image bytes to the product-images folder on disk.
     * Returns localUrl/fileName/localPath only — never persists image bytes or base64 in the DB.
     */
    public Map<String, Object> saveImageBytes(String originalFileName, String contentType, byte[] bytes)
            throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Image data is empty.");
        }
        if (bytes.length > 8 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be 8 MB or smaller.");
        }
        String ext = extensionFor(originalFileName, contentType);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = imagesDir.resolve(storedName).normalize();
        if (!target.startsWith(imagesDir)) {
            throw new IllegalArgumentException("Invalid image path.");
        }
        Files.write(target, bytes);
        return resultFor(storedName, target);
    }

    /** Decode request base64 only to write a file on disk (not stored in DB). */
    public Map<String, Object> saveBase64Image(String originalFileName, String contentType, String base64Data)
            throws IOException {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("Image data is required.");
        }
        String payload = base64Data.trim();
        int comma = payload.indexOf(',');
        if (payload.startsWith("data:") && comma > 0) {
            String meta = payload.substring(5, comma);
            if (contentType == null || contentType.isBlank()) {
                int semi = meta.indexOf(';');
                contentType = semi > 0 ? meta.substring(0, semi) : meta;
            }
            payload = payload.substring(comma + 1);
        }
        byte[] bytes = Base64.getDecoder().decode(payload);
        return saveImageBytes(originalFileName, contentType, bytes);
    }

    public Map<String, Object> cacheFromWebUrl(String webUrl) throws IOException, InterruptedException {
        if (webUrl == null || webUrl.isBlank()) {
            throw new IllegalArgumentException("Web image URL is required.");
        }
        String url = webUrl.trim();
        if (url.startsWith(LOCAL_IMAGE_URL_PREFIX) || url.contains(LOCAL_IMAGE_URL_PREFIX)) {
            String fileName = extractFileNameFromLocalUrl(url);
            Path existing = resolveExistingFile(fileName);
            if (existing != null) {
                return resultFor(fileName, existing);
            }
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("Web image URL must be http(s).");
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .GET()
                .header("User-Agent", "VenerandaBusinessMaster/1.0")
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to download image (HTTP " + response.statusCode() + ").");
        }
        String contentType = response.headers().firstValue("content-type").orElse("image/jpeg");
        String ext = extensionFor(url, contentType);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = imagesDir.resolve(storedName).normalize();
        try (InputStream in = response.body()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.size(target) <= 0) {
            Files.deleteIfExists(target);
            throw new IOException("Downloaded image was empty.");
        }
        return resultFor(storedName, target);
    }

    public Path resolveExistingFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String safe = Paths.get(fileName.trim()).getFileName().toString();
        if (safe.isBlank() || safe.contains("..")) {
            return null;
        }
        Path target = imagesDir.resolve(safe).normalize();
        if (!target.startsWith(imagesDir) || !Files.isRegularFile(target)) {
            return null;
        }
        return target;
    }

    public boolean localFileExists(String localUrlOrFileName) {
        String fileName = extractFileNameFromLocalUrl(localUrlOrFileName);
        return resolveExistingFile(fileName) != null;
    }

    public String extractFileNameFromLocalUrl(String localUrlOrFileName) {
        if (localUrlOrFileName == null) {
            return null;
        }
        String value = localUrlOrFileName.trim();
        if (value.isEmpty()) {
            return null;
        }
        int idx = value.lastIndexOf('/');
        if (idx >= 0 && idx < value.length() - 1) {
            return value.substring(idx + 1);
        }
        return value;
    }

    public String toLocalUrl(String storedFileName) {
        return LOCAL_IMAGE_URL_PREFIX + storedFileName;
    }

    private Map<String, Object> resultFor(String storedName, Path target) {
        return Map.of(
                "fileName", storedName,
                "localUrl", toLocalUrl(storedName),
                "localPath", target.toAbsolutePath().toString()
        );
    }

    private Path resolveImagesDir() {
        if (configuredDir != null && !configuredDir.isBlank() && !"__AUTO__".equals(configuredDir.trim())) {
            return Paths.get(configuredDir.trim()).toAbsolutePath().normalize();
        }
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) {
            home = ".";
        }
        return Paths.get(home, "VenerandaBusinessMaster", "product-images").toAbsolutePath().normalize();
    }

    private static String extensionFor(String nameOrUrl, String contentType) {
        String fromName = "";
        if (nameOrUrl != null) {
            String clean = nameOrUrl;
            int q = clean.indexOf('?');
            if (q > 0) {
                clean = clean.substring(0, q);
            }
            int dot = clean.lastIndexOf('.');
            if (dot > 0 && dot < clean.length() - 1) {
                fromName = clean.substring(dot).toLowerCase(Locale.ROOT);
                if (fromName.length() <= 5 && fromName.matches("\\.[a-z0-9]+")) {
                    return fromName;
                }
            }
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (type.contains("png")) {
            return ".png";
        }
        if (type.contains("webp")) {
            return ".webp";
        }
        if (type.contains("gif")) {
            return ".gif";
        }
        if (type.contains("bmp")) {
            return ".bmp";
        }
        return ".jpg";
    }
}
