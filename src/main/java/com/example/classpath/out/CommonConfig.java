package com.example.classpath.out;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackageClasses = CommonConfig.class)
public class CommonConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
