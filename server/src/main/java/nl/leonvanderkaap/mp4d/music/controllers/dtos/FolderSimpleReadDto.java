package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

@Getter
public class FolderSimpleReadDto {

    private int id;
    private String name;

    public FolderSimpleReadDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getFolderName();
    }
}
