package nl.leonvanderkaap.mp4d.music.services;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FileInformation {
    private String directory;
    private String fileName;

    public FileInformation(String basePath, Path path) {
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