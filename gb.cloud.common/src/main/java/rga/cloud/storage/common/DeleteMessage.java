package rga.cloud.storage.common;

public class DeleteMessage extends AbstractMessage {

    private String deletingFileName;

    public DeleteMessage(String deletingFileName) {
        this.deletingFileName = deletingFileName;
    }

    public String getDeletingFileName() {
        return deletingFileName;
    }
}