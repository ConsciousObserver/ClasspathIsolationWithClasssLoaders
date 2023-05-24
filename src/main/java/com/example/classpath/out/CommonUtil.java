package com.example.classpath.out;

import java.util.HashMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonUtil {

    private final RestTemplate restTemplate;

    String callUrl(String url) {
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null,
                String.class, new HashMap<>());

        String responseBody = response.getBody();

        log.info("http response: {}", responseBody);

        return responseBody;
    }
}
