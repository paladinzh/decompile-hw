package fyusion.vislib;

/* compiled from: Unknown */
public class BoolVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public BoolVec() {
        this(FyuseWrapperJNI.new_BoolVec__SWIG_0(), true);
    }

    public BoolVec(long j) {
        this(FyuseWrapperJNI.new_BoolVec__SWIG_1(j), true);
    }

    public BoolVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(BoolVec boolVec) {
        return boolVec != null ? boolVec.swigCPtr : 0;
    }

    public void add(boolean z) {
        FyuseWrapperJNI.BoolVec_add(this.swigCPtr, this, z);
    }

    public long capacity() {
        return FyuseWrapperJNI.BoolVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        FyuseWrapperJNI.BoolVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_BoolVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public boolean get(int i) {
        return FyuseWrapperJNI.BoolVec_get(this.swigCPtr, this, i);
    }

    public boolean isEmpty() {
        return FyuseWrapperJNI.BoolVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        FyuseWrapperJNI.BoolVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, boolean z) {
        FyuseWrapperJNI.BoolVec_set(this.swigCPtr, this, i, z);
    }

    public long size() {
        return FyuseWrapperJNI.BoolVec_size(this.swigCPtr, this);
    }
}
