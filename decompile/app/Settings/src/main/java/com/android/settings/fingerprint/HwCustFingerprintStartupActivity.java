package com.android.settings.fingerprint;

public class HwCustFingerprintStartupActivity {
    protected FingerprintStartupActivity mFingerprintStartupActivity;

    public HwCustFingerprintStartupActivity(FingerprintStartupActivity fingerprintStartupActivity) {
        this.mFingerprintStartupActivity = fingerprintStartupActivity;
    }

    public boolean isSupportVoliceWakeUp() {
        return false;
    }

    public void startActivityForVoliceWakeUp() {
    }
}
