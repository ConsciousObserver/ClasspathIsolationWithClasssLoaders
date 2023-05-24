package com.example.classpath.in;

public class ClasspathInMain {
    public static void main(String[] args) {
        String jarLocation = "target/ClassloaderIsolationTest-0.0.1-SNAPSHOT-classpath.out.jar";
        ClasspathIsolationUtil classpathIsolationUtil = new ClasspathIsolationUtil(jarLocation);

        String qualifiedClassName = "com.example.classpath.out.SpringUtil";

        Object object = classpathIsolationUtil.getObjectFromIsolatedClass(qualifiedClassName, new Class[0],
                new Object[] {});

        classpathIsolationUtil.invokeMethod(object, "callUrl", new Class[] { String.class },
                new Object[] { "https://google.com" });

        //        springUtilClass.getMethod
    }
}
