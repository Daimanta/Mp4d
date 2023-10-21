package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Song;

@Getter
public class SongWithFolderReferenceReadDto {
    private String uuid;
    private String name;
    private int folder;
    private String folderName;

    public SongWithFolderReferenceReadDto(Song song) {
        this.uuid = song.getUuid();
        this.name = song.getName();
        this.folder = song.getFolder().getId();
        this.folderName = song.getFolder().getFolderName();
    }
}
