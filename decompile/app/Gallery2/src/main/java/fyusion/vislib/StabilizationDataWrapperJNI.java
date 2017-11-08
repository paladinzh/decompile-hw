package fyusion.vislib;

import org.opencv.core.Size;

/* compiled from: Unknown */
public class StabilizationDataWrapperJNI {
    public static final native void BoolVec_add(long j, BoolVec boolVec, boolean z);

    public static final native long BoolVec_capacity(long j, BoolVec boolVec);

    public static final native void BoolVec_clear(long j, BoolVec boolVec);

    public static final native boolean BoolVec_get(long j, BoolVec boolVec, int i);

    public static final native boolean BoolVec_isEmpty(long j, BoolVec boolVec);

    public static final native void BoolVec_reserve(long j, BoolVec boolVec, long j2);

    public static final native void BoolVec_set(long j, BoolVec boolVec, int i, boolean z);

    public static final native long BoolVec_size(long j, BoolVec boolVec);

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

    public static final native void StabilizationData_addDroppedFrame(long j, StabilizationData stabilizationData);

    public static final native void StabilizationData_addFrame(long j, StabilizationData stabilizationData, long j2, TransformationParameters transformationParameters);

    public static final native long StabilizationData_drop_frame__get(long j, StabilizationData stabilizationData);

    public static final native void StabilizationData_drop_frame__set(long j, StabilizationData stabilizationData, long j2, BoolVec boolVec);

    public static final native long StabilizationData_getFrameDropVector(long j, StabilizationData stabilizationData);

    public static final native long StabilizationData_getInputImageSize(long j, StabilizationData stabilizationData);

    public static final native int StabilizationData_getNumberOfFrames(long j, StabilizationData stabilizationData);

    public static final native int StabilizationData_getNumberOfNonDroppedFrames(long j, StabilizationData stabilizationData);

    public static final native long StabilizationData_getOutputImageSize(long j, StabilizationData stabilizationData);

    public static final native long StabilizationData_getTransformationParameters(long j, StabilizationData stabilizationData);

    public static final native long StabilizationData_getTransformationParametersForFrame(long j, StabilizationData stabilizationData, int i);

    public static final native boolean StabilizationData_isFrameDropped(long j, StabilizationData stabilizationData, int i);

    public static final native boolean StabilizationData_loadFromFile(long j, StabilizationData stabilizationData, String str);

    public static final native long StabilizationData_parameters__get(long j, StabilizationData stabilizationData);

    public static final native void StabilizationData_parameters__set(long j, StabilizationData stabilizationData, long j2, TransformationParametersVec transformationParametersVec);

    public static final native boolean StabilizationData_saveToFile(long j, StabilizationData stabilizationData, String str);

    public static final native void StabilizationData_setInputImageSize(long j, StabilizationData stabilizationData, Size size);

    public static final native void StabilizationData_setOutputImageSize(long j, StabilizationData stabilizationData, Size size);

    public static final native void StabilizationData_setTransformationParameters(long j, StabilizationData stabilizationData, long j2);

    public static final native void StabilizationData_setTransformationParametersForFrame(long j, StabilizationData stabilizationData, int i, long j2);

    public static final native void TransformationParametersVec_add(long j, TransformationParametersVec transformationParametersVec, long j2, TransformationParameters transformationParameters);

    public static final native long TransformationParametersVec_capacity(long j, TransformationParametersVec transformationParametersVec);

    public static final native void TransformationParametersVec_clear(long j, TransformationParametersVec transformationParametersVec);

    public static final native long TransformationParametersVec_get(long j, TransformationParametersVec transformationParametersVec, int i);

    public static final native boolean TransformationParametersVec_isEmpty(long j, TransformationParametersVec transformationParametersVec);

    public static final native void TransformationParametersVec_reserve(long j, TransformationParametersVec transformationParametersVec, long j2);

    public static final native void TransformationParametersVec_set(long j, TransformationParametersVec transformationParametersVec, int i, long j2, TransformationParameters transformationParameters);

    public static final native long TransformationParametersVec_size(long j, TransformationParametersVec transformationParametersVec);

    public static final native long TransformationParameters_interpolateLinearly(long j, TransformationParameters transformationParameters, double d, long j2, TransformationParameters transformationParameters2);

    public static final native long TransformationParameters_matrixOpenCV(long j, TransformationParameters transformationParameters);

    public static final native long TransformationParameters_matrixOpenCV2x3(long j, TransformationParameters transformationParameters);

    public static final native long TransformationParameters_scaleTransformation(long j, TransformationParameters transformationParameters, double d);

    public static final native long TransformationParameters_scaleTranslation(long j, TransformationParameters transformationParameters, double d, double d2);

    public static final native long computeTransformationParametersFromTransformationMatrix(long j);

    public static final native void delete_BoolVec(long j);

    public static final native void delete_FloatVec(long j);

    public static final native void delete_OffsetUV(long j);

    public static final native void delete_StabilizationData(long j);

    public static final native void delete_TransformationParameters(long j);

    public static final native void delete_TransformationParametersVec(long j);

    public static final native long new_BoolVec__SWIG_0();

    public static final native long new_BoolVec__SWIG_1(long j);

    public static final native long new_FloatVec__SWIG_0();

    public static final native long new_FloatVec__SWIG_1(long j);

    public static final native long new_OffsetUV();

    public static final native long new_StabilizationData();

    public static final native long new_TransformationParametersVec__SWIG_0();

    public static final native long new_TransformationParametersVec__SWIG_1(long j);

    public static final native long new_TransformationParameters__SWIG_0();

    public static final native long new_TransformationParameters__SWIG_1(double d, double d2, double d3, double d4);

    public static final native long new_TransformationParameters__SWIG_2(long j);
}
