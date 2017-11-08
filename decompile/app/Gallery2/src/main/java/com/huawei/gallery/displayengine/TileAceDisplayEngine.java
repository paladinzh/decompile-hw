package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;

public class TileAceDisplayEngine extends DisplayEngine {
    TileAceDisplayEngine(int imageWidth, int imageHeight) {
        super(imageWidth, imageHeight);
    }

    public boolean process(Bitmap srcBitmap, Bitmap dstBitmap, int imageWidth, int imageHeight, int startX, int startY, int border, DisplayEngine displayEngine) {
        if (displayEngine == null) {
            return false;
        }
        return aceProcess(srcBitmap, dstBitmap, 1, imageWidth, imageHeight, startX - border, startY - border, false, displayEngine.getNativeHandle());
    }
}
