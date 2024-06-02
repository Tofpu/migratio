package io.tofpu.migratio.config;

import io.tofpu.migratio.config.adapter.FileAdapter;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;

public class ConfigurateFileAdapter<N extends ConfigurationNode> implements FileAdapter {
    private final ConfigurationLoader<N> loader;
    private final N node;

    public ConfigurateFileAdapter(ConfigurationLoader<N> loader) {
        this.loader = loader;
        try {
            this.node = this.loader.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(String path, Type type, Object object) {
        try {
            goToPath(path).set(type, object);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private ConfigurationNode goToPath(String path) {
        return node.node((Object[]) path.split("\\."));
    }

    @Override
    public <T> T read(String path, Type type) {
        try {
            return (T) goToPath(path).get(type);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
