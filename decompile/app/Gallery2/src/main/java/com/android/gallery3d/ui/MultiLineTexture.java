package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import com.huawei.watermark.manager.parse.WMElement;

class MultiLineTexture extends CanvasTexture {
    private final Layout mLayout;

    private MultiLineTexture(Layout layout) {
        super(layout.getWidth(), layout.getHeight());
        this.mLayout = layout;
    }

    public static MultiLineTexture newInstance(String text, int maxWidth, float textSize, int color, Alignment alignment) {
        return new MultiLineTexture(new StaticLayout(text, 0, text.length(), StringTexture.getDefaultPaint(textSize, color), maxWidth, alignment, WMElement.CAMERASIZEVALUE1B1, 0.0f, true, null, 0));
    }

    protected void onDraw(Canvas canvas, Bitmap backing) {
        this.mLayout.draw(canvas);
    }
}
