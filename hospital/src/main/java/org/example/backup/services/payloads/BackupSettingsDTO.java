package org.example.backup.services.payloads;

import org.example.backup.domains.BackupSettings;

public class BackupSettingsDTO {
    public Long id;
    public String facilityName;
    public String dropboxBackupPath;
    public String localBackupRoot;
    public String dropboxAccessToken;
    public String dropboxRefreshToken;
    public String dropboxAppKey;
    public String dropboxAppSecret;
    public String tokenMode;
    public Boolean scheduledEnabled;
    public Boolean localBackupEnabled;
    public Boolean dropboxBackupEnabled;
    public Integer intervalMinutes;
    public String lastBackupAt;
    public String lastLocalBackupAt;
    public String lastDropboxBackupAt;
    public String lastBackupStatus;
    public String lastError;
    public Boolean dropboxConnected;
    public String localBackupPathPreview;
    public String mobileSqliteLocalPathPreview;
    public String mobileSqliteDropboxPathPreview;

    public static BackupSettingsDTO from(BackupSettings s, String localPreview) {
        return from(s, localPreview, null, null);
    }

    public static BackupSettingsDTO from(
            BackupSettings s,
            String localPreview,
            String mobileSqliteLocalPreview,
            String mobileSqliteDropboxPreview) {
        BackupSettingsDTO dto = new BackupSettingsDTO();
        dto.id = s.id;
        dto.facilityName = s.facilityName;
        dto.dropboxBackupPath = s.dropboxBackupPath;
        dto.localBackupRoot = s.localBackupRoot;
        dto.dropboxAccessToken = s.dropboxAccessToken;
        dto.dropboxRefreshToken = s.dropboxRefreshToken;
        dto.dropboxAppKey = s.dropboxAppKey;
        dto.dropboxAppSecret = s.dropboxAppSecret;
        dto.tokenMode = s.tokenMode;
        dto.scheduledEnabled = s.scheduledEnabled;
        dto.localBackupEnabled = s.localBackupEnabled;
        dto.dropboxBackupEnabled = s.dropboxBackupEnabled;
        dto.intervalMinutes = s.intervalMinutes;
        dto.lastBackupAt = s.lastBackupAt != null ? s.lastBackupAt.toString() : null;
        dto.lastLocalBackupAt = s.lastLocalBackupAt != null ? s.lastLocalBackupAt.toString() : null;
        dto.lastDropboxBackupAt = s.lastDropboxBackupAt != null ? s.lastDropboxBackupAt.toString() : null;
        dto.lastBackupStatus = s.lastBackupStatus;
        dto.lastError = s.lastError;
        dto.dropboxConnected = s.dropboxConnected;
        dto.localBackupPathPreview = localPreview;
        dto.mobileSqliteLocalPathPreview = mobileSqliteLocalPreview;
        dto.mobileSqliteDropboxPathPreview = mobileSqliteDropboxPreview;
        return dto;
    }
}
