package com.huawei.gallery.service;

import android.content.Intent;
import android.os.Message;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.BucketHelper;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleAsync;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.util.ArrayList;

public class MediaMountService extends AsyncService {
    private static String[] sNeedResetMediaSetPath = new String[]{"/local/albumoutside", "/local/image/albumoutside", "/local/album/from/camera", "/local/camera", "/local/image/camera", "/virtual/camera_video", "/local/screenshots", "/local/image/screenshots", "/virtual/screenshots_video", "/gallery/album/timebucket"};

    protected String getServiceTag() {
        return "MediaMountService";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.arg1 = startId;
    }

    public boolean handleMessage(Message msg) {
        try {
            GalleryLog.d("MediaMountService", "reset volume start");
            long start = System.currentTimeMillis();
            GalleryStorageManager galleryStorageManager = GalleryStorageManager.getInstance();
            ArrayList<GalleryStorage> oldOuterGalleryStorageList = galleryStorageManager.getOuterGalleryStorageList();
            GalleryUtils.initializeStorageVolume(getApplicationContext());
            if (galleryStorageManager.equalsOfOuterGalleryStorageList(oldOuterGalleryStorageList)) {
                GalleryLog.d("MediaMountService", "outerVolume have not changed, reset end");
                return true;
            }
            GalleryLog.d("MediaMountService", "outerVolumeChanged, need reset");
            PhotoShareUtils.initialCloudAlbumBucketId();
            PhotoShareUtils.initialAutoUploadAlbumBucketId();
            MediaSetUtils.reset();
            BlackList.getInstance().reset();
            BucketHelper.reset();
            RecycleAsync.getInstance().start(getContentResolver());
            for (String path : sNeedResetMediaSetPath) {
                MediaSet mediaSet = (MediaSet) Path.fromString(path).getObject();
                if (mediaSet != null) {
                    mediaSet.reset();
                }
            }
            ((GalleryApp) getApplication()).getGalleryData().queryFavorite(true);
            PhotoShareUtils.resetSDKSettings();
            ((GalleryApp) getApplication()).getDataManager().notifyChange();
            GalleryLog.d("MediaMountService", "volume reset end :" + (System.currentTimeMillis() - start) + "ms");
            stopSelf(msg.arg1);
            return true;
        } finally {
            stopSelf(msg.arg1);
        }
    }
}
