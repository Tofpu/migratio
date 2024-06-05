package io.tofpu.migratio.database;

import io.tofpu.migratio.database.adapter.DefaultDatabaseVersionAdapter;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseVersionAdapterTest {
    @Test
    void write_first_version_and_read_test() {
        Connection connection;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        DefaultDatabaseVersionAdapter versionAdapter = new DefaultDatabaseVersionAdapter(() -> connection);

        versionAdapter.write(new MigrationDetail() {
            @Override
            public String version() {
                return "1.0";
            }

            @Override
            public String description() {
                return "Test";
            }
        });

        assertEquals("1.0", versionAdapter.readCurrentVersion());
    }
}
