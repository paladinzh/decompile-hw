package com.android.gallery3d.ui;

import com.android.gallery3d.common.Utils;

public class ColorTexture implements Texture {
    private final int mColor;
    private int mHeight = 1;
    private int mWidth = 1;

    public ColorTexture(int color) {
        this.mColor = color;
    }

    public void draw(GLCanvas canvas, int x, int y) {
        draw(canvas, x, y, this.mWidth, this.mHeight);
    }

    public void draw(GLCanvas canvas, int x, int y, int w, int h) {
        canvas.fillRect((float) x, (float) y, (float) w, (float) h, this.mColor);
    }

    public boolean isOpaque() {
        return Utils.isOpaque(this.mColor);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }
}
