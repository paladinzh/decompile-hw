package com.android.keyguard;

import android.graphics.Bitmap;
import android.os.SystemClock;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;

public class KeyguardUpdateMonitorCallback {
    private boolean mShowing;
    private long mVisibilityChangedCalled;

    public void onRefreshBatteryInfo(BatteryStatus status) {
    }

    public void onTimeChanged() {
    }

    public void onRefreshCarrierInfo() {
    }

    public void onRingerModeChanged(int state) {
    }

    public void onPhoneStateChanged(int phoneState) {
    }

    public void onKeyguardVisibilityChanged(boolean showing) {
    }

    public void onKeyguardVisibilityChangedRaw(boolean showing) {
        long now = SystemClock.elapsedRealtime();
        if (showing != this.mShowing || now - this.mVisibilityChangedCalled >= 1000) {
            onKeyguardVisibilityChanged(showing);
            this.mVisibilityChangedCalled = now;
            this.mShowing = showing;
        }
    }

    public void onKeyguardBouncerChanged(boolean bouncer) {
    }

    public void onClockVisibilityChanged() {
    }

    public void onDeviceProvisioned() {
    }

    public void onDevicePolicyManagerStateChanged() {
    }

    public void onUserSwitching(int userId) {
    }

    public void onUserSwitchComplete(int userId) {
    }

    public void onSimStateChanged(int subId, int slotId, State simState) {
    }

    public void onUserInfoChanged(int userId) {
    }

    public void onBootCompleted() {
    }

    public void onEmergencyCallAction() {
    }

    public void onSetBackground(Bitmap bitmap) {
    }

    public void onStartedWakingUp() {
    }

    public void onStartedGoingToSleep(int why) {
    }

    public void onFinishedGoingToSleep(int why) {
    }

    public void onScreenTurnedOn() {
    }

    public void onScreenTurnedOff() {
    }

    public void onTrustChanged(int userId) {
    }

    public void onTrustManagedChanged(int userId) {
    }

    public void onTrustGrantedWithFlags(int flags, int userId) {
    }

    public void onFingerprintAcquired(int acquireInfo) {
    }

    public void onFingerprintAuthFailed() {
    }

    public void onFingerprintAuthenticated(int userId, int fingerId) {
    }

    public void onFingerprintHelp(int msgId, String helpString) {
    }

    public void onFingerprintError(int msgId, String errString) {
    }

    public void onFaceUnlockStateChanged(boolean running, int userId) {
    }

    public void onFingerprintRunningStateChanged(boolean running) {
    }

    public void onStrongAuthStateChanged(int userId) {
    }
}
