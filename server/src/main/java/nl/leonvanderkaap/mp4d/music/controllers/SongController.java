package nl.leonvanderkaap.mp4d.music.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.services.SongService;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final ApplicationSettings applicationSettings;


    @GetMapping("/{id}")
    public void getMusic(String id, HttpServletResponse response) throws IOException {
        Song song = songService.getByUuid(id).orElseThrow(() -> new RuntimeException());

        response.setContentType("audio/mpeg");
        InputStream is = new FileInputStream("C:\\temp\\morgen.mp3");
        FileCopyUtils.copy(is, response.getOutputStream());
        response.getOutputStream().close();
    }
}
