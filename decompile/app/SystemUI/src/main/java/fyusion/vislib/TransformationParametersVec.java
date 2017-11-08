package fyusion.vislib;

/* compiled from: Unknown */
public class TransformationParametersVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public TransformationParametersVec() {
        this(FyuseWrapperJNI.new_TransformationParametersVec__SWIG_0(), true);
    }

    public TransformationParametersVec(long j) {
        this(FyuseWrapperJNI.new_TransformationParametersVec__SWIG_1(j), true);
    }

    protected TransformationParametersVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(TransformationParametersVec transformationParametersVec) {
        return transformationParametersVec != null ? transformationParametersVec.swigCPtr : 0;
    }

    public void add(TransformationParameters transformationParameters) {
        FyuseWrapperJNI.TransformationParametersVec_add(this.swigCPtr, this, TransformationParameters.getCPtr(transformationParameters));
    }

    public long capacity() {
        return FyuseWrapperJNI.TransformationParametersVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        FyuseWrapperJNI.TransformationParametersVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_TransformationParametersVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public TransformationParameters get(int i) {
        return new TransformationParameters(FyuseWrapperJNI.TransformationParametersVec_get(this.swigCPtr, this, i), false);
    }

    public boolean isEmpty() {
        return FyuseWrapperJNI.TransformationParametersVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        FyuseWrapperJNI.TransformationParametersVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, TransformationParameters transformationParameters) {
        FyuseWrapperJNI.TransformationParametersVec_set(this.swigCPtr, this, i, TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public long size() {
        return FyuseWrapperJNI.TransformationParametersVec_size(this.swigCPtr, this);
    }
}
