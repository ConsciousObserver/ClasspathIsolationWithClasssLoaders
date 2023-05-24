package com.example.classpath.out;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringUtil {

    private AnnotationConfigApplicationContext applicationContext;

    public SpringUtil() {
        applicationContext = new AnnotationConfigApplicationContext(CommonConfig.class);

        log.info("Initialized Spring Container");
    }

    public String callUrl(String url) {
        return applicationContext.getBean(CommonUtil.class).callUrl(url);
    }
}
