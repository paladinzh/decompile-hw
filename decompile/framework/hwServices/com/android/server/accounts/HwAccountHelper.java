package com.android.server.accounts;

import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.provider.Settings.Secure;
import com.android.server.devicepolicy.HwDevicePolicyManagerService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class HwAccountHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwAccountHelper";

    private static boolean isPrivacyModeStateOn(Context context) {
        try {
            boolean privacyModeStateOn = 1 == Secure.getInt(context.getContentResolver(), HwDevicePolicyManagerService.PRIVACY_MODE_ON, 1) ? 1 == Secure.getInt(context.getContentResolver(), "privacy_mode_state", 0) : false;
            return privacyModeStateOn;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isPrivacyModePkg(String packageName, String account, Context context) {
        String pkgNameList = Secure.getString(context.getContentResolver(), "privacy_app_list");
        if (pkgNameList == null || pkgNameList.equals(AppHibernateCst.INVALID_PKG)) {
            return false;
        }
        String[] pkgNameArray = pkgNameList.contains(";") ? pkgNameList.split(";") : new String[]{pkgNameList};
        int i = 0;
        while (pkgNameArray != null && i < pkgNameArray.length) {
            String tempName = pkgNameArray[i];
            if (tempName.equals(packageName) || tempName.contains(account)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean removed, Context context) {
        if (isPrivacyModeStateOn(context) && removed && isPrivacyModePkg(desc.packageName, desc.type, context)) {
            return true;
        }
        return false;
    }
}
