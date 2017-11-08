package com.android.phone.common;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Vibrator;
import android.provider.Settings.System;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;

public class HapticFeedback {
    private ContentResolver mContentResolver;
    private boolean mEnabled;
    private long[] mHapticPattern;
    private boolean mLon;
    private long[] mLonPattern;
    private boolean mSettingEnabled;
    private System mSystemSettings;
    private Vibrator mVibrator;

    public void init(Context context, boolean enabled) {
        this.mEnabled = enabled;
        if (enabled) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
            this.mHapticPattern = new long[]{0, 10, 20, 30};
            this.mLonPattern = new long[]{60};
            this.mSystemSettings = new System();
            this.mContentResolver = context.getContentResolver();
            this.mLon = false;
        }
    }

    public void setLon() {
        this.mLon = true;
    }

    public void checkSystemSetting() {
        if (this.mEnabled) {
            try {
                boolean z;
                System system = this.mSystemSettings;
                if (System.getInt(this.mContentResolver, "haptic_feedback_enabled", 0) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mSettingEnabled = z;
            } catch (NotFoundException nfe) {
                HwLog.e("HapticFeedback", "Could not retrieve system setting.", nfe);
                ExceptionCapture.captureSystemSettingException("checkSystemSetting:Could not retrieve system setting.", null);
                this.mSettingEnabled = false;
            }
            if (HwLog.HWFLOW) {
                HwLog.i("HapticFeedback", "checkSystemSetting mSettingEnabled:" + this.mSettingEnabled);
            }
        }
    }

    public void vibrate() {
        if (!this.mEnabled || !this.mSettingEnabled) {
            return;
        }
        if (this.mLon) {
            this.mVibrator.vibrate(this.mLonPattern[0]);
            return;
        }
        if (this.mHapticPattern == null || this.mHapticPattern.length != 1) {
            this.mVibrator.vibrate(this.mHapticPattern, -1);
        } else {
            this.mVibrator.vibrate(this.mHapticPattern[0]);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void lonVibrate() {
        if (this.mEnabled && this.mSettingEnabled && this.mVibrator != null) {
            this.mVibrator.vibrate(this.mLonPattern[0]);
        }
    }
}
