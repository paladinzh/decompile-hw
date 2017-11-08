package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;

public class SimilarPhotoTrash extends FileTrash {
    private boolean isSuggestClean;

    public SimilarPhotoTrash(String path) {
        super(path, null);
    }

    public int getType() {
        return 4194304;
    }

    public boolean isSuggestClean() {
        return this.isSuggestClean;
    }

    public boolean isNormal() {
        return false;
    }

    public void setSuggestClean(boolean value) {
        this.isSuggestClean = value;
    }

    public static SimilarPhotoTrash creator(PhotoSimilarBucketItem item) {
        if (item == null) {
            return null;
        }
        SimilarPhotoTrash trash = new SimilarPhotoTrash(item.mPath);
        trash.isSuggestClean = item.mSelected;
        return trash;
    }

    public boolean clean(Context cotnext) {
        boolean value = super.clean(cotnext);
        MediaUtil.deleteMediaProvider(MediaUtil.PHOTO_RUI, this.mPath);
        return value;
    }
}
