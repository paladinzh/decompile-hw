package com.huawei.gallery.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool;

public class CameraNewItemThumbnailService extends AsyncService {
    protected String getServiceTag() {
        return "CameraNewItemThumbnailService";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.arg1 = startId;
        message.obj = intent.getData();
    }

    public boolean handleMessage(Message message) {
        try {
            Uri uri = message.obj;
            if (uri == null) {
                GalleryLog.d("CameraNewItemThumbnailService", "uri is null");
                return true;
            }
            GalleryLog.d("CameraNewItemThumbnailService", "uri is " + uri);
            long start = System.currentTimeMillis();
            String contentType = getContentResolver().getType(uri);
            DataManager dataManager = ((GalleryApp) getApplication()).getDataManager();
            MediaItem mediaItem = (MediaItem) dataManager.getMediaObject(dataManager.findPathByUri(uri, contentType));
            if (mediaItem == null) {
                GalleryLog.d("CameraNewItemThumbnailService", "mediaItem is null");
                stopSelf(message.arg1);
                return true;
            }
            Bitmap bitmap = (Bitmap) mediaItem.requestImage(1).run(ThreadPool.JOB_CONTEXT_STUB);
            if (bitmap != null) {
                bitmap.recycle();
            }
            GalleryLog.d("CameraNewItemThumbnailService", "get thumbnail for item:" + mediaItem.getFilePath() + ", time(ms):" + (System.currentTimeMillis() - start));
            stopSelf(message.arg1);
            return true;
        } catch (Exception e) {
            GalleryLog.w("CameraNewItemThumbnailService", "get thumbnail fail. " + e.getMessage());
        } finally {
            stopSelf(message.arg1);
        }
    }
}
