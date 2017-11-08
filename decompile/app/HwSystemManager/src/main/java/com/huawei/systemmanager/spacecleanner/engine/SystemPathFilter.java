package com.huawei.systemmanager.spacecleanner.engine;

import android.text.TextUtils;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SystemPathFilter implements ITrashFilter {
    private static final List<String> SYSTEMPATH = new ArrayList();

    static {
        SYSTEMPATH.add("/data");
        SYSTEMPATH.add("/data/media/0");
        SYSTEMPATH.add("/storage/emulated/0");
    }

    private static boolean isSystemPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (SYSTEMPATH.indexOf(path.toLowerCase(Locale.ENGLISH)) != -1) {
            return true;
        }
        return false;
    }

    public void filter(Trash trash) {
        if (trash != null) {
            List<String> files = trash.getFiles();
            if (!HsmCollections.isEmpty(files)) {
                for (String file : files) {
                    if (isSystemPath(file)) {
                        trash.removeFile(file);
                    }
                }
            }
        }
    }
}
