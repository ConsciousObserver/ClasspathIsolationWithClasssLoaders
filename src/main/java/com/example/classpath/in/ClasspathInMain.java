package com.example.classpath.in;

import com.example.classpath.common.ISpringUtil;

public class ClasspathInMain {
    public static void main(String[] args) {
        String jarLocation = "target/ClassloaderIsolationTest-0.0.1-SNAPSHOT-classpath.out.jar";
        ClasspathIsolationUtil classpathIsolationUtil = new ClasspathIsolationUtil(jarLocation);

        String qualifiedClassName = "com.example.classpath.out.SpringUtilImpl";

        ISpringUtil springUtil = (ISpringUtil)classpathIsolationUtil.getObjectFromIsolatedClass(qualifiedClassName, new Class[0],
                new Object[] {});

        springUtil.callUrl("https://google.com");
        
//        classpathIsolationUtil.invokeMethod(object, "callUrl", new Class[] { String.class },
//                new Object[] { "https://google.com" });

        //        springUtilClass.getMethod
    }
}
