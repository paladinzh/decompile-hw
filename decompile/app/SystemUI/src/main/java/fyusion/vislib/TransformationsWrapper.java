package fyusion.vislib;

/* compiled from: Unknown */
public class TransformationsWrapper {
    public static TransformationParameters computeTransformationParametersFromTransformationMatrix(SWIGTYPE_p_cv__Mat sWIGTYPE_p_cv__Mat) {
        return new TransformationParameters(TransformationsWrapperJNI.computeTransformationParametersFromTransformationMatrix(SWIGTYPE_p_cv__Mat.getCPtr(sWIGTYPE_p_cv__Mat)), true);
    }
}
