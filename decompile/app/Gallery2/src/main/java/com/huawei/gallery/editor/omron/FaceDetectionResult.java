package com.huawei.gallery.editor.omron;

public final class FaceDetectionResult extends OkaoHandle {
    private static final Object LOCK = new Object();

    private native int nativeClear(long j);

    private native long nativeCreate(int i, int i2);

    private native int nativeDelete(long j);

    private native int nativeGetFaceCount(long j);

    public static FaceDetectionResult createResult(int nMaxFaceNumber, int nMaxSwapNumber) {
        FaceDetectionResult faceDetRes = new FaceDetectionResult();
        synchronized (LOCK) {
            faceDetRes.okaoHandle = faceDetRes.nativeCreate(nMaxFaceNumber, nMaxSwapNumber);
        }
        if (faceDetRes.okaoHandle == 0) {
            return null;
        }
        return faceDetRes;
    }

    public int clearResult() {
        int nativeClear;
        synchronized (LOCK) {
            nativeClear = nativeClear(this.okaoHandle);
        }
        return nativeClear;
    }

    public int deleteResult() {
        int net = -7;
        if (this.okaoHandle != 0) {
            synchronized (LOCK) {
                net = nativeDelete(this.okaoHandle);
            }
            this.okaoHandle = 0;
        }
        return net;
    }

    public int getFaceCount() {
        int nativeGetFaceCount;
        synchronized (LOCK) {
            nativeGetFaceCount = nativeGetFaceCount(this.okaoHandle);
        }
        return nativeGetFaceCount;
    }
}
