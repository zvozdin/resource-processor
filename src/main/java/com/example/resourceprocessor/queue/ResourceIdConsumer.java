package com.example.resourceprocessor.queue;

import com.example.resourceprocessor.client.ResourceServiceClient;
import com.example.resourceprocessor.client.SongServiceClient;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class ResourceIdConsumer {

    private final SongServiceClient songServiceClient;
    private final ResourceServiceClient resourceServiceClient;

    @Bean
    public Consumer<String> onReceive() {
        return resourceId -> {
            log.info("Received the resourceId {} in resource-processor", resourceId);
            try {
                log.info("Calling Resource-Service to get resource for ID {}", resourceId);
                byte[] resource = resourceServiceClient.getResource(resourceId);

                SongRecordMetadataRequestEntity songRecordMetadataRequest =
                        SongRecordMetadataRequestEntity.buildSongRecordMetadataRequestEntityFromByteContent(
                                resourceId, resource);

                log.info("Calling Song-Service to save song metadata {} for ID {}", songRecordMetadataRequest, resourceId);
                songServiceClient.saveSongRecordMetadata(songRecordMetadataRequest);
            } catch (Exception e) {
                log.error("fail to process message, ex {}", e.getMessage());
                throw new AmqpRejectAndDontRequeueException(e.getMessage());
            }
        };
    }

}
