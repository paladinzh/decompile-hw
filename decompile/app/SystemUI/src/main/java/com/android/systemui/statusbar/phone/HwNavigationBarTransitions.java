package com.android.systemui.statusbar.phone;

import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;

public class HwNavigationBarTransitions extends NavigationBarTransitions {
    private int mColor;
    private NavigationBarView mView;

    public HwNavigationBarTransitions(NavigationBarView view) {
        super(view);
        this.mView = view;
        TintManager.getInstance().setNavigationBarTransitions(this);
    }

    public void setBackgroundColor(int color) {
        HwLog.i("HwNavigationBarTransitions", "setBackgroundColor:new=" + color + ", old=" + this.mBarBackground.mColor);
        this.mView.setBackgroundColor(color);
        this.mColor = color;
    }

    public void restoreBackgroundColor() {
        HwLog.i("HwNavigationBarTransitions", "restoreBackgroundColor:new=" + this.mBarBackground.mColor + ", old=" + this.mColor);
        this.mView.setBackground(this.mBarBackground);
    }

    public void transitionTo(int mode, boolean animate) {
        super.transitionTo(mode, animate);
        boolean isEmuiStyle = TintManager.getInstance().isEmuiStyle();
        HwLog.i("HwNavigationBarTransitions", "transitionTo:mode=" + mode + ", isEmuiStyle=" + isEmuiStyle);
        if (1 == mode && isEmuiStyle) {
            setBackgroundColor(TintManager.getInstance().getSemiStatusBarBgColor());
        }
    }
}
