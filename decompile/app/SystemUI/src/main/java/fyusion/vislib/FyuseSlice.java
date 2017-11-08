package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseSlice {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public FyuseSlice() {
        this(FyuseWrapperJNI.new_FyuseSlice(), true);
    }

    protected FyuseSlice(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(FyuseSlice fyuseSlice) {
        return fyuseSlice != null ? fyuseSlice.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_FyuseSlice(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public int getEnd_frame() {
        return FyuseWrapperJNI.FyuseSlice_end_frame_get(this.swigCPtr, this);
    }

    public String getH264_file_name() {
        return FyuseWrapperJNI.FyuseSlice_h264_file_name_get(this.swigCPtr, this);
    }

    public int getIndex() {
        return FyuseWrapperJNI.FyuseSlice_index_get(this.swigCPtr, this);
    }

    public String getIndex_file_name() {
        return FyuseWrapperJNI.FyuseSlice_index_file_name_get(this.swigCPtr, this);
    }

    public boolean getLow_resolution_preview() {
        return FyuseWrapperJNI.FyuseSlice_low_resolution_preview_get(this.swigCPtr, this);
    }

    public String getMjpeg_file_name() {
        return FyuseWrapperJNI.FyuseSlice_mjpeg_file_name_get(this.swigCPtr, this);
    }

    public int getProcessed_height() {
        return FyuseWrapperJNI.FyuseSlice_processed_height_get(this.swigCPtr, this);
    }

    public int getProcessed_width() {
        return FyuseWrapperJNI.FyuseSlice_processed_width_get(this.swigCPtr, this);
    }

    public int getStart_frame() {
        return FyuseWrapperJNI.FyuseSlice_start_frame_get(this.swigCPtr, this);
    }

    public void setEnd_frame(int i) {
        FyuseWrapperJNI.FyuseSlice_end_frame_set(this.swigCPtr, this, i);
    }

    public void setH264_file_name(String str) {
        FyuseWrapperJNI.FyuseSlice_h264_file_name_set(this.swigCPtr, this, str);
    }

    public void setIndex(int i) {
        FyuseWrapperJNI.FyuseSlice_index_set(this.swigCPtr, this, i);
    }

    public void setIndex_file_name(String str) {
        FyuseWrapperJNI.FyuseSlice_index_file_name_set(this.swigCPtr, this, str);
    }

    public void setLow_resolution_preview(boolean z) {
        FyuseWrapperJNI.FyuseSlice_low_resolution_preview_set(this.swigCPtr, this, z);
    }

    public void setMjpeg_file_name(String str) {
        FyuseWrapperJNI.FyuseSlice_mjpeg_file_name_set(this.swigCPtr, this, str);
    }

    public void setProcessed_height(int i) {
        FyuseWrapperJNI.FyuseSlice_processed_height_set(this.swigCPtr, this, i);
    }

    public void setProcessed_width(int i) {
        FyuseWrapperJNI.FyuseSlice_processed_width_set(this.swigCPtr, this, i);
    }

    public void setStart_frame(int i) {
        FyuseWrapperJNI.FyuseSlice_start_frame_set(this.swigCPtr, this, i);
    }
}
