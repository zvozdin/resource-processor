package com.example.resourceprocessor.rest.entity;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;

@Slf4j
public record SongRecordMetadataRequestEntity(

        String name,
        String artist,
        String album,
        String length,
        String resourceId,
        Year year) {

    public static SongRecordMetadataRequestEntity buildSongRecordMetadataRequestEntityFromByteContent(
            String resourceId, byte[] resource) {

        File source = new File(System.getProperty("java.io.tmpdir") + "/mp3");

        try (FileOutputStream stream = new FileOutputStream(source)) {
            stream.write(resource);
            Mp3File mp3file = new Mp3File(source);

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

            String errorMessage = String.format("Resource ID %s doesn't have metadata", resourceId);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        } catch (InvalidDataException | UnsupportedTagException | IOException e) {
            log.error("Error happened during parsing byte array to extract mp3 metadata for resource ID {}", resourceId);
            throw new RuntimeException(e);
        }
    }

    private static String parseToAudioLengthFormat(long lengthInSeconds) {
        int minutes = Math.toIntExact(lengthInSeconds / 60);
        return String.format("%d:%d", minutes, lengthInSeconds - minutes * 60L);
    }

}
