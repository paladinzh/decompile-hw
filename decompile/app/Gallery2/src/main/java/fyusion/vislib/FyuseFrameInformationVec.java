package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseFrameInformationVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseFrameInformationVec() {
        this(FyuseWrapperJNI.new_FyuseFrameInformationVec__SWIG_0(), true);
    }

    public FyuseFrameInformationVec(long j) {
        this(FyuseWrapperJNI.new_FyuseFrameInformationVec__SWIG_1(j), true);
    }

    protected FyuseFrameInformationVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseFrameInformationVec fyuseFrameInformationVec) {
        return fyuseFrameInformationVec != null ? fyuseFrameInformationVec.swigCPtr : 0;
    }

    public void add(FyuseFrameInformation fyuseFrameInformation) {
        FyuseWrapperJNI.FyuseFrameInformationVec_add(this.swigCPtr, this, FyuseFrameInformation.getCPtr(fyuseFrameInformation), fyuseFrameInformation);
    }

    public long capacity() {
        return FyuseWrapperJNI.FyuseFrameInformationVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        FyuseWrapperJNI.FyuseFrameInformationVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseFrameInformationVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public FyuseFrameInformation get(int i) {
        return new FyuseFrameInformation(FyuseWrapperJNI.FyuseFrameInformationVec_get(this.swigCPtr, this, i), false);
    }

    public boolean isEmpty() {
        return FyuseWrapperJNI.FyuseFrameInformationVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        FyuseWrapperJNI.FyuseFrameInformationVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, FyuseFrameInformation fyuseFrameInformation) {
        FyuseWrapperJNI.FyuseFrameInformationVec_set(this.swigCPtr, this, i, FyuseFrameInformation.getCPtr(fyuseFrameInformation), fyuseFrameInformation);
    }

    public long size() {
        return FyuseWrapperJNI.FyuseFrameInformationVec_size(this.swigCPtr, this);
    }
}
