package com.example.resourceprocessor.client;

import com.example.resourceprocessor.rest.entity.SavedSongRecordMetadataResponseEntity;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "song-service")
public interface SongServiceClient {

    @PostMapping("/api/v1/songs")
    SavedSongRecordMetadataResponseEntity saveSongRecordMetadata(
            @Valid @RequestBody SongRecordMetadataRequestEntity request);
}
