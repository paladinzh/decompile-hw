package com.huawei.systemmanager.comm.Storage;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.FileUtil;
import java.util.List;

public final class PathEntry {
    public static final PathEntrySet EMPTY_ENTRY_SET = new PathEntrySet();
    public static final int POSITION_INNER = 2;
    public static final int POSITION_MIX = 1;
    public static final int POSITION_SDCARD = 3;
    public static final int POSITION_UNKNOW = 0;
    public final String mPath;
    public final int mPosition;

    public PathEntry(@NonNull String path, int position) {
        this.mPath = path;
        this.mPosition = position;
    }

    public PathEntry appendPath(String name) {
        return new PathEntry(FileUtil.join(this.mPath, name), this.mPosition);
    }

    public static PathEntrySet buildRootEntrySet() {
        StorageHelper helper = StorageHelper.getStorage();
        List<PathEntry> pathList = Lists.newArrayList();
        pathList.add(new PathEntry(helper.getInnerRootPath(), 2));
        for (String p : helper.getSdcardRootPath()) {
            if (!TextUtils.isEmpty(p)) {
                pathList.add(new PathEntry(p, 3));
            }
        }
        return new PathEntrySet(pathList);
    }

    public static PathEntrySet buildInternalRootEntrySet() {
        StorageHelper helper = StorageHelper.getStorage();
        List<PathEntry> pathList = Lists.newArrayList();
        pathList.add(new PathEntry(helper.getInnerRootPath(), 2));
        return new PathEntrySet(pathList);
    }
}
