package com.android.gallery3d.data;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class WallpaperImage extends MediaItem {
    private GalleryApp mApplication;

    private class BitmapJob extends BaseJob<Bitmap> {
        private int mType;

        protected BitmapJob(int type) {
            this.mType = type;
        }

        public Bitmap run(JobContext jc) {
            int targetSize = MediaItem.getTargetSize(this.mType);
            Drawable wallpaper = ((WallpaperManager) WallpaperImage.this.mApplication.getAndroidContext().getSystemService("wallpaper")).getDrawable();
            if (wallpaper == null) {
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(wallpaper.getIntrinsicWidth(), wallpaper.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            wallpaper.setBounds(0, 0, wallpaper.getIntrinsicWidth(), wallpaper.getIntrinsicHeight());
            wallpaper.draw(canvas);
            if (this.mType == 2 || this.mType == 8) {
                bitmap = BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
            } else {
                bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
            }
            return bitmap;
        }

        public String workContent() {
            return String.format("decode bitmap. type: %s, path: %s", new Object[]{Integer.valueOf(this.mType), WallpaperImage.this.mPath});
        }
    }

    public WallpaperImage(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = (GalleryApp) Utils.checkNotNull(application);
    }

    public Job<Bitmap> requestImage(int type) {
        return new BitmapJob(type);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return null;
    }

    public String getFilePath() {
        return "gallery_home_wallpaper_0.jpg";
    }

    public int getSupportedOperations() {
        return 32;
    }

    public int getMediaType() {
        return 1;
    }

    public String getMimeType() {
        return "";
    }

    public Uri getContentUri() {
        return null;
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }
}
