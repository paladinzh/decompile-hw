package com.huawei.mms.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManager;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.huawei.cspcommon.MLog;

public class HwCallVideoUtils {
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", true);
    private static final boolean mIsCallVideoEnabled = SystemProperties.getBoolean("ro.config.hw_vtlte_on", false);

    public static boolean isCallVideoEnabled(Context context) {
        boolean mIsUserSwitchOn = System.getInt(MmsApp.getApplication().getApplicationContext().getContentResolver(), "hw_volte_user_switch", 0) == 1;
        MLog.d("HwCallVideoUtils", "isCallVideoEnabled mIsCallVideoEnabled = " + mIsCallVideoEnabled + " mIsUserSwitchOn = " + mIsUserSwitchOn);
        if (mIsCallVideoEnabled && mIsUserSwitchOn) {
            return isSimCardCallVideo(context);
        }
        return false;
    }

    public static boolean isSimCardCallVideo(Context context) {
        boolean volteVisiable = false;
        if (!FEATURE_VOLTE_DYN) {
            return true;
        }
        if (context == null) {
            return false;
        }
        CarrierConfigManager cfgMgr = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (cfgMgr == null) {
            return false;
        }
        PersistableBundle b = cfgMgr.getConfigForSubId(HwTelephonyManager.getDefault().getDefault4GSlotId());
        if (b != null) {
            volteVisiable = b.getBoolean("carrier_volte_available_bool");
        }
        return volteVisiable;
    }

    public static void dialNumberVideo(Context context, String number) {
        if (context != null && !TextUtils.isEmpty(number)) {
            Intent intent = new Intent("android.intent.action.CALL", Uri.parse(number));
            intent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 3);
            HwMessageUtils.safeStartActivity(context, intent);
        }
    }
}
