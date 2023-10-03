package nl.leonvanderkaap.mp4d.music.services;

import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.entities.Folder;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistBuilderService {

    private final SongService songService;

    public String buildFolderPlaylist(int folderId) {
        Folder folder = songService.getFolderById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        // Actually traverse subfolders
        List<Song> nestedSongs = new ArrayList<>();
        traverseSongs(folder, nestedSongs);
        return buildPlaylist(nestedSongs);
    }

    public void traverseSongs(Folder folder, List<Song> songs) {
        songs.addAll(folder.getSongs());
        for (Folder nestedFolder: folder.getSubFolders()) {
            traverseSongs(nestedFolder, songs);
        }
    }
    public String buildPlaylistByIds(List<String> songIds) {
        List<Song> songs = songService.getSongsByIds(songIds);
        return buildPlaylist(songs);
    }

    public String buildPlaylist(List<Song> songs) {
        StringBuilder builder = new StringBuilder();
        String hostUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        builder.append("#EXTM3U\n");
        for (Song song: songs) {
            builder.append("#EXTINF:");
            builder.append(song.getLength() != null ? song.getLength() : 0);
            builder.append(",");
            builder.append(song.getName());
            builder.append("\n");
            builder.append(hostUrl);
            builder.append("/api/v1/directplay/");
            builder.append(song.getUuid());
            builder.append("\n");
        }

        return builder.toString();
    }
}
