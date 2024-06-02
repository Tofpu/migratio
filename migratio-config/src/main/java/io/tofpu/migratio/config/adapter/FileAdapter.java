package io.tofpu.migratio.config.adapter;

import java.lang.reflect.Type;

public interface FileAdapter {
    default void write(String path, Object object) {
        Type type = object == null ? null : object.getClass();
        write(path, type, object);
    }
    void write(String path, Type type, Object object);

    default <T> T read(String path, Class<T> type) {
        return read(path, (Type) type);
    }
    <T> T read(String path, Type type);

    void close();
}
