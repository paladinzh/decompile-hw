package com.huawei.gallery.editor.sfb;

import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.watermark.manager.parse.WMElement;

public class FaceBeautifierParameter {
    public static final int SFB_MAXPARA = (EditorLoadLib.isArcSoftLoaded() ? 100 : 255);
    public static final float UIPARA_TO_SFBPARA;
    public int mBlemishParameter = 0;
    public int mBronzerParameter = 0;
    public int mCatchLightParameter = 0;
    public int mDeflashParameter = 0;
    public int mEyeCirclesParameter = 0;
    public int mEyeLargeParameter = 0;
    public int mFaceColorParameter = 0;
    public int mFaceReshapeParameter = 0;
    public int mFaceSmoothParameter = 0;
    public int mSculptedParameter = 0;
    public long mSfbBeauty = 0;
    private int mSfbBlemish = 0;
    private int mSfbBronzer = 0;
    private int mSfbCatchLight = 0;
    private int mSfbDeflash = 0;
    private int mSfbEyeCircles = 0;
    private int mSfbEyeEnlarge = 0;
    private int mSfbFaceColor = 0;
    private int mSfbFaceReshape = 0;
    private int mSfbFaceSmooth = 0;
    private int mSfbSculpted = 0;
    private int mSfbTeethWhiten = 0;
    public int mSfbType = 0;
    public int mTeethWhitenParameter = 0;

    static {
        float f;
        if (EditorLoadLib.isArcSoftLoaded()) {
            f = WMElement.CAMERASIZEVALUE1B1;
        } else {
            f = 2.55f;
        }
        UIPARA_TO_SFBPARA = f;
    }

    public void set(FaceBeautifierParameter source) {
        if (source != null) {
            this.mSfbBronzer = source.mSfbBronzer;
            this.mSfbSculpted = source.mSfbSculpted;
            this.mFaceSmoothParameter = source.mFaceSmoothParameter;
            this.mFaceColorParameter = source.mFaceColorParameter;
            this.mFaceReshapeParameter = source.mFaceReshapeParameter;
            this.mEyeLargeParameter = source.mEyeLargeParameter;
            this.mTeethWhitenParameter = source.mTeethWhitenParameter;
            this.mDeflashParameter = source.mDeflashParameter;
            this.mCatchLightParameter = source.mCatchLightParameter;
            this.mEyeCirclesParameter = source.mEyeCirclesParameter;
            this.mBlemishParameter = source.mBlemishParameter;
            this.mSfbFaceSmooth = source.mSfbFaceSmooth;
            this.mSfbFaceColor = source.mSfbFaceColor;
            this.mSfbFaceReshape = source.mSfbFaceReshape;
            this.mSfbEyeEnlarge = source.mSfbEyeEnlarge;
            this.mSfbTeethWhiten = source.mSfbTeethWhiten;
            this.mSfbDeflash = source.mSfbDeflash;
            this.mSfbCatchLight = source.mSfbCatchLight;
            this.mSfbEyeCircles = source.mSfbEyeCircles;
            this.mSfbBlemish = source.mSfbBlemish;
            this.mSfbBeauty = source.mSfbBeauty;
            this.mSfbType = source.mSfbType;
        }
    }

    public boolean hasModified() {
        boolean z = true;
        if (this.mSfbType == 2) {
            return false;
        }
        if (this.mFaceSmoothParameter == 0 && this.mFaceColorParameter == 0 && this.mBlemishParameter == 0 && this.mFaceReshapeParameter == 0 && this.mEyeLargeParameter == 0 && this.mTeethWhitenParameter == 0 && this.mDeflashParameter == 0 && this.mCatchLightParameter == 0 && this.mEyeCirclesParameter == 0 && this.mBronzerParameter == 0 && this.mSculptedParameter == 0) {
            z = false;
        }
        return z;
    }

