package io.tofpu.migratio;

import io.tofpu.migratio.util.ClassFinder;
import io.tofpu.migratio.util.NaturalOrderComparator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
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

    public abstract static class Builder<M extends Builder<M>> {
        private final String packageName;

        private ClassLoader classLoader;

        protected Builder(String packageName) {
            this.packageName = packageName;
            this.classLoader = Thread.currentThread().getContextClassLoader();
        }

        public M setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            //noinspection unchecked
            return (M) this;
        }

        protected <T extends Migration<?>> Collection<T> findAndSortMigrations(Class<T> migrationType) {
            try {
                return Arrays.stream(ClassFinder.getClasses(packageName, classLoader))
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
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
