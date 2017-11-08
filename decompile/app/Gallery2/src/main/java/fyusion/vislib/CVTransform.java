package fyusion.vislib;

/* compiled from: Unknown */
public class CVTransform {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public CVTransform() {
        this(FyuseWrapperJNI.new_CVTransform(), true);
    }

    protected CVTransform(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(CVTransform cVTransform) {
        return cVTransform != null ? cVTransform.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_CVTransform(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public FloatVec getTransform() {
        long CVTransform_transform_get = FyuseWrapperJNI.CVTransform_transform_get(this.swigCPtr, this);
        return CVTransform_transform_get == 0 ? null : new FloatVec(CVTransform_transform_get, false);
    }

    public void setTransform(FloatVec floatVec) {
        FyuseWrapperJNI.CVTransform_transform_set(this.swigCPtr, this, FloatVec.getCPtr(floatVec), floatVec);
    }
}
