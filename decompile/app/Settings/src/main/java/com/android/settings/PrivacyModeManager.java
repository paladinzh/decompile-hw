package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import com.android.internal.widget.LockPatternUtils;

public class PrivacyModeManager {
    private Context mContext;

    public PrivacyModeManager(Context context) {
        this.mContext = context;
    }

    public boolean isPrivacyModeEnabled() {
        if (!isFeatrueSupported()) {
            return false;
        }
        boolean modeEnabledInDb;
        if (Secure.getInt(this.mContext.getContentResolver(), "privacy_mode_on", 0) == 1) {
            modeEnabledInDb = true;
        } else {
            modeEnabledInDb = false;
        }
        if (!modeEnabledInDb) {
            return false;
        }
        if (new LockPatternUtils(this.mContext).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) >= 131072) {
            return true;
        }
        Secure.putInt(this.mContext.getContentResolver(), "privacy_mode_on", 0);
        MLog.e("privacymode", "There is something wrong:privacy mode is on but no owner password set!");
        return false;
    }

    public boolean isGuestModeOn() {
        if (Secure.getInt(this.mContext.getContentResolver(), "privacy_mode_state", 0) == 1) {
            return isPrivacyModeEnabled();
        }
        return false;
    }

    public static boolean isFeatrueSupported() {
        return SystemProperties.getBoolean("ro.config.hw_privacymode", false);
    }
}
