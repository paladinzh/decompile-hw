package fyusion.vislib;

/* compiled from: Unknown */
public class Date {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public Date() {
        this(FyuseWrapperJNI.new_Date(), true);
    }

    protected Date(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(Date date) {
        return date != null ? date.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_Date(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public double getSeconds_since_1970() {
        return FyuseWrapperJNI.Date_seconds_since_1970_get(this.swigCPtr, this);
    }

    public void setSeconds_since_1970(double d) {
        FyuseWrapperJNI.Date_seconds_since_1970_set(this.swigCPtr, this, d);
    }
}
