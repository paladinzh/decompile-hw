package com.huawei.systemmanager.spacecleanner.engine;

import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.Map;

public class VedioFilter implements ITrashFilter {
    private Map<String, Trash> fileMaps = HsmCollections.newArrayMap();

    public void filter(Trash curTrash) {
        int curTrashType = curTrash.getType();
        if (curTrashType == 256 || curTrashType == 65536) {
            for (String path : curTrash.getFiles()) {
                Trash pre = (Trash) this.fileMaps.get(path);
                if (pre == null) {
                    this.fileMaps.put(path, curTrash);
                } else if (curTrashType != pre.getType()) {
                    if (curTrashType == 256) {
                        curTrash.removeFile(path);
                    } else if (curTrashType == 65536) {
                        this.fileMaps.put(path, curTrash);
                        pre.removeFile(path);
                    }
                }
            }
        }
    }
}
