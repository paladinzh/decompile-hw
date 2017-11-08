package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class ActionImage extends MediaItem {
    private GalleryApp mApplication;
    private int mResourceId;

    private class BitmapJob extends BaseJob<Bitmap> {
        private int mType;

        protected BitmapJob(int type) {
            this.mType = type;
        }

        public Bitmap run(JobContext jc) {
            int targetSize = MediaItem.getTargetSize(this.mType);
            Bitmap bitmap = BitmapFactory.decodeResource(ActionImage.this.mApplication.getResources(), ActionImage.this.mResourceId);
            if (this.mType == 2) {
                return BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
            }
            return BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
        }

        public String workContent() {
            return String.format("decode bitmap. type: %s, resource id: %s, path: %s", new Object[]{Integer.valueOf(this.mType), Integer.valueOf(ActionImage.this.mResourceId), ActionImage.this.mPath});
        }
    }

    public ActionImage(Path path, GalleryApp application, int resourceId) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = (GalleryApp) Utils.checkNotNull(application);
        this.mResourceId = resourceId;
    }

    public Job<Bitmap> requestImage(int type) {
        return new BitmapJob(type);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return null;
    }

    public int getSupportedOperations() {
        return 32768;
    }

    public int getMediaType() {
        return 1;
    }

    public Uri getContentUri() {
        return null;
    }

    public String getMimeType() {
        return "";
    }

    public int getHeight() {
        return 0;
    }

    public int getWidth() {
        return 0;
    }
}
