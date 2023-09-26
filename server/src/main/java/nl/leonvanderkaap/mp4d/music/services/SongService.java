package nl.leonvanderkaap.mp4d.music.services;

import jakarta.transaction.Transactional;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.entities.SongRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class SongService {

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public Optional<Song> getByUuid(String uuid) {
        return songRepository.findById(uuid);
    }


}
