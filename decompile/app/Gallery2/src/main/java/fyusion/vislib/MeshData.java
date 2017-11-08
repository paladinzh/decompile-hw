package fyusion.vislib;

/* compiled from: Unknown */
public class MeshData {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public MeshData() {
        this(MeshRenderingJNI.new_MeshData(), true);
    }

    protected MeshData(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(MeshData meshData) {
        return meshData != null ? meshData.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                MeshRenderingJNI.delete_MeshData(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public IntVec getEdge_pair_indices() {
        long MeshData_edge_pair_indices_get = MeshRenderingJNI.MeshData_edge_pair_indices_get(this.swigCPtr, this);
        return MeshData_edge_pair_indices_get == 0 ? null : new IntVec(MeshData_edge_pair_indices_get, false);
    }

    public FloatVec getMesh_point_coordinates() {
        long MeshData_mesh_point_coordinates_get = MeshRenderingJNI.MeshData_mesh_point_coordinates_get(this.swigCPtr, this);
        return MeshData_mesh_point_coordinates_get == 0 ? null : new FloatVec(MeshData_mesh_point_coordinates_get, false);
    }

    public void setEdge_pair_indices(IntVec intVec) {
        MeshRenderingJNI.MeshData_edge_pair_indices_set(this.swigCPtr, this, IntVec.getCPtr(intVec), intVec);
    }

    public void setMesh_point_coordinates(FloatVec floatVec) {
        MeshRenderingJNI.MeshData_mesh_point_coordinates_set(this.swigCPtr, this, FloatVec.getCPtr(floatVec), floatVec);
    }
}
