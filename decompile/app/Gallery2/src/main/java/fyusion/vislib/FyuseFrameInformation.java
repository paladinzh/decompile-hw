package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseFrameInformation {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseFrameInformation() {
        this(FyuseWrapperJNI.new_FyuseFrameInformation(), true);
    }

    protected FyuseFrameInformation(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseFrameInformation fyuseFrameInformation) {
        return fyuseFrameInformation != null ? fyuseFrameInformation.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseFrameInformation(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public double getBrightness_value() {
        return FyuseWrapperJNI.FyuseFrameInformation_brightness_value_get(this.swigCPtr, this);
    }

    public double getExposure_value() {
        return FyuseWrapperJNI.FyuseFrameInformation_exposure_value_get(this.swigCPtr, this);
    }

    public boolean getHas_imu_direction_settled() {
        return FyuseWrapperJNI.FyuseFrameInformation_has_imu_direction_settled_get(this.swigCPtr, this);
    }

    public double getImu_direction_x() {
        return FyuseWrapperJNI.FyuseFrameInformation_imu_direction_x_get(this.swigCPtr, this);
    }

    public double getImu_direction_y() {
        return FyuseWrapperJNI.FyuseFrameInformation_imu_direction_y_get(this.swigCPtr, this);
    }

    public boolean getIs_blacklisted() {
        return FyuseWrapperJNI.FyuseFrameInformation_is_blacklisted_get(this.swigCPtr, this);
    }

    public boolean getIs_dropped_online() {
        return FyuseWrapperJNI.FyuseFrameInformation_is_dropped_online_get(this.swigCPtr, this);
    }

    public int getIso_value() {
        return FyuseWrapperJNI.FyuseFrameInformation_iso_value_get(this.swigCPtr, this);
    }

    public double getTimestamp_in_seconds() {
        return FyuseWrapperJNI.FyuseFrameInformation_timestamp_in_seconds_get(this.swigCPtr, this);
    }

    public void setBrightness_value(double d) {
        FyuseWrapperJNI.FyuseFrameInformation_brightness_value_set(this.swigCPtr, this, d);
    }

    public void setExposure_value(double d) {
        FyuseWrapperJNI.FyuseFrameInformation_exposure_value_set(this.swigCPtr, this, d);
    }

    public void setHas_imu_direction_settled(boolean z) {
        FyuseWrapperJNI.FyuseFrameInformation_has_imu_direction_settled_set(this.swigCPtr, this, z);
    }

    public void setImu_direction_x(double d) {
        FyuseWrapperJNI.FyuseFrameInformation_imu_direction_x_set(this.swigCPtr, this, d);
    }

    public void setImu_direction_y(double d) {
        FyuseWrapperJNI.FyuseFrameInformation_imu_direction_y_set(this.swigCPtr, this, d);
    }

    public void setIs_blacklisted(boolean z) {
        FyuseWrapperJNI.FyuseFrameInformation_is_blacklisted_set(this.swigCPtr, this, z);
    }

    public void setIs_dropped_online(boolean z) {
        FyuseWrapperJNI.FyuseFrameInformation_is_dropped_online_set(this.swigCPtr, this, z);
    }

    public void setIso_value(int i) {
        FyuseWrapperJNI.FyuseFrameInformation_iso_value_set(this.swigCPtr, this, i);
    }

    public void setTimestamp_in_seconds(double d) {
        FyuseWrapperJNI.FyuseFrameInformation_timestamp_in_seconds_set(this.swigCPtr, this, d);
    }
}
