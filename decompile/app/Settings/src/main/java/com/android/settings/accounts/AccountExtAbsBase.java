package com.android.settings.accounts;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceFrameLayout;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.MLog;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;

abstract class AccountExtAbsBase {
    AccountExtAbsBase() {
    }

    public static boolean isUserLoggedInHwId(AuthenticatorHelper authenticatorHelper) {
        if (authenticatorHelper == null) {
            return false;
        }
        for (String accountType : authenticatorHelper.getEnabledAccountTypes()) {
            if ("com.huawei.hwid".equalsIgnoreCase(accountType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldBeIgnored(String accountType) {
        if ("com.android.huawei.sim".equalsIgnoreCase(accountType) || "com.android.huawei.secondsim".equalsIgnoreCase(accountType) || "com.android.huawei.phone".equalsIgnoreCase(accountType)) {
            return true;
        }
        return false;
    }

    public static void updateHwCloudServicePreference(PreferenceScreen screen, Context context, Preference hwCloudServicePreference) {
        if (screen != null && context != null && hwCloudServicePreference != null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(Uri.parse("content://com.huawei.android.hicloud.loginProvider/login_user"), null, null, null, null);
                if (cursor == null || !Utils.hasIntentActivity(context.getPackageManager(), hwCloudServicePreference.getIntent())) {
                    screen.removePreference(hwCloudServicePreference);
                } else {
                    try {
                        hwCloudServicePreference.setIcon(context.getPackageManager().getPackageInfo(HwCustAccountSettingsImpl.AAB_PACKAGE_NAME, 0).applicationInfo.loadIcon(context.getPackageManager()));
                    } catch (Exception ex) {
                        MLog.e("AccountSettings", "Error happens loading CloudService icon, error msg: " + ex.getMessage());
                        hwCloudServicePreference.setIcon(2130838360);
                    }
                    if (cursor.moveToFirst()) {
                        CharSequence accountName = cursor.getString(cursor.getColumnIndex("accountName"));
                        hwCloudServicePreference.setSummary(hwCloudServicePreference.getTitle());
                        hwCloudServicePreference.setTitle(accountName);
                    } else {
                        hwCloudServicePreference.setTitle(2131627368);
                        hwCloudServicePreference.setSummary((CharSequence) "");
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                MLog.e("AccountSettings", "Unable to get login state, error msg: " + e.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public static void removeRedundantMargin(Activity activity) {
        if (activity != null) {
            PreferenceFrameLayout layout = (PreferenceFrameLayout) activity.findViewById(16909261);
            if (layout != null) {
                layout.setPadding(0, 0, 0, 0);
            }
        }
    }
}
