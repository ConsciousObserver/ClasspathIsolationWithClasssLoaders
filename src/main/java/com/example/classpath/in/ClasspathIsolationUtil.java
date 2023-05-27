package com.example.classpath.in;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Loads a Jar into a ClassLoader which is child of bootstrap ClassLoader. This
 * means that Jar's ClassLoader cannot see classes from current class path,
 * other than classes from JRE itself and cannot have a conflict with classes
 * from current class path.
 * 
 */
public class ClasspathIsolationUtil {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ClassLoader jarClassLoader;

    private final Map<String, Class<?>> classByClassName = new ConcurrentHashMap<>();

    /**
     * Only these classes are loaded from in-classpath class loader, all other
     * classes are loaded from Jar' ClassLoader. Bundling these classes with the
     * Jar will have no effect as they'll never be loaded.
     */
    private final Set<String> sharedCommonQualifiedClassNames;

    public ClasspathIsolationUtil(String jarLocation, Set<String> sharedCommonQualifiedClassNames) {
        this.sharedCommonQualifiedClassNames = sharedCommonQualifiedClassNames;
        try {
            File jarFile = new File(jarLocation);

            if (!jarFile.exists()) {
                throw new RuntimeException("Jar file does not exist: " + jarFile.toURI());
            }

            URL jarUrl = jarFile.toURI().toURL();

            logger.info("jar URL::: " + jarUrl);

            jarClassLoader = getJarClassLoader(jarUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        logger.info("Loaded jarClassLoader: " + jarClassLoader);
    }

    /**
     * Returns instance of given class, by invoking it's constructor matching to
     * the provided constructor arguments.
     * 
     * @param qualifiedClassName
     * @param constructorArgumentTypes
     * @param constructorArguments
     * @return
     */
    public Object getObjectFromIsolatedClass(String qualifiedClassName, Class<Object>[] constructorArgumentTypes,
            Object[] constructorArguments) {
        return runWithIsolatedClassLoaders(() -> {
            Class<?> klass = getClassFromIsolatedJar(qualifiedClassName);

            return klass.getConstructor(constructorArgumentTypes).newInstance(constructorArguments);
        });
    }
    
    /**
     * Changes current thread's Context ClassLoader to {@link #jarClassLoader}
     * before running the {@link Callable} argument
     * 
     * @param <T>
     * @param callable
     * @return
     */
    public <T> T runWithIsolatedClassLoaders(Callable<T> callable) {
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
     * Loads JAR into a ClassLoader that's child of bootstrap ClassLoader. This
     * means that this ClassLoader has only it's own classes and JRE classes on
     * it's class path.
     * 
     * <p>
     * It loads some classes specified in
     * {@link #sharedCommonQualifiedClassNames} from current class loader.
     * 
     * @param jarUrl
     * @return
     */
    private synchronized ClassLoader getJarClassLoader(URL jarUrl) {

        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
        
        return new URLClassLoader(new URL[] { jarUrl },
                bootstrapClassLoader) {
            @Override
            public Class<?> loadClass(String qualifiedClassName) throws ClassNotFoundException {
                Class<?> klass = null;

                if (sharedCommonQualifiedClassNames.contains(qualifiedClassName)) {
                    klass = this.getClass().getClassLoader().loadClass(qualifiedClassName);
                } else {
                    klass = super.loadClass(qualifiedClassName);
                }

                return klass;
            }
        };
    }

    /**
     * loads and returns the Class<?> instance of requested class.
     * 
     * @param qualifiedClassName
     * @return
     */
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
}
