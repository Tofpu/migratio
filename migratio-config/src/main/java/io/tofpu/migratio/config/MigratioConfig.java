package io.tofpu.migratio.config;

import io.tofpu.migratio.Migratio;
import io.tofpu.migratio.config.adapter.FileAdapter;
import io.tofpu.migratio.config.adapter.FileVersionAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.function.Function;

@SuppressWarnings("SpellCheckingInspection")
public class MigratioConfig extends Migratio<ConfigMigration> {
    private final File file;
    private final Function<File, FileAdapter> fileFileAdapterFunction;

    public static Builder newBuilder(String packageName, File file) {
        return new Builder(packageName, file);
    }

    private MigratioConfig(File file, Function<File, FileAdapter> fileFileAdapterFunction, Collection<ConfigMigration> migrations) {
        super(migrations);
        this.file = requireNonDirectory(file);
        this.fileFileAdapterFunction = fileFileAdapterFunction;
    }

    private File requireNonDirectory(File file) {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("%s is a directory!");
        }

        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            try {
                Files.createDirectory(parentFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!file.exists()) {
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    public void migrate() {
        FileAdapter fileAdapter = fileFileAdapterFunction.apply(file);
        try {
            ConfigMigrationContext context = new ConfigMigrationContext(fileAdapter);

            FileVersionAdapter versionMigrationAdapter = FileVersionAdapter.from(fileAdapter);
            String currentVersion = versionMigrationAdapter.readVersion();

            migrate(currentVersion, configMigration -> {
                configMigration.handle(context);
                versionMigrationAdapter.writeVersion(configMigration.version());
            });
        } finally {
            fileAdapter.close();
        }
    }

    public static class Builder extends Migratio.Builder<Builder, ConfigMigration> {
        private final File file;

        Builder(String packageName, File file) {
            super(packageName);
            this.file = file;
        }

        public MigratioConfig build(Function<File, FileAdapter> fileFileAdapterFunction) {
            return new MigratioConfig(file, fileFileAdapterFunction, findAndSortMigrations());
        }

        private Collection<ConfigMigration> findAndSortMigrations() {
            return super.findAndSortMigrations(ConfigMigration.class);
        }
    }
}
