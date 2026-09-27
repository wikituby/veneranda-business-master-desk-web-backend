package org.example.backup.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import org.example.backup.domains.BackupSettings;
import org.example.backup.domains.repositories.BackupSettingsRepository;
import org.example.backup.services.payloads.BackupActionResultDTO;
import org.example.subscription.domains.HealthFacility;
import org.example.subscription.domains.repositories.HealthFacilityRepository;

@ApplicationScoped
public class BackupOrchestratorService {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    BackupSettingsRepository settingsRepository;

    @Inject
    HealthFacilityRepository healthFacilityRepository;

    @Inject
    DatabaseBackupService databaseBackupService;

    @Inject
    DropboxService dropboxService;

    public boolean isRunning() {
        return running.get();
    }

    @Transactional
    public BackupSettings ensureSettings() {
        BackupSettings settings = settingsRepository.getSingleton();
        if (settings == null) {
            settings = new BackupSettings();
            settings.facilityName = resolveDefaultFacilityName();
            settings.dropboxBackupPath = buildDropboxPath(settings.facilityName);
            settings.localBackupRoot = "C:/MediCenter-Backups";
            settingsRepository.persist(settings);
        } else {
            refreshGeneratedPaths(settings);
            if (settings.localBackupEnabled == null) {
                settings.localBackupEnabled = Boolean.TRUE;
            }
        }
        return settings;
    }

    @Transactional
    public BackupActionResultDTO runBackupNow() {
        BackupSettings settings = ensureSettings();
        return executeBackup(settings, true);
    }

    @Transactional
    public BackupActionResultDTO runScheduledBackupIfDue() {
        BackupSettings settings = ensureSettings();
        if (!Boolean.TRUE.equals(settings.scheduledEnabled)) {
            return null;
        }
        if (running.get()) {
            return null;
        }
        int interval = settings.intervalMinutes != null && settings.intervalMinutes > 0
                ? settings.intervalMinutes : 5;
        if (settings.lastBackupAt != null
                && settings.lastBackupAt.isAfter(LocalDateTime.now().minusMinutes(interval))) {
            return null;
        }
        return executeBackup(settings, false);
    }

    @Transactional
    public BackupActionResultDTO restoreNow(String source) {
        return restoreNow(source, null);
    }

    @Transactional
    public BackupActionResultDTO restoreNow(String source, String dropboxPathOverride) {
        BackupSettings settings = ensureSettings();
        BackupActionResultDTO result = new BackupActionResultDTO();
        Path tempDownload = null;
        try {
            Path dumpFile;
            String src = source != null ? source.trim().toLowerCase() : "local";
            if ("dropbox".equals(src)) {
                if (!DropboxService.isDropboxConfigured(settings)) {
                    throw new IllegalStateException(
                            "Dropbox is not configured. Set Dropbox credentials under Backups & Restoration first.");
                }
                String remotePath = resolveDropboxRestorePath(settings, dropboxPathOverride);
                if (dropboxPathOverride != null && !dropboxPathOverride.isBlank()) {
                    settings.dropboxBackupPath = normalizeDropboxPath(dropboxPathOverride);
                    settings.dropboxBackupPath = normalizeDropboxBackupPath(settings);
                }
                tempDownload = Files.createTempFile("kmc-restore-", guessSuffix(remotePath));
                try {
                    dropboxService.downloadFile(settings, remotePath, tempDownload);
                } catch (Exception primaryErr) {
                    String lower = remotePath.toLowerCase();
                    String fallback = null;
                    if (lower.endsWith(".zip")) {
                        fallback = remotePath.substring(0, remotePath.length() - 4) + ".sql";
                    } else if (lower.endsWith(".sql")) {
                        fallback = remotePath.substring(0, remotePath.length() - 4) + ".db";
                    }
                    if (fallback != null) {
                        try {
                            dropboxService.downloadFile(settings, fallback, tempDownload);
                            remotePath = fallback;
                        } catch (Exception ignored) {
                            throw primaryErr;
                        }
                    } else {
                        throw primaryErr;
                    }
                }
                dumpFile = tempDownload;
                result.dropboxPath = remotePath;
            } else {
                Path localLatest = resolveLocalBackupForRestore(settings);
                dumpFile = localLatest;
                result.localFilePath = localLatest.toString();
            }

            databaseBackupService.restoreFromDump(dumpFile);
            result.success = true;
            result.message = "Database updated successfully from " + src;
            result.lastBackupStatus = "restored";
            result.lastBackupAt = LocalDateTime.now().toString();
            settings.lastError = null;
        } catch (Exception e) {
            result.success = false;
            result.message = e.getMessage();
            result.lastBackupStatus = "restore_failed";
            settings.lastError = e.getMessage();
        } finally {
            if (tempDownload != null) {
                try {
                    Files.deleteIfExists(tempDownload);
                } catch (Exception ignored) {
                    // best-effort
                }
            }
        }
        return result;
    }

