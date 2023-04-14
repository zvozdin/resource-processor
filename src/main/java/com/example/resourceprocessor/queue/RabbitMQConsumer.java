package com.example.resourceprocessor.queue;

import com.example.resourceprocessor.config.ServicesConfig;
import com.example.resourceprocessor.rest.entity.SavedSongRecordMetadataResponseEntity;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import com.example.resourceprocessor.retry.RestTemplateWithTimeoutAndRetry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMQConsumer {

    private final RestTemplateWithTimeoutAndRetry restTemplateWithTimeoutAndRetry;
    private final ServicesConfig servicesConfig;

    // todo: try to use Spring Cloud Stream(RabbitMQ)
    // todo: try to retry pattern

    @RabbitListener(queues = {"${spring.rabbitmq.queue.name}"})
    public void receiveResourceId(@Payload String resourceId) {
        try {
            log.info("Calling Resource-Service to get resource for ID {}", resourceId);

            RequestEntity<Void> resourceContentRequestEntity =
                    RequestEntity
                            .get(UriComponentsBuilder.fromUriString(servicesConfig.getResourceServiceUrl() + "/{id}")
                                    .buildAndExpand(Map.of("id", resourceId))
                                    .toUri())
                            .build();

            ResponseEntity<byte[]> response =
                    restTemplateWithTimeoutAndRetry.exchange(resourceContentRequestEntity, byte[].class);

            SongRecordMetadataRequestEntity songRecordMetadataRequest =
                    SongRecordMetadataRequestEntity.buildSongRecordMetadataRequestEntityFromByteContent(
                            resourceId, response.getBody());

            log.info("Calling Song-Service to save song metadata {} for ID {}", songRecordMetadataRequest, resourceId);

            RequestEntity<SongRecordMetadataRequestEntity> songMetadataRequestEntity =
                    RequestEntity
                            .post(servicesConfig.getSongServiceUrl())
                            .body(songRecordMetadataRequest);

            restTemplateWithTimeoutAndRetry.exchange(songMetadataRequestEntity, SavedSongRecordMetadataResponseEntity.class);
        } catch (Exception e) {
            log.error("fail to process message, ex {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }

}
