package nl.leonvanderkaap.mp4d.music.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Folder implements Comparable<Folder>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private int id;

    private String path;

    @OneToMany(mappedBy = "folder")
    private final List<Song> songs = new ArrayList<>();

    @ManyToOne(optional = true)
    private Folder parent;

    @OneToMany(mappedBy = "parent")
    private final List<Folder> subFolders = new ArrayList<>();

    public Folder(String path, Folder parent) {
        this.path = path;
        this.parent = parent;
    }

    public String getFolderName() {
        if (path.equals("/")) {
            return "/";
        } else {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }

    @Override
    public int compareTo(Folder o) {
        return this.path.compareTo(o.getPath());
    }
}
