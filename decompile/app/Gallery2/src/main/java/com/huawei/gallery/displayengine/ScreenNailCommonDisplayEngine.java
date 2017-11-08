package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class ScreenNailCommonDisplayEngine extends DisplayEngine {
    private int mAlgoType = 0;

    ScreenNailCommonDisplayEngine(int screenNailWidth, int screenNailHeight, int algoType) {
        super(screenNailWidth, screenNailHeight);
        this.mAlgoType = algoType;
    }

    private boolean initialize() {
        boolean z = true;
        TraceController.traceBegin("ScreenNailCommonDisplayEngine.initialize");
        GalleryLog.d("ScreenNailCommonDisplayEngine", "displayEngineCommonCreate begin mCreateSuccess = " + this.mCreateSuccess);
        if (this.mCreateSuccess) {
            return false;
        }
        int ret;
        synchronized (this.mNativeLockObject) {
            ret = DisplayEngineUtils.displayEngineCommonCreate(this.mXmlFilePath, this.mImageWidth, this.mImageHeight, this.mAlgoType, this);
            this.mCreateSuccess = ret == 0;
        }
        GalleryLog.d("ScreenNailCommonDisplayEngine", "ScreenNailCommonDisplayEngine initialize:" + ret + ", engine is " + this + ", handle is " + this.mNativeAlgoHandle);
        TraceController.traceEnd();
        if (ret != 0) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    private boolean process(Bitmap nailBitmap, int iso, int colorSpace) {
        boolean z = false;
        GalleryLog.d("ScreenNailCommonDisplayEngine", "ScreenNailCommonDisplayEngine start to process, mCreateSuccess=" + this.mCreateSuccess);
        if (nailBitmap == null || nailBitmap.isRecycled() || !this.mCreateSuccess || nailBitmap.getConfig() != Config.ARGB_8888) {
            return false;
        }
        int ret;
        TraceController.traceBegin("ScreenNailCommonDisplayEngine.process");
        synchronized (this.mNativeLockObject) {
            ret = DisplayEngineUtils.displayEngineCommonProcess(nailBitmap, iso, colorSpace, this.mNativeAlgoHandle);
        }
        GalleryLog.d("ScreenNailCommonDisplayEngine", "ScreenNailCommonDisplayEngine process:" + ret);
        TraceController.traceEnd();
        if (ret == 0) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    public synchronized boolean destroy() {
        boolean z = false;
        synchronized (this) {
            GalleryLog.d("ScreenNailCommonDisplayEngine", "displayEngineCommonDestroy start to destroy, mCreateSuccess=" + this.mCreateSuccess);
            if (this.mCreateSuccess) {
                int ret;
                TraceController.traceBegin("ScreenNailCommonDisplayEngine.destroy");
                long handle = this.mNativeAlgoHandle;
                synchronized (this.mNativeLockObject) {
                    ret = DisplayEngineUtils.displayEngineCommonDestroy(handle);
                    if (ret == 0) {
                        this.mCreateSuccess = false;
                    }
                }
                GalleryLog.d("ScreenNailCommonDisplayEngine", "ScreenNailCommonDisplayEngine destroy:" + ret + ", engine is " + this + ", handle is " + handle);
                TraceController.traceEnd();
                if (ret == 0) {
                    z = true;
                }
            } else {
                return false;
            }
        }
    }

    public boolean extractCommonInfoFromScreenNail(Bitmap screenNail, int iso, int colorSpace) {
        if (screenNail == null || !initialize()) {
            return false;
        }
        if (process(screenNail, iso, colorSpace)) {
            return true;
        }
        if (!destroy()) {
            GalleryLog.w("ScreenNailCommonDisplayEngine", "extractCommonInfoFromScreenNail process screen nail failed, destroy failed too");
        }
        return false;
    }

    public int getSharpnessLevel() {
        if (this.mCreateSuccess) {
            return DisplayEngineUtils.displayEngineGetCommonSharpnessLevel(this.mNativeAlgoHandle);
        }
        GalleryLog.w("ScreenNailCommonDisplayEngine", "getSharpnessLevel failed, this displayEngineCommon has destroyed.");
        return -202;
    }

    protected void finalize() {
        GalleryLog.d("ScreenNailCommonDisplayEngine", "destroy ScreenNailCommonDisplayEngine in finalize, engine:" + this);
        destroy();
    }
}
