package io.tofpu.migratio.database.migration;

import io.tofpu.migratio.database.DatabaseMigration;
import io.tofpu.migratio.database.DatabaseMigrationContext;

import java.sql.Connection;
import java.sql.SQLException;

public class AddAgeToPersonTableMigration implements DatabaseMigration {
    @Override
    public String version() {
        return "1.1";
    }

    @Override
    public String description() {
        return "Adds age column to the Person table";
    }

    @Override
    public void handle(DatabaseMigrationContext context) {
        Connection connection = context.connection();

        try {
            connection.createStatement().execute("ALTER TABLE persons ADD age varchar(255);");
            connection.createStatement().execute("UPDATE persons SET age = '99' WHERE name = 'Tofpu'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
