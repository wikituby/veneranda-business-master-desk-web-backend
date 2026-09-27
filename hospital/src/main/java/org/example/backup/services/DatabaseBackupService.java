package org.example.backup.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DatabaseBackupService {

    private static final Logger LOG = Logger.getLogger(DatabaseBackupService.class);

    /** SQLite snapshot for mother mobile app (Dropbox + local restore on phone). */
    public static final String MOBILE_SQLITE_FILE_NAME = "kmc-latest.db";

    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;

    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;

    @ConfigProperty(name = "backup.mysqldump-path", defaultValue = "mysqldump")
    String mysqldumpPath;

    @ConfigProperty(name = "backup.mysql-path", defaultValue = "mysql")
    String mysqlPath;

    @ConfigProperty(name = "backup.mobile-sqlite.enabled", defaultValue = "true")
    boolean mobileSqliteEnabled;

    @ConfigProperty(name = "backup.mobile-sqlite.python.executable")
    Optional<String> mobileSqlitePython;

    @ConfigProperty(name = "backup.mobile-sqlite.script-path", defaultValue = "scripts/import_mysql_to_sqlite.py")
    String mobileSqliteScriptPath;

    public boolean isMobileSqliteEnabled() {
        return mobileSqliteEnabled;
    }

    public String extractDatabaseName() {
        // jdbc:mysql://localhost:3306/vena
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash < 0 || slash == jdbcUrl.length() - 1) {
            return "vena";
        }
        String tail = jdbcUrl.substring(slash + 1);
        int q = tail.indexOf('?');
        return q >= 0 ? tail.substring(0, q) : tail;
    }

    public Path createDump(Path targetFile) throws Exception {
        Files.createDirectories(targetFile.getParent());
        String dbName = extractDatabaseName();

        ProcessBuilder pb = new ProcessBuilder(
                mysqldumpPath,
                "-h", "localhost",
                "-u", dbUser,
                "--password=" + dbPassword,
                "--single-transaction",
                "--routines",
                "--triggers",
                dbName
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (var in = process.getInputStream()) {
            Files.copy(in, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException("mysqldump failed with exit code " + code);
        }
        if (!Files.exists(targetFile) || Files.size(targetFile) == 0) {
            throw new IllegalStateException("Backup file was not created or is empty");
        }
        return targetFile;
    }

    public void restoreFromDump(Path dumpFile) throws Exception {
        if (!Files.exists(dumpFile)) {
            throw new IllegalStateException("Backup file not found: " + dumpFile);
        }

        Path sqlFile = dumpFile;
        Path extracted = null;
        String name = dumpFile.getFileName().toString().toLowerCase();
        if (name.endsWith(".zip")) {
            extracted = extractSqlFromZip(dumpFile);
            sqlFile = extracted;
        }

        try {
            restoreSqlFile(sqlFile);
        } finally {
            if (extracted != null) {
                Files.deleteIfExists(extracted);
            }
        }
    }

    private void restoreSqlFile(Path sqlFile) throws Exception {
        String dbName = extractDatabaseName();

        ProcessBuilder pb = new ProcessBuilder(
                mysqlPath,
                "-h", "localhost",
                "-u", dbUser,
                "--password=" + dbPassword,
                dbName
        );
        pb.redirectInput(sqlFile.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder err = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                err.append(line).append('\n');
            }
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException("mysql restore failed: " + err);
        }
    }

    /** Unzip and return backup.sql if present, otherwise the first .sql entry. */
    private Path extractSqlFromZip(Path zipFile) throws Exception {
        Path preferred = null;
        Path firstSql = null;

        try (java.util.zip.ZipInputStream zis =
                     new java.util.zip.ZipInputStream(Files.newInputStream(zipFile))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                String base = entryName.substring(entryName.lastIndexOf('/') + 1).toLowerCase();
                if (!base.endsWith(".sql")) {
                    continue;
                }
                Path target = Files.createTempFile("kmc-entry-", ".sql");
                Files.copy(zis, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                if ("backup.sql".equals(base)) {
                    if (preferred != null) {
                        Files.deleteIfExists(preferred);
                    }
                    preferred = target;
                } else if (firstSql == null) {
                    firstSql = target;
                } else {
                    Files.deleteIfExists(target);
                }
            }
        }

        Path chosen = preferred != null ? preferred : firstSql;
        if (chosen == null) {
            throw new IllegalStateException("No .sql file found inside zip: " + zipFile);
        }
        if (preferred != null && firstSql != null && !preferred.equals(firstSql)) {
            Files.deleteIfExists(firstSql);
        }
        if (Files.size(chosen) == 0) {
            Files.deleteIfExists(chosen);
            throw new IllegalStateException("Extracted SQL dump is empty");
        }
        return chosen;
    }

    public Path buildLatestLocalPath(Path facilityDir) {
        return facilityDir.resolve(buildLatestBackupFileName());
    }

    public String buildLatestBackupFileName() {
        return extractDatabaseName() + ".sql";
    }

    public String buildDropboxBackupFileName() {
        return extractDatabaseName() + ".zip";
    }

    public String buildMobileSqliteFileName() {
        return MOBILE_SQLITE_FILE_NAME;
    }

    public Path buildLatestMobileSqlitePath(Path facilityDir) {
        return facilityDir.resolve(MOBILE_SQLITE_FILE_NAME);
    }

    /** Convert a MySQL dump to mother-app SQLite ({@code kmc-latest.db}). */
    public void exportMobileSqliteFromDump(Path sqlDump, Path outputDb, String dropboxPath) throws Exception {
        if (!mobileSqliteEnabled) {
            throw new IllegalStateException("Mobile SQLite export is disabled");
        }
        if (!Files.exists(sqlDump) || Files.size(sqlDump) == 0) {
            throw new IllegalStateException("SQL dump is missing or empty: " + sqlDump);
        }

        Path script = resolveMobileSqliteScriptPath();
        if (!Files.isRegularFile(script)) {
            throw new IllegalStateException("Mobile SQLite script not found: " + script);
        }

        String python = resolveMobileSqlitePython();
        List<String> command = new ArrayList<>();
        command.add(python);
        command.add(script.toAbsolutePath().toString());
        command.add(sqlDump.toAbsolutePath().toString());
        command.add(outputDb.toAbsolutePath().toString());
        if (dropboxPath != null && !dropboxPath.isBlank()) {
            command.add("--dropbox-path");
            command.add(dropboxPath);
        }

        LOG.infof("Exporting mobile SQLite: %s -> %s", sqlDump, outputDb);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }

        int code = process.waitFor();
        if (code != 0) {
            throw new IllegalStateException(
                    "Mobile SQLite export failed (code " + code + "): " + output);
        }

        if (!Files.exists(outputDb) || Files.size(outputDb) == 0) {
            throw new IllegalStateException("Mobile SQLite file was not created or is empty: " + outputDb);
        }

        LOG.infof("Mobile SQLite export completed: %s (%d bytes). %s",
                outputDb, Files.size(outputDb), output.toString().trim());
    }

    private Path resolveMobileSqliteScriptPath() {
        Path configured = Path.of(mobileSqliteScriptPath);
        if (configured.isAbsolute() && Files.isRegularFile(configured)) {
            return configured;
        }

        Path relative = Path.of(System.getProperty("user.dir", ".")).resolve(mobileSqliteScriptPath);
        if (Files.isRegularFile(relative)) {
            return relative;
        }

        Path parent = Path.of(System.getProperty("user.dir", ".")).getParent();
        if (parent != null) {
            Path parentRelative = parent.resolve(mobileSqliteScriptPath);
            if (Files.isRegularFile(parentRelative)) {
                return parentRelative;
            }
        }

        Path motherMobileScript = Path.of(System.getProperty("user.dir", "."))
                .resolve("../../../../mother mobile/tools/mysql_port/import_mysql_to_sqlite.py")
                .normalize();
        if (Files.isRegularFile(motherMobileScript)) {
            return motherMobileScript;
        }

        return relative;
    }

    private String resolveMobileSqlitePython() throws Exception {
        if (mobileSqlitePython.isPresent() && !mobileSqlitePython.get().isBlank()) {
            return mobileSqlitePython.get().trim();
        }

        String env = System.getenv("MOBILE_SQLITE_PYTHON");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }

        for (String candidate : List.of("python", "python3")) {
            if (isPythonRunnable(candidate)) {
                return candidate;
            }
        }

        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            String onPath = findPythonOnWindowsPath();
            if (onPath != null) {
                return onPath;
            }
        }

        throw new IllegalStateException(
                "Python not found for mobile SQLite export. Install Python 3 or set "
                        + "backup.mobile-sqlite.python.executable / MOBILE_SQLITE_PYTHON.");
    }

    private static boolean isPythonRunnable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String findPythonOnWindowsPath() {
        try {
            Process process = new ProcessBuilder("where.exe", "python").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && Files.isRegularFile(Path.of(line))) {
                        return line;
                    }
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
            // best-effort
        }
        return null;
    }

    /** Zip a SQL dump for Dropbox upload (single entry named like vena.sql). */
    public Path zipSqlDump(Path sqlFile) throws Exception {
        if (!Files.exists(sqlFile) || Files.size(sqlFile) == 0) {
            throw new IllegalStateException("SQL dump is missing or empty: " + sqlFile);
        }
        Path zipFile = Files.createTempFile("backup-upload-", ".zip");
        String entryName = sqlFile.getFileName().toString();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            Files.copy(sqlFile, zos);
            zos.closeEntry();
        }
        if (!Files.exists(zipFile) || Files.size(zipFile) == 0) {
            Files.deleteIfExists(zipFile);
            throw new IllegalStateException("Zip archive was not created or is empty");
        }
        return zipFile;
    }

    /** Remove older timestamped dumps; only the single latest file is kept. */
    public void removeStaleLocalBackups(Path facilityDir) throws Exception {
        if (!Files.isDirectory(facilityDir)) {
            return;
        }
        Path latestSql = buildLatestLocalPath(facilityDir);
        Path latestMobile = buildLatestMobileSqlitePath(facilityDir);
        try (Stream<Path> entries = Files.list(facilityDir)) {
            entries.filter(Files::isRegularFile)
                    .filter(p -> !p.equals(latestSql) && !p.equals(latestMobile))
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("kmc-")
                                || name.endsWith(".sql")
                                || name.endsWith(".zip")
                                || name.endsWith(".db");
                    })
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                            // best-effort cleanup
                        }
                    });
        }
    }
}
