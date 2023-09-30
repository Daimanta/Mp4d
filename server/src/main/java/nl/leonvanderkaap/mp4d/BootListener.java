package nl.leonvanderkaap.mp4d;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.leonvanderkaap.mp4d.music.services.SongDatabaseBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BootListener {

    private final SongDatabaseBuilder songDatabaseBuilder;

    @EventListener
    public void buildDatabase(ApplicationStartedEvent event) throws IOException {
        songDatabaseBuilder.buildDatabase();
    }

}
