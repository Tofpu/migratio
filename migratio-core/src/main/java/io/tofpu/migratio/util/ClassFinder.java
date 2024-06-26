package io.tofpu.migratio.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class ClassFinder {
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     *
     * @return The classes
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        return getClasses(packageName, classLoader);
    }

    public static Class<?>[] getClasses(String packageName, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        final String path = packageName.replace('.', '/');
        final Enumeration<URL> resources = classLoader.getResources(path);
        final List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        final ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[0]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory The base directory
     * @param packageName The package name for classes found inside the base directory
     *
     * @return The classes
     *
     */
    private static List<Class<?>> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
        final List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        final File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName()
                        .substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
