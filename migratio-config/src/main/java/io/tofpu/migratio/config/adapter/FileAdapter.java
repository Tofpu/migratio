package io.tofpu.migratio.config.adapter;

public interface FileAdapter {
    void write(String path, Object object);

    <T> T read(String path, Class<T> type);

    void close();
}
