package nl.leonvanderkaap.mp4d.music.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {

    Optional<Folder> findByPath(String path);
}
