package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.Texture;

public class BaseEntry {
    public BitmapTexture bitmapTexture;
    public Texture content;
    protected BitmapLoader contentLoader;
    public boolean guessDeleted = true;
    public boolean inDeleteAnimation;
    public Object index;
    public boolean isCloudPlaceHolder;
    public boolean isNoThumb;
    public boolean isPreview;
    public boolean isUploadFailed;
    public boolean isWaitToUpload;
    public Path path;
    public int rotation;

    public boolean startLoad() {
        if (this.content != null || this.contentLoader == null) {
            return false;
        }
        this.contentLoader.startLoad();
        return this.contentLoader.isRequestInProgress();
    }

    public void cancelLoad() {
        if (this.contentLoader != null) {
            this.contentLoader.cancelLoad();
        }
    }

    public void recycle() {
        if (!this.inDeleteAnimation) {
            if (this.contentLoader != null) {
                this.contentLoader.recycle();
            }
            if (this.bitmapTexture != null) {
                this.bitmapTexture.recycle();
                this.bitmapTexture = null;
            }
        }
    }

    public void updateTexture(Bitmap bitmap, boolean isOpaque, boolean isPreview) {
        if (this.bitmapTexture != null) {
            this.bitmapTexture.recycle();
            this.bitmapTexture = null;
        }
        Texture bitmapTexture = new BitmapTexture(bitmap);
        this.bitmapTexture = bitmapTexture;
        this.content = bitmapTexture;
        this.bitmapTexture.setOpaque(isOpaque);
    }

    public void set(BaseEntry entry) {
        if (entry != null && entry.bitmapTexture != null) {
            Texture bitmapTexture = new BitmapTexture(entry.bitmapTexture.getBitmap().copy(Config.ARGB_8888, true));
            this.bitmapTexture = bitmapTexture;
            this.content = bitmapTexture;
            this.bitmapTexture.setOpaque(false);
        }
    }
}
