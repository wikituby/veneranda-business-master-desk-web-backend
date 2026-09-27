package org.example.backup.services.payloads;

public class BackupActionResultDTO {
    public boolean success;
    public String message;
    public String lastBackupAt;
    public String lastBackupStatus;
    public String localFilePath;
    public String dropboxPath;
    /** Local path to kmc-latest.db (mother mobile SQLite snapshot). */
    public String mobileSqliteLocalPath;
    /** Dropbox path where kmc-latest.db was uploaded. */
    public String mobileSqliteDropboxPath;
}
