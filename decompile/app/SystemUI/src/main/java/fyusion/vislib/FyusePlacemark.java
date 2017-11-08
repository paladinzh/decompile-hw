package fyusion.vislib;

/* compiled from: Unknown */
public class FyusePlacemark {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyusePlacemark() {
        this(FyuseWrapperJNI.new_FyusePlacemark(), true);
    }

    protected FyusePlacemark(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyusePlacemark fyusePlacemark) {
        return fyusePlacemark != null ? fyusePlacemark.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyusePlacemark(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public FyuseAddress getAddress() {
        long FyusePlacemark_address_get = FyuseWrapperJNI.FyusePlacemark_address_get(this.swigCPtr, this);
        return FyusePlacemark_address_get == 0 ? null : new FyuseAddress(FyusePlacemark_address_get, false);
    }

    public float getLatitude() {
        return FyuseWrapperJNI.FyusePlacemark_latitude_get(this.swigCPtr, this);
    }

    public float getLongitude() {
        return FyuseWrapperJNI.FyusePlacemark_longitude_get(this.swigCPtr, this);
    }

    public String getPreferred_location() {
        return FyuseWrapperJNI.FyusePlacemark_preferred_location_get(this.swigCPtr, this);
    }

    public void setAddress(FyuseAddress fyuseAddress) {
        FyuseWrapperJNI.FyusePlacemark_address_set(this.swigCPtr, this, FyuseAddress.getCPtr(fyuseAddress), fyuseAddress);
    }

    public void setLatitude(float f) {
        FyuseWrapperJNI.FyusePlacemark_latitude_set(this.swigCPtr, this, f);
    }

    public void setLongitude(float f) {
        FyuseWrapperJNI.FyusePlacemark_longitude_set(this.swigCPtr, this, f);
    }

    public void setPreferred_location(String str) {
        FyuseWrapperJNI.FyusePlacemark_preferred_location_set(this.swigCPtr, this, str);
    }
}
