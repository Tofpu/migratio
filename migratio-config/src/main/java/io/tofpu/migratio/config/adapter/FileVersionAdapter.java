package io.tofpu.migratio.config.adapter;

public interface FileVersionAdapter {
    static FileVersionAdapter from(FileAdapter adapter) {
        return new FileVersionAdapter() {
            @Override
            public String readVersion() {
                return adapter.read("version", String.class);
            }

            @Override
            public void writeVersion(String version) {
                adapter.write("version", version);
            }
        };
    }

    String readVersion();

    void writeVersion(String version);
}
