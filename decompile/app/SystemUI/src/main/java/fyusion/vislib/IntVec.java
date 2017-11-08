package fyusion.vislib;

/* compiled from: Unknown */
public class IntVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public IntVec() {
        this(MeshRenderingJNI.new_IntVec__SWIG_0(), true);
    }

    public IntVec(long j) {
        this(MeshRenderingJNI.new_IntVec__SWIG_1(j), true);
    }

    protected IntVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(IntVec intVec) {
        return intVec != null ? intVec.swigCPtr : 0;
    }

    public void add(int i) {
        MeshRenderingJNI.IntVec_add(this.swigCPtr, this, i);
    }

    public long capacity() {
        return MeshRenderingJNI.IntVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        MeshRenderingJNI.IntVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                MeshRenderingJNI.delete_IntVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public int get(int i) {
        return MeshRenderingJNI.IntVec_get(this.swigCPtr, this, i);
    }

    public boolean isEmpty() {
        return MeshRenderingJNI.IntVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        MeshRenderingJNI.IntVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, int i2) {
        MeshRenderingJNI.IntVec_set(this.swigCPtr, this, i, i2);
    }

    public long size() {
        return MeshRenderingJNI.IntVec_size(this.swigCPtr, this);
    }
}
