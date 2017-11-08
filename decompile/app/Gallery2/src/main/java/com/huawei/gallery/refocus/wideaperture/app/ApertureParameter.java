package com.huawei.gallery.refocus.wideaperture.app;

import com.android.gallery3d.util.GalleryLog;

public class ApertureParameter {
    static final double[] DOUBLE_SUPPORTED_VALUES = new double[]{0.95d, 1.2d, 1.4d, 2.0d, 2.4d, 2.8d, 3.2d, 3.5d, 4.0d, 4.5d, 5.6d, 6.3d, 7.1d, 8.0d, 11.0d, 13.0d, 16.0d};
    private static final String[] SUPPORTED_VALUES = new String[]{"0.95", "1.2", "1.4", "2.0", "2.4", "2.8", "3.2", "3.5", "4.0", "4.5", "5.6", "6.3", "7.1", "8.0", "11", "13", "16"};
    private int mCurrentValueIndex = 1;
    private int mLevelCount = 17;
    private int[] mSupportedApertureValue = new int[this.mLevelCount];

    public ApertureParameter() {
        initSupportedApertureValue();
    }

    public void setValue(int value) {
        if (value < this.mLevelCount && this.mLevelCount >= 0) {
            for (int index = 0; index < this.mSupportedApertureValue.length; index++) {
                if (this.mSupportedApertureValue[index] == value) {
                    this.mCurrentValueIndex = index;
                    return;
                }
            }
            GalleryLog.i("ApertureParameter", "invalid wide aperture value!");
        }
    }

    public int getValue() {
        return this.mSupportedApertureValue[this.mCurrentValueIndex];
    }

    public void setLevelCount(int levelCount) {
        if (this.mLevelCount != levelCount && levelCount > 0) {
            this.mLevelCount = levelCount;
            this.mSupportedApertureValue = new int[this.mLevelCount];
            initSupportedApertureValue();
        }
    }

    public int getLevelCount() {
        return this.mLevelCount;
    }

    public String getShowingValue() {
        if (this.mCurrentValueIndex > SUPPORTED_VALUES.length - 1) {
            return null;
        }
        return SUPPORTED_VALUES[this.mCurrentValueIndex];
    }

    private void initSupportedApertureValue() {
        for (int i = 0; i < this.mLevelCount; i++) {
            this.mSupportedApertureValue[i] = i;
        }
    }
}
