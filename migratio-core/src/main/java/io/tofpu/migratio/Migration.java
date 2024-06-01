package io.tofpu.migratio;

public interface Migration<C extends MigrationContext> {
    String version();
    String description();
    void handle(C context);
}
