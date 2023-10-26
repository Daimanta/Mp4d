package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FolderTreeRecursiveDto {
    private int id;
    private String name;

    private List<FolderTreeRecursiveDto> children = new ArrayList<>();

    public FolderTreeRecursiveDto(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getFolderName();
        List<Folder> sortedSubfolders = folder.getSubFolders().stream().sorted().toList();
        for(Folder subFolder: sortedSubfolders) {
            children.add(new FolderTreeRecursiveDto(subFolder));
        }
    }

}
