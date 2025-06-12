package nl.leonvanderkaap.mp4d.music.services;

import com.sun.nio.file.ExtendedWatchEventModifier;
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

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileWatcherService {

    private final ApplicationSettings applicationSettings;
    private WatchService watcher;
    private final SongDatabaseBuilder songDatabaseBuilder;
    private Lock lock = new ReentrantLock();

    @PostConstruct
    public void postConstruct() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            log.error("Could not instantiate watch service", e);
            throw new RuntimeException(e);
        }
        WatchEvent.Kind<?>[] kinds = { ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE };

        Path dir = FileSystems.getDefault().getPath(applicationSettings.getBasepath());
        try {
            dir.register(watcher, kinds, ExtendedWatchEventModifier.FILE_TREE);
        } catch (IOException e) {
            log.error("Could not instantiate watch service", e);
            throw new RuntimeException(e);
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    private void busyLoop() {
        if (!lock.tryLock()) {
            return;
        }
        while (true) {
            try {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path path = ev.context();
                    String absolutePath = path.toAbsolutePath().toString();
                    if (!absolutePath.endsWith("~")) {
                        String kindName = kind.name();
                        if (!path.toFile().isDirectory()) {
                            if (kindName.equals("ENTRY_CREATE")) {
                                songDatabaseBuilder.upsertPath(path);
                            } else if (kindName.equals("ENTRY_DELETE")) {
                                songDatabaseBuilder.deletePath(path);
                            }
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                lock.unlock();
                break;
            }
        }
    }
}
