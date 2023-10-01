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
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final int[] oldSong = {0};
        final int[] newSong = {0};
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

        final Folder[] currentFolder = {null};
        final List<Song>[] folderSongs = new List[]{new ArrayList<>()};

        Files.walkFileTree(path, new FileVisitor<>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                currentFolder[0] = upsertFolder(absoluteBasePath, dir);
                // Force fetch
                currentFolder[0].getSongs().size();
                folderSongs[0] = currentFolder[0].getSongs();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!isMusicFile(file)) return FileVisitResult.CONTINUE;

                boolean isNew = upsertSong(currentFolder[0], folderSongs[0], absoluteBasePath, file);
                if (isNew) {
                    newSong[0]++;} else {
                    oldSong[0]++;}
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
        log.info("Done with folder indexing. Found {} existing songs and {} new songs", oldSong, newSong);
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
            File file = path.toFile();
            Integer bitrate = null;
            Integer length = null;
            Integer mtime = null;
            int size = (int) file.length();
            try {
                AudioFile audioFile = AudioFileIO.read(path.toFile());
                AudioHeader header = audioFile.getAudioHeader();
                bitrate = (int) header.getBitRateAsNumber();
                length = header.getTrackLength();
                mtime = 0;
            } catch (CannotReadException e) {
                throw new RuntimeException(e);
            } catch (IOException|TagException|ReadOnlyFileException|InvalidAudioFrameException e) {
                throw new RuntimeException(e);
            }
            Song song = new Song(folder, fileInformation.getFileName(), bitrate, length, mtime, size);
            songRepository.save(song);
            return true;
        } else {
            return false;
        }
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
}
