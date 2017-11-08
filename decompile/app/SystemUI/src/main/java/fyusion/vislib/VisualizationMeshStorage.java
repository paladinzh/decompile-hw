package fyusion.vislib;

/* compiled from: Unknown */
public class VisualizationMeshStorage {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public VisualizationMeshStorage() {
        this(FyuseWrapperJNI.new_VisualizationMeshStorage(), true);
    }

    protected VisualizationMeshStorage(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VisualizationMeshStorage visualizationMeshStorage) {
        return visualizationMeshStorage != null ? visualizationMeshStorage.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_VisualizationMeshStorage(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public VisualizationMeshFrameDataVec getFrames() {
        long VisualizationMeshStorage_frames_get = FyuseWrapperJNI.VisualizationMeshStorage_frames_get(this.swigCPtr, this);
        return VisualizationMeshStorage_frames_get == 0 ? null : new VisualizationMeshFrameDataVec(VisualizationMeshStorage_frames_get, false);
    }

    public void setFrames(VisualizationMeshFrameDataVec visualizationMeshFrameDataVec) {
        FyuseWrapperJNI.VisualizationMeshStorage_frames_set(this.swigCPtr, this, VisualizationMeshFrameDataVec.getCPtr(visualizationMeshFrameDataVec), visualizationMeshFrameDataVec);
    }
}
