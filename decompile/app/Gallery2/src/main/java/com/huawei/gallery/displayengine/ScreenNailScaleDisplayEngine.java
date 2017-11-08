package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;

public class ScreenNailScaleDisplayEngine extends DisplayEngine {
    ScreenNailScaleDisplayEngine(int imageWidth, int imageHeight) {
        super(imageWidth, imageHeight);
    }

    public boolean process(Bitmap srcBitmap, Bitmap dstBitmap, float scaleRatio, int fullImageWidth, int fullImageHeight, int level, DisplayEngine displayEngine) {
        if (srcBitmap.getWidth() != this.mImageWidth || srcBitmap.getHeight() != this.mImageHeight) {
            GalleryLog.e("ScreenNailScaleDisplayEngine", "ScreenNailScaleDisplayEngine process error return false: srcWidth=" + srcBitmap.getWidth() + ",srcHeight=" + srcBitmap.getHeight() + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            return false;
        } else if (displayEngine == null) {
            return false;
        } else {
            TraceController.traceBegin("ScreenNailScaleDisplayEngine.process srcWidth=" + srcBitmap.getWidth() + ",srcHeight=" + srcBitmap.getHeight() + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            boolean scaleResult = scaleProcess(srcBitmap, dstBitmap, scaleRatio, 0.0f, 0.0f, fullImageWidth, fullImageHeight, level, displayEngine);
            GalleryLog.d("ScreenNailScaleDisplayEngine", "ScreenNailScaleDisplayEngine scaleProcess " + scaleResult);
            TraceController.traceEnd();
            return scaleResult;
        }
    }
}
