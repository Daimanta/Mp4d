package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Song;

@Getter
public class SongReferenceReadDto {
    private String uuid;
    private String name;

    public SongReferenceReadDto(Song song) {
        this.uuid = song.getUuid();
        this.name = song.getName();
    }
}
