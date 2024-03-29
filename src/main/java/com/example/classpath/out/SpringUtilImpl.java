package com.example.classpath.out;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.classpath.common.ISpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements {@link ISpringUtil}.
 * 
 * <p>
 * {@link ISpringUtil} is not bundled with this class into classpath-out Jar
 * file.
 *
 */
@Slf4j
public class SpringUtilImpl implements ISpringUtil {

    private AnnotationConfigApplicationContext applicationContext;

    public SpringUtilImpl() {
        applicationContext = new AnnotationConfigApplicationContext(CommonConfig.class);

        log.info("Initialized Spring Container");
    }

    @Override
    public String callUrl(String url) {
        return applicationContext.getBean(CommonUtil.class).callUrl(url);
    }
}
