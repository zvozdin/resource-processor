package com.example.resourceprocessor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resource-service")
public interface ResourceServiceClient {

    @GetMapping(value = "/api/v1/resources/{id}")
    byte[] getResource(@PathVariable String id);
}
