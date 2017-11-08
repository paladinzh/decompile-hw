package com.huawei.gallery.storage;

import android.content.Context;
import android.os.storage.StorageVolume;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class GalleryInnerStorage extends AbsGalleryStorage {
    public GalleryInnerStorage(Context context, StorageVolume volume) {
        this.mPath = volume.getPath();
        this.mName = context.getString(R.string.internal_storage);
        this.mIsMounted = true;
        this.mRootBucketID = GalleryUtils.getBucketId(this.mPath);
    }

    public boolean isRemovable() {
        return false;
    }

    public boolean isMountedOnCurrentUser() {
        return true;
    }
}
