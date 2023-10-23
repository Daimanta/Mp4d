package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import nl.leonvanderkaap.mp4d.music.entities.Folder;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FolderTreeParentReadDto {
    private int id;
    private String name;

    private List<FolderTreeRecursiveDto> children = new ArrayList<>();

    public FolderTreeParentReadDto(Folder top) {
        this.id = top.getId();
        this.name = top.getFolderName();
        List<Folder> sortedSubfolders = top.getSubFolders().stream().sorted().toList();
        for(Folder folder: sortedSubfolders) {
            children.add(new FolderTreeRecursiveDto(folder));
        }
    }
}
