package com.example.resourceprocessor.queue;

import com.example.resourceprocessor.config.ServicesConfig;
import com.example.resourceprocessor.rest.entity.SavedSongRecordMetadataResponseEntity;
import com.example.resourceprocessor.rest.entity.SongRecordMetadataRequestEntity;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMQConsumer {

    private final RestTemplate restTemplate;
    private final ServicesConfig servicesConfig;

    // todo: try to use Spring Cloud Stream(RabbitMQ)

    @RabbitListener(queues = {"${spring.rabbitmq.queue.name}"})
    public void receiveResourceId(@Payload String resourceId) {
        try {
            log.info("Calling Resource-Service to get resource for ID {}", resourceId);

            byte[] resource =
                    restTemplate.getForObject(
                            UriComponentsBuilder.fromUriString(servicesConfig.getResourceServiceUrl() + "/{id}dsda")
                                    .buildAndExpand(Map.of("id", resourceId))
                                    .toUri(),
                            byte[].class);

            log.info("Calling Song-Service to save sing metadata for ID {}", resourceId);

            restTemplate.postForEntity(
                    UriComponentsBuilder.fromUriString(servicesConfig.getSongServiceUrl())
                            .buildAndExpand(Map.of("id", resourceId))
                            .toUri(),
                    buildSongServiceRequestEntity(resourceId, resource),
                    SavedSongRecordMetadataResponseEntity.class);
        } catch (Exception e) {
            log.error("fail to process message, ex {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }

    }

    private SongRecordMetadataRequestEntity buildSongServiceRequestEntity(String resourceId, byte[] resource) {
        File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/mp3");

        try (FileOutputStream stream = new FileOutputStream(convertedFile)) {
            stream.write(resource);
            Mp3File mp3file = new Mp3File(convertedFile);

            if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                return new SongRecordMetadataRequestEntity(
                        id3v1Tag.getTitle(),
                        id3v1Tag.getArtist(),
                        id3v1Tag.getAlbum(),
                        parseToAudioLengthFormat(mp3file.getLengthInSeconds()),
                        resourceId,
                        Year.parse(id3v1Tag.getYear())
                );
            }

            throw new RuntimeException(String.format("Resource ID %s doesn't have metadata", resourceId));
        } catch (InvalidDataException | UnsupportedTagException | IOException e) {
            // todo: log error
//            log.error();
            throw new RuntimeException(e);
        }
    }

    private String parseToAudioLengthFormat(long lengthInSeconds) {
        int minutes = Math.toIntExact(lengthInSeconds / 60);
        return String.format("%d:%d", minutes, lengthInSeconds - minutes * 60L);
    }

}
