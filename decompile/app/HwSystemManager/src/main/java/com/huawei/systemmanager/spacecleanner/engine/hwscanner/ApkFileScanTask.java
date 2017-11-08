package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwApkFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil.DataHandler;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.io.File;
import java.util.List;

public class ApkFileScanTask extends Task implements DataHandler {
    private static final String TAG = "ApkFileScanTask";
    private PathEntrySet mEntrySet;
    private final List<ApkFileTrash> mTempApkList = Lists.newArrayList();

    public ApkFileScanTask(Context context) {
        super(context);
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 2;
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_apk_file);
    }

    protected void doTask(ScanParams p) {
        this.mEntrySet = p.getEntrySet();
        queryApkFile(getContext(), MediaUtil.APK_SELECTION);
        this.mTempApkList.clear();
        onPublishProgress(100, "");
        onPublishEnd();
    }

    private void queryApkFile(Context ctx, String selection) {
        Closeable closeable = null;
        try {
            closeable = ctx.getContentResolver().query(MediaUtil.FILE_URI, new String[]{"title", "_data"}, selection, null, null);
            if (closeable != null) {
                int count = closeable.getCount();
                HwLog.i(TAG, "total apk file number:" + count);
                int progress = 0;
                while (closeable.moveToNext()) {
                    String path = closeable.getString(1);
                    progress++;
                    onPublishProgress((progress / count) * 100, path);
                    if (!new File(path).exists()) {
                        HwLog.i(TAG, "file is not exist, ignore.");
                    } else if (!handlerData(path)) {
                        break;
                    }
                }
                Closeables.close(closeable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.close(closeable);
        }
    }

    public boolean handlerData(String path) {
        boolean z = false;
        PathEntry pathEntry = this.mEntrySet.getPathEntry(path);
        if (pathEntry == null) {
            HwLog.e(TAG, "handlerData can not find the pathentry of file");
            if (!isCanceled()) {
                z = true;
            }
            return z;
        }
        ApkFileTrash trash = HwApkFileTrash.createApkFileTrash(getContext(), path, pathEntry);
        for (ApkFileTrash apkFile : this.mTempApkList) {
            if (apkFile.checkIfRepeat(trash)) {
                trash.setRepeat(true);
                break;
            }
        }
        this.mTempApkList.add(trash);
        if (checkShoulPublish(trash)) {
            onPublishItemUpdate(trash);
        }
        if (!isCanceled()) {
            z = true;
        }
        return z;
    }

    public List<Integer> getSupportTrashType() {
        return Lists.newArrayList(Integer.valueOf(1024));
    }

    private boolean checkShoulPublish(ApkFileTrash trash) {
        if (getParams().getType() != 3 || trash.isBroken()) {
            return true;
        }
        return false;
    }

    public boolean isNormal() {
        return true;
    }
}
