package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;

public class PhotoTrash extends FileTrash {
    public PhotoTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 128;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean clean(Context cotnext) {
        boolean res = super.clean(cotnext);
        MediaUtil.deleteMediaProvider(MediaUtil.PHOTO_RUI, this.mPath);
        return res;
    }
}
