package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.storage.GalleryStorageManager;

public class LocalScreenshotsAlbum extends LocalMergeCardAlbum {
    public LocalScreenshotsAlbum(Path path, GalleryApp application) {
        super(path, application);
    }

    protected String getInternalBucketID() {
        String subUserBucketId = GalleryStorageManager.getInstance().getSubUserScreenshotsBucketId();
        if ("0".equals(subUserBucketId)) {
            return "" + MediaSetUtils.getScreenshotsBucketID();
        }
        return MediaSetUtils.getScreenshotsBucketID() + "," + subUserBucketId;
    }

    protected String getExternalBucketID() {
        return GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDsMountedOnCurrentUser();
    }

    public String getName() {
        return this.mApplication.getResources().getString(MediaSetUtils.getScreenshotsAlbumStringId());
    }

    public String getLabel() {
        return "screenshots";
    }
}
