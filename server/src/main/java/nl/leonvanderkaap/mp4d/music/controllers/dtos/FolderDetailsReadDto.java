package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

import java.util.List;
@Getter
public class FolderDetailsReadDto {

    private List<SongReferenceReadDto> songs;
    private List<FolderReferenceReadDto> subFolders;

    public FolderDetailsReadDto(Folder folder) {
        this.songs = folder.getSongs().stream().map(SongReferenceReadDto::new).toList();
        this.subFolders = folder.getSubFolders().stream().map(FolderReferenceReadDto::new).toList();
    }
}
