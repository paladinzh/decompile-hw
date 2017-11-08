package com.huawei.systemmanager.applock.utils;

import android.content.Context;
import android.content.Intent;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.applock.password.AuthEnterAppLockActivity;
import com.huawei.systemmanager.applock.password.AuthEnterRelockSelfActivity;
import com.huawei.systemmanager.applock.password.AuthLaunchLockedAppActivity;
import com.huawei.systemmanager.applock.password.SetPasswordActivity;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.view.ApplicationListActivity;

public class ActivityIntentUtils {
    public static Intent getStartAppLockMainIntent(Context context) {
        Intent intent;
        if (AppLockPwdUtils.isPasswordSet(context)) {
            intent = new Intent(context, AuthEnterAppLockActivity.class);
        } else {
            intent = new Intent(context, SetPasswordActivity.class);
        }
        intent.setFlags(335544320);
        return intent;
    }

    public static Intent getRelockSelfActivityIntent(Context context) {
        Intent intent = new Intent(context, AuthEnterRelockSelfActivity.class);
        intent.setFlags(335544320);
        return intent;
    }

    public static Intent getApplicationListActivityIntent(Context context) {
        return new Intent(context, ApplicationListActivity.class).setFlags(335544320);
    }

    public static Intent getStartLaunchAppAuthActivityIntent(Context context) {
        Intent intent = new Intent(context, AuthLaunchLockedAppActivity.class);
        intent.setFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        return intent;
    }

    public static Intent getStartHomeActivityIntent(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(270532608);
        return intent;
    }

    public static Intent getStartAuthActivity(Context context, int scenario) {
        switch (scenario) {
            case 1:
                return getStartAppLockMainIntent(context);
            case 2:
                return getRelockSelfActivityIntent(context);
            case 3:
                return getStartLaunchAppAuthActivityIntent(context);
            case 4:
                return null;
            default:
                return null;
        }
    }
}
