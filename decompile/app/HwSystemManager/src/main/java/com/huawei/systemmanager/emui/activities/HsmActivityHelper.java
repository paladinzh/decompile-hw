package com.huawei.systemmanager.emui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.useragreement.UserAgreementActivity;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;

public class HsmActivityHelper {
    private static final String TAG = "HsmActivityHelper";

    public static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        win.clearFlags(201326592);
        win.addFlags(Integer.MIN_VALUE);
    }

    public static void setHsmThemeStyle(Activity activity, int nThemeStyle) {
        if (!Utility.isSupportSystemTheme()) {
            if (nThemeStyle == 0) {
                setImmersionStyle(activity);
            } else {
                activity.setTheme(nThemeStyle);
            }
        }
    }

    public static void setImmersionStyle(Activity activity) {
        activity.setTheme(R.style.HsmImmersionTheme);
    }

    public static void updateActionBarStyle(Activity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(32768);
        }
    }

    public static boolean checkShouldShowUserAgreement(Context context) {
        if (!CustomizeManager.getInstance().isFeatureEnabled(8)) {
            HwLog.i(TAG, "checkShouldShowUserAgreement: User agreement is not needed, skip");
            return false;
        } else if (UserAgreementHelper.getAgreedOnThisStart()) {
            HwLog.i(TAG, "checkShouldShowUserAgreement: Already remind on this start, skip");
            return false;
        } else if (UserAgreementHelper.getUserAgreementVersion(context) != 2) {
            HwLog.i(TAG, "checkShouldShowUserAgreement: User agreement is updated , show it");
            return true;
        } else if (!UserAgreementHelper.getUserAgreementState(context)) {
            HwLog.i(TAG, "checkShouldShowUserAgreement: User agreement is not agreed , show it");
            return true;
        } else if (UserAgreementHelper.getUserAgreementNotRemindFlag(context)) {
            return false;
        } else {
            HwLog.i(TAG, "checkShouldShowUserAgreement: User agreement is agreed without not remind flag, show it");
            return true;
        }
    }

    public static boolean checkAndShowAgreement(Activity activity) {
        if (!checkShouldShowUserAgreement(activity)) {
            return false;
        }
        activity.startActivityForResult(new Intent(activity, UserAgreementActivity.class), UserAgreementActivity.REQUEST_CODE_USERAGREEMRNT);
        return true;
    }

    public static void setRequestedOrientation(Activity activity) {
        if (Utility.isSupportOrientation()) {
            activity.setRequestedOrientation(-1);
        } else {
            activity.setRequestedOrientation(1);
        }
    }

    public static void setStatusBarHide(Activity activity, boolean hide) {
        Window win = activity.getWindow();
        LayoutParams winParams = win.getAttributes();
        if (hide) {
            winParams.flags |= 1024;
        } else {
            winParams.flags &= -1025;
        }
        win.setAttributes(winParams);
    }

    public static void setTranslucentNavigation(Activity activity, boolean translucent) {
        Window win = activity.getWindow();
        if (win != null) {
            if (translucent) {
                win.addFlags(134217728);
            } else {
                win.clearFlags(134217728);
            }
        }
    }

    public static void initActionBar(Activity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }
    }
}
