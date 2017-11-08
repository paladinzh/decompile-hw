package fyusion.vislib;

/* compiled from: Unknown */
public class MeshRendering {
    public static MeshData computeMeshDataForFrameId(int i, ISDJavaHelper iSDJavaHelper) {
        return new MeshData(MeshRenderingJNI.computeMeshDataForFrameId__SWIG_1(i, ISDJavaHelper.getCPtr(iSDJavaHelper), iSDJavaHelper), true);
    }

    public static MeshData computeMeshDataForFrameId(int i, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage) {
        return new MeshData(MeshRenderingJNI.computeMeshDataForFrameIdFromStream(i, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage.getCPtr(sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage)), true);
    }

    public static MeshData computeMeshDataForFrameId(int i, String str) {
        return new MeshData(MeshRenderingJNI.computeMeshDataForFrameId__SWIG_0(i, str), true);
    }

    public static ISDJavaHelper load_isd_java(String str) {
        return new ISDJavaHelper(MeshRenderingJNI.load_isd_java(str), true);
    }
}
