package org.example.subscription.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Loads the facility logo for PDF documents: configured URL first, then classpath fallback.
 */
@ApplicationScoped
public class FacilityPdfLogoService {

    private static final String DEFAULT_CLASSPATH_LOGO = "logo.png";

    @Inject
    FacilityBrandingService facilityBrandingService;

    public Image createLogoImage() {
        FacilityBranding branding = facilityBrandingService.resolveDefaultBranding();
        String logoUrl = branding != null ? branding.facilityLogoUrl : null;
        return createLogoImage(logoUrl);
    }

    public Image createLogoImage(String logoUrl) {
        try {
            ImageData imageData = loadImageData(logoUrl);
            Image logo = new Image(imageData);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            return logo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load facility logo for PDF.", e);
        }
    }

    private ImageData loadImageData(String logoUrl) throws IOException {
        if (logoUrl != null && !logoUrl.isBlank()) {
            String trimmed = logoUrl.trim();
            try {
                return ImageDataFactory.create(URI.create(trimmed).toURL());
            } catch (Exception ignored) {
                try {
                    return ImageDataFactory.create(trimmed);
                } catch (Exception ignoredAgain) {
                    /* use default below */
                }
            }
        }

        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(DEFAULT_CLASSPATH_LOGO);
        if (stream == null) {
            throw new IOException("Default logo not found on classpath: " + DEFAULT_CLASSPATH_LOGO);
        }
        try (InputStream in = stream) {
            return ImageDataFactory.create(in.readAllBytes());
        }
    }
}
