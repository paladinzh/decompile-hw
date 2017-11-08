package fyusion.vislib;

/* compiled from: Unknown */
public class TransformationsWrapperJNI {
    public static final native void FloatVec_add(long j, FloatVec floatVec, float f);

    public static final native long FloatVec_capacity(long j, FloatVec floatVec);

    public static final native void FloatVec_clear(long j, FloatVec floatVec);

    public static final native float FloatVec_get(long j, FloatVec floatVec, int i);

    public static final native boolean FloatVec_isEmpty(long j, FloatVec floatVec);

    public static final native void FloatVec_reserve(long j, FloatVec floatVec, long j2);

    public static final native void FloatVec_set(long j, FloatVec floatVec, int i, float f);

    public static final native long FloatVec_size(long j, FloatVec floatVec);

    public static final native float OffsetUV_u_get(long j, OffsetUV offsetUV);

    public static final native void OffsetUV_u_set(long j, OffsetUV offsetUV, float f);

    public static final native float OffsetUV_v_get(long j, OffsetUV offsetUV);

    public static final native void OffsetUV_v_set(long j, OffsetUV offsetUV, float f);

    public static final native long TransformationParameters_interpolateLinearly(long j, TransformationParameters transformationParameters, double d, long j2, TransformationParameters transformationParameters2);

    public static final native long TransformationParameters_matrixOpenCV(long j, TransformationParameters transformationParameters);

    public static final native long TransformationParameters_matrixOpenCV2x3(long j, TransformationParameters transformationParameters);

    public static final native long TransformationParameters_scaleTransformation(long j, TransformationParameters transformationParameters, double d);

    public static final native long TransformationParameters_scaleTranslation(long j, TransformationParameters transformationParameters, double d, double d2);

    public static final native double VislibJavaHelper_getScale(long j, TransformationParameters transformationParameters);

    public static final native double VislibJavaHelper_getTheta(long j, TransformationParameters transformationParameters);

    public static final native long VislibJavaHelper_getTransformForParameters(long j, TransformationParameters transformationParameters);

    public static final native double VislibJavaHelper_getX(long j, TransformationParameters transformationParameters);

    public static final native double VislibJavaHelper_getY(long j, TransformationParameters transformationParameters);

    public static final native void VislibJavaHelper_setUseIMU(boolean z);

    public static final native boolean VislibJavaHelper_startPipeline(String str, long j, boolean z, boolean z2, boolean z3, int i, long j2);

    public static final native boolean VislibJavaHelper_startPipelineLegacy(Object obj, Object obj2, String str, boolean z, boolean z2, boolean z3, int i);

    public static final native boolean VislibJavaHelper_startPipelineWithLC(String str, long j, boolean z, boolean z2, boolean z3, int i, long j2, boolean z4);

    public static final native boolean VislibJavaHelper_startPipelineWithLCLegacy(Object obj, Object obj2, String str, boolean z, boolean z2, boolean z3, int i, boolean z4);

    public static final native boolean VislibJavaHelper_tagJPEGIfPossible(String str, String str2);

    public static final native long computeTransformationParametersFromTransformationMatrix(long j);

    public static final native void delete_FloatVec(long j);

    public static final native void delete_OffsetUV(long j);

    public static final native void delete_TransformationParameters(long j);

    public static final native long new_FloatVec__SWIG_0();

    public static final native long new_FloatVec__SWIG_1(long j);

    public static final native long new_OffsetUV();

    public static final native long new_TransformationParameters__SWIG_0();

    public static final native long new_TransformationParameters__SWIG_1(double d, double d2, double d3, double d4);

    public static final native long new_TransformationParameters__SWIG_2(long j);
}
