package com.huawei.systemmanager.power.data.devstatus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import com.android.internal.view.RotationPolicy;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class DevStatusUtil {
    public static final int LOCATION_MODE_BATTERY_SAVING = 2;
    public static final int LOCATION_MODE_HIGH_ACCURACY = 3;
    public static final int LOCATION_MODE_OFF = 0;
    public static final int LOCATION_MODE_SENSORS_ONLY = 1;
    private static final String TAG = DevStatusUtil.class.getSimpleName();
    private static final int TOUCH_FEEDBACK_OFF = 0;
    private static final int TOUCH_FEEDBACK_ON = 1;

    public static void setMobileDataState(Context ctx, boolean flag) {
        ((TelephonyManager) ctx.getSystemService("phone")).setDataEnabled(flag);
    }

    public static boolean getMobileDataState(Context ctx) {
        TelephonyManager telemamanger = (TelephonyManager) ctx.getSystemService("phone");
        boolean z = false;
        try {
            z = telemamanger.getDataEnabled();
        } catch (Exception e) {
            HwLog.e(TAG, "GetMobileDataState error!");
        }
        if (isAirModeOn(ctx)) {
            z = false;
        }
        if (telemamanger.getSimState() != 5) {
            return false;
        }
        return z;
    }

    public static int getScreenBrightnessState(Context ctx) {
        int brightnessValue = 0;
        try {
            brightnessValue = System.getInt(ctx.getContentResolver(), "screen_brightness");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightnessValue;
    }

    public static int getScreenBrightnessPercent(Context ctx, int state) {
        PowerManager pm = (PowerManager) ctx.getSystemService(BatteryStatisticsHelper.DB_POWER);
        int minimum = pm.getMinimumScreenBrightnessSetting();
        int maximum = pm.getMaximumScreenBrightnessSetting();
        if (state == 255) {
            return 100;
        }
        int percent = (int) Math.round(Math.pow((double) ((state - minimum) * (maximum - minimum)), 0.425531914893617d));
        if (percent < 2) {
            percent = 2;
        }
        if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    public static void setScreenAutoBrightnessState(Context ctx, boolean flag) {
        int i;
        ContentResolver contentResolver = ctx.getContentResolver();
        String str = "screen_brightness_mode";
        if (flag) {
            i = 1;
        } else {
            i = 0;
        }
        System.putInt(contentResolver, str, i);
    }

    public static boolean isBrightnessAutoOptimizeState(Context ctx) {
        return getScreenAutoBrightnessState(ctx);
    }

    public static boolean getScreenAutoBrightnessState(Context ctx) {
        if (1 == getBrightnessMode(ctx, 0)) {
            return true;
        }
        return false;
    }

    public static boolean isMobileDateOptimzeState(Context ctx) {
        return !getMobileDataState(ctx);
    }

    public static int getBrightnessMode(Context ctx, int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = System.getInt(ctx.getContentResolver(), "screen_brightness_mode");
        } catch (SettingNotFoundException snfe) {
            snfe.printStackTrace();
        }
        return brightnessMode;
    }

    public static boolean isBrightnessOptimzeState(Context ctx) {
        int brightness = getScreenBrightnessState(ctx);
        if (isBrightnessAutoOptimizeState(ctx) || getScreenBrightnessPercent(ctx, brightness) <= 50) {
            return true;
        }
        return false;
    }

    public static void setScreenTimeoutState(Context ctx, long flag) {
        System.putLong(ctx.getContentResolver(), "screen_off_timeout", flag);
    }

    public static long getScreenTimeoutState(Context ctx) {
        return System.getLong(ctx.getContentResolver(), "screen_off_timeout", 30000);
    }

    public static boolean isScreenTimeoutOptimzeState(Context ctx) {
        if (getScreenTimeoutState(ctx) == 15000) {
            return true;
        }
        return false;
    }

    public static boolean getSoundState(Context ctx) {
        int ringerMode = ((AudioManager) ctx.getSystemService("audio")).getRingerMode();
        if (ringerMode == 0) {
            return false;
        }
        return ringerMode == 2 ? true : true;
    }

    public static void setVibrateState(Context ctx, boolean flag) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService("audio");
        if (flag) {
            audioManager.setRingerMode(1);
        } else {
            audioManager.setRingerMode(2);
        }
    }

    public static boolean getVibrateState(Context ctx) {
        int ringerMode = ((AudioManager) ctx.getSystemService("audio")).getRingerMode();
        if (ringerMode == 1) {
            return true;
        }
        return ringerMode == 2 ? false : false;
    }

    public static boolean isVibrateOptimzeState(Context ctx) {
        return !getVibrateState(ctx);
    }

    public static void setGpsState(Context ctx, boolean flag) {
        if (!flag) {
            Secure.putInt(ctx.getContentResolver(), "location_mode", 0);
        }
    }

    public static boolean setGpsState(Context ctx, int GpsState) {
        return Secure.putInt(ctx.getContentResolver(), "location_mode", GpsState);
    }

    public static boolean getGpsState(Context ctx) {
        if (Secure.getInt(ctx.getContentResolver(), "location_mode", 0) != 0) {
            return true;
        }
        return false;
    }

    public static int getGpsDetailState(Context ctx) {
        return Secure.getInt(ctx.getContentResolver(), "location_mode", 0);
    }

    public static boolean isGpsOptimzeState(Context ctx) {
        int gpsState = getGpsDetailState(ctx);
        if (gpsState == 0 || gpsState == 2) {
            return true;
        }
        return false;
    }

    public static boolean getAirplaneModeState(Context ctx) {
        try {
            if (System.getInt(ctx.getContentResolver(), "airplane_mode_on") == 1) {
                return true;
            }
            return false;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setBluetoothState(boolean flag) {
        if (flag) {
            BluetoothAdapter.getDefaultAdapter().enable();
        } else {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    public static boolean getBluetoothState() {
        boolean z = true;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        int state = adapter.getState();
        if (!(12 == state || 11 == state)) {
            z = false;
        }
        return z;
    }

    public static boolean isBLEConnected() {
        BluetoothManager mBluetoothManager = new BluetoothManager(GlobalContext.getContext());
        return mBluetoothManager.getDevicesMatchingConnectionStates(7, new int[]{2, 1}).size() > 0 || mBluetoothManager.getDevicesMatchingConnectionStates(8, new int[]{2, 1}).size() > 0;
    }

    public static boolean isBlueToothStateOptimize() {
        Set<BluetoothDevice> set = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (getBluetoothState()) {
            return (set != null && set.size() > 0) || isBLEConnected();
        } else {
            return true;
        }
    }

    public static void setAutoSyncState(boolean flag) {
        ContentResolver.setMasterSyncAutomatically(flag);
    }

    public static boolean getAutoSyncState() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    public static boolean isAutoSyncOptimizeState() {
        return !getAutoSyncState();
    }

    public static void setAutoRotateState(Context ctx, boolean flag) {
        RotationPolicy.setRotationLock(ctx.getApplicationContext(), !flag);
    }

    public static boolean getAutoRotateState(Context ctx) {
        return !RotationPolicy.isRotationLocked(ctx.getApplicationContext());
    }

    public static boolean isAutoRotateOptimzeState(Context ctx) {
        return !getAutoRotateState(ctx);
    }

    public static void setTouchFeedbackState(Context ctx, boolean flag) {
        if (flag) {
            System.putInt(ctx.getContentResolver(), "haptic_feedback_enabled", 1);
        } else {
            System.putInt(ctx.getContentResolver(), "haptic_feedback_enabled", 0);
        }
    }

    public static boolean getTouchFeedbackState(Context ctx) {
        if (1 == System.getInt(ctx.getContentResolver(), "haptic_feedback_enabled", 0)) {
            return true;
        }
        return false;
    }

    public static boolean isTouchFeedbackOptimzeState(Context ctx) {
        return !getTouchFeedbackState(ctx);
    }

    public static void setScreenBrightnessState(Context ctx, int flag) {
        if (1 != getBrightnessMode(ctx, 0)) {
            PowerManager power = (PowerManager) ctx.getSystemService(BatteryStatisticsHelper.DB_POWER);
            if (power != null && power.isScreenOn()) {
                power.setBacklightBrightness(flag);
            }
            System.putInt(ctx.getContentResolver(), "screen_brightness", flag);
        }
    }

    public static void setWifiState(Context ctx, boolean flag) {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService("wifi");
        if (flag) {
            wifiMgr.setWifiApEnabled(null, false);
        }
        wifiMgr.setWifiEnabled(flag);
    }

    public static boolean getWifiState(Context ctx) {
        int wifiState = ((WifiManager) ctx.getSystemService("wifi")).getWifiState();
        if (wifiState == 3 || wifiState == 2) {
            return true;
        }
        return false;
    }

    public static boolean isWlanStateOptimize(Context ctx) {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService("wifi");
        boolean flag = getWifiState(ctx);
        WifiInfo connInfo = wifiMgr.getConnectionInfo();
        int ipAddress = 0;
        if (connInfo != null) {
            ipAddress = connInfo.getIpAddress();
        }
        if (flag && ipAddress == 0) {
            return false;
        }
        return true;
    }

    public static boolean isAirModeOn(Context context) {
        return Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1;
    }
}
