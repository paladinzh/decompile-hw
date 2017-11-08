package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Drawable;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;

public class HwPhoneStatusBarTransitions extends PhoneStatusBarTransitions {
    private int mColor;
    private float mLastAlpha = 0.5f;
    private PhoneStatusBarView mView;

    public HwPhoneStatusBarTransitions(PhoneStatusBarView view) {
        super(view);
        this.mView = view;
        TintManager.getInstance().setPhoneStatusBarTransitions(this);
    }

    public void setBackgroundColor(int color) {
        HwLog.i("HwPhoneStatusBarTransitions", "setBackgroundColor:new=" + color + ", old=" + this.mBarBackground.mColor);
        this.mView.setBackgroundColor(color);
        this.mColor = color;
    }

    public void restoreBackgroundColor() {
        HwLog.i("HwPhoneStatusBarTransitions", "restoreBackgroundColor:new=" + this.mBarBackground.mColor + ", old=" + this.mColor);
        this.mView.setBackground(this.mBarBackground);
    }

    public void setBackgroundDrawable(Drawable drawable) {
        HwLog.i("HwPhoneStatusBarTransitions", "setBackgroundDrawable:drawable=" + drawable);
        this.mView.setBackgroundDrawable(drawable);
    }

    public void setBackgroundAlpha(float alpha) {
        if (isFloatChanged(this.mLastAlpha, alpha)) {
            HwLog.i("HwPhoneStatusBarTransitions", "setBackgroundAlpha:alpha=" + alpha);
            this.mView.setAlpha(alpha);
            this.mLastAlpha = alpha;
        }
    }

    public void transitionTo(int mode, boolean animate) {
        super.transitionTo(mode, animate);
        if (1 == mode && TintManager.getInstance().isEmuiStyle()) {
            setBackgroundColor(TintManager.getInstance().getSemiStatusBarBgColor());
        }
    }

    private boolean isFloatChanged(float data1, float data2) {
        return Math.abs(data1 - data2) > 1.0E-6f;
    }
}
