package org.example.backup.services;



import jakarta.enterprise.context.ApplicationScoped;

import jakarta.json.Json;

import jakarta.json.JsonObject;

import jakarta.json.JsonReader;

import java.io.IOException;

import java.io.StringReader;

import java.net.URI;

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.time.Duration;

import org.example.backup.domains.BackupSettings;



@ApplicationScoped

public class DropboxService {



    private final HttpClient http = HttpClient.newBuilder()

            .connectTimeout(Duration.ofSeconds(15))

            .build();



    public boolean isInternetAvailable() {

        try {

            HttpRequest req = HttpRequest.newBuilder()

                    .uri(URI.create("https://www.dropbox.com"))

                    .timeout(Duration.ofSeconds(5))

                    .GET()

                    .build();

            HttpResponse<Void> res = http.send(req, HttpResponse.BodyHandlers.discarding());

            return res.statusCode() >= 200 && res.statusCode() < 500;

        } catch (Exception e) {

            return false;

        }

    }



    public static boolean hasRefreshCredentials(BackupSettings settings) {

        return settings != null

                && settings.dropboxRefreshToken != null && !settings.dropboxRefreshToken.isBlank()

                && settings.dropboxAppKey != null && !settings.dropboxAppKey.isBlank()

                && settings.dropboxAppSecret != null && !settings.dropboxAppSecret.isBlank();

    }



    /** True when Dropbox upload/download can run (refresh OAuth or long-lived access token). */

    public static boolean isDropboxConfigured(BackupSettings settings) {

        if (settings == null) {

            return false;

        }

        if (hasRefreshCredentials(settings)) {

            return true;

        }

        return settings.dropboxAccessToken != null && !settings.dropboxAccessToken.isBlank();

    }



    /**

     * Resolves a valid Dropbox access token. Refresh credentials always take priority so backups

     * work with only a refresh token + app key + secret (no manual access token required).

     */

    public String resolveAccessToken(BackupSettings settings) throws IOException, InterruptedException {

        if (hasRefreshCredentials(settings)) {

            return refreshAccessToken(settings);

        }

        if (settings.dropboxAccessToken != null && !settings.dropboxAccessToken.isBlank()) {

            settings.tokenMode = "access_token";

            return settings.dropboxAccessToken.trim();

        }

        throw new IllegalStateException(

                "Dropbox OAuth credentials are incomplete. Provide refresh token + app key + secret.");

    }



    public String refreshAccessToken(BackupSettings settings) throws IOException, InterruptedException {

        if (!hasRefreshCredentials(settings)) {

            throw new IllegalStateException("Dropbox refresh credentials are incomplete");

        }



        String body = "grant_type=refresh_token"

                + "&refresh_token=" + urlEncode(settings.dropboxRefreshToken.trim())

                + "&client_id=" + urlEncode(settings.dropboxAppKey.trim())

                + "&client_secret=" + urlEncode(settings.dropboxAppSecret.trim());



        HttpRequest req = HttpRequest.newBuilder()

                .uri(URI.create("https://api.dropbox.com/oauth2/token"))

                .timeout(Duration.ofSeconds(30))

                .header("Content-Type", "application/x-www-form-urlencoded")

                .POST(HttpRequest.BodyPublishers.ofString(body))

                .build();



        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {

            throw new IllegalStateException("Dropbox token refresh failed: " + res.body());

        }



        JsonObject json;

        try (JsonReader reader = Json.createReader(new StringReader(res.body()))) {

            json = reader.readObject();

        }

        String access = json.containsKey("access_token") ? json.getString("access_token") : null;

        if (access == null || access.isBlank()) {

            throw new IllegalStateException("Dropbox token refresh returned no access token");

        }

        settings.dropboxAccessToken = access;

        settings.tokenMode = "oauth_refresh";

        settings.dropboxConnected = Boolean.TRUE;

        return access;

    }



    public void uploadFile(BackupSettings settings, Path localFile, String dropboxPath) throws Exception {

        uploadFile(settings, localFile, dropboxPath, false);

    }



    private void uploadFile(BackupSettings settings, Path localFile, String dropboxPath, boolean isRetry)

            throws Exception {

        String token = isRetry ? refreshAccessToken(settings) : resolveAccessToken(settings);

        String normalizedPath = dropboxPath.startsWith("/") ? dropboxPath : "/" + dropboxPath;

        String argJson = Json.createObjectBuilder()

                .add("path", normalizedPath)

                .add("mode", "overwrite")

                .add("autorename", false)

                .add("mute", true)

                .build()

                .toString();



        byte[] fileBytes = Files.readAllBytes(localFile);



        HttpRequest req = HttpRequest.newBuilder()

                .uri(URI.create("https://content.dropboxapi.com/2/files/upload"))

                .timeout(Duration.ofMinutes(10))

                .header("Authorization", "Bearer " + token)

                .header("Content-Type", "application/octet-stream")

                .header("Dropbox-API-Arg", argJson)

                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))

                .build();



        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() >= 400) {

            if (!isRetry && isInvalidAccessToken(res.statusCode(), res.body()) && hasRefreshCredentials(settings)) {

                settings.dropboxAccessToken = null;

                uploadFile(settings, localFile, dropboxPath, true);

                return;

            }

            throw new IllegalStateException("Dropbox upload failed: " + res.body());

        }

        settings.dropboxConnected = Boolean.TRUE;

    }



    public Path downloadFile(BackupSettings settings, String dropboxPath, Path targetFile) throws Exception {

        return downloadFile(settings, dropboxPath, targetFile, false);

    }



    private Path downloadFile(BackupSettings settings, String dropboxPath, Path targetFile, boolean isRetry)

            throws Exception {

        String token = isRetry ? refreshAccessToken(settings) : resolveAccessToken(settings);

        String normalizedPath = dropboxPath.startsWith("/") ? dropboxPath : "/" + dropboxPath;

        String argJson = Json.createObjectBuilder()

                .add("path", normalizedPath)

                .build()

                .toString();



        HttpRequest req = HttpRequest.newBuilder()

                .uri(URI.create("https://content.dropboxapi.com/2/files/download"))

                .timeout(Duration.ofMinutes(10))

                .header("Authorization", "Bearer " + token)

                .header("Dropbox-API-Arg", argJson)

                .POST(HttpRequest.BodyPublishers.noBody())

                .build();



        HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());

        if (res.statusCode() >= 400) {

            String errBody = res.body() != null ? new String(res.body(), StandardCharsets.UTF_8) : "";

            if (!isRetry && isInvalidAccessToken(res.statusCode(), errBody) && hasRefreshCredentials(settings)) {

                settings.dropboxAccessToken = null;

                return downloadFile(settings, dropboxPath, targetFile, true);

            }

            throw new IllegalStateException("Dropbox download failed: HTTP " + res.statusCode()

                    + (errBody.isBlank() ? "" : " — " + errBody));

        }

        Files.createDirectories(targetFile.getParent());

        Files.write(targetFile, res.body());

        return targetFile;

    }



    public void revokeSession(BackupSettings settings) {

        settings.dropboxAccessToken = null;

        settings.dropboxRefreshToken = null;

        settings.dropboxAppKey = null;

        settings.dropboxAppSecret = null;

        settings.dropboxConnected = Boolean.FALSE;

        settings.tokenMode = "oauth_refresh";

    }



    private static boolean isInvalidAccessToken(int statusCode, String body) {

        if (statusCode == 401) {

            return true;

        }

        return body != null && body.contains("invalid_access_token");

    }



    private static String urlEncode(String value) {

        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);

    }

}


