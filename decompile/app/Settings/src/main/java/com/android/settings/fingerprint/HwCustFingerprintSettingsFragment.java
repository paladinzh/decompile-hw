package com.android.settings.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.support.v7.preference.PreferenceGroup;
import java.util.List;

public class HwCustFingerprintSettingsFragment {
    public FingerprintSettingsFragment mFingerprintSettingsFragment;

    public HwCustFingerprintSettingsFragment(FingerprintSettingsFragment fingerprintSettingsFragment) {
        this.mFingerprintSettingsFragment = fingerprintSettingsFragment;
    }

    public void updateStatus() {
    }

    public boolean isShowFingerprintVibration() {
        return false;
    }

    public String queryFpSummary(Context context, int fpId) {
        return null;
    }

    public boolean fingerPrintShotcut() {
        return false;
    }

    public void setFpSummary(Context context, List<Fingerprint> list) {
    }

    public boolean isFrontFingerPrint() {
        return false;
    }

    public void initRecognisePreferene() {
    }

    public void refreshFpPreference(PreferenceGroup fpListCat) {
    }

    public void identifyFpID(int fpId) {
    }

    public void frontFpLockoutReset() {
    }

    public void sendCancelIdentifyMessage() {
    }
}
