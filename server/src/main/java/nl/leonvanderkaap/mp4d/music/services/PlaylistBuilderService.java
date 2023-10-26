package nl.leonvanderkaap.mp4d.music.services;

import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.entities.Folder;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

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

    public String buildMultiFolderPlaylist(List<Integer> folderIds) {
        List<Folder> folders = songService.getFoldersByIds(folderIds);
        List<Folder> nestedFolders = getNestedFolders(folders).stream().sorted().toList();
        List<Song> songs = new ArrayList<>();
        for (Folder folder: nestedFolders) {
            songs.addAll(folder.getSongs().stream().sorted().toList());
        }
        return buildPlaylist(songs);
    }

    public List<Folder> getNestedFolders(List<Folder> folders) {
        List<Folder> result = new ArrayList<>();
        Set<Integer> found = new HashSet<>();
        List<Folder> ordered = folders.stream().sorted().toList();
        for (Folder folder: ordered) {
            addFolders(folder, result, found);
        }
        return result;
    }

    private void addFolders(Folder folder, List<Folder> folders, Set<Integer> matched) {
        if (matched.contains(folder.getId())) return;
        folders.add(folder);
        matched.add(folder.getId());
        List<Folder> sortedSubfolders = folder.getSubFolders().stream().sorted().toList();
        for (Folder child: sortedSubfolders) {
            addFolders(child, folders, matched);
        }
    }

    public void traverseSongs(Folder folder, List<Song> songs) {
        songs.addAll(folder.getSongs().stream().sorted().toList());
        List<Folder> sortedSubfolders = folder.getSubFolders().stream().sorted().toList();
        for (Folder nestedFolder: sortedSubfolders) {
            traverseSongs(nestedFolder, songs);
        }
    }
    public String buildPlaylistByIds(List<String> songIds) {
        List<Song> songs = songService.getSongsByIds(songIds);
        List<Song> ordered = new ArrayList<>();
        for (String songId: songIds) {
            for (Song song: songs) {
                if (song.getUuid().equals(songId)) {
                    ordered.add(song);
                    break;
                }
            }
        }
        return buildPlaylist(ordered);
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
