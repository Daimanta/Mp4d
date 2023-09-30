package nl.leonvanderkaap.mp4d.music.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.music.entities.Folder;
import nl.leonvanderkaap.mp4d.music.entities.FolderRepository;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.entities.SongRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SongService {

    private final ApplicationSettings applicationSettings;
    private final SongRepository songRepository;
    private final FolderRepository folderRepository;
    private final EntityManager entityManager;


    public Optional<Song> getByUuid(String uuid) {
        return songRepository.findById(uuid);
    }

    public Optional<Song> getMatchingSong(Path path) {
        FileInformation fileInformation = new FileInformation(path);
        return getMatchingSong(fileInformation);
    }

    public Optional<Song> getMatchingSong(FileInformation fileInformation) {
        return songRepository.findByFolderPathAndName(fileInformation.directory, fileInformation.fileName);
    }

    public Optional<Folder> getMatchingFolder(String relativeDirectory) {
        return folderRepository.findByPath(relativeDirectory);
    }

    public boolean upsertFolder(String absoluteBasePath, Path path) {
        String absoluteFolderPath = path.toAbsolutePath().toString();
        String relativeFolderPath = stripPrefix(absoluteBasePath, absoluteFolderPath);
        Optional<Folder> folderOpt = getMatchingFolder(relativeFolderPath);
        if (folderOpt.isPresent()) return false;
        folderRepository.save(new Folder(relativeFolderPath));
        return true;
    }

    public String stripPrefix(String absoluteBasePath, String absolutePath) {
        String subString = absolutePath.substring(absoluteBasePath.length());
        if (!subString.isEmpty()) return subString.replaceAll("\\\\", "/");
        return "/";
    }

    public boolean upsertSong(Path path) {
        FileInformation fileInformation = new FileInformation(path);
        Optional<Song> songOpt = getMatchingSong(fileInformation);
        if (songOpt.isEmpty()) {
            Folder folder = getMatchingFolder(fileInformation.directory).orElseThrow(() -> new RuntimeException());
            Song song = new Song(folder, fileInformation.fileName);
            songRepository.save(song);
            return true;
        } else {
            return false;
        }
    }

    private class FileInformation {
        private String directory;
        private String fileName;

        private FileInformation(Path path) {
            String basePath = applicationSettings.getBasepath();
            String directoryPath = path.getParent().toString().replace("\\", "/");
            String fileName = path.getFileName().toString();

            String relativeDirectoryPath = directoryPath;
            if (directoryPath.startsWith(basePath)) {
                relativeDirectoryPath = relativeDirectoryPath.substring(basePath.length());
            }
            if (relativeDirectoryPath.isEmpty()) relativeDirectoryPath = "/";

            this.directory = relativeDirectoryPath;
            this.fileName = fileName;
        }

        private FileInformation(String directory, String fileName) {
            this.directory = directory;
            this.fileName = fileName;
        }
    }

}
