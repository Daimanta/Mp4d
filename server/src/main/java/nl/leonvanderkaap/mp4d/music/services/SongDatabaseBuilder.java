package nl.leonvanderkaap.mp4d.music.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.music.entities.Folder;
import nl.leonvanderkaap.mp4d.music.entities.FolderRepository;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.entities.SongRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SongDatabaseBuilder {

    public static final List<String> VALID_EXTENSION = List.of("mp3", "m4a", "flac");

    private final ApplicationSettings applicationSettings;
    private final SongService songService;
    private final SongRepository songRepository;
    private final FolderRepository folderRepository;

    @Transactional
    public void buildDatabase() throws IOException {
        Path path = Path.of(applicationSettings.getBasepath());
        String absoluteBasePath = path.toAbsolutePath().toString();
        log.info("Starting folder indexing");

        Files.walkFileTree(path, new FileVisitor<>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                songService.upsertFolder(absoluteBasePath, dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        FileInfoHolder fileInfoHolder = new FileInfoHolder();
        iterateOverFiles(path.toFile(), fileInfoHolder, absoluteBasePath);
        log.info("Done with folder indexing. Found {} existing songs, {} new songs and deleted {} songs", fileInfoHolder.oldSongs, fileInfoHolder.newSongs, fileInfoHolder.deletedSongs);
    }

    private void iterateOverFiles(File directory, FileInfoHolder fileInfoHolder, String absoluteBasePath) {

        fileInfoHolder.currentFolder = upsertFolder(absoluteBasePath, directory.toPath());
        fileInfoHolder.updateCurrentFolderSongs();

        Set<String> fileNames = new HashSet<>();

        for (File file: directory.listFiles()) {
            if (!file.isDirectory()) {
                if (!isMusicFile(file.toPath())) continue;
                FileInformation fileInformation = new FileInformation(absoluteBasePath, file.toPath());
                fileNames.add(fileInformation.getFileName());
                boolean isNew = upsertSong(fileInfoHolder.currentFolder, fileInfoHolder.currentFolderSongs, absoluteBasePath, file.toPath());
                fileInfoHolder.increment(isNew);
            }
        }

        for (Song song: fileInfoHolder.currentFolderSongs) {
            if (!fileNames.contains(song.getName())) {
                songService.deleteSong(song);
                fileInfoHolder.decrement();
            }
        }


        for (File file: directory.listFiles()) {
            if (file.isDirectory()) {
                iterateOverFiles(file, fileInfoHolder, absoluteBasePath);
            }
        }

    }

    public Folder upsertFolder(String absoluteBasePath, Path path) {
        String absoluteFolderPath = path.toAbsolutePath().toString();
        String relativeFolderPath = stripPrefix(absoluteBasePath, absoluteFolderPath);
        Optional<Folder> folderOpt = songService.getMatchingFolder(relativeFolderPath);
        if (folderOpt.isPresent()) {
            return folderOpt.get();
        }

        Folder parent = null;
        if (!relativeFolderPath.equals("/")) {
            String relativeParentPath = stripPrefix(absoluteBasePath, path.getParent().toAbsolutePath().toString());
            parent = songService.getMatchingFolder(relativeParentPath).get();
        }
        Folder folder = new Folder(relativeFolderPath, parent);
        return folderRepository.save(folder);
    }

    public boolean upsertSong(Folder folder, List<Song> existingSongs, String absoluteBasePath, Path path) {
        FileInformation fileInformation = new FileInformation(absoluteBasePath, path);
        String fileName = fileInformation.getFileName();
        Song matched = null;
        for (Song song: existingSongs) {
            if (song.getName().equals(fileName)) {
                matched = song;
                break;
            }
        }

        if (matched == null) {
            Song song = songPathToSong(path, folder, fileInformation.getFileName());
            songRepository.save(song);
            return true;
        } else {
            return false;
        }
    }

    private Song songPathToSong(Path path, Folder folder, String fileName) {
        File file = path.toFile();
        Integer bitrate = null;
        Integer length = null;
        Integer mtime = null;
        int size = (int) file.length();
        String artist = null;
        String album = null;
        Integer year = null;
        String genre = null;
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            AudioHeader header = audioFile.getAudioHeader();
            bitrate = (int) header.getBitRateAsNumber();
            length = header.getTrackLength();
            mtime = 0;
            Tag tag = audioFile.getTag();
            if (tag != null) {
                artist = tag.getFirst(FieldKey.ARTIST);
                album = tag.getFirst(FieldKey.ALBUM);
                try {
                    year = Integer.parseInt(tag.getFirst(FieldKey.YEAR));
                } catch (Exception e) {}
                genre = tag.getFirst(FieldKey.GENRE);
            }
        } catch (CannotReadException e) {
            throw new RuntimeException(e);
        } catch (IOException|TagException|ReadOnlyFileException|InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        }
        if (artist != null && artist.isEmpty()) artist = null;
        if (album != null && album.isEmpty()) album = null;
        if (genre != null && genre.isEmpty()) genre = null;

        return new Song(folder, fileName, bitrate, length, mtime, size, artist, album, year, genre);
    }

    public boolean upsertPath(Path path) {
        if (!isMusicFile(path)) return false;
        String normalizedPath = path.toString().replace("\\", "/");
        int lastSlash = normalizedPath.lastIndexOf("/");
        String folderPath = lastSlash == -1 ? "/" : "/" + normalizedPath.substring(0, lastSlash);
        String fileName = normalizedPath.substring(lastSlash + 1);
        Folder folder = songService.upsertFolder(folderPath);
        Optional<Song> songOpt = songService.getMatchingSong(fileName, folder);
        if (songOpt.isPresent()) {
            return false;
        } else {
            Song song = songPathToSong(Path.of(applicationSettings.getBasepath() + "/" + path.toString()), folder, fileName);
            songRepository.save(song);
            return true;
        }
    }

    public boolean deletePath(Path path) {
        if (!isMusicFile(path)) return false;
        String normalizedPath = path.toString().replace("\\", "/");
        int lastSlash = normalizedPath.lastIndexOf("/");
        String folderPath = lastSlash == -1 ? "/" : "/" + normalizedPath.substring(0, lastSlash);
        String fileName = normalizedPath.substring(lastSlash + 1);
        Folder folder = songService.upsertFolder(folderPath);
        Optional<Song> songOpt = songService.getMatchingSong(fileName, folder);
        if  (songOpt.isPresent()) {
            songService.deleteSong(songOpt.get());
            return true;
        }
        return false;
    }


    public boolean isMusicFile(Path file) {
        String fileString = file.toString();
        int index = fileString.lastIndexOf(".");
        if (index == -1 ||(index == fileString.length() - 1)) return false;
        String extension = fileString.substring(index + 1);
        return VALID_EXTENSION.contains(extension);
    }

    public String stripPrefix(String absoluteBasePath, String absolutePath) {
        String subString = absolutePath.substring(absoluteBasePath.length());
        if (!subString.isEmpty()) return subString.replaceAll("\\\\", "/");
        return "/";
    }

    private static class FileInfoHolder {
        private Folder currentFolder;
        private List<Song> currentFolderSongs;
        private int oldSongs = 0;
        private int newSongs = 0;
        private int deletedSongs = 0;

        private void updateCurrentFolderSongs() {
            this.currentFolderSongs = currentFolder.getSongs();
        }

        private void increment(boolean isNew) {
            if (isNew) {
                newSongs++;
            } else {
                oldSongs++;
            }
        }

        private void decrement() {
            deletedSongs++;
        }
    }
}
