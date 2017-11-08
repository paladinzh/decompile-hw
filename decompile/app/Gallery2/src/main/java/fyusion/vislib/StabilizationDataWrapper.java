package fyusion.vislib;

/* compiled from: Unknown */
public class StabilizationDataWrapper {
    public static TransformationParameters computeTransformationParametersFromTransformationMatrix(SWIGTYPE_p_cv__Mat sWIGTYPE_p_cv__Mat) {
        return new TransformationParameters(StabilizationDataWrapperJNI.computeTransformationParametersFromTransformationMatrix(SWIGTYPE_p_cv__Mat.getCPtr(sWIGTYPE_p_cv__Mat)), true);
    }
}
