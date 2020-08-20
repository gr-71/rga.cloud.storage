package rga.cloud.storage.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {

    private String fileName;
    private byte [] fileData;

    public FileMessage(Path filePath) throws IOException {
        fileName = filePath.getFileName().toString();
        fileData = Files.readAllBytes(filePath);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
