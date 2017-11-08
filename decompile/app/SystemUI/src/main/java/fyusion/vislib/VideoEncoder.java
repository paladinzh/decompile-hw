package fyusion.vislib;

/* compiled from: Unknown */
public class VideoEncoder {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public VideoEncoder() {
        this(FyuseWrapperJNI.new_VideoEncoder(), true);
    }

    protected VideoEncoder(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VideoEncoder videoEncoder) {
        return videoEncoder != null ? videoEncoder.swigCPtr : 0;
    }

    public void createEncoder(String str, int i, int i2, int i3, int i4) {
        FyuseWrapperJNI.VideoEncoder_createEncoder(this.swigCPtr, this, str, i, i2, i3, i4);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_VideoEncoder(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public void destroyEncoder() {
        FyuseWrapperJNI.VideoEncoder_destroyEncoder(this.swigCPtr, this);
    }

    public boolean encodeFrame(byte[] bArr, int i, int i2, int i3, boolean z) {
        return FyuseWrapperJNI.VideoEncoder_encodeFrame(this.swigCPtr, this, bArr, i, i2, i3, z);
    }

    protected void finalize() {
        delete();
    }
}
