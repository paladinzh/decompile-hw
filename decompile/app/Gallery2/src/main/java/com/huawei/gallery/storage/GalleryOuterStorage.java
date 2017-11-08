package com.huawei.gallery.storage;

import android.content.Context;
import android.os.storage.StorageVolume;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class GalleryOuterStorage extends AbsGalleryStorage {
    private int mIndex;
    private boolean mMountedOnCurrentUser = true;
    private boolean mNameFromSystemVolume = true;
    private boolean mRemovable = true;

    public GalleryOuterStorage(Context context, StorageVolume volume, int index, boolean mountedOnCurrentUser) {
        this.mPath = volume.getPath();
        this.mIsMounted = StorageUtils.isStorageMounted(volume);
        this.mRemovable = volume.isRemovable();
        String description = volume.getDescription(context);
        if (description == null) {
            this.mNameFromSystemVolume = false;
            description = context.getString(R.string.external_multi_storage, new Object[]{Integer.valueOf(index)});
        }
        this.mName = description;
        this.mIndex = index;
        this.mRootBucketID = GalleryUtils.getBucketId(this.mPath);
        this.mMountedOnCurrentUser = mountedOnCurrentUser;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void updateName(Context context) {
        if (!this.mNameFromSystemVolume) {
            this.mName = context.getString(R.string.external_storage);
        }
    }

    public boolean isRemovable() {
        return this.mRemovable;
    }

    public boolean isMountedOnCurrentUser() {
        return this.mMountedOnCurrentUser;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!super.equals(o) || !(o instanceof GalleryOuterStorage)) {
            return false;
        }
        GalleryOuterStorage galleryOuterStorage = (GalleryOuterStorage) o;
        if (galleryOuterStorage.mIndex == this.mIndex && galleryOuterStorage.mNameFromSystemVolume == this.mNameFromSystemVolume) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString() + ", index:" + this.mIndex;
    }

    public void copy(GalleryStorage galleryStorage) {
        super.copy(galleryStorage);
        if (galleryStorage instanceof GalleryOuterStorage) {
            this.mIndex = ((GalleryOuterStorage) galleryStorage).getIndex();
            this.mNameFromSystemVolume = ((GalleryOuterStorage) galleryStorage).mNameFromSystemVolume;
            this.mRemovable = ((GalleryOuterStorage) galleryStorage).mRemovable;
            this.mMountedOnCurrentUser = ((GalleryOuterStorage) galleryStorage).mMountedOnCurrentUser;
        }
    }
}
