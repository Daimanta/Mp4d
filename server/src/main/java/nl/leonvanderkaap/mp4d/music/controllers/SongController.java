package nl.leonvanderkaap.mp4d.music.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.FolderDetailsReadDto;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.services.SongService;
import org.flywaydb.core.internal.util.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.AudioSystem;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final ApplicationSettings applicationSettings;


    @GetMapping(value = "/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public FolderDetailsReadDto getFolder(@RequestParam(name = "id", required = false) Integer id) {
        return new FolderDetailsReadDto(songService.getById(id).orElseThrow(() -> new NotFoundException("Folder not found")));
    }

    @GetMapping(value = "/directplay/{id}", produces = "audio/mpeg")
    public byte[] getMusic(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Song song = songService.getByUuid(id).orElseThrow(() -> new NotFoundException("Song not found"));
        String path = applicationSettings.getBasepath() + song.getRelativePath();
        response.setHeader("Content-Disposition", "attachment; filename="+song.getName());
        InputStream is = new FileInputStream(path);
        return is.readAllBytes();
    }

    @GetMapping("/songplaylist/{id}")
    public void getSingularSongPlaylist(@PathVariable("id") String id) {

    }
}
