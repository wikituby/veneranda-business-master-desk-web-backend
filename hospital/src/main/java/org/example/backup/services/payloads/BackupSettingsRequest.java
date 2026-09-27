package org.example.backup.services.payloads;

public class BackupSettingsRequest {
    public String dropboxAccessToken;
    public String dropboxRefreshToken;
    public String dropboxAppKey;
    public String dropboxAppSecret;
    public String dropboxBackupPath;
    public String localBackupRoot;
    public String tokenMode;
    public Boolean scheduledEnabled;
    public Boolean localBackupEnabled;
    public Boolean dropboxBackupEnabled;
    public Integer intervalMinutes;
    public String facilityName;
}
