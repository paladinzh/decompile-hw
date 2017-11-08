package com.android.mms.layout;

import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint({"NewApi", "NewApi"})
public class HVGALayoutParameters implements LayoutParameters {
    private static int mImageHeightLandscape;
    private static int mImageHeightPortrait;
    private static int mMaxHeight;
    private static int mMaxWidth;
    private static int mTextHeightLandscape;
    private static int mTextHeightPortrait;
    private int mType = -1;

    public HVGALayoutParameters(Context context, int type) {
        if (type == 10 || type == 11) {
            this.mType = type;
            setParameters(context);
            return;
        }
        throw new IllegalArgumentException("Bad layout type detected: " + type);
    }

    private static void setParameters(Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        mMaxWidth = (int) ((((float) context.getResources().getConfiguration().screenWidthDp) * scale) + 0.5f);
        mMaxHeight = (int) ((((float) context.getResources().getConfiguration().screenHeightDp) * scale) + 0.5f);
        mImageHeightLandscape = (int) (((float) mMaxWidth) * 0.9f);
        mTextHeightLandscape = (int) (((float) mMaxWidth) * 0.1f);
        mImageHeightPortrait = (int) (((float) mMaxHeight) * 0.9f);
        mTextHeightPortrait = (int) (((float) mMaxHeight) * 0.1f);
    }

    public int getWidth() {
        return mMaxWidth;
    }

    public int getHeight() {
        return mMaxHeight;
    }

    public int getImageHeight() {
        return mImageHeightPortrait;
    }

    public int getTextHeight() {
        return mTextHeightPortrait;
    }

    public int getImageWidth() {
        return mImageHeightLandscape;
    }

    public int getTextWidth() {
        return mTextHeightLandscape;
    }
}
