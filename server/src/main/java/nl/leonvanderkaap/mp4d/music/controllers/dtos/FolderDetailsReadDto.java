package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

import java.util.Comparator;
import java.util.List;
@Getter
public class FolderDetailsReadDto {

    private int id;
    private String name;
    private List<SongReferenceReadDto> songs;
    private List<FolderReferenceReadDto> subFolders;

    public FolderDetailsReadDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getFolderName();
        this.songs = folder.getSongs().stream().map(SongReferenceReadDto::new).sorted(Comparator.comparing(SongReferenceReadDto::getName)).toList();
        this.subFolders = folder.getSubFolders().stream().map(FolderReferenceReadDto::new).sorted(Comparator.comparing(FolderReferenceReadDto::getName)).toList();
    }
}
