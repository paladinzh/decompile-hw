package com.huawei.gallery.editor.omron;

import android.graphics.Bitmap;
import android.graphics.RectF;

public final class FaceDetection extends OkaoHandle {
    private static final Object LOCK = new Object();

    private static native int getOmronVersion();

    private native long nativeCreate();

    private native int nativeDelete(long j);

    private native int nativeDetection(Bitmap bitmap, long j, int i, long j2);

    private native RectF[] nativeGetFaceInfo(Bitmap bitmap, long j, int i, long j2, RectF rectF);

    private native int nativeSetAngle(long j, int[] iArr, int i);

    private native int nativeSetFaceSizeRange(long j, int i, int i2);

    private native int nativeSetMode(long j, int i);

    public static FaceDetection create() {
        FaceDetection faceDetection = new FaceDetection();
        synchronized (LOCK) {
            faceDetection.okaoHandle = faceDetection.nativeCreate();
        }
        if (faceDetection.okaoHandle == 0) {
            return null;
        }
        return faceDetection;
    }

    public int delete() {
        int net = -7;
        if (this.okaoHandle != 0) {
            synchronized (LOCK) {
                net = nativeDelete(this.okaoHandle);
            }
            this.okaoHandle = 0;
        }
        return net;
    }

    public int detection(Bitmap bitmap, int nAccuracy, FaceDetectionResult result) {
        long hDt = this.okaoHandle;
        if (result == null) {
            return -7;
        }
        int nativeDetection;
        long hDtResult = result.getOkaoHandle();
        synchronized (LOCK) {
            nativeDetection = nativeDetection(bitmap, hDt, nAccuracy, hDtResult);
        }
        return nativeDetection;
    }

    public RectF[] detection(Bitmap bitmap, int nAccuracy, FaceDetectionResult result, RectF rect) {
        long hDt = this.okaoHandle;
        if (result == null) {
            return new RectF[0];
        }
        RectF[] nativeGetFaceInfo;
        long hDtResult = result.getOkaoHandle();
        synchronized (LOCK) {
            nativeGetFaceInfo = nativeGetFaceInfo(bitmap, hDt, nAccuracy, hDtResult, rect);
        }
        return nativeGetFaceInfo;
    }

    public int setMode(int nMode) {
        int nativeSetMode;
        synchronized (LOCK) {
            nativeSetMode = nativeSetMode(this.okaoHandle, nMode);
        }
        return nativeSetMode;
    }

    public int setFaceSizeRange(int nMinSize, int nMaxSize) {
        int nativeSetFaceSizeRange;
        synchronized (LOCK) {
            nativeSetFaceSizeRange = nativeSetFaceSizeRange(this.okaoHandle, nMinSize, nMaxSize);
        }
        return nativeSetFaceSizeRange;
    }

    public int setAngle(int[] anNonTrackingAngle, int nTrackingAngleExtension) {
        int nativeSetAngle;
        synchronized (LOCK) {
            nativeSetAngle = nativeSetAngle(this.okaoHandle, anNonTrackingAngle, nTrackingAngleExtension);
        }
        return nativeSetAngle;
    }

    public static int getOmronSoVersion() {
        int omronVersion;
        synchronized (LOCK) {
            omronVersion = getOmronVersion();
        }
        return omronVersion;
    }
}
