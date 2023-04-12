package com.example.resourceprocessor.rest.entity;

import java.time.Year;

public record SongRecordMetadataRequestEntity(

        String name,
        String artist,
        String album,
        String length,
        String resourceId,
        Year year) {
}
