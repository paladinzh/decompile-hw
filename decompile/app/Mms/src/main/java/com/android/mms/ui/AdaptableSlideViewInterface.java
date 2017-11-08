package com.android.mms.ui;

public interface AdaptableSlideViewInterface extends SlideViewInterface {

    public interface OnSizeChangedListener {
        void onSizeChanged(int i, int i2);
    }

    void setImageRegion(int i, int i2, int i3, int i4);

    void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener);

    void setTextRegion(int i, int i2, int i3, int i4);

    void setVideoRegion(int i, int i2, int i3, int i4);
}
