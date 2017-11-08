package fyusion.vislib;

/* compiled from: Unknown */
public class MeshRenderingJNI {
    public static final native void FloatVec_add(long j, FloatVec floatVec, float f);

    public static final native long FloatVec_capacity(long j, FloatVec floatVec);

    public static final native void FloatVec_clear(long j, FloatVec floatVec);

    public static final native float FloatVec_get(long j, FloatVec floatVec, int i);

    public static final native boolean FloatVec_isEmpty(long j, FloatVec floatVec);

    public static final native void FloatVec_reserve(long j, FloatVec floatVec, long j2);

    public static final native void FloatVec_set(long j, FloatVec floatVec, int i, float f);

    public static final native long FloatVec_size(long j, FloatVec floatVec);

    public static final native void ISDJavaHelper_clear(long j, ISDJavaHelper iSDJavaHelper);

    public static final native long ISDJavaHelper_isd_data__get(long j, ISDJavaHelper iSDJavaHelper);

    public static final native void ISDJavaHelper_isd_data__set(long j, ISDJavaHelper iSDJavaHelper, long j2);

    public static final native boolean ISDJavaHelper_isd_is_loaded__get(long j, ISDJavaHelper iSDJavaHelper);

    public static final native void ISDJavaHelper_isd_is_loaded__set(long j, ISDJavaHelper iSDJavaHelper, boolean z);

    public static final native void IntVec_add(long j, IntVec intVec, int i);

    public static final native long IntVec_capacity(long j, IntVec intVec);

    public static final native void IntVec_clear(long j, IntVec intVec);

    public static final native int IntVec_get(long j, IntVec intVec, int i);

    public static final native boolean IntVec_isEmpty(long j, IntVec intVec);

    public static final native void IntVec_reserve(long j, IntVec intVec, long j2);

    public static final native void IntVec_set(long j, IntVec intVec, int i, int i2);

    public static final native long IntVec_size(long j, IntVec intVec);

    public static final native long MeshData_edge_pair_indices_get(long j, MeshData meshData);

    public static final native void MeshData_edge_pair_indices_set(long j, MeshData meshData, long j2, IntVec intVec);

    public static final native long MeshData_mesh_point_coordinates_get(long j, MeshData meshData);

    public static final native void MeshData_mesh_point_coordinates_set(long j, MeshData meshData, long j2, FloatVec floatVec);

    public static final native long computeMeshDataForFrameIdFromStream(int i, long j);

    public static final native long computeMeshDataForFrameId__SWIG_0(int i, String str);

    public static final native long computeMeshDataForFrameId__SWIG_1(int i, long j, ISDJavaHelper iSDJavaHelper);

    public static final native void delete_FloatVec(long j);

    public static final native void delete_ISDJavaHelper(long j);

    public static final native void delete_IntVec(long j);

    public static final native void delete_MeshData(long j);

    public static final native long load_isd_java(String str);

    public static final native long new_FloatVec__SWIG_0();

    public static final native long new_FloatVec__SWIG_1(long j);

    public static final native long new_ISDJavaHelper();

    public static final native long new_IntVec__SWIG_0();

    public static final native long new_IntVec__SWIG_1(long j);

    public static final native long new_MeshData();
}
