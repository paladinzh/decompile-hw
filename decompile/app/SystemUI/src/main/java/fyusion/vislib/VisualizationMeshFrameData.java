package fyusion.vislib;

/* compiled from: Unknown */
public class VisualizationMeshFrameData {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public VisualizationMeshFrameData() {
        this(FyuseWrapperJNI.new_VisualizationMeshFrameData(), true);
    }

    protected VisualizationMeshFrameData(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VisualizationMeshFrameData visualizationMeshFrameData) {
        return visualizationMeshFrameData != null ? visualizationMeshFrameData.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_VisualizationMeshFrameData(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public IntVec getEdge_pair_indices() {
        long VisualizationMeshFrameData_edge_pair_indices_get = FyuseWrapperJNI.VisualizationMeshFrameData_edge_pair_indices_get(this.swigCPtr, this);
        return VisualizationMeshFrameData_edge_pair_indices_get == 0 ? null : new IntVec(VisualizationMeshFrameData_edge_pair_indices_get, false);
    }

    public FloatVec getMesh_point_coordinates() {
        long VisualizationMeshFrameData_mesh_point_coordinates_get = FyuseWrapperJNI.VisualizationMeshFrameData_mesh_point_coordinates_get(this.swigCPtr, this);
        return VisualizationMeshFrameData_mesh_point_coordinates_get == 0 ? null : new FloatVec(VisualizationMeshFrameData_mesh_point_coordinates_get, false);
    }

    public void setEdge_pair_indices(IntVec intVec) {
        FyuseWrapperJNI.VisualizationMeshFrameData_edge_pair_indices_set(this.swigCPtr, this, IntVec.getCPtr(intVec), intVec);
    }

    public void setMesh_point_coordinates(FloatVec floatVec) {
        FyuseWrapperJNI.VisualizationMeshFrameData_mesh_point_coordinates_set(this.swigCPtr, this, FloatVec.getCPtr(floatVec), floatVec);
    }
}
