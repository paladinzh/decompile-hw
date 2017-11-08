package com.huawei.gallery.storage;

import android.content.Context;

public interface GalleryStorage {
    void copy(GalleryStorage galleryStorage);

    int getBucketID(String str);

    String getName();

    String getPath();

    int getRootBucketID();

    boolean isMounted();

    boolean isMountedOnCurrentUser();

    boolean isRemovable();

    void updateName(Context context);
}
