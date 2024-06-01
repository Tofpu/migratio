package io.tofpu.migratio.config;

import io.tofpu.migratio.config.adapter.FileAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurateFileAdapterTest {
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
    void yaml_loader_test() {
        File file = new File(directory, "config.yml");

        FileAdapter fileAdapter = new ConfigurateFileAdapter<>(YamlConfigurationLoader.builder()
                .file(file)
                .nodeStyle(NodeStyle.BLOCK)
                .build()
        );

        try {
            fileAdapter.write("string", "hello");
            fileAdapter.write("number", 1);
            fileAdapter.write("nested.boolean", true);

            assertEquals("hello", fileAdapter.read("string", String.class));
            assertEquals(1, fileAdapter.read("number", Integer.class));
            assertEquals(true, fileAdapter.read("nested.boolean", Boolean.class));
        } finally {
            fileAdapter.close();
        }
    }
}
