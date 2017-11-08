package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import com.android.systemui.statusbar.policy.BatteryController;

public class LightStatusBarController {
    private final BatteryController mBatteryController;
    private FingerprintUnlockController mFingerprintUnlockController;
    private final StatusBarIconController mIconController;
    private final Rect mLastDockedBounds = new Rect();
    private final Rect mLastFullscreenBounds = new Rect();

    public LightStatusBarController(StatusBarIconController iconController, BatteryController batteryController) {
        this.mIconController = iconController;
        this.mBatteryController = batteryController;
    }

    public void setFingerprintUnlockController(FingerprintUnlockController fingerprintUnlockController) {
        this.mFingerprintUnlockController = fingerprintUnlockController;
    }
}
