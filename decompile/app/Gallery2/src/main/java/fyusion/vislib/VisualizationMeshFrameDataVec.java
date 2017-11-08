package fyusion.vislib;

/* compiled from: Unknown */
public class VisualizationMeshFrameDataVec {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public VisualizationMeshFrameDataVec() {
        this(FyuseWrapperJNI.new_VisualizationMeshFrameDataVec__SWIG_0(), true);
    }

    public VisualizationMeshFrameDataVec(long j) {
        this(FyuseWrapperJNI.new_VisualizationMeshFrameDataVec__SWIG_1(j), true);
    }

    protected VisualizationMeshFrameDataVec(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VisualizationMeshFrameDataVec visualizationMeshFrameDataVec) {
        return visualizationMeshFrameDataVec != null ? visualizationMeshFrameDataVec.swigCPtr : 0;
    }

    public void add(VisualizationMeshFrameData visualizationMeshFrameData) {
        FyuseWrapperJNI.VisualizationMeshFrameDataVec_add(this.swigCPtr, this, VisualizationMeshFrameData.getCPtr(visualizationMeshFrameData), visualizationMeshFrameData);
    }

    public long capacity() {
        return FyuseWrapperJNI.VisualizationMeshFrameDataVec_capacity(this.swigCPtr, this);
    }

    public void clear() {
        FyuseWrapperJNI.VisualizationMeshFrameDataVec_clear(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_VisualizationMeshFrameDataVec(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public VisualizationMeshFrameData get(int i) {
        return new VisualizationMeshFrameData(FyuseWrapperJNI.VisualizationMeshFrameDataVec_get(this.swigCPtr, this, i), false);
    }

    public boolean isEmpty() {
        return FyuseWrapperJNI.VisualizationMeshFrameDataVec_isEmpty(this.swigCPtr, this);
    }

    public void reserve(long j) {
        FyuseWrapperJNI.VisualizationMeshFrameDataVec_reserve(this.swigCPtr, this, j);
    }

    public void set(int i, VisualizationMeshFrameData visualizationMeshFrameData) {
        FyuseWrapperJNI.VisualizationMeshFrameDataVec_set(this.swigCPtr, this, i, VisualizationMeshFrameData.getCPtr(visualizationMeshFrameData), visualizationMeshFrameData);
    }

    public long size() {
        return FyuseWrapperJNI.VisualizationMeshFrameDataVec_size(this.swigCPtr, this);
    }
}
