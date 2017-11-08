package fyusion.vislib;

import android.graphics.Bitmap;

/* compiled from: Unknown */
public class OnlineImageStabilizerWrapper {
    private long jni_ois_pointer_;

    public OnlineImageStabilizerWrapper() {
        jni_init();
    }

    public boolean addProcessedFrame(boolean z) {
        return jni_addProcessedFrame(z);
    }

    public float computeRotationCompensatedFlow(float f, float f2, float f3) {
        return jni_computeRotationCompensatedFlow(f, f2, f3);
    }

    public boolean didMoveBackwards() {
        return jni_didMoveBackwards();
    }

    protected void finalize() throws Throwable {
        try {
            jni_deleteOnlineImageStabilizer();
        } finally {
            super.finalize();
        }
    }

    protected native boolean jni_addProcessedFrame(boolean z);

    protected native float jni_computeRotationCompensatedFlow(float f, float f2, float f3);

    protected native void jni_deleteOnlineImageStabilizer();

    protected native boolean jni_didMoveBackwards();

    protected native void jni_init();

    protected native boolean jni_processBGRABitmap(Bitmap bitmap, int i, int i2, int i3);

    protected native boolean jni_processBGRAFrame(byte[] bArr, int i, int i2, int i3);

    protected native boolean jni_processYV12Frame(byte[] bArr, int i, int i2, int i3, boolean z);

    protected native boolean jni_processYV12FrameWithDepth(byte[] bArr, int i, int i2, int i3, boolean z, byte[] bArr2, int i4, int i5, int i6);

    protected native void jni_releaseData();

    protected native boolean jni_writeToFile(String str);

    public boolean processBGRABitmap(Bitmap bitmap, int i, int i2, int i3) {
        return jni_processBGRABitmap(bitmap, i, i2, i3);
    }

    public boolean processBGRAFrame(byte[] bArr, int i, int i2, int i3) {
        return jni_processBGRAFrame(bArr, i, i2, i3);
    }

    public boolean processYV12Frame(byte[] bArr, int i, int i2, int i3, boolean z) {
        return jni_processYV12Frame(bArr, i, i2, i3, z);
    }

    public boolean processYV12FrameWithDepth(byte[] bArr, int i, int i2, int i3, boolean z, byte[] bArr2, int i4, int i5, int i6) {
        return jni_processYV12FrameWithDepth(bArr, i, i2, i3, z, bArr2, i4, i5, i6);
    }

    public void releaseData() {
        jni_releaseData();
    }

    public boolean writeToFile(String str) {
        return jni_writeToFile(str);
    }
}
