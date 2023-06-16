package com.example.resourceprocessor.client;

import com.example.resourceprocessor.rest.entity.SavedResourceResponseEntity;
import com.example.resourceprocessor.rest.entity.SavedSongRecordMetadataResponseEntity;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "gateway-service")
public interface GatewayClient {

    @GetMapping(value = "/api/v1/resources/{id}")
    byte[] getResource(@PathVariable String id);

    @GetMapping("/api/v1/resources/migrate/{id}")
    SavedResourceResponseEntity changeResourceDestination(@PathVariable String id);

    @PostMapping("/api/v1/songs")
    SavedSongRecordMetadataResponseEntity saveSongRecordMetadata(
            @Valid @RequestBody SongRecordMetadataRequestEntity request);
}
