package nl.leonvanderkaap.mp4d.music.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongDatabaseBuilder {

    public static final List<String> VALID_EXTENSION = List.of("mp3", "m4a", "flac");

    private final ApplicationSettings applicationSettings;
    private final SongService songService;

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

        Files.walkFileTree(path, new FileVisitor<>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!isMusicFile(file)) return FileVisitResult.CONTINUE;

                boolean isNew = songService.upsertSong(absoluteBasePath, file);
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
        log.info("Done with folder indexing");
    }

    public boolean isMusicFile(Path file) {
        String fileString = file.toString();
        int index = fileString.lastIndexOf(".");
        if (index == -1 ||(index == fileString.length() - 1)) return false;
        String extension = fileString.substring(index + 1);
        return VALID_EXTENSION.contains(extension);
    }
}
