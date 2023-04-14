package com.example.resourceprocessor.queue;

import com.example.resourceprocessor.config.ServicesConfig;
import com.example.resourceprocessor.rest.entity.SavedSongRecordMetadataResponseEntity;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import com.example.resourceprocessor.retry.RestTemplateWithTimeoutAndRetry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.context.annotation.Bean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResourceIdConsumer {

    private final RestTemplateWithTimeoutAndRetry restTemplateWithTimeoutAndRetry;
    private final ServicesConfig servicesConfig;

    // todo: try to retry pattern
    // todo: https://www.springcloud.io/post/2022-06/spring-cloud-stream-rabbitmq/#gsc.tab=0

    @Bean
    public Consumer<String> onReceive() {
        return resourceId -> {
            log.info("Received the resourceId {} in resource-processor", resourceId);
            try {
                RequestEntity<Void> resourceContentRequestEntity =
                        RequestEntity
                                .get(UriComponentsBuilder.fromUriString(servicesConfig.getResourceServiceUrl() + "/{id}")
                                        .buildAndExpand(Map.of("id", resourceId))
                                        .toUri())
                                .build();

                log.info("Calling Resource-Service to get resource for ID {}", resourceId);
                ResponseEntity<byte[]> response =
                        restTemplateWithTimeoutAndRetry.exchange(resourceContentRequestEntity, byte[].class);

                SongRecordMetadataRequestEntity songRecordMetadataRequest =
                        SongRecordMetadataRequestEntity.buildSongRecordMetadataRequestEntityFromByteContent(
                                resourceId, response.getBody());

                RequestEntity<SongRecordMetadataRequestEntity> songMetadataRequestEntity =
                        RequestEntity
                                .post(servicesConfig.getSongServiceUrl())
                                .body(songRecordMetadataRequest);

                log.info("Calling Song-Service to save song metadata {} for ID {}", songRecordMetadataRequest, resourceId);
                restTemplateWithTimeoutAndRetry.exchange(songMetadataRequestEntity, SavedSongRecordMetadataResponseEntity.class);
            } catch (Exception e) {
                log.error("fail to process message, ex {}", e.getMessage());
                // todo: handle properly using SCS
//                throw new AmqpRejectAndDontRequeueException(e.getMessage());
                throw new ImmediateAcknowledgeAmqpException(e.getMessage());
            }
        };
    }

}
