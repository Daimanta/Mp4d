package nl.leonvanderkaap.mp4d.music.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.commons.exceptions.BadRequestException;
import nl.leonvanderkaap.mp4d.music.entities.Folder;
import nl.leonvanderkaap.mp4d.music.entities.FolderRepository;
import nl.leonvanderkaap.mp4d.music.entities.Song;
import nl.leonvanderkaap.mp4d.music.entities.SongRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public Optional<Folder> getFolderById(Integer id) {
        if (id == null || id == 0) {
            return folderRepository.findByPath("/");
        } else {
            return folderRepository.findById(id);
        }
    }

    public List<Song> getRandomSongs(int number) {
        return entityManager.createNativeQuery(String.format("SELECT * FROM song ORDER BY RANDOM() LIMIT %d", number), Song.class).getResultList();
    }

    public List<Song> getSongsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Some ids are null");
        }
        List<Song> result = this.songRepository.findAllById(ids).stream().toList();
        if (result.size() < ids.size()) throw new BadRequestException("Some ids are not found");
        return result;
    }

    public Optional<Song> getMatchingSong(String absoluteBasePath, Path path) {
        FileInformation fileInformation = new FileInformation(path);
        return getMatchingSong(absoluteBasePath, fileInformation);
    }

    public Optional<Song> getMatchingSong(String absoluteBasePath, FileInformation fileInformation) {
        return songRepository.findByFolderPathAndName(stripPrefix(absoluteBasePath, fileInformation.directory), fileInformation.fileName);
    }

    public Optional<Folder> getMatchingFolder(String relativeDirectory) {
        return folderRepository.findByPath(relativeDirectory);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Folder upsertFolder(String absoluteBasePath, Path path) {
        String absoluteFolderPath = path.toAbsolutePath().toString();
        String relativeFolderPath = stripPrefix(absoluteBasePath, absoluteFolderPath);
        Optional<Folder> folderOpt = getMatchingFolder(relativeFolderPath);
        if (folderOpt.isPresent()) {
            return folderOpt.get();
        }

        Folder parent = null;
        if (!relativeFolderPath.equals("/")) {
            String relativeParentPath = stripPrefix(absoluteBasePath, path.getParent().toAbsolutePath().toString());
            parent = getMatchingFolder(relativeParentPath).get();
        }
        Folder folder = new Folder(relativeFolderPath, parent);
        return folderRepository.save(folder);
    }

    public String stripPrefix(String absoluteBasePath, String absolutePath) {
        String subString = absolutePath.substring(absoluteBasePath.length());
        if (!subString.isEmpty()) return subString.replaceAll("\\\\", "/");
        return "/";
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
