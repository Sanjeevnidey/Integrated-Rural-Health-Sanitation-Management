package com.irhsm.healthCampService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration                         // Defines beans (shared objects) for Spring
public class AppConfig {

    @Bean                              // One RestTemplate injected wherever it is needed
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));   // Stop trying to connect after 3 s
        factory.setReadTimeout(Duration.ofSeconds(5));      // Stop waiting for a reply after 5 s
        return new RestTemplate(factory);
    }
}