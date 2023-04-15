package com.example.resourceprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@EnableRetry
@Configuration
public class RestTemplateConfig {

    @Value("${app.server.restTemplate.connectTimeoutMilliSeconds:2000}")
    private int connectTimeout;
    @Value("${app.server.restTemplate.readTimeoutMilliSeconds:2000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(this.connectTimeout))
                .setReadTimeout(Duration.ofMillis(this.readTimeout))
                .build();
        }

}
