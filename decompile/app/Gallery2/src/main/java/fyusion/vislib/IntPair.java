package fyusion.vislib;

/* compiled from: Unknown */
public class IntPair {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public IntPair() {
        this(FyuseWrapperJNI.new_IntPair__SWIG_0(), true);
    }

    public IntPair(int i, int i2) {
        this(FyuseWrapperJNI.new_IntPair__SWIG_1(i, i2), true);
    }

    protected IntPair(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public IntPair(IntPair intPair) {
        this(FyuseWrapperJNI.new_IntPair__SWIG_2(getCPtr(intPair), intPair), true);
    }

    protected static long getCPtr(IntPair intPair) {
        return intPair != null ? intPair.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_IntPair(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public int getFirst() {
        return FyuseWrapperJNI.IntPair_first_get(this.swigCPtr, this);
    }

    public int getSecond() {
        return FyuseWrapperJNI.IntPair_second_get(this.swigCPtr, this);
    }

    public void setFirst(int i) {
        FyuseWrapperJNI.IntPair_first_set(this.swigCPtr, this, i);
    }

    public void setSecond(int i) {
        FyuseWrapperJNI.IntPair_second_set(this.swigCPtr, this, i);
    }
}
