package fyusion.vislib;

/* compiled from: Unknown */
public class IMUData {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public IMUData() {
        this(ImuDataWrapperJNI.new_IMUData(), true);
    }

    protected IMUData(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public static SWIGTYPE_p_std__vectorT_Eigen__Quaterniond_t computeFrameRotations(IMUData iMUData, SWIGTYPE_p_std__vectorT_double_t sWIGTYPE_p_std__vectorT_double_t) {
        return new SWIGTYPE_p_std__vectorT_Eigen__Quaterniond_t(ImuDataWrapperJNI.IMUData_computeFrameRotations(getCPtr(iMUData), iMUData, SWIGTYPE_p_std__vectorT_double_t.getCPtr(sWIGTYPE_p_std__vectorT_double_t)), true);
    }

    protected static long getCPtr(IMUData iMUData) {
        return iMUData != null ? iMUData.swigCPtr : 0;
    }

    public static SWIGTYPE_p_Eigen__Matrix3d iPhoneToVisionCoordinates(SWIGTYPE_p_Eigen__Matrix3d sWIGTYPE_p_Eigen__Matrix3d, boolean z) {
        return new SWIGTYPE_p_Eigen__Matrix3d(ImuDataWrapperJNI.IMUData_iPhoneToVisionCoordinates(SWIGTYPE_p_Eigen__Matrix3d.getCPtr(sWIGTYPE_p_Eigen__Matrix3d), z), true);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ImuDataWrapperJNI.delete_IMUData(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t getAccelerations_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t(ImuDataWrapperJNI.IMUData_accelerations__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t getGravities_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t(ImuDataWrapperJNI.IMUData_gravities__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t getMagnetic_fields_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t(ImuDataWrapperJNI.IMUData_magnetic_fields__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t getRotation_matrices_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t(ImuDataWrapperJNI.IMUData_rotation_matrices__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t getRotation_rates_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t(ImuDataWrapperJNI.IMUData_rotation_rates__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t getRpys_() {
        return new SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t(ImuDataWrapperJNI.IMUData_rpys__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_std__vectorT_double_t getTimestamps_() {
        return new SWIGTYPE_p_std__vectorT_double_t(ImuDataWrapperJNI.IMUData_timestamps__get(this.swigCPtr, this), true);
    }

    public boolean loadFromFile(String str) {
        return ImuDataWrapperJNI.IMUData_loadFromFile(this.swigCPtr, this, str);
    }

    public void setAccelerations_(SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t) {
        ImuDataWrapperJNI.IMUData_accelerations__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t));
    }

    public void setGravities_(SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t) {
        ImuDataWrapperJNI.IMUData_gravities__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t));
    }

    public void setMagnetic_fields_(SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t) {
        ImuDataWrapperJNI.IMUData_magnetic_fields__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t));
    }

    public void setRotation_matrices_(SWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t sWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t) {
        ImuDataWrapperJNI.IMUData_rotation_matrices__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Matrix3d_t));
    }

    public void setRotation_rates_(SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t) {
        ImuDataWrapperJNI.IMUData_rotation_rates__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t));
    }

    public void setRpys_(SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t) {
        ImuDataWrapperJNI.IMUData_rpys__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_Eigen__Vector3d_t.getCPtr(sWIGTYPE_p_std__vectorT_Eigen__Vector3d_t));
    }

    public void setTimestamps_(SWIGTYPE_p_std__vectorT_double_t sWIGTYPE_p_std__vectorT_double_t) {
        ImuDataWrapperJNI.IMUData_timestamps__set(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_double_t.getCPtr(sWIGTYPE_p_std__vectorT_double_t));
    }
}
