package fyusion.vislib;

/* compiled from: Unknown */
public class FrameBlender {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FrameBlender() {
        this(FyuseWrapperJNI.new_FrameBlender(), true);
    }

    protected FrameBlender(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FrameBlender frameBlender) {
        return frameBlender != null ? frameBlender.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FrameBlender(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public float getStabilizationScale() {
        return FyuseWrapperJNI.FrameBlender_getStabilizationScale(this.swigCPtr, this);
    }

    public boolean queryBlendingInfoForFrameId(float f, int[] iArr, float[] fArr, TransformationParameters transformationParameters, int[] iArr2, float[] fArr2, TransformationParameters transformationParameters2) {
        return FyuseWrapperJNI.FrameBlender_queryBlendingInfoForFrameId(this.swigCPtr, this, f, iArr, fArr, TransformationParameters.getCPtr(transformationParameters), transformationParameters, iArr2, fArr2, TransformationParameters.getCPtr(transformationParameters2), transformationParameters2);
    }

    public void setIndexingOffset(int i) {
        FyuseWrapperJNI.FrameBlender_setIndexingOffset(this.swigCPtr, this, i);
    }

    public void setLoopClosed(boolean z, int i, int i2) {
        FyuseWrapperJNI.FrameBlender_setLoopClosed(this.swigCPtr, this, z, i, i2);
    }

    public void setSizes(int i, int i2, int i3, int i4, float f) {
        FyuseWrapperJNI.FrameBlender_setSizes(this.swigCPtr, this, i, i2, i3, i4, f);
    }

    public void setStabilizedMJPEG(boolean z) {
        FyuseWrapperJNI.FrameBlender_setStabilizedMJPEG(this.swigCPtr, this, z);
    }

    public boolean setTweeningFileAndSizes(String str, int i, int i2, int i3, int i4, float f) {
        return FyuseWrapperJNI.FrameBlender_setTweeningFileAndSizes(this.swigCPtr, this, str, i, i2, i3, i4, f);
    }
}
