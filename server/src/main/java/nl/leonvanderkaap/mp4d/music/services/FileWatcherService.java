package nl.leonvanderkaap.mp4d.music.services;

import io.methvin.watcher.DirectoryChangeListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import io.methvin.watcher.DirectoryWatcher;

import static io.methvin.watcher.DirectoryChangeEvent.EventType.CREATE;
import static io.methvin.watcher.DirectoryChangeEvent.EventType.DELETE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileWatcherService {

    private final ApplicationSettings applicationSettings;
    private DirectoryWatcher watcher;
    private final SongDatabaseBuilder songDatabaseBuilder;
    private Lock lock = new ReentrantLock();

    @PostConstruct
    public void postConstruct() {
        Path dir = FileSystems.getDefault().getPath(applicationSettings.getBasepath());

        try {
            watcher = DirectoryWatcher.builder()
                    .path(dir)
                    .listener(getListener())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Started file watcher");
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    private void busyLoop() {
        if (!lock.tryLock()) {
            return;
        }
        while (true) {
            try {
                watcher.watch();
            } catch (Exception e) {
                try {
                    watcher.close();
                } catch (IOException ex) {}
                lock.unlock();
                break;
            }
        }
    }

    private DirectoryChangeListener getListener() {
        return event -> {
            Path relativePath = Path.of(event.path().toString().substring(event.rootPath().toString().length() + 1));
            if (event.eventType() == CREATE) {
                songDatabaseBuilder.upsertPath(relativePath);
            } else if (event.eventType() == DELETE) {
                songDatabaseBuilder.deletePath(relativePath);
            }
        };
    };

}
