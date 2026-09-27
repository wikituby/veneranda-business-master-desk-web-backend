package org.example.backup.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.backup.domains.BackupSettings;
import org.example.backup.domains.repositories.BackupSettingsRepository;
import org.example.backup.services.payloads.BackupSettingsDTO;
import org.example.backup.services.payloads.BackupSettingsRequest;
import org.example.subscription.services.SubscriptionService;
import org.example.user.domains.User;

@ApplicationScoped
public class BackupSettingsService {

    @Inject
    BackupSettingsRepository settingsRepository;

    @Inject
    BackupOrchestratorService orchestratorService;

    @Inject
    DropboxService dropboxService;

    public void assertCanManage(User user) {
        if (user == null || !SubscriptionService.canManageSubscription(user.role)) {
            throw new WebApplicationException("Only MD or admin users can manage backups", 403);
        }
    }

    @Transactional
    public BackupSettingsDTO getSettings(User user) {
        assertCanManage(user);
        BackupSettings settings = orchestratorService.ensureSettings();
        return toDto(settings);
    }

    @Transactional
    public BackupSettingsDTO saveSettings(User user, BackupSettingsRequest request) {
        assertCanManage(user);
        BackupSettings settings = orchestratorService.ensureSettings();

        if (request == null) {
            throw new WebApplicationException("Request body required", 400);
        }

        if (request.facilityName != null && !request.facilityName.isBlank()) {
            settings.facilityName = request.facilityName.trim();
        }
        if (request.localBackupRoot != null && !request.localBackupRoot.isBlank()) {
            settings.localBackupRoot = request.localBackupRoot.trim().replace('\\', '/');
        }
        if (request.dropboxBackupPath != null) {
            if (!request.dropboxBackupPath.isBlank()) {
                settings.dropboxBackupPath = request.dropboxBackupPath.trim().replace('\\', '/');
                while (settings.dropboxBackupPath.startsWith("/")) {
                    settings.dropboxBackupPath = settings.dropboxBackupPath.substring(1);
                }
                settings.dropboxBackupPath = orchestratorService.normalizeDropboxBackupPath(settings);
            } else {
                settings.dropboxBackupPath = orchestratorService.buildDropboxPath(settings.facilityName);
            }
        }
        if (request.dropboxAppKey != null) {
            settings.dropboxAppKey = blankToNull(request.dropboxAppKey);
        }
        if (request.dropboxAppSecret != null) {
            settings.dropboxAppSecret = blankToNull(request.dropboxAppSecret);
        }
        if (request.dropboxAccessToken != null) {
            settings.dropboxAccessToken = blankToNull(request.dropboxAccessToken);
        }
        if (request.dropboxRefreshToken != null) {
            settings.dropboxRefreshToken = blankToNull(request.dropboxRefreshToken);
        }
        if (DropboxService.hasRefreshCredentials(settings)) {
            settings.tokenMode = "oauth_refresh";
            settings.dropboxAccessToken = null;
        } else if (request.tokenMode != null && !request.tokenMode.isBlank()) {
            settings.tokenMode = request.tokenMode.trim();
        }
        if (request.scheduledEnabled != null) {
            settings.scheduledEnabled = request.scheduledEnabled;
        }
        if (request.localBackupEnabled != null) {
            settings.localBackupEnabled = request.localBackupEnabled;
        }
        if (request.dropboxBackupEnabled != null) {
            settings.dropboxBackupEnabled = request.dropboxBackupEnabled;
        }
        if (request.intervalMinutes != null && request.intervalMinutes > 0) {
            settings.intervalMinutes = request.intervalMinutes;
        }

        if (settings.dropboxRefreshToken != null && settings.dropboxAppKey != null && settings.dropboxAppSecret != null) {
            settings.dropboxConnected = Boolean.TRUE;
        }

        return toDto(settings);
    }

    @Transactional
    public BackupSettingsDTO logoutDropbox(User user) {
        assertCanManage(user);
        BackupSettings settings = orchestratorService.ensureSettings();
        dropboxService.revokeSession(settings);
        return toDto(settings);
    }

    private BackupSettingsDTO toDto(BackupSettings settings) {
        String localPreview = orchestratorService.resolveLatestLocalBackup(settings).toString();
        String mobileLocalPreview = orchestratorService.resolveLatestMobileSqliteBackup(settings).toString();
        String mobileDropboxPreview = orchestratorService.buildMobileDropboxPath(settings);
        return BackupSettingsDTO.from(settings, localPreview, mobileLocalPreview, mobileDropboxPreview);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
