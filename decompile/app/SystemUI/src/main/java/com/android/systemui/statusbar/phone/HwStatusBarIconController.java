package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.View;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;

public class HwStatusBarIconController extends StatusBarIconController {
    public HwStatusBarIconController(Context context, View statusBar, View keyguardStatusBar, PhoneStatusBar phoneStatusBar) {
        super(context, statusBar, keyguardStatusBar, phoneStatusBar);
    }

    protected void applyIconTint() {
        HwLog.i("HwStatusBarIconController", "applyIconTint");
        if (TintManager.getInstance().isUseTint()) {
            HwLog.i("HwStatusBarIconController", "applyIconTint ignore");
        }
    }

    protected void setIconTintInternal(float darkIntensity) {
        super.setIconTintInternal(darkIntensity);
        HwLog.i("HwStatusBarIconController", "setIconTintInternal:" + darkIntensity + ", tintColor:" + this.mIconTint);
    }
}