    private String resolveDropboxRestorePath(BackupSettings settings, String override) {
        String path = override != null && !override.isBlank()
                ? override.trim()
                : settings.dropboxBackupPath;
        path = normalizeDropboxPath(path);
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Dropbox backup path is empty. Choose a path first.");
        }
        // Folder path -> use latest zip dump name
        if (!looksLikeFile(path)) {
            String zipName = databaseBackupService.buildDropboxBackupFileName();
            return path.endsWith("/") ? path + zipName : path + "/" + zipName;
        }
        return normalizeDropboxBackupFilePath(path);
    }

    /** Normalize a Dropbox file path to the current zip backup name when applicable. */
    private String normalizeDropboxBackupFilePath(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".db")) {
            return path.substring(0, path.length() - 3) + ".zip";
        }
        if (lower.endsWith(".sql")) {
            return path.substring(0, path.length() - 4) + ".zip";
        }
        return path;
    }

    private static String normalizeDropboxPath(String path) {
        if (path == null) {
            return null;
        }
        String p = path.trim().replace('\\', '/');
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p;
    }

    private static boolean looksLikeFile(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.contains(".");
    }

    private static String guessSuffix(String remotePath) {
        String lower = remotePath.toLowerCase();
        if (lower.endsWith(".zip")) {
            return ".zip";
        }
        if (lower.endsWith(".sql")) {
            return ".sql";
        }
        return ".db";
    }

    private BackupActionResultDTO executeBackup(BackupSettings settings, boolean manual) {
        if (!running.compareAndSet(false, true)) {
            BackupActionResultDTO busy = new BackupActionResultDTO();
            busy.success = false;
            busy.message = "A backup is already running";
            return busy;
        }

        BackupActionResultDTO result = new BackupActionResultDTO();
        boolean localOk = false;
        boolean dropboxOk = false;
        boolean mobileSqliteOk = false;
        StringBuilder errors = new StringBuilder();
        Path dropboxZip = null;
        Path mobileSqlitePath = null;

        try {
            refreshGeneratedPaths(settings);
            Path facilityDir = resolveFacilityDir(settings);
            Files.createDirectories(facilityDir);

            Path latest = databaseBackupService.buildLatestLocalPath(facilityDir);
            databaseBackupService.createDump(latest);

            mobileSqlitePath = databaseBackupService.buildLatestMobileSqlitePath(facilityDir);
            if (databaseBackupService.isMobileSqliteEnabled()) {
                try {
                    databaseBackupService.exportMobileSqliteFromDump(
                            latest,
                            mobileSqlitePath,
                            buildMobileDropboxPath(settings));
                    mobileSqliteOk = true;
                    result.mobileSqliteLocalPath = mobileSqlitePath.toString();
                } catch (Exception e) {
                    errors.append("Mobile SQLite: ").append(e.getMessage());
                }
            }

            databaseBackupService.removeStaleLocalBackups(facilityDir);

            // Local backup always runs — never blocked by Dropbox configuration or upload.
            localOk = true;
            settings.lastLocalBackupAt = LocalDateTime.now();
            result.localFilePath = latest.toString();

            boolean dropboxRequested = Boolean.TRUE.equals(settings.dropboxBackupEnabled);
            if (dropboxRequested && DropboxService.isDropboxConfigured(settings)) {
                if (dropboxService.isInternetAvailable()) {
                    try {
                        String dropboxPath = normalizeDropboxBackupPath(settings);
                        settings.dropboxBackupPath = dropboxPath;
                        dropboxZip = databaseBackupService.zipSqlDump(latest);
                        dropboxService.uploadFile(settings, dropboxZip, dropboxPath);
                        dropboxOk = true;
                        settings.lastDropboxBackupAt = LocalDateTime.now();
                        result.dropboxPath = settings.dropboxBackupPath;

                        if (mobileSqliteOk && mobileSqlitePath != null && Files.exists(mobileSqlitePath)) {
                            try {
                                String mobileDropboxPath = buildMobileDropboxPath(settings);
                                dropboxService.uploadFile(settings, mobileSqlitePath, mobileDropboxPath);
                                result.mobileSqliteDropboxPath = mobileDropboxPath;
                            } catch (Exception e) {
                                errors.append(errors.length() > 0 ? "; " : "")
                                        .append("Dropbox mobile SQLite: ")
                                        .append(e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        errors.append("Dropbox: ").append(e.getMessage());
                    }
                } else {
                    errors.append("Dropbox: skipped (no internet)");
                }
            }

            settings.lastBackupAt = LocalDateTime.now();
            settings.lastBackupStatus = dropboxOk ? "success" : "local_only";
            result.success = true;
            if (dropboxOk) {
                result.message = manual
                        ? "Backup completed (local + Dropbox"
                        : "Scheduled backup completed (local + Dropbox";
                if (mobileSqliteOk) {
                    result.message += " + kmc-latest.db";
                }
                result.message += ")";
            } else if (errors.length() > 0) {
                result.message = (manual ? "Local backup completed" : "Scheduled local backup completed");
                if (mobileSqliteOk) {
                    result.message += " (kmc-latest.db)";
                }
                result.message += "; " + errors;
            } else {
                result.message = manual ? "Local backup completed" : "Scheduled local backup completed";
                if (mobileSqliteOk) {
                    result.message += " (kmc-latest.db)";
                }
            }

            settings.lastError = errors.length() > 0 ? errors.toString() : null;
            result.lastBackupAt = settings.lastBackupAt.toString();
            result.lastBackupStatus = settings.lastBackupStatus;
        } catch (Exception e) {
            settings.lastBackupStatus = "failed";
            settings.lastError = e.getMessage();
            result.success = false;
            result.message = e.getMessage();
            result.lastBackupStatus = "failed";
        } finally {
            if (dropboxZip != null) {
                try {
                    Files.deleteIfExists(dropboxZip);
                } catch (Exception ignored) {
                    // best-effort
                }
            }
            running.set(false);
        }
        return result;
    }

    public String buildDropboxPath(String facilityName) {
        return sanitizeFacilityName(facilityName) + "/" + databaseBackupService.buildDropboxBackupFileName();
    }

    /** Dropbox path for mother-mobile SQLite snapshot ({@code kmc-latest.db}). */
    public String buildMobileDropboxPath(BackupSettings settings) {
        String zipPath = normalizeDropboxBackupPath(settings);
        int slash = zipPath.lastIndexOf('/');
        if (slash >= 0) {
            return zipPath.substring(0, slash + 1) + databaseBackupService.buildMobileSqliteFileName();
        }
        return sanitizeFacilityName(settings.facilityName) + "/"
                + databaseBackupService.buildMobileSqliteFileName();
    }

    public Path resolveLatestMobileSqliteBackup(BackupSettings settings) {
        return databaseBackupService.buildLatestMobileSqlitePath(resolveFacilityDir(settings));
    }

    /** Ensure Dropbox path uses a .zip archive (containing the SQL dump). */
    public String normalizeDropboxBackupPath(BackupSettings settings) {
        String path = settings.dropboxBackupPath;
        if (path == null || path.isBlank()) {
            return buildDropboxPath(settings.facilityName);
        }
        path = normalizeDropboxPath(path);
        String zipFile = databaseBackupService.buildDropboxBackupFileName();
        if (!looksLikeFile(path)) {
            return path.endsWith("/") ? path + zipFile : path + "/" + zipFile;
        }
        return normalizeDropboxBackupFilePath(path);
    }

    public Path resolveLatestLocalBackup(BackupSettings settings) {
        return databaseBackupService.buildLatestLocalPath(resolveFacilityDir(settings));
    }

    private Path resolveLocalBackupForRestore(BackupSettings settings) throws Exception {
        Path facilityDir = resolveFacilityDir(settings);
        Path sql = databaseBackupService.buildLatestLocalPath(facilityDir);
        if (Files.exists(sql)) {
            return sql;
        }
        Path legacyKmc = facilityDir.resolve("kmc.db");
        if (Files.exists(legacyKmc)) {
            return legacyKmc;
        }
        throw new IllegalStateException("No local backup found at " + sql);
    }

    public Path resolveFacilityDir(BackupSettings settings) {
        String root = settings.localBackupRoot != null && !settings.localBackupRoot.isBlank()
                ? settings.localBackupRoot.trim() : "C:/MediCenter-Backups";
        return Path.of(root.replace('\\', '/')).resolve(sanitizeFacilityName(settings.facilityName));
    }

    private void refreshGeneratedPaths(BackupSettings settings) {
        if (settings.facilityName == null || settings.facilityName.isBlank()) {
            settings.facilityName = resolveDefaultFacilityName();
        }
        if (settings.dropboxBackupPath == null || settings.dropboxBackupPath.isBlank()) {
            settings.dropboxBackupPath = buildDropboxPath(settings.facilityName);
        } else {
            settings.dropboxBackupPath = normalizeDropboxBackupPath(settings);
        }
    }

    private String resolveDefaultFacilityName() {
        HealthFacility facility = healthFacilityRepository.findAll().firstResult();
        if (facility != null && facility.name != null && !facility.name.isBlank()) {
            return facility.name.trim();
        }
        return "Kavuma-Medical-Clinic";
    }

    public static String sanitizeFacilityName(String name) {
        if (name == null || name.isBlank()) {
            return "facility";
        }
        return name.trim()
                .replaceAll("[^a-zA-Z0-9._-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
