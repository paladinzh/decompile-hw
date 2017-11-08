package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.storage.GalleryStorageManager;

public class LocalCameraAlbum extends LocalMergeCardAlbum {
    public LocalCameraAlbum(Path path, GalleryApp application) {
        super(path, application);
    }

    protected String getInternalBucketID() {
        String subUserBucketId = GalleryStorageManager.getInstance().getSubUserCameraBucketId();
        if ("0".equals(subUserBucketId)) {
            return "" + MediaSetUtils.getCameraBucketId();
        }
        return MediaSetUtils.getCameraBucketId() + "," + subUserBucketId;
    }

    protected String getExternalBucketID() {
        return GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDsMountedOnCurrentUser();
    }

    public String getName() {
        return this.mApplication.getResources().getString(MediaSetUtils.getCameraAlbumStringId());
    }

    public String getLabel() {
        return "camera";
    }

    public boolean supportCacheQuery() {
        return true;
    }
}
