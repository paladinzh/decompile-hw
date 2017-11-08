package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import com.huawei.gallery.editor.cache.BitmapCache;

public class Buffer {
    private Bitmap mBitmap;
    private ImagePreset mPreset;

    public Buffer(Bitmap bitmap, BitmapCache cache) {
        if (bitmap != null) {
            this.mBitmap = cache.getBitmapCopy(bitmap);
        }
    }

    public boolean isSameSize(Bitmap bitmap) {
        boolean z = false;
        if (this.mBitmap == null || bitmap == null) {
            return false;
        }
        if (this.mBitmap.getWidth() == bitmap.getWidth() && this.mBitmap.getHeight() == bitmap.getHeight()) {
            z = true;
        }
        return z;
    }

    public synchronized void useBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas(this.mBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
    }

    public synchronized Bitmap getBitmap() {
        return this.mBitmap;
    }

    public ImagePreset getPreset() {
        return this.mPreset;
    }

    public void setPreset(ImagePreset preset) {
        this.mPreset = new ImagePreset(preset);
    }

    public void remove(BitmapCache cache) {
        if (cache.cache(this.mBitmap)) {
            this.mBitmap = null;
        }
    }
}
