package com.huawei.gallery.editor.filters.fx;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class FilterChangableParameter {
    public int mBrightnessParameter = 0;
    public int mContrastParameter = 0;
    public int mEVParameter = 0;
    public int mGrainParameter = 0;
    public int mHighlightParameter = 0;
    public int mLowlightParameter = 0;
    public int mSharpenessParameter = 0;
    public int mStrengthParameter = 0;
    public int mToneParameter = 0;

    public FilterChangableParameter() {
        reset();
    }

    public void setOneParameter(int key, int value) {
        switch (key) {
            case 0:
                this.mStrengthParameter = value;
                return;
            case 1:
                this.mContrastParameter = value;
                return;
            case 2:
                this.mGrainParameter = value;
                return;
            case 3:
                this.mSharpenessParameter = value;
                return;
            case 4:
                this.mToneParameter = value;
                return;
            case 5:
                this.mBrightnessParameter = value;
                return;
            case 6:
                this.mEVParameter = value;
                return;
            case 7:
                this.mHighlightParameter = value;
                return;
            case 8:
                this.mLowlightParameter = value;
                return;
            default:
                return;
        }
    }

    public int getOneParameter(int key) {
        switch (key) {
            case 0:
                return this.mStrengthParameter;
            case 1:
                return this.mContrastParameter;
            case 2:
                return this.mGrainParameter;
            case 3:
                return this.mSharpenessParameter;
            case 4:
                return this.mToneParameter;
            case 5:
                return this.mBrightnessParameter;
            case 6:
                return this.mEVParameter;
            case 7:
                return this.mHighlightParameter;
            case 8:
                return this.mLowlightParameter;
            default:
                return 50;
        }
    }

    public void setParameter(FilterChangableParameter source) {
        if (source != null) {
            this.mStrengthParameter = source.mStrengthParameter;
            this.mContrastParameter = source.mContrastParameter;
            this.mGrainParameter = source.mGrainParameter;
            this.mSharpenessParameter = source.mSharpenessParameter;
            this.mToneParameter = source.mToneParameter;
            this.mBrightnessParameter = source.mBrightnessParameter;
            this.mEVParameter = source.mEVParameter;
            this.mHighlightParameter = source.mHighlightParameter;
            this.mLowlightParameter = source.mLowlightParameter;
        }
    }

    public void reset() {
        this.mStrengthParameter = 50;
        this.mContrastParameter = 50;
        this.mGrainParameter = 0;
        this.mSharpenessParameter = 0;
        this.mToneParameter = 50;
        this.mBrightnessParameter = 50;
        this.mEVParameter = 50;
        this.mHighlightParameter = 50;
        this.mLowlightParameter = 50;
    }

    @SuppressWarnings({"HE_EQUALS_USE_HASHCODE", "EQ_SELF_USE_OBJECT"})
    public boolean equals(FilterChangableParameter source) {
        if (source != null && this.mStrengthParameter == source.mStrengthParameter && this.mContrastParameter == source.mContrastParameter && this.mGrainParameter == source.mGrainParameter && this.mSharpenessParameter == source.mSharpenessParameter && this.mToneParameter == source.mToneParameter && this.mBrightnessParameter == source.mBrightnessParameter && this.mEVParameter == source.mEVParameter && this.mHighlightParameter == source.mHighlightParameter && this.mLowlightParameter == source.mLowlightParameter) {
            return true;
        }
        return false;
    }
}
