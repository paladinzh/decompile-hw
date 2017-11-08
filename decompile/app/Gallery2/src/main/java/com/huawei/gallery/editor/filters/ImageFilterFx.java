package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.fx.FilterChangableParameter;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;

public abstract class ImageFilterFx extends ImageFilter {
    public static final Object FILTER_LOCK = new Object();
    private static int sHuaweiFilterVersion = -1;

    private static native int nativeGetHuaweiFilterVersion(int i);

    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, Bitmap bitmap2, int i3, int i4, int i5, int i6);

    protected native void nativeApplyFilter2(Bitmap bitmap, int i, int i2, int i3, int i4);

    protected native void nativeApplyFilterIllusionBand(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, int i7);

    protected native void nativeApplyFilterIllusionCircle(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6);

    protected native void nativeApplyFilterMist(Bitmap bitmap, int i, int i2);

    protected native void nativeApplyFilterMorpho(Bitmap bitmap, String str, int i, int i2, int i3, FilterChangableParameter filterChangableParameter);

    public static int getVersion() {
        synchronized (FILTER_LOCK) {
            if (EditorLoadLib.FILTERJNI_LOADED) {
                if (sHuaweiFilterVersion == -1) {
                    sHuaweiFilterVersion = nativeGetHuaweiFilterVersion(0);
                    GalleryLog.d("ImageFilterFx", "get sHuaweiFilterVersion:" + sHuaweiFilterVersion);
                }
                int i = sHuaweiFilterVersion;
                return i;
            }
            i = sHuaweiFilterVersion;
            return i;
        }
    }

    public static boolean getFilterChangeable() {
        boolean z = false;
        if (!EditorLoadLib.FILTERJNI_LOADED) {
            return false;
        }
        if (sHuaweiFilterVersion == -1) {
            getVersion();
        }
        if (sHuaweiFilterVersion >= 61040) {
            z = true;
        }
        return z;
    }

    public static boolean supportIllusionFilter() {
        boolean z = false;
        if (!EditorLoadLib.FILTERJNI_LOADED) {
            return false;
        }
        if (sHuaweiFilterVersion == -1) {
            getVersion();
        }
        if (sHuaweiFilterVersion >= 61090) {
            z = true;
        }
        return z;
    }

    public static boolean supportSplashFilter() {
        boolean z = false;
        if (!EditorLoadLib.FILTERJNI_LOADED) {
            return false;
        }
        if (sHuaweiFilterVersion == -1) {
            getVersion();
        }
        if (sHuaweiFilterVersion >= 80012) {
            z = true;
        }
        return z;
    }
}
