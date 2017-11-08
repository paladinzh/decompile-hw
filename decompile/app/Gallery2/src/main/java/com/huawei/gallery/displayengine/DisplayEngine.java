package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.DisplayEngineUtils.Display_ImageType;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class DisplayEngine {
    protected boolean mCreateSuccess;
    protected int mImageHeight;
    protected Display_ImageType mImageType;
    protected int mImageWidth;
    protected long mNativeAlgoHandle;
    protected final Object mNativeLockObject;
    protected String mXmlFilePath;

    DisplayEngine(int imageWidth, int imageHeight, Display_ImageType imageType) {
        this.mCreateSuccess = false;
        this.mNativeLockObject = new Object();
        this.mXmlFilePath = DisplayEngineUtils.getXmlFilePath();
        this.mImageWidth = imageWidth;
        this.mImageHeight = imageHeight;
        this.mImageType = imageType;
    }

    DisplayEngine(int imageWidth, int imageHeight) {
        this(imageWidth, imageHeight, Display_ImageType.RGBA);
    }

    public long getNativeHandle() {
        return this.mNativeAlgoHandle;
    }

    public boolean initialize(int border, int algoType, float maxScale) {
        TraceController.traceBegin("DisplayEngine create");
        GalleryLog.d("DisplayEngine", "display create begin mCreateSuccess = " + this.mCreateSuccess);
        if (this.mCreateSuccess) {
            return false;
        }
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            int ret;
            boolean z;
            if (maxScale > 2.0f) {
                maxScale = 2.0f;
            }
            synchronized (this.mNativeLockObject) {
                ret = DisplayEngineUtils.displayEngineCreate(this.mXmlFilePath, this.mImageWidth, this.mImageHeight, maxScale, border, algoType, this);
                if (ret == 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mCreateSuccess = z;
                GalleryLog.d("DisplayEngine", "xml file path is " + this.mXmlFilePath + ", engine create result:" + ret);
            }
            TraceController.traceEnd();
            if (ret == 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
        TraceController.traceEnd();
        return false;
    }

    @SuppressWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    public boolean destroy() {
        boolean z = false;
        GalleryLog.d("DisplayEngine", "display engine want to destroy, engine has created success ? " + this.mCreateSuccess);
        if (!this.mCreateSuccess) {
            return false;
        }
        int ret;
        TraceController.traceBegin("DisplayEngine destroy");
        synchronized (this.mNativeLockObject) {
            ret = DisplayEngineUtils.displayEngineDestroy(this.mNativeAlgoHandle);
            if (ret == 0) {
                this.mCreateSuccess = false;
            }
        }
        GalleryLog.d("DisplayEngine", "display engine has destroyed:" + ret);
        TraceController.traceEnd();
        if (ret == 0) {
            z = true;
        }
        return z;
    }

    @SuppressWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    protected boolean aceProcess(Bitmap srcBitmap, Bitmap dstBitmap, int aceBlockNum, int aceWidth, int aceHeight, int aceStartX, int aceStartY, boolean isScreenNail, long nativeHandle) {
        if (!this.mCreateSuccess) {
            return false;
        }
        if (srcBitmap == null || dstBitmap == null || srcBitmap.isRecycled() || dstBitmap.isRecycled()) {
            GalleryLog.e("DisplayEngine", "srProcess srcBitmap or dstBitmap is illegal");
            return false;
        } else if (srcBitmap.getConfig() != Config.ARGB_8888 || dstBitmap.getConfig() != Config.ARGB_8888) {
            return false;
        } else {
            int ret;
            boolean z;
            synchronized (this.mNativeLockObject) {
                ret = DisplayEngineUtils.displayEngineACEProcess(srcBitmap, dstBitmap, this.mImageType.getType(), this.mNativeAlgoHandle, aceBlockNum, aceWidth, aceHeight, aceStartX, aceStartY, isScreenNail, nativeHandle);
            }
            if (ret == 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
    }

    @SuppressWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    protected boolean scaleProcess(Bitmap srcBitmap, Bitmap dstBitmap, float scaleRatio, float xStart, float yStart, int fullImageWidth, int fullImageHeight, int level, DisplayEngine displayEngine) {
        if (!this.mCreateSuccess || displayEngine == null) {
            return false;
        }
        if (srcBitmap == null || dstBitmap == null || srcBitmap.isRecycled() || dstBitmap.isRecycled()) {
            GalleryLog.e("DisplayEngine", "srProcess srcBitmap or dstBitmap is illegal");
            return false;
        }
        Config srcConfig = srcBitmap.getConfig();
        Config dstConfig = dstBitmap.getConfig();
        if (srcConfig != Config.ARGB_8888 || dstConfig != Config.ARGB_8888) {
            return false;
        }
        boolean z;
        synchronized (this.mNativeLockObject) {
            int ret = DisplayEngineUtils.displayEngineSRProcess(srcBitmap, dstBitmap, this.mImageType.getType(), this.mNativeAlgoHandle, scaleRatio, xStart, yStart, fullImageWidth, fullImageHeight, level, displayEngine.getNativeHandle());
        }
        if (ret == 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }
}
