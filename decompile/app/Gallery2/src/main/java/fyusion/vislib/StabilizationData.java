package fyusion.vislib;

import org.opencv.core.Size;

/* compiled from: Unknown */
public class StabilizationData {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public StabilizationData() {
        this(StabilizationDataWrapperJNI.new_StabilizationData(), true);
    }

    protected StabilizationData(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(StabilizationData stabilizationData) {
        return stabilizationData != null ? stabilizationData.swigCPtr : 0;
    }

    public void addDroppedFrame() {
        StabilizationDataWrapperJNI.StabilizationData_addDroppedFrame(this.swigCPtr, this);
    }

    public void addFrame(TransformationParameters transformationParameters) {
        StabilizationDataWrapperJNI.StabilizationData_addFrame(this.swigCPtr, this, TransformationParameters.getCPtr(transformationParameters), transformationParameters);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                StabilizationDataWrapperJNI.delete_StabilizationData(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public BoolVec getDrop_frame_() {
        long StabilizationData_drop_frame__get = StabilizationDataWrapperJNI.StabilizationData_drop_frame__get(this.swigCPtr, this);
        return StabilizationData_drop_frame__get == 0 ? null : new BoolVec(StabilizationData_drop_frame__get, false);
    }

    public BoolVec getFrameDropVector() {
        return new BoolVec(StabilizationDataWrapperJNI.StabilizationData_getFrameDropVector(this.swigCPtr, this), false);
    }

    public SWIGTYPE_p_cv__Size getInputImageSize() {
        return new SWIGTYPE_p_cv__Size(StabilizationDataWrapperJNI.StabilizationData_getInputImageSize(this.swigCPtr, this), true);
    }

    public int getNumberOfFrames() {
        return StabilizationDataWrapperJNI.StabilizationData_getNumberOfFrames(this.swigCPtr, this);
    }

    public int getNumberOfNonDroppedFrames() {
        return StabilizationDataWrapperJNI.StabilizationData_getNumberOfNonDroppedFrames(this.swigCPtr, this);
    }

    public SWIGTYPE_p_cv__Size getOutputImageSize() {
        return new SWIGTYPE_p_cv__Size(StabilizationDataWrapperJNI.StabilizationData_getOutputImageSize(this.swigCPtr, this), true);
    }

    public TransformationParametersVec getParameters_() {
        long StabilizationData_parameters__get = StabilizationDataWrapperJNI.StabilizationData_parameters__get(this.swigCPtr, this);
        return StabilizationData_parameters__get == 0 ? null : new TransformationParametersVec(StabilizationData_parameters__get, false);
    }

    public TransformationParametersVec getTransformationParameters() {
        return new TransformationParametersVec(StabilizationDataWrapperJNI.StabilizationData_getTransformationParameters(this.swigCPtr, this), false);
    }

    public SWIGTYPE_p_TransformationParameters getTransformationParametersForFrame(int i) {
        return new SWIGTYPE_p_TransformationParameters(StabilizationDataWrapperJNI.StabilizationData_getTransformationParametersForFrame(this.swigCPtr, this, i), false);
    }

    public boolean isFrameDropped(int i) {
        return StabilizationDataWrapperJNI.StabilizationData_isFrameDropped(this.swigCPtr, this, i);
    }

    public boolean loadFromFile(String str) {
        return StabilizationDataWrapperJNI.StabilizationData_loadFromFile(this.swigCPtr, this, str);
    }

    public boolean saveToFile(String str) {
        return StabilizationDataWrapperJNI.StabilizationData_saveToFile(this.swigCPtr, this, str);
    }

    public void setDrop_frame_(BoolVec boolVec) {
        StabilizationDataWrapperJNI.StabilizationData_drop_frame__set(this.swigCPtr, this, BoolVec.getCPtr(boolVec), boolVec);
    }

    public void setInputImageSize(Size size) {
        StabilizationDataWrapperJNI.StabilizationData_setInputImageSize(this.swigCPtr, this, size);
    }

    public void setOutputImageSize(Size size) {
        StabilizationDataWrapperJNI.StabilizationData_setOutputImageSize(this.swigCPtr, this, size);
    }

    public void setParameters_(TransformationParametersVec transformationParametersVec) {
        StabilizationDataWrapperJNI.StabilizationData_parameters__set(this.swigCPtr, this, TransformationParametersVec.getCPtr(transformationParametersVec), transformationParametersVec);
    }

    public void setTransformationParameters(SWIGTYPE_p_std__vectorT_TransformationParameters_t sWIGTYPE_p_std__vectorT_TransformationParameters_t) {
        StabilizationDataWrapperJNI.StabilizationData_setTransformationParameters(this.swigCPtr, this, SWIGTYPE_p_std__vectorT_TransformationParameters_t.getCPtr(sWIGTYPE_p_std__vectorT_TransformationParameters_t));
    }

    public void setTransformationParametersForFrame(int i, SWIGTYPE_p_TransformationParameters sWIGTYPE_p_TransformationParameters) {
        StabilizationDataWrapperJNI.StabilizationData_setTransformationParametersForFrame(this.swigCPtr, this, i, SWIGTYPE_p_TransformationParameters.getCPtr(sWIGTYPE_p_TransformationParameters));
    }
}
