package com.android.deskclock.alarmclock;

import android.content.Context;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.text.TextUtils;

public class HwCustAlarmKlaxonImpl extends HwCustAlarmKlaxon {
    private static final boolean IS_VIBRATION_TYPE_ENABLED = SystemProperties.getBoolean("ro.config.hw_vibration_type", IS_VIBRATION_TYPE_ENABLED);
    protected static final String KEY_VIBRATE_APP_ALARM = "vibrate_app_alarm";

    private boolean isVibrationPatternAvailable() {
        return IS_VIBRATION_TYPE_ENABLED;
    }

    public boolean vibrate(Context context, Vibrator vibrator) {
        if (!isVibrationPatternAvailable()) {
            return Boolean.FALSE.booleanValue();
        }
        String vibrationType = Global.getString(context.getContentResolver(), KEY_VIBRATE_APP_ALARM);
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
        vibrator.vibrate(result, 0);
        return Boolean.TRUE.booleanValue();
    }

    private long[] getLongArray(String pattern) {
        String[] items = pattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        long[] result = new long[items.length];
        int i = 0;
        while (i < items.length) {
            try {
                result[i] = Long.parseLong(items[i]);
                i++;
            } catch (NumberFormatException e) {
                return new long[0];
            }
        }
        return result;
    }
}
