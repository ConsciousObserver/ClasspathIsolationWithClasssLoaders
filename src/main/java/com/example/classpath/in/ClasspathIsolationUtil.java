package com.example.classpath.in;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClasspathIsolationUtil {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ClassLoader jarClassLoader;

    private Map<String, Class<?>> classByClassName = new ConcurrentHashMap<>();

    public ClasspathIsolationUtil(String jarLocation) {
        try {
            File jarFile = new File(jarLocation);

            if (!jarFile.exists()) {
                throw new RuntimeException("Jar file does not exist: " + jarFile.toURI());
            }

            URL jarUrl = jarFile.toURI().toURL();

            System.out.println("jar URL::: " + jarUrl);

            jarClassLoader = getJarClassLoader(jarUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Loaded jarClassLoader: " + jarClassLoader);
    }

    private synchronized Class<?> getClassFromIsolatedJar(String qualifiedClassName) {

        classByClassName.putIfAbsent(qualifiedClassName,
                runWithIsolatedClassLoaders(() -> {

                    Class<?> klass = null;

                    //prioritizing jarClassloader
                    if (null != jarClassLoader.getResource(qualifiedClassName.replace('.', '/') + ".class")) {
                        klass = jarClassLoader.loadClass(qualifiedClassName);
                    } else {
                        klass = jarClassLoader.getParent().loadClass(qualifiedClassName);
                    }

                    return klass;
                }));

        return classByClassName.get(qualifiedClassName);
    }

    public Object getObjectFromIsolatedClass(String qualifiedClassName, Class<Object>[] constructorArgumentTypes,
            Object[] constructorArguments) {
        return runWithIsolatedClassLoaders(() -> {
            Class<?> klass = getClassFromIsolatedJar(qualifiedClassName);

            return klass.getConstructor(constructorArgumentTypes).newInstance(constructorArguments);
        });
    }

    /**
     * Changes current thread's Context ClassLoader to {{@link #jarClassLoader}
     * before running the {@link Callable} argument
     * 
     * @param <T>
     * @param callable
     * @return
     */
    private <T> T runWithIsolatedClassLoaders(Callable<T> callable) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(jarClassLoader);
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * Loads JAR into a ClassLoader that's child of bootstrap ClassLoader
     * 
     * @param jarUrl
     * @return
     */
    private synchronized ClassLoader getJarClassLoader(URL jarUrl) {

        if (jarClassLoader == null) {
            jarClassLoader = new URLClassLoader(new URL[] { jarUrl },
                    ClassLoader.getSystemClassLoader().getParent());
        }

        return jarClassLoader;
    }

    public Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes,
            Object[] args) {
        return runWithIsolatedClassLoaders(() -> {
            Object result = null;
            try {
                Method method = object.getClass().getMethod(methodName, parameterTypes);

                if (method.getReturnType().equals(Void.TYPE)) {
                    result = null;
                    method.invoke(object, args);
                } else {
                    result = method.invoke(object, args);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {

                logger.log(Level.SEVERE, "Error running method " + methodName);

                throw new RuntimeException(e);
            }

            return result;
        });
    }
}
