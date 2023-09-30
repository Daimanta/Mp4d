package nl.leonvanderkaap.mp4d;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.music.services.SongService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Component
@Slf4j
@RequiredArgsConstructor
public class BootListener {

    private final ApplicationSettings applicationSettings;
    private final SongService songService;

    @EventListener
    public void buildDatabase(ApplicationStartedEvent event) throws IOException {
        Path path = Path.of(applicationSettings.getBasepath());
        String absoluteBasePath = path.toAbsolutePath().toString();
        final int[] oldSong = {0};
        final int[] newSong = {0};
        Files.walkFileTree(path, new FileVisitor<Path>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                songService.upsertFolder(absoluteBasePath, dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean isNew = songService.upsertSong(file);
                if (isNew) {
                    newSong[0]++;} else {
                    oldSong[0]++;}
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
