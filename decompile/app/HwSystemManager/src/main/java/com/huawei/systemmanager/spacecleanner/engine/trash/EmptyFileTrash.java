package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class EmptyFileTrash extends FileTrash {
    private static final String TAG = "EmptyFileTrash";

    public EmptyFileTrash(String file, PathEntry pathEntry) {
        super(file, pathEntry);
    }

    public int getType() {
        return 32;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean clean(Context cotnext) {
        deleteFileInner(new File(getPath()));
        setCleaned();
        return true;
    }

    public long getTrashSize() {
        if (this.mSizeCache >= 0) {
            return this.mSizeCache;
        }
        if (TextUtils.isEmpty(this.mPath)) {
            return 0;
        }
        this.mSizeCache = FileUtil.getFileSize(this.mPath);
        return this.mSizeCache;
    }

    private void deleteFileInner(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileInner(child);
                }
            }
            if (!file.delete()) {
                HwLog.d(TAG, "delete file failed");
            }
        }
    }
}
