package io.tofpu.migratio;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MigratioBuilderTest {
    @Test
    void alternative_classes_supplier_test() {
        TestBuilder builder = new TestBuilder("io.tofpu.migratio")
                .setClassesSupplier(() -> new Class[0]);

        Collection<TestMigration> collection = builder.findAndSortMigrations(TestMigration.class);
        assertEquals(0, collection.size());
    }

    @Test
    void migration_supplier_test() {
        TestBuilder builder = new TestBuilder("io.tofpu.migratio")
                .setMigrationSupplier(() -> Collections.singletonList(TestMigration.class).toArray(new TestMigration[0]));

        Collection<TestMigration> collection = builder.findAndSortMigrations(TestMigration.class);
        assertEquals(1, collection.size());

    }

    static class TestBuilder extends Migratio.Builder<TestBuilder, TestMigration> {
        protected TestBuilder(String packageName) {
            super(packageName);
        }
    }

    @SuppressWarnings("unused")
    public static class TestMigration implements Migration<MigrationContext> {
        @Override
        public String version() {
            return "1.0";
        }

        @Override
        public void handle(MigrationContext context) {
        }
    }

    @SuppressWarnings("unused")
    public static class AnotherTestMigration implements Migration<MigrationContext> {
        @Override
        public String version() {
            return "1.1";
        }

        @Override
        public void handle(MigrationContext context) {
        }
    }
}
