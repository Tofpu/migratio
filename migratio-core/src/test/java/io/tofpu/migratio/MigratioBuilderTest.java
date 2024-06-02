package io.tofpu.migratio;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MigratioBuilderTest {
    @Test
    void alternative_classes_supplier_test() {
        Migratio.Builder<?> builder = new Migratio.Builder<>("io.tofpu.migratio")
                .setClassesSupplier(() -> new Class[0]);

        Collection<Migration> collection = builder.findAndSortMigrations(Migration.class);
        assertEquals(0, collection.size());
    }

    @SuppressWarnings("unused")
    static class TestMigration implements Migration<MigrationContext> {
        @Override
        public String version() {
            return "1.0";
        }

        @Override
        public void handle(MigrationContext context) {
        }
    }
}
