package nl.leonvanderkaap.mp4d.music.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.FolderDetailsReadDto;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.FolderTreeParentReadDto;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.SongReferenceReadDto;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.SongWithFolderReferenceReadDto;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.services.PlaylistBuilderService;
import nl.leonvanderkaap.mp4d.music.services.SongService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final PlaylistBuilderService playlistBuilderService;
    private final ApplicationSettings applicationSettings;


    @GetMapping(value = "/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public FolderDetailsReadDto getFolder(@RequestParam(name = "id", required = false) Integer id) {
        return new FolderDetailsReadDto(songService.getFolderById(id).orElseThrow(() -> new NotFoundException("Folder not found")));
    }

    @GetMapping(value = "/directplay/{id}", produces = "audio/mpeg")
    public byte[] getMusic(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Song song = songService.getByUuid(id).orElseThrow(() -> new NotFoundException("Song not found"));
        String path = applicationSettings.getBasepath() + song.getRelativePath();
        response.setHeader("Content-Disposition", "filename="+song.getName());
        InputStream is = new FileInputStream(path);
        return is.readAllBytes();
    }

    @GetMapping(value = "/songplaylist/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getSingularSongPlaylist(@PathVariable("id") String id, HttpServletResponse response) {
        String playlistContent = playlistBuilderService.buildPlaylistByIds(List.of(id));
        byte[] result = playlistContent.getBytes(StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "filename="+id+".m3u8");
        return result;
    }

    @GetMapping(value = "/folderplaylist/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getFolderPlaylist(@PathVariable(name = "id", required = false) Integer id, HttpServletResponse response) {
        String playlistContent = playlistBuilderService.buildFolderPlaylist(id);
        byte[] result = playlistContent.getBytes(StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename="+id+".m3u8");
        return result;
    }

    @GetMapping("/random")
    public List<SongWithFolderReferenceReadDto> getRandomSongs() {
        return songService.getRandomSongs(20).stream().map(SongWithFolderReferenceReadDto::new).toList();
    }

    @GetMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public FolderTreeParentReadDto getFolderTree() {
        return new FolderTreeParentReadDto(songService.getFolderById(null).get());
    }
}
