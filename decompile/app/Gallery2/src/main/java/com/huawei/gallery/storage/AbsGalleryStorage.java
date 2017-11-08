package com.huawei.gallery.storage;

import android.content.Context;
import android.text.TextUtils;
import com.android.gallery3d.util.GalleryUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbsGalleryStorage implements GalleryStorage {
    private Map<String, Integer> mBucketIds = new HashMap(10);
    protected boolean mIsMounted;
    protected String mName;
    protected String mPath;
    protected int mRootBucketID;

    public abstract boolean isMountedOnCurrentUser();

    public abstract boolean isRemovable();

    public String getPath() {
        return this.mPath;
    }

    public String getName() {
        return this.mName;
    }

    public boolean isMounted() {
        return this.mIsMounted;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof AbsGalleryStorage)) {
            return false;
        }
        AbsGalleryStorage absGalleryStorage = (AbsGalleryStorage) o;
        if (TextUtils.equals(absGalleryStorage.mPath, this.mPath) && TextUtils.equals(absGalleryStorage.mName, this.mName) && absGalleryStorage.mIsMounted == this.mIsMounted && absGalleryStorage.mRootBucketID == this.mRootBucketID && absGalleryStorage.isMountedOnCurrentUser() == isMountedOnCurrentUser() && absGalleryStorage.isRemovable() == isRemovable()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mPath.hashCode();
    }

    public String toString() {
        return "Path:" + this.mPath + ", mounted:" + this.mIsMounted + ", name:" + this.mName;
    }

    public void copy(GalleryStorage galleryStorage) {
        this.mPath = galleryStorage.getPath();
        this.mName = galleryStorage.getName();
        this.mIsMounted = galleryStorage.isMounted();
        this.mRootBucketID = galleryStorage.getRootBucketID();
    }

    public void updateName(Context context) {
    }

    public int getRootBucketID() {
        return this.mRootBucketID;
    }

    public int getBucketID(String path) {
        Integer bucketId = (Integer) this.mBucketIds.get(path);
        if (bucketId == null) {
            bucketId = Integer.valueOf(GalleryUtils.getBucketId(new File(this.mPath, path).toString()));
            this.mBucketIds.put(path, bucketId);
        }
        return bucketId.intValue();
    }
}
