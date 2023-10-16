package nl.leonvanderkaap.mp4d.music.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Song implements Comparable<Song>{

    @Id
    private String uuid;
    private String name;
    private Integer bitrate;
    private Integer length;
    private Integer mtime;
    private int size;
    private String artist;
    private String album;
    private Integer year;
    private String genre;

    @ManyToOne(optional = false)
    private Folder folder;
    public Song(Folder folder, String name, Integer bitrate, Integer length, Integer mtime, int size, String artist, String album, Integer year, String genre) {
        this.uuid = UUID.randomUUID().toString();
        this.folder = folder;
        this.name = name;
        this.bitrate = bitrate;
        this.length = length;
        this.mtime = mtime;
        this.size = size;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
    }

    public String getRelativePath() {
        return folder.getPath() + "/" + name;
    }

    @Override
    public int compareTo(Song o) {
        return this.name.compareTo(o.getName());
    }
}
