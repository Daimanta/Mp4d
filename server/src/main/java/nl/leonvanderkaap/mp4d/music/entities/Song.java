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
    private String name;
    private Integer bitrate;
    private Integer length;
    private Integer mtime;
    private int size;

    @ManyToOne(optional = false)
    private Folder folder;
    public Song(Folder folder, String name, Integer bitrate, Integer length, Integer mtime, int size) {
        this.uuid = UUID.randomUUID().toString();
        this.folder = folder;
        this.name = name;
        this.bitrate = bitrate;
        this.length = length;
        this.mtime = mtime;
        this.size = size;
    }

    public String getRelativePath() {
        return folder.getPath() + "/" + name;
    }
}
