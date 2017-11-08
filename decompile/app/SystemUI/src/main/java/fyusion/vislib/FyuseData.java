package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseData {
    private transient long sharedPtr;
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseData() {
        this(FyuseDataWrapperJNI.new_FyuseData(), true);
        this.sharedPtr = FyuseDataWrapperJNI.FyuseData_getSharedPtr(this.swigCPtr);
    }

    protected FyuseData(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseData fyuseData) {
        return fyuseData != null ? fyuseData.swigCPtr : 0;
    }

    protected static long getSharedPtr(FyuseData fyuseData) {
        return fyuseData != null ? fyuseData.sharedPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseDataWrapperJNI.delete_FyuseData(this.swigCPtr, this.sharedPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public Fyuse getFyuse() {
        return new Fyuse(FyuseDataWrapperJNI.FyuseData_getFyuse(this.swigCPtr, this), false);
    }

    public SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage getIsdData() {
        return new SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage(FyuseDataWrapperJNI.FyuseData_getIsdData(this.swigCPtr, this), false);
    }

    public StabilizationData getStabilizationData() {
        return new StabilizationData(FyuseDataWrapperJNI.FyuseData_getStabilizationData(this.swigCPtr, this), false);
    }

    public boolean loadFromFile(String str) {
        return FyuseDataWrapperJNI.FyuseData_loadFromFile(this.swigCPtr, this, str);
    }

    public boolean populateFyuseData(String str) {
        return FyuseDataWrapperJNI.FyuseData_populateFyuseData(this.swigCPtr, this, str);
    }

    public void setFyuseClass(Fyuse fyuse) {
        FyuseDataWrapperJNI.FyuseData_setFyuse(this.swigCPtr, this, Fyuse.getCPtr(fyuse), fyuse);
    }

    public boolean writeToFile(String str, String str2, FyuseContainerType fyuseContainerType) {
        return FyuseDataWrapperJNI.FyuseData_writeToFile(this.swigCPtr, this, str, str2, fyuseContainerType.swigValue());
    }
}
