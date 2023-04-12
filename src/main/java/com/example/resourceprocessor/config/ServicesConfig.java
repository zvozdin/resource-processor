package com.example.resourceprocessor.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ServicesConfig {

    @Value("${services.url.resource-service}")
    private String resourceServiceUrl;
    @Value("${services.url.song-service}")
    private String songServiceUrl;
}
