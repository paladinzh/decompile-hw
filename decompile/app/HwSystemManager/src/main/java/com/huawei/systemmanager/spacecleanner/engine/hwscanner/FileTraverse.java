package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.text.TextUtils;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.List;

public abstract class FileTraverse {
    private static final String TAG = "FileTraverse";
    private volatile boolean mCanceld;
    private int scanProgress = 0;

    public void start() {
    }

    protected void startScan(List<PathEntry> entries) {
        for (PathEntry entry : entries) {
            String path = entry.mPath;
            if (TextUtils.isEmpty(path)) {
                HwLog.e(TAG, "path is empty! position = " + entry.mPosition);
            } else {
                scanFile(path, FileVisitSign.create(entry), 0);
            }
        }
    }

    protected void scanFile(String path, FileVisitSign visitResult, int deepLevel) {
        if (!checkCancelSignal(path, visitResult, deepLevel)) {
            if (FileUtil.isDirectory(path)) {
                File file = new File(path);
                FileVisitSign vr = onCheckBeforeListDirectory(file, visitResult, deepLevel);
                if (vr != FileVisitSign.TERMINATE) {
                    String[] children = file.list();
                    if (children != null) {
                        for (String childName : children) {
                            scanFile(path + File.separatorChar + childName, vr, deepLevel + 1);
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            onCheckFile(path, visitResult, deepLevel);
        }
    }

    protected boolean checkCancelSignal(String path, FileVisitSign visitResult, int deepLevel) {
        if (visitResult == FileVisitSign.TERMINATE || this.mCanceld) {
            return true;
        }
        if (FileUtil.isExsist(path)) {
            return false;
        }
        HwLog.i(TAG, "file not exsist");
        return true;
    }

    public void cancel() {
        this.mCanceld = true;
    }

    protected FileVisitSign onCheckBeforeListDirectory(File file, FileVisitSign visitResult, int deepLevel) {
        return visitResult;
    }

    protected void onCheckFile(String path, FileVisitSign visitResult, int deepLevel) {
    }

    public int getProgress() {
        return this.scanProgress;
    }

    public boolean isCanceled() {
        return this.mCanceld;
    }
}
