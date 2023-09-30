package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

@Getter
public class FolderReferenceReadDto {

    private int id;
    private String name;
    private int songs;

    public FolderReferenceReadDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getFolderName();
        this.songs = folder.getSongs().size();
    }
}