    public void convertAllToSfbParameter() {
        if (FaceEdit.getSupportVersion()) {
            this.mSfbBronzer = getSfbParameter(12);
            this.mSfbSculpted = getSfbParameter(13);
        }
        this.mSfbFaceSmooth = getSfbParameter(2);
        this.mSfbFaceColor = getSfbParameter(3);
        this.mSfbFaceReshape = getSfbParameter(5);
        this.mSfbEyeEnlarge = getSfbParameter(9);
        this.mSfbTeethWhiten = getSfbParameter(11);
        this.mSfbDeflash = getSfbParameter(7);
        this.mSfbCatchLight = getSfbParameter(10);
        this.mSfbEyeCircles = getSfbParameter(8);
        this.mSfbBlemish = getSfbParameter(4);
    }

    private int getSfbParameter(int type) {
        int parameter;
        int maxParameter = SFB_MAXPARA;
        switch (type) {
            case 2:
                parameter = this.mFaceSmoothParameter;
                break;
            case 3:
                parameter = this.mFaceColorParameter;
                break;
            case 4:
                parameter = this.mBlemishParameter;
                break;
            case 5:
                parameter = this.mFaceReshapeParameter;
                break;
            case 7:
                parameter = this.mDeflashParameter;
                break;
            case 8:
                parameter = this.mEyeCirclesParameter;
                break;
            case 9:
                parameter = this.mEyeLargeParameter;
                break;
            case 10:
                parameter = this.mCatchLightParameter;
                break;
            case 11:
                parameter = this.mTeethWhitenParameter;
                break;
            case 12:
                parameter = this.mBronzerParameter;
                break;
            case 13:
                parameter = this.mSculptedParameter;
                break;
            default:
                return 0;
        }
        return ((int) ((((float) (Math.max(Math.min(100, parameter), 0) + 0)) * ((float) (maxParameter + 0))) / ((float) 100))) + 0;
    }

    public void clearParameter() {
        this.mBronzerParameter = 0;
        this.mSculptedParameter = 0;
        this.mFaceSmoothParameter = 0;
        this.mFaceColorParameter = 0;
        this.mFaceReshapeParameter = 0;
        this.mEyeLargeParameter = 0;
        this.mTeethWhitenParameter = 0;
        this.mDeflashParameter = 0;
        this.mCatchLightParameter = 0;
        this.mEyeCirclesParameter = 0;
        this.mBlemishParameter = 0;
    }

    public void updateParameter(int type, int parameter) {
        switch (type) {
            case 2:
                this.mFaceSmoothParameter = parameter;
                return;
            case 3:
                this.mFaceColorParameter = parameter;
                return;
            case 4:
                this.mBlemishParameter = parameter;
                return;
            case 5:
                this.mFaceReshapeParameter = parameter;
                return;
            case 7:
                this.mDeflashParameter = parameter;
                return;
            case 8:
                this.mEyeCirclesParameter = parameter;
                return;
            case 9:
                this.mEyeLargeParameter = parameter;
                return;
            case 10:
                this.mCatchLightParameter = parameter;
                return;
            case 11:
                this.mTeethWhitenParameter = parameter;
                return;
            case 12:
                this.mBronzerParameter = parameter;
                return;
            case 13:
                this.mSculptedParameter = parameter;
                return;
            default:
                return;
        }
    }

    public void updateParameter(int type, int[] parameter) {
        switch (type) {
            case 14:
                this.mFaceColorParameter = parameter[0];
                this.mBronzerParameter = parameter[1];
                return;
            default:
                return;
        }
    }

    public int getSfbPara(int faceType) {
        int parameter;
        switch (faceType) {
            case 2:
                parameter = this.mSfbFaceSmooth;
                break;
            case 3:
                parameter = this.mSfbFaceColor;
                break;
            case 4:
                parameter = this.mSfbBlemish;
                break;
            case 5:
                parameter = this.mSfbFaceReshape;
                break;
            case 7:
                parameter = this.mSfbDeflash;
                break;
            case 8:
                parameter = this.mSfbEyeCircles;
                break;
            case 9:
                parameter = this.mSfbEyeEnlarge;
                break;
            case 10:
                parameter = this.mSfbCatchLight;
                break;
            case 11:
                parameter = this.mSfbTeethWhiten;
                break;
            case 12:
                parameter = this.mSfbBronzer;
                break;
            case 13:
                parameter = this.mSfbSculpted;
                break;
            default:
                return 0;
        }
        return parameter;
    }
}
