package io.tofpu.migratio;

public interface Migration<C extends MigrationContext> {
    String version();
    void handle(C context);
}
