package io.tofpu.migratio.database.adapter;

import io.tofpu.migratio.database.MigrationDetail;
import io.tofpu.migratio.util.NaturalOrderComparator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultDatabaseVersionAdapter implements DatabaseVersionAdapter {
    private final Connection connection;

    private boolean tableExists = false;

    public DefaultDatabaseVersionAdapter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String readCurrentVersion() {
        try {
            // checks whether the table exists before attempting to grab the versions
            if (!tableExists) {
                createTable();
                tableExists = true;
            }

            ResultSet result = connection.createStatement().executeQuery("SELECT version FROM migratio_history");

            String highestVersion = null;
            do {
                String version = result.getString("version");
                if (highestVersion == null) {
                    highestVersion = version;
                    continue;
                }

                if (NaturalOrderComparator.compareTo(highestVersion, version) < 0) {
                    highestVersion = version;
                }
            } while (result.next());
            return highestVersion;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(MigrationDetail detail) {
        if (!tableExists) {
            createTable();
            tableExists = true;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO migratio_history VALUES (?1, ?2)");
            preparedStatement.setString(1, detail.version());
            preparedStatement.setString(2, detail.description());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable() {
        try {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS migratio_history ("
            + "version varchar(255),"
            + "description varchar(255))").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
