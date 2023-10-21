package nl.leonvanderkaap.mp4d.music.controllers.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SearchRequestDto {
    private boolean all;
    private String artist;
    private String album;
    private String song;
    private Integer year;
}
