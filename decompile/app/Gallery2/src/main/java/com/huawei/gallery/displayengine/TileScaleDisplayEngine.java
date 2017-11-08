package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;

public class TileScaleDisplayEngine extends DisplayEngine {
    TileScaleDisplayEngine(int imageWidth, int imageHeight) {
        super(imageWidth, imageHeight);
    }

    public boolean process(Bitmap srcBitmap, Bitmap dstBitmap, float scaleRatio, float xStart, float yStart, int fullImageWidth, int fullImageHeight, int level, DisplayEngine displayEngine) {
        if (srcBitmap.getWidth() != this.mImageWidth || srcBitmap.getHeight() != this.mImageHeight) {
            GalleryLog.e("TileScaleDisplayEngine", "TileScaleDisplayEngine process error return false: srcWidth=" + srcBitmap.getWidth() + ",srcHeight=" + srcBitmap.getHeight() + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            return false;
        } else if (displayEngine == null) {
            return false;
        } else {
            TraceController.traceBegin("TileScaleDisplayEngine.process srcWidth=" + srcBitmap.getWidth() + ",srcHeight=" + srcBitmap.getHeight() + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            boolean scaleResult = scaleProcess(srcBitmap, dstBitmap, scaleRatio, xStart, yStart, fullImageWidth, fullImageHeight, level, displayEngine);
            GalleryLog.d("TileScaleDisplayEngine", "TileScaleDisplayEngine scaleProcess " + scaleResult);
            TraceController.traceEnd();
            return scaleResult;
        }
    }
}
