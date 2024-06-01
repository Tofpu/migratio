package io.tofpu.migratio.config;

import io.tofpu.migratio.config.adapter.FileAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MigratioConfigTest {
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
    }

    @Test
    void migrate_test() {
        File file = new File(directory, "config.yml");

        MigratioConfig migratioConfig = MigratioConfig.newBuilder("io.tofpu.migratio.config", file)
                        .build(MyFileAdapter::new);
        migratioConfig.migrate();

        MyFileAdapter myFileAdapter = new MyFileAdapter(file);
        try {
            assertEquals("1.1", myFileAdapter.read("version", String.class));
            assertNull(myFileAdapter.read("name", String.class));
            assertEquals("Tofpu", myFileAdapter.read("person.name", String.class));
        } finally {
            myFileAdapter.close();
        }
    }

    private static class MyFileAdapter implements FileAdapter {
        private final YamlFile yamlFile;

        public MyFileAdapter(File file) {
            this.yamlFile = new YamlFile(file);
            try {
                this.yamlFile.loadWithComments();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(String path, Object object) {
            yamlFile.set(path, object);
        }

        @Override
        public <T> T read(String path, Class<T> type) {
            Object result = yamlFile.get(path);
            if (result == null) {
                return null;
            }
            
            if (!type.isInstance(result)) {
                throw new IllegalArgumentException(String.format("Incorrect type: %s. Expected %s", type, result.getClass()));
            }
            return type.cast(result);
        }

        @Override
        public void close() {
            try {
                yamlFile.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
