package com.huawei.powergenie.modules.devicepower;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager.Stub;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;

public final class SwitcherControl {
    private static boolean mIsSupportSmartBacklight;
    private ContentResolver mContentResolver;
    private Context mContext;
    private ICoreContext mICoreContext;
    private IDeviceState mIDeviceState;
    private WifiManager mWifiManager = null;

    static {
        boolean z;
        if (System.getUriFor("smart_backlight_enable") != null) {
            z = true;
        } else {
            z = false;
        }
        mIsSupportSmartBacklight = z;
    }

    public SwitcherControl(Context context, ICoreContext coreContext) {
        this.mContext = context;
        this.mICoreContext = coreContext;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    protected void setBluetoothSwitcher(boolean enable) {
        BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (localBluetoothAdapter != null) {
            boolean state = localBluetoothAdapter.isEnabled();
            if (enable && !state) {
                Log.i("SwitcherControl", "Enable bluetooth switcher.");
                localBluetoothAdapter.enable();
            }
            if (!enable && state) {
                Log.i("SwitcherControl", "Disable bluetooth switcher.");
                localBluetoothAdapter.disable();
            }
        }
    }

    protected boolean getBluetoothSwitcher() {
        BluetoothAdapter localBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (localBluetoothAdapter != null) {
            return localBluetoothAdapter.isEnabled();
        }
        return false;
    }

    protected void setWiFiSwitcher(boolean enable) {
        if (enable) {
            int wifiApState = this.mWifiManager.getWifiApState();
            if (wifiApState == 13 || wifiApState == 12) {
                Log.i("SwitcherControl", "WiFi AP is enable, no need to enable WiFi.");
                return;
            }
        }
        Log.i("SwitcherControl", enable ? "Enable " : "Disable wifi switcher.");
        this.mWifiManager.setWifiEnabled(enable);
    }

    protected boolean getWiFiSwitcher() {
        return this.mWifiManager.isWifiEnabled();
    }

    protected void setAutoSyncSwitcher(boolean enable) {
        String str;
        if (enable) {
            if (!ContentResolver.getMasterSyncAutomatically()) {
                ContentResolver.setMasterSyncAutomatically(true);
            }
        } else if (ContentResolver.getMasterSyncAutomatically()) {
            ContentResolver.setMasterSyncAutomatically(false);
        }
        String str2 = "SwitcherControl";
        if (enable) {
            str = "Enable ";
        } else {
            str = "Disable auto sync switcher.";
        }
        Log.i(str2, str);
    }

    protected boolean getAutoSyncSwitcher() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    protected void setGPSSwitcher(boolean enable) {
        if (enable != getGPSSwitcher()) {
            Log.i("SwitcherControl", enable ? "Enable " : "Disable gps switcher.");
            Secure.putInt(this.mContentResolver, "location_mode", enable ? -1 : 0);
            return;
        }
        Log.i("SwitcherControl", "gps the same as current state.");
    }

    protected boolean getGPSSwitcher() {
        if (Secure.getInt(this.mContentResolver, "location_mode", 0) != 0) {
            return true;
        }
        return false;
    }

    protected void setDataServiceEnabled(boolean enable) {
        if (enable != getDataServiceSwither()) {
            Log.i("SwitcherControl", enable ? "Enable " : "Disable data service switcher.");
            this.mIDeviceState.setMobileDataEnabled(enable);
        }
    }

    protected boolean getDataServiceSwither() {
        return ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getMobileDataEnabled();
    }

    protected void setScreenTimeout(int millsTimeout) {
        System.putInt(this.mContentResolver, "screen_off_timeout", millsTimeout);
    }

    protected int getScreenTimeout() {
        return System.getInt(this.mContentResolver, "screen_off_timeout", 15000);
    }

    protected void setWindowAutoRotation(int enable) {
        boolean isOffAutoRotation = isRotationLocked(this.mContext);
        if (enable == 0 && !isOffAutoRotation) {
            System.putInt(this.mContentResolver, "accelerometer_rotation", 0);
        } else if (enable == 1 && isOffAutoRotation) {
            System.putInt(this.mContentResolver, "accelerometer_rotation", 1);
        }
    }

    private static boolean isRotationLocked(Context context) {
        return System.getIntForUser(context.getContentResolver(), "accelerometer_rotation", 0, -2) == 0;
    }

    protected int getWindowAutoRotation() {
        return System.getInt(this.mContentResolver, "accelerometer_rotation", 0);
    }

    protected void setBrightnessMode(int brightnessMode) {
        System.putInt(this.mContentResolver, "screen_brightness_mode", brightnessMode);
        Intent intent = new Intent("com.android.huawei.BRIGHTNESS_ACTION_SETTING_CHANGED");
        intent.setComponent(new ComponentName("com.huawei.android.toolbox", "com.huawei.android.toolbox.ToolBoxProvider"));
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    protected int getBrightnessMode() {
        return System.getInt(this.mContentResolver, "screen_brightness_mode", 0);
    }

    protected void setHapticFeedback(int hapticFeedback) {
        System.putInt(this.mContentResolver, "haptic_feedback_enabled", hapticFeedback);
    }

    protected int getHapticFeedback() {
        return System.getInt(this.mContentResolver, "haptic_feedback_enabled", 0);
    }

    protected void setTouchSounds(int touchsounds) {
        System.putInt(this.mContentResolver, "sound_effects_enabled", touchsounds);
    }

    protected int getTouchSounds() {
        return System.getInt(this.mContentResolver, "sound_effects_enabled", 0);
    }

    protected void setLockScreenSounds(int lockScreenSounds) {
        System.putInt(this.mContentResolver, "lockscreen_sounds_enabled", lockScreenSounds);
    }

    protected int getLockScreenSounds() {
        return System.getInt(this.mContentResolver, "lockscreen_sounds_enabled", 0);
    }

    protected void setDialingSounds(int dialingSounds) {
        System.putInt(this.mContentResolver, "dtmf_tone", dialingSounds);
    }

    protected int getDialingSounds() {
        return System.getInt(this.mContentResolver, "dtmf_tone", 0);
    }

    protected void setWindowAnimation(int which, float scale) {
        try {
            Stub.asInterface(ServiceManager.getService("window")).setAnimationScale(which, scale);
        } catch (RemoteException e) {
            Log.e("SwitcherControl", "get mWindowManager manager error ", e);
        }
    }

    protected float getWindowAnimation(int which) {
        try {
            return Stub.asInterface(ServiceManager.getService("window")).getAnimationScale(which);
        } catch (RemoteException e) {
            Log.e("SwitcherControl", "get mWindowManager manager error ", e);
            return -1.0f;
        }
    }

    protected int getFloatWindow() {
        try {
            return this.mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.FloatTasksContentProvider"), "get", null, null).getInt("float_task_state");
        } catch (Exception e) {
            Log.w("SwitcherControl", "can't get float button state !");
            return -1;
        }
    }

    protected void setFloatWindow(boolean enable) {
        try {
            this.mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.FloatTasksContentProvider"), "set", enable ? "1" : "0", null);
        } catch (Exception e) {
            Log.w("SwitcherControl", "can't set float button state !");
        }
    }

    protected void setSmartBacklight(int value) {
        if (mIsSupportSmartBacklight) {
            System.putInt(this.mContentResolver, "smart_backlight_enable", value);
        }
    }

    protected int getSmartBacklight() {
        if (mIsSupportSmartBacklight) {
            return System.getInt(this.mContentResolver, "smart_backlight_enable", 0);
        }
        return -1;
    }

    protected int getLCDBrightness() {
        return System.getInt(this.mContentResolver, "screen_brightness", 35);
    }

    protected int getMFlipSilent() {
        return getMotionEnableState(this.mContext, "motion_flip_silent");
    }

    protected void setMFlipSilent(boolean on) {
        int newEnable = on ? 1 : 0;
        int oldEnable = getMFlipSilent();
        Log.i("SwitcherControl", "setMFlipSilent " + oldEnable + " -> " + newEnable);
        if (-1 != oldEnable && oldEnable != newEnable) {
            setMotionEnableState(this.mContext, "motion_flip_silent", newEnable);
        }
    }

    private void setMotionEnableState(Context context, String motionitemkey, int enable) {
        System.putInt(context.getContentResolver(), motionitemkey, enable);
    }

    private int getMotionEnableState(Context context, String motionitemkey) {
        int enabled = System.getInt(context.getContentResolver(), motionitemkey, -1);
        if (enabled != -1 || !isMotionServiceRunning(context)) {
            return enabled;
        }
        Log.i("SwitcherControl", "motion service running and adjust the flip key enable as 1");
        return 1;
    }

    private boolean isMotionServiceRunning(Context context) {
        for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (service.process.equals("com.huawei.motionservice")) {
                return true;
            }
        }
        return false;
    }

    protected int getNetworkMode() {
        return this.mIDeviceState.getNetworkMode();
    }

    protected boolean setNetworkMode(int lteMode) {
        boolean z = true;
        if (this.mIDeviceState.isCalling()) {
            Log.i("SwitcherControl", "Call busy, cannot set net mode:" + lteMode);
            return true;
        } else if (lteMode < 0) {
            Log.w("SwitcherControl", "setNetworkMode, can't set the value :" + lteMode);
            return true;
        } else {
            this.mIDeviceState.setNetworkMode(lteMode);
            if (lteMode != 0) {
                z = false;
            }
            return z;
        }
    }

    protected void setWifiApSwitcher(boolean enable) {
        if (enable != this.mWifiManager.isWifiApEnabled()) {
            this.mWifiManager.setWifiApEnabled(null, enable);
        }
    }

    protected boolean getWifiApSwitcher() {
        return this.mWifiManager.isWifiApEnabled();
    }

    protected void setNfc(boolean enable) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter != null) {
            Log.i("SwitcherControl", "set nfc enable = " + enable);
            if (enable) {
                nfcAdapter.enable();
            } else {
                nfcAdapter.disable();
            }
        }
    }

    protected int getNfcState() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter == null) {
            return -1;
        }
        int nfcState = nfcAdapter.getAdapterState();
        if (3 == nfcState || 2 == nfcState) {
            return 1;
        }
        if (1 == nfcState || 4 == nfcState) {
            return 0;
        }
        return -1;
    }

    protected void setSIM1RingVibrate(int val) {
        Log.i("SwitcherControl", "set sim1 ring vibrate val = " + val);
        System.putInt(this.mContentResolver, "vibrate_when_ringing", val);
    }

    protected int getSIM1RingVibrateState() {
        return System.getInt(this.mContentResolver, "vibrate_when_ringing", -1);
    }

    protected void setSIM2RingVibrate(int val) {
        Log.i("SwitcherControl", "set sim2 ring vibrate val = " + val);
        try {
            System.putInt(this.mContentResolver, "vibrate_when_ringing2", val);
        } catch (Exception e) {
            Log.w("SwitcherControl", "setSIM2RingVibrate Exception !");
        }
    }

    protected int getSIM2RingVibrateState() {
        int ret = -1;
        try {
            ret = System.getInt(this.mContentResolver, "vibrate_when_ringing2", 1);
        } catch (Exception e) {
            Log.w("SwitcherControl", "getSIM2RingVibrateState Exception !");
        }
        return ret;
    }
}
