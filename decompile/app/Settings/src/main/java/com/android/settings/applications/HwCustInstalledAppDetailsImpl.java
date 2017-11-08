package com.android.settings.applications;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import com.android.internal.util.ArrayUtils;
import com.android.settings.CalculatorModel;
import com.android.settings.HwCustSettingsUtils;
import com.android.settings.UtilsCustEx;

public class HwCustInstalledAppDetailsImpl extends HwCustInstalledAppDetails {
    private static final String AUTOREG_PACKAGE_NAME = "com.huawei.ChnCmccAutoReg";
    private static final String DMCLIENT_PACKAGE_NAME = "com.huawei.android.dmclient";
    static final String[] NOT_DISABLABLED_APP_LIST = new String[]{"com.android.providers.media", "com.huawei.camera", "com.android.gallery3d", "com.android.facelock", "com.google.android.gms", "com.google.android.gsf.login", "com.google.android.gsf", "com.android.providers.downloads", "com.android.providers.downloads.ui", "com.android.exchange", "com.android.calendar", "com.android.providers.calendar", "com.huawei.hwvplayer", "com.huawei.hwvplayer.youku"};
    static final String[] NOT_DISABLABLED_SPRINT_APP_LIST = new String[]{"com.sprint.w.installer", "com.sprint.zone", "com.coremobility.app.vnotes", "com.sprint.dsa", "com.sprint.ce.updater", "com.itsoninc.android.uid", "com.itsoninc.android.itsonservice", "com.itsoninc.android.itsonclient", "com.facebook.system", "com.facebook.appmanager"};
    static final String[] NOT_FORCE_STOP_SPRINT_APP_LIST = new String[]{"com.itsoninc.android.uid", "com.itsoninc.android.itsonservice", "com.facebook.system", "com.facebook.appmanager"};
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String TAG = "HwCustInstalledAppDetailsImpl";
    private static final String TEB_SHARED_USER_ID = "com.itsoninc";
    private static final int TEB_USER_ID = 1500;

    public HwCustInstalledAppDetailsImpl(InstalledAppDetails installedAppDetails) {
        super(installedAppDetails);
    }

    public boolean isEnableSpecialDisableButton(Context context) {
        if (AUTOREG_PACKAGE_NAME.equals(this.mInstalledAppDetails.mPackageInfo.packageName) || DMCLIENT_PACKAGE_NAME.equals(this.mInstalledAppDetails.mPackageInfo.packageName)) {
            return false;
        }
        return true;
    }

    public boolean isForbidDisablableBtn(Context context) {
        if (ArrayUtils.contains(NOT_DISABLABLED_APP_LIST, this.mInstalledAppDetails.mPackageInfo.packageName)) {
            return true;
        }
        if (!HwCustSettingsUtils.IS_SPRINT || (!ArrayUtils.contains(NOT_DISABLABLED_SPRINT_APP_LIST, this.mInstalledAppDetails.mPackageInfo.packageName) && !TEB_SHARED_USER_ID.equals(this.mInstalledAppDetails.mPackageInfo.sharedUserId) && TEB_USER_ID != this.mInstalledAppDetails.mPackageInfo.applicationInfo.uid)) {
            return false;
        }
        Log.i(TAG, "isForbidDisablableBtn :true");
        return true;
    }

    public boolean getUninstallBtnEnableState(boolean enabled) {
        if (HwCustSettingsUtils.IS_SPRINT && TEB_USER_ID == this.mInstalledAppDetails.mPackageInfo.applicationInfo.uid) {
            enabled = false;
        }
        Log.i(TAG, "getUninstallBtnEnableState :" + enabled);
        return enabled;
    }

    public void custUpdateForceStopButton(Button forceStopButton) {
        if (HwCustSettingsUtils.IS_SPRINT && ArrayUtils.contains(NOT_FORCE_STOP_SPRINT_APP_LIST, this.mInstalledAppDetails.mPackageInfo.packageName)) {
            forceStopButton.setEnabled(false);
            Log.i(TAG, "forceStopButton state:" + forceStopButton.isEnabled());
        }
    }

    public boolean isUmsStorageMounted(Context context) {
        return UtilsCustEx.isUmsStorageMounted(context);
    }

    public void custClearUserData(Context context, String packageName) {
        if (SETTINGS_PACKAGE_NAME.equals(packageName)) {
            CalculatorModel calculatorModel = CalculatorModel.getInstance(context);
            calculatorModel.setCalculatorEnable(false);
            calculatorModel.setCalculatorLastState(false);
        }
    }
}
