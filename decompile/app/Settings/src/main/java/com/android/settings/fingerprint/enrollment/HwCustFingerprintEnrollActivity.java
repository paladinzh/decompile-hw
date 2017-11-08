package com.android.settings.fingerprint.enrollment;

public class HwCustFingerprintEnrollActivity {
    public FingerprintEnrollActivity mFingerprintEnrollActivity;

    public HwCustFingerprintEnrollActivity(FingerprintEnrollActivity fingerprintEnrollActivity) {
        this.mFingerprintEnrollActivity = fingerprintEnrollActivity;
    }

    public void hideVirtualKey() {
    }

    public void recoverVirtualKey() {
    }

    public boolean isFrontFingerPrint() {
        return false;
    }
}
