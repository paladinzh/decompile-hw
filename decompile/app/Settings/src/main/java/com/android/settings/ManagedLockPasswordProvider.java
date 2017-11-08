package com.android.settings;

import android.content.Context;
import android.content.Intent;

public class ManagedLockPasswordProvider {
    static ManagedLockPasswordProvider get(Context context, int userId) {
        return new ManagedLockPasswordProvider();
    }

    protected ManagedLockPasswordProvider() {
    }

    boolean isSettingManagedPasswordSupported() {
        return false;
    }

    boolean isManagedPasswordChoosable() {
        return false;
    }

    String getPickerOptionTitle(boolean forFingerprint) {
        return "";
    }

    int getResIdForLockUnlockScreen(boolean forProfile) {
        if (forProfile) {
            return 2131230869;
        }
        return 2131230868;
    }

    int getResIdForLockUnlockSubScreen() {
        return 2131230870;
    }

    Intent createIntent(boolean requirePasswordToDecrypt, String password) {
        return null;
    }
}
