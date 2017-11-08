package fyusion.vislib;

/* compiled from: Unknown */
public class OfflineImageStabilizer {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public OfflineImageStabilizer() {
        this(FyuseWrapperJNI.new_OfflineImageStabilizer(), true);
    }

    protected OfflineImageStabilizer(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public static void drawMeshOnBitmap(byte[] bArr, int i, int i2, int i3, VisualizationMeshStorage visualizationMeshStorage, int i4, boolean z) {
        FyuseWrapperJNI.OfflineImageStabilizer_drawMeshOnBitmap(bArr, i, i2, i3, VisualizationMeshStorage.getCPtr(visualizationMeshStorage), visualizationMeshStorage, i4, z);
    }

    public static void drawMeshOnBitmapUsingISD(byte[] bArr, int i, int i2, int i3, String str, int i4, boolean z) {
        FyuseWrapperJNI.OfflineImageStabilizer_drawMeshOnBitmapUsingISD(bArr, i, i2, i3, str, i4, z);
    }

    protected static long getCPtr(OfflineImageStabilizer offlineImageStabilizer) {
        return offlineImageStabilizer != null ? offlineImageStabilizer.swigCPtr : 0;
    }

    public SWIGTYPE_p_fyusion__TransformationParameters computeTransformationParametersUsingOpenCV(SWIGTYPE_p_std__vectorT_cv__Point2f_t sWIGTYPE_p_std__vectorT_cv__Point2f_t, SWIGTYPE_p_std__vectorT_cv__Point2f_t sWIGTYPE_p_std__vectorT_cv__Point2f_t2) {
        return new SWIGTYPE_p_fyusion__TransformationParameters(FyuseWrapperJNI.OfflineImageStabilizer_computeTransformationParametersUsingOpenCV(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_cv__Point2f_t.getCPtr(sWIGTYPE_p_std__vectorT_cv__Point2f_t), SWIGTYPE_p_std__vectorT_cv__Point2f_t.getCPtr(sWIGTYPE_p_std__vectorT_cv__Point2f_t2)), true);
    }

    public void computeVisualizationMesh(SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage, VisualizationMeshStorage visualizationMeshStorage) {
        FyuseWrapperJNI.OfflineImageStabilizer_computeVisualizationMesh(this.swigCPtr, this, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage.getCPtr(sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage), VisualizationMeshStorage.getCPtr(visualizationMeshStorage), visualizationMeshStorage);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_OfflineImageStabilizer(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public SWIGTYPE_p_cv__Size estimateProcessedSize(Fyuse fyuse) {
        return new SWIGTYPE_p_cv__Size(FyuseWrapperJNI.OfflineImageStabilizer_estimateProcessedSize(this.swigCPtr, this, Fyuse.getCPtr(fyuse), fyuse), true);
    }

    protected void finalize() {
        delete();
    }

    public void updateDroppedFramesBasedOnOnlineIMUSelection(Fyuse fyuse, BoolVec boolVec) {
        FyuseWrapperJNI.OfflineImageStabilizer_updateDroppedFramesBasedOnOnlineIMUSelection(this.swigCPtr, this, Fyuse.getCPtr(fyuse), fyuse, BoolVec.getCPtr(boolVec), boolVec);
    }

    public void updateTransformations(SWIGTYPE_p_IntermediateStabilizationDataStorage sWIGTYPE_p_IntermediateStabilizationDataStorage, int i, int i2, BoolVec boolVec, SWIGTYPE_p_bool sWIGTYPE_p_bool) {
        FyuseWrapperJNI.OfflineImageStabilizer_updateTransformations(this.swigCPtr, this, SWIGTYPE_p_IntermediateStabilizationDataStorage.getCPtr(sWIGTYPE_p_IntermediateStabilizationDataStorage), i, i2, BoolVec.getCPtr(boolVec), boolVec, SWIGTYPE_p_bool.getCPtr(sWIGTYPE_p_bool));
    }
}
