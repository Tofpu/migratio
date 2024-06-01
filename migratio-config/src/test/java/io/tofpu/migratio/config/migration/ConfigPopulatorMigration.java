package io.tofpu.migratio.config.migration;

import io.tofpu.migratio.config.ConfigMigration;
import io.tofpu.migratio.config.ConfigMigrationContext;
import io.tofpu.migratio.config.adapter.FileAdapter;

public class ConfigPopulatorMigration implements ConfigMigration {
    @Override
    public String version() {
        return "1";
    }

    @Override
    public void handle(ConfigMigrationContext context) {
        FileAdapter fileAdapter = context.fileAdapter();

        fileAdapter.write("name", "Tofpu");
    }
}
