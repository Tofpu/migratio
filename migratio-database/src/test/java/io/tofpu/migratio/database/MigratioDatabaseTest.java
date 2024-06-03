package io.tofpu.migratio.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigratioDatabaseTest {

    File directory = new File("test-results");

    @BeforeEach
    void setUp() throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }
        Files.deleteIfExists(directory.toPath());

        directory.mkdir();
    }

    @Test
    void sync_migrate_test() {
        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite:test-results/temp.db");
        SyncMigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .buildSync(connectionProvider);

        try {
            migratioDatabase.migrate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = connectionProvider.get()) {
            assertTrue(connection.createStatement().execute("SELECT name FROM persons WHERE name = 'Tofpu' AND age = '99'"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void async_migrate_test() {
        ConnectionProvider connectionProvider = ConnectionProvider.accept("jdbc:sqlite:test-results/temp.db");
        AsyncMigratioDatabase migratioDatabase = MigratioDatabase.newBuilder("io.tofpu.migratio")
                .buildAsync(connectionProvider);

        try {
            migratioDatabase.migrate().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = connectionProvider.get()) {
            assertTrue(connection.createStatement().execute("SELECT name FROM persons WHERE name = 'Tofpu' AND age = '99'"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}