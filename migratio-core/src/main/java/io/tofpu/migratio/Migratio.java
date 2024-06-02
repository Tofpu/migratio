package io.tofpu.migratio;

import io.tofpu.migratio.util.ClassFinder;
import io.tofpu.migratio.util.NaturalOrderComparator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Migratio<M extends Migration<?>> {
    private final Collection<M> migrations;

    protected Migratio(Collection<M> migrations) {
        this.migrations = new LinkedList<>(migrations);
    }

    protected void migrate(String currentVersion, Consumer<M> migrationConsumer) {
        for (M migration : migrations) {
            String migrationVersion = migration.version();
            boolean next = currentVersion != null && NaturalOrderComparator.compareTo(currentVersion, migrationVersion) >= 0;

            // skips if current version is greater than or equal to the given migration version
            if (next) {
                continue;
            }

            migrationConsumer.accept(migration);
            currentVersion = migrationVersion;
        }
    }

    public static class Builder<M extends Builder<M>> {
        private ClassLoader classLoader;
        private Supplier<Class<?>[]> classesSupplier;

        protected Builder(String packageName) {
            this.classLoader = Thread.currentThread().getContextClassLoader();
            this.classesSupplier = () -> {
                try {
                    return ClassFinder.getClasses(packageName, classLoader);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        public M setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            //noinspection unchecked
            return (M) this;
        }

        public M setClassesSupplier(Supplier<Class<?>[]> classesSupplier) {
            this.classesSupplier = classesSupplier;
            //noinspection unchecked
            return (M) this;
        }

        protected <T extends Migration<?>> Collection<T> findAndSortMigrations(Class<T> migrationType) {
            return Arrays.stream(classesSupplier.get())
                    .filter(type -> !type.isInterface())
                    .filter(migrationType::isAssignableFrom)
                    .map(type -> {
                        try {
                            return migrationType.cast(type.newInstance());
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted((o1, o2) -> NaturalOrderComparator.INSTANCE.compare(o1.version(), o2.version()))
                    .collect(Collectors.toList());
        }
    }
}
