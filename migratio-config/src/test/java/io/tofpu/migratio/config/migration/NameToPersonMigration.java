package io.tofpu.migratio.config.migration;

import io.tofpu.migratio.config.ConfigMigration;
import io.tofpu.migratio.config.ConfigMigrationContext;
import io.tofpu.migratio.config.adapter.FileAdapter;

public class NameToPersonMigration implements ConfigMigration {
    @Override
    public String version() {
        return "1.1";
    }

    @Override
    public String description() {
        return "Migrates path 'name' to 'person'";
    }

    @Override
    public void handle(ConfigMigrationContext context) {
        FileAdapter fileAdapter = context.fileAdapter();

        String name = fileAdapter.read("name", String.class);
        fileAdapter.write("name", null);

        fileAdapter.write("person.name", name);
    }
}
