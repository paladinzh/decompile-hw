package fyusion.vislib;

/* compiled from: Unknown */
public class ISDJavaHelper {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public ISDJavaHelper() {
        this(MeshRenderingJNI.new_ISDJavaHelper(), true);
    }

    protected ISDJavaHelper(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(ISDJavaHelper iSDJavaHelper) {
        return iSDJavaHelper != null ? iSDJavaHelper.swigCPtr : 0;
    }

    public void clear() {
        MeshRenderingJNI.ISDJavaHelper_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                MeshRenderingJNI.delete_ISDJavaHelper(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage getIsd_data_() {
        return new SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage(MeshRenderingJNI.ISDJavaHelper_isd_data__get(this.swigCPtr, this), true);
    }

    public boolean getIsd_is_loaded_() {
        return MeshRenderingJNI.ISDJavaHelper_isd_is_loaded__get(this.swigCPtr, this);
    }

    public void setIsd_data_(SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage) {
        MeshRenderingJNI.ISDJavaHelper_isd_data__set(this.swigCPtr, this, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage.getCPtr(sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage));
    }

    public void setIsd_is_loaded_(boolean z) {
        MeshRenderingJNI.ISDJavaHelper_isd_is_loaded__set(this.swigCPtr, this, z);
    }
}
