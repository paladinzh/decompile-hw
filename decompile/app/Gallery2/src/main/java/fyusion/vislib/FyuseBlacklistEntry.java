package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseBlacklistEntry {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseBlacklistEntry() {
        this(FyuseWrapperJNI.new_FyuseBlacklistEntry(), true);
    }

    protected FyuseBlacklistEntry(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseBlacklistEntry fyuseBlacklistEntry) {
        return fyuseBlacklistEntry != null ? fyuseBlacklistEntry.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseBlacklistEntry(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public int getFrame() {
        return FyuseWrapperJNI.FyuseBlacklistEntry_frame_get(this.swigCPtr, this);
    }

    public String getReason() {
        return FyuseWrapperJNI.FyuseBlacklistEntry_reason_get(this.swigCPtr, this);
    }

    public void setFrame(int i) {
        FyuseWrapperJNI.FyuseBlacklistEntry_frame_set(this.swigCPtr, this, i);
    }

    public void setReason(String str) {
        FyuseWrapperJNI.FyuseBlacklistEntry_reason_set(this.swigCPtr, this, str);
    }
}
