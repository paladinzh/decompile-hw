package com.huawei.cspcommon.util;

import android.content.Context;
import com.google.android.gms.R;

public class CommonConstants {
    private static boolean mIsSimplifiedMode;

    public static boolean isOnlySyncMyContactsEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_only_sync_mycontacts_enabled);
    }

    public static boolean isPrivacyFeatureEnabled() {
        return false;
    }

    public static boolean isSimplifiedModeEnabled() {
        return mIsSimplifiedMode;
    }

    public static void setSimplifiedModeEnabled(boolean aSimplifiedMode) {
        mIsSimplifiedMode = aSimplifiedMode;
    }
}
