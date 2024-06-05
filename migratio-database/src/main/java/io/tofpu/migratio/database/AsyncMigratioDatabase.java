package io.tofpu.migratio.database;

import java.util.concurrent.CompletableFuture;

public interface AsyncMigratioDatabase {
    CompletableFuture<?> migrate();
    CompletableFuture<String> getCurrentVersion();
}
