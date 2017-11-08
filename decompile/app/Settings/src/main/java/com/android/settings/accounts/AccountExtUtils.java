package com.android.settings.accounts;

import android.app.Activity;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.accounts.AuthenticatorHelper;

public class AccountExtUtils {
    public static boolean isUserLoggedInHwId(AuthenticatorHelper authenticatorHelper) {
        if (authenticatorHelper == null) {
            return false;
        }
        AccountPlatformImp platformImp = new AccountPlatformImp();
        return AccountExtAbsBase.isUserLoggedInHwId(authenticatorHelper);
    }

    public static boolean shouldBeIgnored(String accountType) {
        if (accountType == null) {
            return true;
        }
        AccountPlatformImp platformImp = new AccountPlatformImp();
        return AccountExtAbsBase.shouldBeIgnored(accountType);
    }

    public static void updateHwCloudServicePreference(PreferenceScreen screen, Context context, Preference hwCloudServicePreference) {
        if (screen != null && context != null && hwCloudServicePreference != null) {
            AccountPlatformImp platformImp = new AccountPlatformImp();
            AccountExtAbsBase.updateHwCloudServicePreference(screen, context, hwCloudServicePreference);
        }
    }

    public static void removeRedundantMargin(Activity activity) {
        if (activity != null) {
            AccountPlatformImp platformImp = new AccountPlatformImp();
            AccountExtAbsBase.removeRedundantMargin(activity);
        }
    }
}
