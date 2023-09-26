package nl.leonvanderkaap.mp4d.music.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Song {

    @Id
    private String uuid;

    private String folderPath;
    private String name;

    public String getRelativePath() {
        return folderPath + name;
    }
}
