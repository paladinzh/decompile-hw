package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class SnailItem extends MediaItem {
    private ScreenNail mScreenNail;

    public SnailItem(Path path) {
        super(path, MediaObject.nextVersionNumber());
    }

    public Job<Bitmap> requestImage(int type) {
        return new BaseJob<Bitmap>() {
            public Bitmap run(JobContext jc) {
                return null;
            }

            public String workContent() {
                return "nothing !!! ";
            }
        };
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new BaseJob<BitmapRegionDecoder>() {
            public BitmapRegionDecoder run(JobContext jc) {
                return null;
            }

            public String workContent() {
                return "nothing !!! ";
            }
        };
    }

    public ScreenNail getScreenNail() {
        return this.mScreenNail;
    }

    public String getMimeType() {
        return "";
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public void updateVersion() {
        this.mDataVersion = MediaObject.nextVersionNumber();
    }
}
