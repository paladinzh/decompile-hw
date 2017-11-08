package fyusion.vislib;

/* compiled from: Unknown */
public class OffsetUV {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public OffsetUV() {
        this(TransformationsWrapperJNI.new_OffsetUV(), true);
    }

    protected OffsetUV(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(OffsetUV offsetUV) {
        return offsetUV != null ? offsetUV.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                TransformationsWrapperJNI.delete_OffsetUV(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public float getU() {
        return TransformationsWrapperJNI.OffsetUV_u_get(this.swigCPtr, this);
    }

    public float getV() {
        return TransformationsWrapperJNI.OffsetUV_v_get(this.swigCPtr, this);
    }

    public void setU(float f) {
        TransformationsWrapperJNI.OffsetUV_u_set(this.swigCPtr, this, f);
    }

    public void setV(float f) {
        TransformationsWrapperJNI.OffsetUV_v_set(this.swigCPtr, this, f);
    }
}
