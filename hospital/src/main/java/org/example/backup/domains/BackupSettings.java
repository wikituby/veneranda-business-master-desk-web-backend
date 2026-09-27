package org.example.backup.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_settings")
public class BackupSettings extends PanacheEntity {

    @Column(length = 200)
    public String facilityName;

    @Column(length = 500)
    public String dropboxBackupPath;

    @Column(length = 500)
    public String localBackupRoot = "C:/MediCenter-Backups";

    @Column(columnDefinition = "TEXT")
    public String dropboxAccessToken;

    @Column(columnDefinition = "TEXT")
    public String dropboxRefreshToken;

    @Column(length = 255)
    public String dropboxAppKey;

    @Column(length = 255)
    public String dropboxAppSecret;

    /** oauth_refresh | access_token */
    @Column(length = 50)
    public String tokenMode = "oauth_refresh";

    public Boolean scheduledEnabled = Boolean.TRUE;
    public Boolean localBackupEnabled = Boolean.TRUE;
    public Boolean dropboxBackupEnabled = Boolean.TRUE;

    public Integer intervalMinutes = 5;

    public LocalDateTime lastBackupAt;
    public LocalDateTime lastLocalBackupAt;
    public LocalDateTime lastDropboxBackupAt;

    @Column(length = 50)
    public String lastBackupStatus;

    @Column(length = 1000)
    public String lastError;

    public Boolean dropboxConnected = Boolean.FALSE;
}
