package com.huawei.gallery.refocus.wideaperture.utils;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;

public class WideAperturePhotoUtil {
    private static boolean sSupport3DView;
    private static boolean sSupportPhotoEdit;
    private static boolean sSupportRangeMeasure;

    public static native boolean canBeMeasured(byte[] bArr);

    public static native int compressDepthData(long j, byte[] bArr);

    public static native int create3DView(long j, Bitmap bitmap, byte[] bArr, int[] iArr, int[] iArr2);

    public static native int destroy(long j);

    public static native int destroy3DView(long j);

    public static native int getDepthHeaderLength();

    public static native int getPhotoTakenAngle(byte[] bArr);

    public static native int getRefocusPhotoMode(byte[] bArr);

    public static native int getUncompressedDepthDataLength(byte[] bArr);

    public static native long init();

    public static native int invalidate3DView(long j, int[] iArr);

    private static native boolean isSupport3DView();

    private static native boolean isSupportRangeMeasure();

    public static native int prepare(long j, int i, Bitmap bitmap, byte[] bArr);

    public static native int process(long j, Bitmap bitmap, byte[] bArr, Bitmap bitmap2);

    public static native int rangeMeasure(long j, int i, int i2, int i3, int i4, int i5, int i6, byte[] bArr);

    public static native int refocusAndFilterGetProperty(long j, int i, int[] iArr, byte[] bArr);

    public static native int refocusAndFilterSetProperty(long j, int i, int[] iArr, byte[] bArr);

    public static native int set3DViewProperty(long j, int i, int[] iArr);

    private static native boolean supportEdit();

    public static native int uninit(long j);

    static {
        try {
            System.loadLibrary("jni_wide_aperture");
            sSupportPhotoEdit = supportEdit();
            sSupport3DView = isSupport3DView();
            sSupport3DView &= sSupportPhotoEdit;
            sSupportRangeMeasure = isSupportRangeMeasure();
        } catch (UnsatisfiedLinkError e) {
            GalleryLog.i("WideAperturePhotoUtil", "loadLibrary(\"jni_wide_aperture\") failed, reason: UnsatisfiedLinkError.");
            sSupportPhotoEdit = false;
            sSupport3DView = false;
            sSupportRangeMeasure = false;
        }
    }

    public static boolean supportPhotoEdit() {
        return sSupportPhotoEdit;
    }

    public static boolean support3DView() {
        return sSupport3DView;
    }

    public static boolean supportRangeMeasure() {
        return sSupportRangeMeasure;
    }
}
