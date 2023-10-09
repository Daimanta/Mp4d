package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

@Getter
public class FolderReferenceReadDto {

    private int id;
    private String name;
    private int songs;
    private int subFolders;

    public FolderReferenceReadDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getFolderName();
        this.songs = folder.getSongs().size();
        this.subFolders = folder.getSubFolders().size();
    }
}
