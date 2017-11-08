package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseAddress {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseAddress() {
        this(FyuseWrapperJNI.new_FyuseAddress(), true);
    }

    protected FyuseAddress(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseAddress fyuseAddress) {
        return fyuseAddress != null ? fyuseAddress.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseAddress(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public String getAdministrative_area() {
        return FyuseWrapperJNI.FyuseAddress_administrative_area_get(this.swigCPtr, this);
    }

    public String getCountry() {
        return FyuseWrapperJNI.FyuseAddress_country_get(this.swigCPtr, this);
    }

    public String getIso_country_code() {
        return FyuseWrapperJNI.FyuseAddress_iso_country_code_get(this.swigCPtr, this);
    }

    public String getLocality() {
        return FyuseWrapperJNI.FyuseAddress_locality_get(this.swigCPtr, this);
    }

    public String getPostal_code() {
        return FyuseWrapperJNI.FyuseAddress_postal_code_get(this.swigCPtr, this);
    }

    public String getSub_administrative_area() {
        return FyuseWrapperJNI.FyuseAddress_sub_administrative_area_get(this.swigCPtr, this);
    }

    public String getSub_locality() {
        return FyuseWrapperJNI.FyuseAddress_sub_locality_get(this.swigCPtr, this);
    }

    public String getSub_thoroughfare() {
        return FyuseWrapperJNI.FyuseAddress_sub_thoroughfare_get(this.swigCPtr, this);
    }

    public String getThoroughfare() {
        return FyuseWrapperJNI.FyuseAddress_thoroughfare_get(this.swigCPtr, this);
    }

    public void setAdministrative_area(String str) {
        FyuseWrapperJNI.FyuseAddress_administrative_area_set(this.swigCPtr, this, str);
    }

    public void setCountry(String str) {
        FyuseWrapperJNI.FyuseAddress_country_set(this.swigCPtr, this, str);
    }

    public void setIso_country_code(String str) {
        FyuseWrapperJNI.FyuseAddress_iso_country_code_set(this.swigCPtr, this, str);
    }

    public void setLocality(String str) {
        FyuseWrapperJNI.FyuseAddress_locality_set(this.swigCPtr, this, str);
    }

    public void setPostal_code(String str) {
        FyuseWrapperJNI.FyuseAddress_postal_code_set(this.swigCPtr, this, str);
    }

    public void setSub_administrative_area(String str) {
        FyuseWrapperJNI.FyuseAddress_sub_administrative_area_set(this.swigCPtr, this, str);
    }

    public void setSub_locality(String str) {
        FyuseWrapperJNI.FyuseAddress_sub_locality_set(this.swigCPtr, this, str);
    }

    public void setSub_thoroughfare(String str) {
        FyuseWrapperJNI.FyuseAddress_sub_thoroughfare_set(this.swigCPtr, this, str);
    }

    public void setThoroughfare(String str) {
        FyuseWrapperJNI.FyuseAddress_thoroughfare_set(this.swigCPtr, this, str);
    }
}
