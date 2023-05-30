package com.example.resourceprocessor.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    // todo:
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 2000, 3);
    }
}
