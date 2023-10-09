package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Song;

@Getter
public class SongDetailsReadDto {
    private String uuid;
    private int folderId;
    private String name;
    private int bitrate;
    private int length;
    private int size;

    public SongDetailsReadDto(Song song) {
        this.uuid = song.getUuid();
        this.folderId = song.getFolder().getId();
        this.name = song.getName();
        this.bitrate = song.getBitrate();
        this.length = song.getLength();
        this.size = song.getSize();
    }
}
