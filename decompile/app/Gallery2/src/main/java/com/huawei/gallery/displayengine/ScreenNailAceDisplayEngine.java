package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;

public class ScreenNailAceDisplayEngine extends DisplayEngine {
    ScreenNailAceDisplayEngine(int screenNailWidth, int screenNailHeight) {
        super(screenNailWidth, screenNailHeight);
    }

    public boolean process(Bitmap srcBitmap, Bitmap dstBitmap, int aceWidth, int aceHeight, DisplayEngine displayEngine) {
        if (aceWidth != this.mImageWidth || aceHeight != this.mImageHeight) {
            GalleryLog.e("ScreenNailAceDisplayEngine", "ScreenNailAceDisplayEngine process error return false: aceWidth=" + aceWidth + ",aceHeight=" + aceHeight + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            return false;
        } else if (displayEngine == null) {
            return false;
        } else {
            TraceController.traceBegin("ScreenNailAceDisplayEngine.process aceWidth=" + aceWidth + ",aceHeight=" + aceHeight + ",mImageWidth=" + this.mImageWidth + ",mImageHeight=" + this.mImageHeight);
            boolean aceResult = aceProcess(srcBitmap, dstBitmap, 1, aceWidth, aceHeight, 0, 0, true, displayEngine.getNativeHandle());
            GalleryLog.d("ScreenNailAceDisplayEngine", "ScreenNailAceDisplayEngine aceProcess " + aceResult);
            TraceController.traceEnd();
            return aceResult;
        }
    }
}
