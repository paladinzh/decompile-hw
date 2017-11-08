package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;

public class HwPhoneStatusBarView extends PhoneStatusBarView {
    public HwPhoneStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        HwPhoneStatusBar.getInstance().onAllPanelsCollapsed();
    }

    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        HwPhoneStatusBar.getInstance().onPanelFullyOpened();
    }
}
