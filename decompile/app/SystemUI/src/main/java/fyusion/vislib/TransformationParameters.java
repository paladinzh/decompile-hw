package fyusion.vislib;

/* compiled from: Unknown */
public class TransformationParameters {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public TransformationParameters() {
        this(TransformationsWrapperJNI.new_TransformationParameters__SWIG_0(), true);
    }

    public TransformationParameters(double d, double d2, double d3, double d4) {
        this(TransformationsWrapperJNI.new_TransformationParameters__SWIG_1(d, d2, d3, d4), true);
    }

    public TransformationParameters(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public TransformationParameters(SWIGTYPE_p_gtsam__Similarity2 sWIGTYPE_p_gtsam__Similarity2) {
        this(TransformationsWrapperJNI.new_TransformationParameters__SWIG_2(SWIGTYPE_p_gtsam__Similarity2.getCPtr(sWIGTYPE_p_gtsam__Similarity2)), true);
    }

    protected static long getCPtr(TransformationParameters transformationParameters) {
        return transformationParameters != null ? transformationParameters.swigCPtr : 0;
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                TransformationsWrapperJNI.delete_TransformationParameters(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public TransformationParameters interpolateLinearly(double d, TransformationParameters transformationParameters) {
        return new TransformationParameters(TransformationsWrapperJNI.TransformationParameters_interpolateLinearly(this.swigCPtr, this, d, getCPtr(transformationParameters), transformationParameters), true);
    }

    public SWIGTYPE_p_cv__Mat matrixOpenCV() {
        return new SWIGTYPE_p_cv__Mat(TransformationsWrapperJNI.TransformationParameters_matrixOpenCV(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_cv__Mat matrixOpenCV2x3() {
        return new SWIGTYPE_p_cv__Mat(TransformationsWrapperJNI.TransformationParameters_matrixOpenCV2x3(this.swigCPtr, this), true);
    }

    public TransformationParameters scaleTransformation(double d) {
        return new TransformationParameters(TransformationsWrapperJNI.TransformationParameters_scaleTransformation(this.swigCPtr, this, d), true);
    }

    public TransformationParameters scaleTranslation(double d, double d2) {
        return new TransformationParameters(TransformationsWrapperJNI.TransformationParameters_scaleTranslation(this.swigCPtr, this, d, d2), true);
    }
}
