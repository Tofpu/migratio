package io.tofpu.migratio.database.adapter;

import io.tofpu.migratio.database.MigrationDetail;

public interface DatabaseVersionAdapter {
    String readCurrentVersion();
    void write(MigrationDetail detail);
}
