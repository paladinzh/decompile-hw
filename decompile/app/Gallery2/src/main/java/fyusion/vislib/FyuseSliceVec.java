package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseSliceVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseSliceVec() {
        this(FyuseWrapperJNI.new_FyuseSliceVec__SWIG_0(), true);
    }

    public FyuseSliceVec(long j) {
        this(FyuseWrapperJNI.new_FyuseSliceVec__SWIG_1(j), true);
    }

    protected FyuseSliceVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseSliceVec fyuseSliceVec) {
        return fyuseSliceVec != null ? fyuseSliceVec.swigCPtr : 0;
    }

    public void add(FyuseSlice fyuseSlice) {
        FyuseWrapperJNI.FyuseSliceVec_add(this.swigCPtr, this, FyuseSlice.getCPtr(fyuseSlice), fyuseSlice);
    }

    public long capacity() {
        return FyuseWrapperJNI.FyuseSliceVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        FyuseWrapperJNI.FyuseSliceVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseSliceVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public FyuseSlice get(int i) {
        return new FyuseSlice(FyuseWrapperJNI.FyuseSliceVec_get(this.swigCPtr, this, i), false);
    }

    public boolean isEmpty() {
        return FyuseWrapperJNI.FyuseSliceVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        FyuseWrapperJNI.FyuseSliceVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, FyuseSlice fyuseSlice) {
        FyuseWrapperJNI.FyuseSliceVec_set(this.swigCPtr, this, i, FyuseSlice.getCPtr(fyuseSlice), fyuseSlice);
    }

    public long size() {
        return FyuseWrapperJNI.FyuseSliceVec_size(this.swigCPtr, this);
    }
}
