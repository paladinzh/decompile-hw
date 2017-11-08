package com.android.mms.transaction;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.ClassZeroActivity;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MccMncConfig;

public class HwCustMessagingNotificationImpl extends HwCustMessagingNotification {
    private static final boolean IS_VIBRATION_TYPE_ENABLED = SystemProperties.getBoolean("ro.config.hw_vibration_type", false);
    protected static final String KEY_VIBRATE_APP_MESSAGE = "vibrate_app_message";
    public static final int NOTIFICATION_CLASS_ZERO_ID = 716;

    public void classZeroMessageNotification(Context context) {
        String ringtoneStr;
        Uri uri = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Notification notification = new Notification();
        if (MmsConfig.isContainsTheKey(context)) {
            ringtoneStr = HwMessageUtils.getRingtoneString(context);
        } else {
            ringtoneStr = sp.getString("pref_key_ringtone", null);
            MmsConfig.setRingToneUriToDatabase(context, ringtoneStr);
        }
        if (!TextUtils.isEmpty(ringtoneStr)) {
            uri = Uri.parse(ringtoneStr);
        }
        notification.sound = uri;
        notification.flags |= 1;
        notification.defaults |= 4;
        ((NotificationManager) context.getSystemService("notification")).notify(NOTIFICATION_CLASS_ZERO_ID, notification);
    }

    public void checkShowZeroMessageNotification(Context context) {
        if (HwCustMmsConfigImpl.isEnableChangeClassZeroMessageShow()) {
            classZeroMessageNotification(context);
        }
    }

    public void checkWindowAddFlag(ClassZeroActivity zeroActivity) {
        if (HwCustMmsConfigImpl.isEnableChangeClassZeroMessageShow()) {
            zeroActivity.getWindow().addFlags(524288);
        }
    }

    public void checkMessageCancelNotification(Context context) {
        if (HwCustMmsConfigImpl.isEnableChangeClassZeroMessageShow()) {
            MessagingNotification.cancelNotification(context, NOTIFICATION_CLASS_ZERO_ID);
        }
    }

    public boolean getEnableSmsDeliverToast() {
        return HwCustMmsConfigImpl.getEnableSmsDeliverToast();
    }

    public boolean enableSmsNotifyInSilentMode() {
        return HwCustMmsConfigImpl.getEnableSmsNotifyInSilentMode();
    }

    private boolean isVibrationPatternAvailable() {
        return IS_VIBRATION_TYPE_ENABLED;
    }

    public boolean vibrate(Context context, Vibrator vibrator) {
        if (!isVibrationPatternAvailable()) {
            return Boolean.FALSE.booleanValue();
        }
        String vibrationType = Global.getString(context.getContentResolver(), KEY_VIBRATE_APP_MESSAGE);
        if (TextUtils.isEmpty(vibrationType)) {
            return Boolean.FALSE.booleanValue();
        }
        String pattern = Global.getString(context.getContentResolver(), vibrationType);
        if (TextUtils.isEmpty(pattern) || vibrator == null) {
            return Boolean.FALSE.booleanValue();
        }
        long[] result = getLongArray(pattern);
        if (result.length <= 0) {
            return Boolean.FALSE.booleanValue();
        }
        vibrator.vibrate(result, -1);
        return Boolean.TRUE.booleanValue();
    }

    public boolean setNotificationVibrate(Context context, Builder noti) {
        if (!isVibrationPatternAvailable()) {
            return Boolean.FALSE.booleanValue();
        }
        String vibrationType = Global.getString(context.getContentResolver(), KEY_VIBRATE_APP_MESSAGE);
        if (TextUtils.isEmpty(vibrationType)) {
            return Boolean.FALSE.booleanValue();
        }
        String pattern = Global.getString(context.getContentResolver(), vibrationType);
        if (TextUtils.isEmpty(pattern) || noti == null) {
            return Boolean.FALSE.booleanValue();
        }
        long[] result = getLongArray(pattern);
        if (result.length <= 0) {
            return Boolean.FALSE.booleanValue();
        }
        noti.setVibrate(result);
        return Boolean.TRUE.booleanValue();
    }

    private long[] getLongArray(String pattern) {
        String[] items = pattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        long[] results = new long[items.length];
        int i = 0;
        while (i < items.length) {
            try {
                results[i] = Long.parseLong(items[i]);
                i++;
            } catch (NumberFormatException e) {
                return new long[0];
            }
        }
        return results;
    }

    public boolean getDisableSmsDeliverToastByCard() {
        boolean result = false;
        try {
            String configStr = HwCustMmsConfigImpl.getDisableSmsDeliverToastByCard();
            if (TextUtils.isEmpty(configStr)) {
                return false;
            }
            String currentMccmnc;
            String[] mccMncList = configStr.split(",");
            if (MmsApp.getDefaultTelephonyManager().isMultiSimEnabled()) {
                currentMccmnc = MmsApp.getDefaultTelephonyManager().getSimOperator(SubscriptionManager.getDefaultSubscriptionId());
            } else {
                currentMccmnc = MmsApp.getDefaultTelephonyManager().getSimOperator();
            }
            if (!MccMncConfig.isValideOperator(currentMccmnc)) {
                return false;
            }
            int i = 0;
            while (i < mccMncList.length) {
                if (currentMccmnc.startsWith(mccMncList[i]) || currentMccmnc.equals(mccMncList[i])) {
                    result = true;
                    break;
                }
                i++;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
    }
}
