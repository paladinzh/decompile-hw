package com.huawei.systemmanager.useragreement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.antivirus.notify.TimerRemindNotify;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.pref.NetworkAccessPreference;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference;
import com.huawei.systemmanager.util.HwLog;

public class UserAgreementHelper {
    public static final String ACTION_USERAGREEMENT_STATE_CHANGE = "com.huawei.systemmanager.useragreement_state_change";
    private static final String TAG = "UserAgreementHelper";
    private static boolean mAgreedFlag = false;

    public static boolean getAgreedOnThisStart() {
        return mAgreedFlag;
    }

    public static void setAgreedOnThisStart() {
        mAgreedFlag = true;
    }

    public static void turnOnNetSettings() {
        HwLog.i(TAG, "turnOnNetSettings");
        Context context = GlobalContext.getContext();
        AntiVirusTools.startAutoUpdateVirusLibAlarm(context, AntiVirusTools.getUpdateRate(context));
        TimerRemindNotify notify = new TimerRemindNotify();
        if (AntiVirusTools.isGlobalTimerSwitchOn(context)) {
            notify.schduleTimingNotify(context);
        } else {
            notify.cancelTimingNotify(context);
        }
        if (!AntiVirusTools.isAbroad()) {
            SpaceSettingPreference.getDefault().getUpdateSetting().setValue(Boolean.valueOf(true));
        }
        NetworkAccessPreference.setNetworkAccess(true);
        NetworkAccessPreference.setNetworkAccessForCloudClient(true);
        if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
            UpdateHelper.scheduleAutoUpdate(context);
        }
        if (!AntiVirusTools.isAbroad()) {
            AdUtils.update(context, 2);
        }
    }

    public static void turnOffNetSettings() {
        HwLog.i(TAG, "turnOffNetSettings");
        Context context = GlobalContext.getContext();
        if (!AntiVirusTools.isAbroad()) {
            AntiVirusTools.cancelAutoUpdateVirusLibAlarm(context);
        }
        SpaceSettingPreference.getDefault().getUpdateSetting().setValue(Boolean.valueOf(false));
        NetworkAccessPreference.setNetworkAccess(false);
        NetworkAccessPreference.setNetworkAccessForCloudClient(false);
        if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
            UpdateHelper.cancelAutoUpdateSchedule(context);
        }
    }

    @Deprecated
    public static void setUserAgreementVersion(Context context, int nVersion) {
        context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).edit().putInt(SystemManagerConst.KEY_USERAGREEMENT_VERSION, nVersion).commit();
    }

    public static int getUserAgreementVersion(Context context) {
        return context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).getInt(SystemManagerConst.KEY_USERAGREEMENT_VERSION, 0);
    }

    public static void setUserAgreementState(Context context, boolean agree) {
        context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).edit().putBoolean(SystemManagerConst.KEY_USERAGREEMENT_AGREED, agree).commit();
        sendUserAgreementStateChange(context);
    }

    public static boolean getUserAgreementState(Context context) {
        return context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).getBoolean(SystemManagerConst.KEY_USERAGREEMENT_AGREED, false);
    }

    @Deprecated
    public static void setUserAgreementNotRemindFlag(Context context, boolean notRemind) {
        context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).edit().putBoolean(SystemManagerConst.KEY_USERAGREEMENT_NOT_REMIND, notRemind).commit();
    }

    public static boolean getUserAgreementNotRemindFlag(Context context) {
        return context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).getBoolean(SystemManagerConst.KEY_USERAGREEMENT_NOT_REMIND, false);
    }

    public static void setUserAgreementBatch(Context context, boolean agree, int nVersion, boolean notRemind) {
        Editor editor = context.getSharedPreferences(SystemManagerConst.SYSTEM_MANAGER_PERFERENCE, 4).edit();
        editor.putBoolean(SystemManagerConst.KEY_USERAGREEMENT_AGREED, agree);
        editor.putInt(SystemManagerConst.KEY_USERAGREEMENT_VERSION, nVersion);
        editor.putBoolean(SystemManagerConst.KEY_USERAGREEMENT_NOT_REMIND, notRemind);
        editor.commit();
        sendUserAgreementStateChange(context);
    }

    public static void resetNetworkSettings(Context context) {
        if (!getUserAgreementNotRemindFlag(context)) {
            setUserAgreementState(context, false);
            turnOffNetSettings();
        }
    }

    private static void sendUserAgreementStateChange(Context context) {
        Intent intent = new Intent(ACTION_USERAGREEMENT_STATE_CHANGE);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}
