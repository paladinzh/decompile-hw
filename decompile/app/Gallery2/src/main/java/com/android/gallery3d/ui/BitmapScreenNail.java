package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class BitmapScreenNail extends AbstractGifScreenNail implements ScreenNail {
    private final BitmapTexture mBitmapTexture;

    public BitmapScreenNail(Bitmap bitmap) {
        this.mBitmapTexture = new BitmapTexture(bitmap);
    }

    public int getWidth() {
        return this.mBitmapTexture.getWidth();
    }

    public int getHeight() {
        return this.mBitmapTexture.getHeight();
    }

    public void draw(GLCanvas canvas, int x, int y, int width, int height) {
        if (!super.drawGifIfNecessary(canvas, x, y, width, height)) {
            this.mBitmapTexture.draw(canvas, x, y, width, height);
        }
    }

    public void noDraw() {
    }

    public void recycle() {
        this.mBitmapTexture.recycle();
        super.recycle();
    }

    public void draw(GLCanvas canvas, RectF source, RectF dest) {
        canvas.drawTexture(this.mBitmapTexture, source, dest);
    }

    public Bitmap getBitmap() {
        return this.mBitmapTexture.getBitmap();
    }

    public boolean isLoaded() {
        return this.mBitmapTexture.isLoaded();
    }
}
