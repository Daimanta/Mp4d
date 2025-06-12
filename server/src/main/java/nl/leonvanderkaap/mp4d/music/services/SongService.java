package nl.leonvanderkaap.mp4d.music.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.leonvanderkaap.mp4d.commons.ApplicationSettings;
import nl.leonvanderkaap.mp4d.commons.exceptions.BadRequestException;
import nl.leonvanderkaap.mp4d.commons.exceptions.NotFoundException;
import nl.leonvanderkaap.mp4d.music.controllers.dtos.SearchRequestDto;
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

    public List<Folder> getFolderPathToRoot(int id) {
        List<Folder> result;
        List<Folder> reversed = new ArrayList<>();
        Folder current = getFolderById(id).orElseThrow(() -> new NotFoundException("Folder not found"));
        while (current != null) {
            reversed.add(current);
            current = current.getParent();
        }
        result = reversed.reversed();
        return result;
    }

    public List<Folder> getFoldersByIds(List<Integer> ids) {
        return folderRepository.findAllById(ids);
    }

    public List<Song> getRandomSongs(int number) {
        return entityManager.createNativeQuery(String.format("SELECT * FROM song ORDER BY RANDOM() LIMIT %d", number), Song.class).getResultList();
    }

    public Folder getRandomFolder() {
        return (Folder) entityManager.createNativeQuery("SELECT * FROM folder ORDER BY RANDOM() LIMIT 1", Folder.class).getSingleResult();
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

    public Optional<Song> getSongById(String uuid) {
        return this.songRepository.findById(uuid);
    }

    public Optional<Song> getMatchingSong(String absoluteBasePath, Path path) {
        FileInformation fileInformation = new FileInformation(path);
        return getMatchingSong(absoluteBasePath, fileInformation);
    }

    public Optional<Song> getMatchingSong(String absoluteBasePath, FileInformation fileInformation) {
        return songRepository.findByFolderPathAndName(stripPrefix(absoluteBasePath, fileInformation.directory), fileInformation.fileName);
    }

    public Optional<Song> getMatchingSong(String songName, Folder folder) {
        return songRepository.findByFolderAndFilename(folder, songName);
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

    public Folder upsertFolder(String relativePath) {
        Optional<Folder> folderOpt = getMatchingFolder(relativePath);
        if (folderOpt.isPresent()) {
            return folderOpt.get();
        }
        return null;
    }

    public String stripPrefix(String absoluteBasePath, String absolutePath) {
        String subString = absolutePath.substring(absoluteBasePath.length());
        if (!subString.isEmpty()) return subString.replaceAll("\\\\", "/");
        return "/";
    }

    public List<StringGroup> getGroupedByArtist() {
        List<Object[]> res =  (List<Object[]>) entityManager.createNativeQuery("SELECT artist, COUNT(*) FROM song WHERE artist IS NOT NULL GROUP BY ARTIST", Object[].class).getResultList();
        return res.stream().map(x -> new StringGroup((String)x[0], (int)x[1])).toList();
    }

    public List<StringGroup> getGroupedByAlbum() {
        List<Object[]> res = (List<Object[]>) entityManager.createNativeQuery("SELECT album, COUNT(*) FROM song WHERE album IS NOT NULL GROUP BY album", Object[].class).getResultList();
        return res.stream().map(x -> new StringGroup((String)x[0], (int)x[1])).toList();
    }

    public List<IntegerGroup> getGroupedByYear() {
        List<Object[]> res = (List<Object[]>) entityManager.createNativeQuery("SELECT year, COUNT(*) FROM song WHERE year IS NOT NULL GROUP BY year", Object[].class).getResultList();
        return res.stream().map(x -> new IntegerGroup((int)x[0], (int)x[1])).toList();
    }

    public List<StringGroup> getGroupedByGenre() {
        List<Object[]> res = (List<Object[]>) entityManager.createNativeQuery("SELECT genre, COUNT(*) FROM song WHERE genre IS NOT NULL GROUP BY genre", Object[].class).getResultList();
        return res.stream().map(x -> new StringGroup((String)x[0], (int)x[1])).toList();
    }

    public List<Song> searchSongs(SearchRequestDto searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Song> cq = cb.createQuery(Song.class);

        Root<Song> root = cq.from(Song.class);

        List<Predicate> predicates = new ArrayList<>();

        if (searchRequest.getSong() != null && !searchRequest.getSong().isBlank()) {
            predicates.add(iLikePredicate(cb, root, "name", searchRequest.getSong()));
        }

        if (searchRequest.getArtist() != null && !searchRequest.getArtist().isBlank()) {
            predicates.add(iLikePredicate(cb, root, "artist", searchRequest.getArtist()));
        }

        if (searchRequest.getAlbum() != null && !searchRequest.getAlbum().isBlank()) {
            predicates.add(iLikePredicate(cb, root, "album", searchRequest.getAlbum()));
        }

        if (searchRequest.getYear() != null) {
            predicates.add(cb.equal(root.get("year"), searchRequest.getYear()));
        }

        if (searchRequest.isAll()) {
            cq.where(predicates.toArray(new Predicate[0]));
        } else {
            cq.where(cb.or(predicates.toArray(new Predicate[0])));
        }

        cq.distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }

    private static Predicate iLikePredicate(CriteriaBuilder criteriaBuilder, Root<?> root, String fieldname, String matcher) {
        return criteriaBuilder.like(criteriaBuilder.lower(root.get(fieldname)), String.format("%%%s%%", matcher));
    }

    public void deleteSong(Song song) {
        songRepository.delete(song);
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


    @Getter
    @AllArgsConstructor
    public static class StringGroup {
        String string;
        int count;
    }

    @Getter
    @AllArgsConstructor
    public static class IntegerGroup {
        int integer;
        int count;
    }
}
