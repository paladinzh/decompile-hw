package com.android.settings.fingerprint;

public class HwCustFingerprintMainSettingsFragment {
    protected FingerprintMainSettingsFragment mFingerprintMainSettingsFragment;

    public HwCustFingerprintMainSettingsFragment(FingerprintMainSettingsFragment fingerprintMainSettingsFragment) {
        this.mFingerprintMainSettingsFragment = fingerprintMainSettingsFragment;
    }

    public void addPreferencesFromResource() {
    }

    public void updateSwitchState() {
    }

    public boolean hasCustomizeFingerprint() {
        return false;
    }

    public void checkRemovePref(String key) {
    }
}
