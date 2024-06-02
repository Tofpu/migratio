package io.tofpu.migratio.config.adapter;

import java.lang.reflect.Type;

public interface FileAdapter {
    void write(String path, Object object);

    default <T> T read(String path, Class<T> type) {
        return read(path, (Type) type);
    }
    <T> T read(String path, Type type);

    void close();
}
