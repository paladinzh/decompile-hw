package com.android.settings.fingerprint.enrollment;

import android.app.StatusBarManager;
import android.os.SystemProperties;

public class HwCustFingerprintEnrollActivityImpl extends HwCustFingerprintEnrollActivity {
    private int flags;
    private StatusBarManager mStatusBarManager;

    public HwCustFingerprintEnrollActivityImpl(FingerprintEnrollActivity fingerprintEnrollActivity) {
        super(fingerprintEnrollActivity);
    }

    public boolean isFrontFingerPrint() {
        return SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    }

    public void hideVirtualKey() {
        this.mStatusBarManager = (StatusBarManager) this.mFingerprintEnrollActivity.getSystemService("statusbar");
        this.flags |= 18874368;
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(this.flags);
        }
    }

    public void recoverVirtualKey() {
        this.mStatusBarManager = (StatusBarManager) this.mFingerprintEnrollActivity.getSystemService("statusbar");
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(0);
        }
    }
}
