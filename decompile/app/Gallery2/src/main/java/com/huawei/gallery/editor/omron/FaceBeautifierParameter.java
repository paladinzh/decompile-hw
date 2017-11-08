package com.huawei.gallery.editor.omron;

import com.android.gallery3d.util.GalleryLog;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class FaceBeautifierParameter {
    private boolean mEyeEnlargeValid = true;
    public int mEyeReshapeParameter = 0;
    public int mFaceColorParameter = 0;
    public int mFaceReshapeParameter = 0;
    public int mFaceSmoothParameter = 0;
    public int mNoseReshapeParameter = 0;
    private int mOmronEyeEnlarge = 0;
    private int mOmronFaceColor = 0;
    private int mOmronFaceReshape = 0;
    private int mOmronFaceSmooth = 0;
    private int mOmronIrisEnlarge = 0;
    private int mOmronNoseReshape = 0;
    private int mOmronTeethWhiten = 0;
    private boolean mReShapeValid = true;
    private boolean mTeethWhiteValid = true;
    public int mTeethWhitenParameter = 0;

    public void set(FaceBeautifierParameter source) {
        if (source != null) {
            this.mFaceSmoothParameter = source.mFaceSmoothParameter;
            this.mFaceColorParameter = source.mFaceColorParameter;
            this.mFaceReshapeParameter = source.mFaceReshapeParameter;
            this.mNoseReshapeParameter = source.mNoseReshapeParameter;
            this.mEyeReshapeParameter = source.mEyeReshapeParameter;
            this.mTeethWhitenParameter = source.mTeethWhitenParameter;
            this.mReShapeValid = source.mReShapeValid;
            this.mTeethWhiteValid = source.mTeethWhiteValid;
            this.mEyeEnlargeValid = source.mEyeEnlargeValid;
        }
    }

    public void updateParameter(int type, int parameter) {
        switch (type) {
            case 0:
                this.mFaceSmoothParameter = parameter;
                return;
            case 1:
                this.mFaceColorParameter = parameter;
                return;
            case 2:
                this.mFaceReshapeParameter = parameter;
                return;
            case 3:
                this.mNoseReshapeParameter = parameter;
                return;
            case 5:
                this.mEyeReshapeParameter = parameter;
                return;
            case 6:
                this.mTeethWhitenParameter = parameter;
                return;
            default:
                return;
        }
    }

    public boolean hasModified() {
        if (this.mFaceSmoothParameter == 0 && this.mFaceColorParameter == 0 && this.mFaceReshapeParameter == 0 && this.mNoseReshapeParameter == 0 && this.mEyeReshapeParameter == 0 && this.mTeethWhitenParameter == 0) {
            return false;
        }
        return true;
    }

    public void convertAllToOmronParameter() {
        this.mOmronFaceSmooth = getOmronParameter(0);
        this.mOmronFaceColor = getOmronParameter(1);
        this.mOmronFaceReshape = getOmronParameter(2);
        this.mOmronNoseReshape = getOmronParameter(3);
        this.mOmronIrisEnlarge = getOmronParameter(4);
        this.mOmronEyeEnlarge = getOmronParameter(5);
        this.mOmronTeethWhiten = getOmronParameter(6);
        GalleryLog.v("FaceBeautifierParamter", "fs:" + this.mOmronFaceSmooth + " fc:" + this.mOmronFaceColor + " fr:" + this.mOmronFaceReshape + " nr:" + this.mOmronNoseReshape + " ie:" + this.mOmronIrisEnlarge + " ee:" + this.mOmronEyeEnlarge + " tw:" + this.mOmronTeethWhiten + " eev:" + this.mEyeEnlargeValid);
    }

    private int getOmronParameter(int type) {
        int maxParameter;
        int maxUIParameter;
        int minUIParameter;
        int parameter;
        switch (type) {
            case 0:
                maxParameter = 130;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mFaceSmoothParameter;
                break;
            case 1:
                maxParameter = 110;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mFaceColorParameter;
                break;
            case 2:
                maxParameter = SmsCheckResult.ESCT_200;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mFaceReshapeParameter;
                break;
            case 3:
                maxParameter = SmsCheckResult.ESCT_200;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mNoseReshapeParameter;
                break;
            case 4:
                maxParameter = 0;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mEyeReshapeParameter;
                break;
            case 5:
                maxParameter = SmsCheckResult.ESCT_200;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mEyeReshapeParameter;
                break;
            case 6:
                maxParameter = SmsCheckResult.ESCT_200;
                maxUIParameter = 100;
                minUIParameter = 0;
                parameter = this.mTeethWhitenParameter;
                break;
            default:
                return 0;
        }
        GalleryLog.v("FaceBeautifierParamter", "Omron Parameters");
        return ((int) ((((float) (Math.max(Math.min(maxUIParameter, parameter), minUIParameter) + 0)) * ((float) (maxParameter + 0))) / ((float) 100))) + 0;
    }

    public void clearParameter() {
        GalleryLog.v("FaceBeautifierParamter", "Omron clearParameter");
        this.mFaceSmoothParameter = 0;
        this.mFaceColorParameter = 0;
        this.mFaceReshapeParameter = 0;
        this.mNoseReshapeParameter = 0;
        this.mEyeReshapeParameter = 0;
        this.mTeethWhitenParameter = 0;
    }
}
