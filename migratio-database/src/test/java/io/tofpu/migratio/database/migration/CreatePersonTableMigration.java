package io.tofpu.migratio.database.migration;

import io.tofpu.migratio.database.DatabaseMigration;
import io.tofpu.migratio.database.DatabaseMigrationContext;

import java.sql.Connection;
import java.sql.SQLException;

public class CreatePersonTableMigration implements DatabaseMigration {
    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String description() {
        return "Creates the Person table";
    }

    @Override
    public void handle(DatabaseMigrationContext context) {
        Connection connection = context.connection();

        try {
            connection.createStatement().execute("CREATE TABLE persons (name VARCHAR(255) PRIMARY KEY)");
            connection.createStatement().execute("INSERT INTO persons VALUES ('Tofpu')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
