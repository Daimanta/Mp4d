package nl.leonvanderkaap.mp4d.music.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {

    @Query("SELECT s FROM Song s INNER JOIN Folder f ON s.folder = f WHERE s.name = :name AND f.path = :folderPath")
    Optional<Song> findByFolderPathAndName(String folderPath, String name);
}
