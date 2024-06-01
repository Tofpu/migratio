package io.tofpu.migratio.config;

import io.tofpu.migratio.MigrationContext;
import io.tofpu.migratio.config.adapter.FileAdapter;

public class ConfigMigrationContext extends MigrationContext {
    private final FileAdapter fileAdapter;

    public ConfigMigrationContext(FileAdapter fileAdapter) {
        this.fileAdapter = fileAdapter;
    }

    public FileAdapter fileAdapter() {
        return fileAdapter;
    }
}
