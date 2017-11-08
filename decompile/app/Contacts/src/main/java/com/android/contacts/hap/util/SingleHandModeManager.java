package com.android.contacts.hap.util;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.contacts.util.HwLog;

public class SingleHandModeManager {
    private static final String TAG = SingleHandModeManager.class.getSimpleName();
    private static boolean isSingleHandModeFeatureEnabled = false;
    private static boolean mSmartSingleHandModeFeatureEnabled = false;
    private static SingleHandModeManager sInstance = null;
    private Context mContext;

    private SingleHandModeManager(Context aContext) {
        this.mContext = aContext;
    }

    public static SingleHandModeManager getInstance(Context aContext) {
        boolean z = true;
        if (sInstance == null) {
            sInstance = new SingleHandModeManager(aContext);
        }
        int singleHandMode = SystemProperties.getInt("ro.config.hw_singlehand", 0);
        boolean z2 = singleHandMode != 1 ? singleHandMode == 2 : true;
        isSingleHandModeFeatureEnabled = z2;
        if (singleHandMode != 2) {
            z = false;
        }
        mSmartSingleHandModeFeatureEnabled = z;
        return sInstance;
    }

    public boolean isSingleHandFeatureEnabled() {
        boolean lIsSingleHandModeFeatureEnabled = isSingleHandModeFeatureEnabled;
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "isSingleHandModeSwitchedOn: " + lIsSingleHandModeFeatureEnabled);
        }
        return lIsSingleHandModeFeatureEnabled;
    }

    public boolean isSingleHandModeSwitchedOn() {
        boolean lIsSingleHandModeEnabled = false;
        if (isSingleHandFeatureEnabled()) {
            lIsSingleHandModeEnabled = System.getInt(this.mContext.getContentResolver(), "single_hand_switch", 0) == 1;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "isSingleHandFeatureEnabled: " + lIsSingleHandModeEnabled);
        }
        return lIsSingleHandModeEnabled;
    }

    public int getCurrentHandMode() {
        int lCurrentMode;
        if (isSingleHandModeSwitchedOn()) {
            lCurrentMode = System.getInt(this.mContext.getContentResolver(), "single_hand_mode", 2);
        } else {
            lCurrentMode = 0;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "getCurrentHandMode:" + lCurrentMode);
        }
        return lCurrentMode;
    }

    public void saveUserSelectionHandMode(int aMode) {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "saveUserSelectionHandMode aMode :: " + aMode);
        }
        System.putInt(this.mContext.getContentResolver(), "single_hand_mode", aMode);
    }

    public Uri getUriForSingleHandModeSwitch() {
        return System.getUriFor("single_hand_switch");
    }

    public Uri getUriForSingleHandMode() {
        return System.getUriFor("single_hand_mode");
    }

    public Uri getUriForSmartSingleHandModeSwitch() {
        return System.getUriFor("single_hand_smart");
    }

    public boolean isSmartSingleHandModeOn() {
        boolean z = true;
        if (!isSmartSingleHandFeatureEnabled() || !isSingleHandModeSwitchedOn()) {
            return false;
        }
        if (System.getInt(this.mContext.getContentResolver(), "single_hand_smart", 0) != 1) {
            z = false;
        }
        return z;
    }

    public boolean isSmartSingleHandFeatureEnabled() {
        return mSmartSingleHandModeFeatureEnabled ? isSingleHandModeFeatureEnabled : false;
    }
}
