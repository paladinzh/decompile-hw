package fyusion.vislib;

/* compiled from: Unknown */
public class VislibJavaHelper {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    /* compiled from: Unknown */
    public enum ZoomMode {
        NONE(0),
        FULL(1),
        FULL_WITH_NONE_FOR_360(2);
        
        private int value;

        private ZoomMode(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }

        public void setValue(int i) {
            this.value = i;
        }
    }

    protected VislibJavaHelper(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VislibJavaHelper vislibJavaHelper) {
        return vislibJavaHelper != null ? vislibJavaHelper.swigCPtr : 0;
    }

    public static double getScale(TransformationParameters transformationParameters) {
        return TransformationsWrapperJNI.VislibJavaHelper_getScale(TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public static double getTheta(TransformationParameters transformationParameters) {
        return TransformationsWrapperJNI.VislibJavaHelper_getTheta(TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public static CVTransform getTransformForParameters(TransformationParameters transformationParameters) {
        return new CVTransform(TransformationsWrapperJNI.VislibJavaHelper_getTransformForParameters(TransformationParameters.getCPtr(transformationParameters), transformationParameters), true);
    }

    public static double getX(TransformationParameters transformationParameters) {
        return TransformationsWrapperJNI.VislibJavaHelper_getX(TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public static double getY(TransformationParameters transformationParameters) {
        return TransformationsWrapperJNI.VislibJavaHelper_getY(TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public static void setUseIMU(boolean z) {
        TransformationsWrapperJNI.VislibJavaHelper_setUseIMU(z);
    }

    public static boolean startPipeline(Object obj, Object obj2, String str, boolean z, boolean z2, boolean z3, ZoomMode zoomMode) {
        return TransformationsWrapperJNI.VislibJavaHelper_startPipelineLegacy(obj, obj2, str, z, z2, z3, zoomMode.getValue());
    }

    public static boolean startPipeline(Object obj, Object obj2, String str, boolean z, boolean z2, boolean z3, ZoomMode zoomMode, boolean z4) {
        return TransformationsWrapperJNI.VislibJavaHelper_startPipelineWithLCLegacy(obj, obj2, str, z, z2, z3, zoomMode.getValue(), z4);
    }

    public static boolean startPipeline(String str, BoolVec boolVec, boolean z, boolean z2, boolean z3, ZoomMode zoomMode, FyuseData fyuseData) {
        return TransformationsWrapperJNI.VislibJavaHelper_startPipeline(str, BoolVec.getCPtr(boolVec), z, z2, z3, zoomMode.getValue(), FyuseData.getSharedPtr(fyuseData));
    }

    public static boolean startPipeline(String str, BoolVec boolVec, boolean z, boolean z2, boolean z3, ZoomMode zoomMode, FyuseData fyuseData, boolean z4) {
        return TransformationsWrapperJNI.VislibJavaHelper_startPipelineWithLC(str, BoolVec.getCPtr(boolVec), z, z2, z3, zoomMode.getValue(), FyuseData.getSharedPtr(fyuseData), z4);
    }

    public static boolean tagJPEGIfPossible(String str, String str2) {
        return TransformationsWrapperJNI.VislibJavaHelper_tagJPEGIfPossible(str, str2);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
            this.swigCPtr = 0;
        }
    }
}
