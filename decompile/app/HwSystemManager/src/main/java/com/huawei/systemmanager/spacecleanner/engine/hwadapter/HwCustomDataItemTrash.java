package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash;
import com.huawei.systemmanager.spacecleanner.utils.FileTypeHelper;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;

public class HwCustomDataItemTrash extends FileTrash {
    private int mFileBigType;
    private String mPkg;
    private int mType;

    public HwCustomDataItemTrash(String filePath, PathEntry pathEntry, String pkg, int type) {
        super(filePath, pathEntry);
        this.mType = type;
        this.mPkg = pkg;
        this.mFileBigType = FileTypeHelper.getFileType(filePath);
    }

    public int getFileBigType() {
        return this.mFileBigType;
    }

    public String getFileFromPkg() {
        return this.mPkg;
    }

    public int getType() {
        return this.mType;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean clean(Context cotnext) {
        boolean res = super.clean(cotnext);
        switch (getFileBigType()) {
            case 1:
                MediaUtil.deleteMediaProvider(MediaUtil.AUDIO_URI, this.mPath);
                break;
            case 2:
                MediaUtil.deleteMediaProvider(MediaUtil.VIDEO_RUI, this.mPath);
                break;
            case 3:
                MediaUtil.deleteMediaProvider(MediaUtil.PHOTO_RUI, this.mPath);
                break;
            default:
                MediaUtil.deleteMediaProvider(MediaUtil.FILE_URI, this.mPath);
                break;
        }
        return res;
    }
}
