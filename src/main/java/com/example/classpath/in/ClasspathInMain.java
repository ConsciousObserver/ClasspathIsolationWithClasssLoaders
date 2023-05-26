package com.example.classpath.in;

import java.util.Set;

import com.example.classpath.common.ISpringUtil;

public class ClasspathInMain {
    public static void main(String[] args) {
        String jarLocation = "target/ClassloaderIsolationTest-0.0.1-SNAPSHOT-classpath.out.jar";

        //List of interfaces shared between classpath.in and classpath.out. These are not packaged with classpath.out Jar
        //classpath.out Jar gets them from this class's ClassLoader when needed.
        Set<String> sharedClassNames = Set.of("com.example.classpath.common.ISpringUtil");

        ClasspathIsolationUtil classpathIsolationUtil = new ClasspathIsolationUtil(jarLocation, sharedClassNames);

        String qualifiedClassName = "com.example.classpath.out.SpringUtilImpl";

        @SuppressWarnings("unchecked")
        ISpringUtil springUtil = (ISpringUtil) classpathIsolationUtil.getObjectFromIsolatedClass(qualifiedClassName,
                new Class[0],
                new Object[] {});

        springUtil.callUrl("https://google.com");
    }
}
