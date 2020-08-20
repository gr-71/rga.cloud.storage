package rga.cloud.storage.common;

import java.util.TreeMap;

public class UpdateServerStorageMessage extends AbstractMessage {

    private TreeMap<String, Long> treeMap;

    public UpdateServerStorageMessage(TreeMap<String, Long> treeMap) {
        this.treeMap = treeMap;
    }

    public TreeMap<String, Long> getTreeMap() {
        return treeMap;
    }
}
