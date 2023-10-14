package nl.leonvanderkaap.mp4d.music.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.*;
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

    @PostMapping(value = "/songplaylist/", produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getMultisongPlaylist(@RequestBody List<String> uuids, HttpServletResponse response) {
        String playlistContent = playlistBuilderService.buildPlaylistByIds(uuids);
        byte[] result = playlistContent.getBytes(StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "filename=multi.m3u8");
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

    @PostMapping(value = "/multifolderplaylist", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getMultiFolderPlaylist(@RequestBody List<Integer> folderIds, HttpServletResponse response) {
        String playlistContent = playlistBuilderService.buildMultiFolderPlaylist(folderIds);
        byte[] result = playlistContent.getBytes(StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=multi.m3u8");
        return result;
    }

    @GetMapping(value = "/folderpath/{id}")
    public List<FolderSimpleReadDto> getFolderPathToRoot(@PathVariable(name = "id", required = true) int id) {
        return songService.getFolderPathToRoot(id).stream().map(FolderSimpleReadDto::new).toList();
    }

    @GetMapping("/randomfolder")
    public FolderDetailsReadDto getRandomFolder() {
        return new FolderDetailsReadDto(songService.getRandomFolder());
    }

    @GetMapping("/song/{id}")
    public SongDetailsReadDto getSong(@PathVariable("id") String id) {
        return new SongDetailsReadDto(songService.getSongById(id).orElseThrow(() -> new NotFoundException("Song not found")));
    }

    @GetMapping("/grouped/artist")
    public List<String> getGroupedByArtist() {
        return songService.getGroupedByArtist();
    }

    @GetMapping("/grouped/album")
    public List<String> getGroupedByAlbum() {
        return songService.getGroupedByAlbum();
    }

    @GetMapping("/grouped/year")
    public List<Integer> getGroupedByYear() {
        return songService.getGroupedByYear();
    }

    @GetMapping("/grouped/genre")
    public List<String> getGroupedByGenre() {
        return songService.getGroupedByGenre();
    }
}
