package nl.leonvanderkaap.mp4d.music.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Song {

    @Id
    private String uuid;

    @ManyToOne(optional = false)
    private Folder folder;
    private String name;

    public Song(Folder folder, String name) {
        this.uuid = UUID.randomUUID().toString();
        this.folder = folder;
        this.name = name;
    }

    public String getRelativePath() {
        return folder.getPath() + name;
    }
}
