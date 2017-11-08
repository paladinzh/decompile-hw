package fyusion.vislib;

/* compiled from: Unknown */
public class FloatVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FloatVec() {
        this(MeshRenderingJNI.new_FloatVec__SWIG_0(), true);
    }

    public FloatVec(long j) {
        this(MeshRenderingJNI.new_FloatVec__SWIG_1(j), true);
    }

    protected FloatVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FloatVec floatVec) {
        return floatVec != null ? floatVec.swigCPtr : 0;
    }

    public void add(float f) {
        MeshRenderingJNI.FloatVec_add(this.swigCPtr, this, f);
    }

    public long capacity() {
        return MeshRenderingJNI.FloatVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        MeshRenderingJNI.FloatVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                MeshRenderingJNI.delete_FloatVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public float get(int i) {
        return MeshRenderingJNI.FloatVec_get(this.swigCPtr, this, i);
    }

    public boolean isEmpty() {
        return MeshRenderingJNI.FloatVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        MeshRenderingJNI.FloatVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, float f) {
        MeshRenderingJNI.FloatVec_set(this.swigCPtr, this, i, f);
    }

    public long size() {
        return MeshRenderingJNI.FloatVec_size(this.swigCPtr, this);
    }
}
