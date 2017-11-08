package com.huawei.keyguard.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.FingerprintNavigator;
import fyusion.vislib.BuildConfig;
import java.util.List;

public class FpUtils {
    public static final String TAG = GET_TAG("Utils");
    private static long mLastInputFingerTime = 0;
    private static long mLastStartWakeUpTime = 0;
    private static boolean sNeedSreenOn = false;

    static String GET_TAG(String suffix) {
        return "KG_FP." + suffix;
    }

    public static boolean isFingerprintEnabled(Context context, int userId) {
        boolean z = true;
        if (context == null) {
            HwLog.w(TAG, "Call isFingerprintEnabled with null context");
            return false;
        }
        try {
            if (Secure.getIntForUser(context.getContentResolver(), "fp_keyguard_enable", userId) != 1) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            HwLog.e(TAG, "get fp_keyguard_enable value :" + e);
            if (FingerprintNavigator.getInst().getFingerprintListSize(context) <= 0 || !HwKeyguardUpdateMonitor.getInstance(context).isSecure(userId)) {
                return false;
            }
            OsUtils.putSecureInt(context, "fp_keyguard_enable", 1);
            Log.d(TAG, "save the fp_keyguard_enable value when fp exists and the fp_keyguard_enable lost");
            return true;
        }
    }

    public static void vibrate(Context context, long duration) {
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return;
        }
        Vibrator vb = (Vibrator) context.getSystemService("vibrator");
        if (duration <= 0 || duration >= 500) {
            duration = 80;
        }
        if (vb.hasVibrator()) {
            vb.vibrate(duration);
        }
    }

    public static boolean isScreenOff(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return false;
        }
        PowerManager power = (PowerManager) context.getSystemService("power");
        if (power != null) {
            if (!power.isScreenOn()) {
                z = true;
            }
            return z;
        }
        HwLog.e(TAG, "PowerManager is null");
        return false;
    }

    public static boolean isScreenOn(Context context) {
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return false;
        }
        PowerManager power = (PowerManager) context.getSystemService("power");
        if (power != null) {
            return power.isScreenOn();
        }
        HwLog.e(TAG, "PowerManager is null");
        return false;
    }

    public static void turnOnScreen(Context context) {
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return;
        }
        HwLog.i(TAG, "turnOnScreen");
        PowerManager power = (PowerManager) context.getSystemService("power");
        if (power != null) {
            power.wakeUp(SystemClock.uptimeMillis());
        } else {
            HwLog.e(TAG, "powermanager is null");
        }
    }

    public static boolean isKeyguardLocked(Context context) {
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return false;
        }
        KeyguardManager keyguard = (KeyguardManager) context.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        HwLog.e(TAG, "keyguardmanager is null");
        return false;
    }

    public static boolean isFpNativigationAppForeground(Context context) {
        if (context == null) {
            HwLog.w(TAG, "input context is null");
            return false;
        }
        List<RunningTaskInfo> infos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (infos != null) {
            for (RunningTaskInfo i : infos) {
                ComponentName comp = i.topActivity;
                if (comp != null) {
                    String pkgName = comp.getPackageName();
                    String clsName = comp.getClassName();
                    if ("com.huawei.camera".equals(pkgName) && ("com.huawei.camera.controller.SecureCameraActivity".equals(clsName) || "com.huawei.camera".equals(clsName))) {
                        return true;
                    }
                    if (KeyguardCfg.isFrontFpNavigationSupport() && ("com.android.calculator2".equals(pkgName) || "com.android.soundrecorder".equals(pkgName) || "com.android.gallery3d".equals(pkgName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isTakePhotoByFingerEnabled(Context context) {
        return OsUtils.getSecureInt(context, "fp_take_photo", 1) == 1;
    }

    public static void setInFastScreenOn(boolean needScreenOn) {
        sNeedSreenOn = needScreenOn;
    }

    public static boolean isInFastScreenOn() {
        return sNeedSreenOn;
    }

    public static boolean isChangeTipsForAbnormalReboot(Context context) {
        if (OsUtils.isSupportMultiUser(context) || isSystemNormalReboot(context)) {
            return false;
        }
        return true;
    }

    private static boolean isSystemNormalReboot(Context context) {
        String resetType = SystemProperties.get("sys.resettype");
        if (resetType == null || resetType.isEmpty()) {
            return true;
        }
        int index = resetType.indexOf(":");
        if (index < 0) {
            index = 1;
        }
        String subString = resetType.substring(0, index);
        boolean isResetTypeRead = getResetTypeHasRead(context);
        HwLog.i(TAG, "getPropSystemReboot ,resetType=" + resetType + ",subString=" + subString + ",isResetTypeRead = " + isResetTypeRead);
        boolean result = false;
        if (!isResetTypeRead) {
            if ("normal".equalsIgnoreCase(subString)) {
                result = true;
            } else {
                setResetTypeHasRead(context, true);
            }
        }
        return result;
    }

    public static void setResetTypeHasRead(Context context, boolean isRead) {
        HwLog.d(TAG, "set reset_type has read = " + isRead);
        if (context != null) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean("reset_type_has_read", isRead);
            editor.commit();
        }
    }

    private static boolean getResetTypeHasRead(Context context) {
        if (context == null) {
            return false;
        }
        boolean isRead = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("reset_type_has_read", false);
        HwLog.d(TAG, "get reset_type has read = " + isRead);
        return isRead;
    }

    public static void doBlackStopWakeUpReady(Context context) {
        if (isInFastScreenOn()) {
            stopWakeUpReady(context, false);
        }
    }

    public static void fingerTurnOnScreen(Context context) {
        HwLog.i(TAG, "FpPerformance FpUtils.fingerTurnOnScreen():" + isInFastScreenOn());
        if (!isInFastScreenOn()) {
            turnOnScreen(context);
        } else if (System.currentTimeMillis() - mLastStartWakeUpTime >= 3000) {
            HwLog.i(TAG, "finger TurnOnScreen fast screen wait timeout");
            turnOnScreen(context);
            setInFastScreenOn(false);
        } else {
            stopWakeUpReady(context, true);
        }
    }

    private static void stopWakeUpReady(Context context, boolean turnOn) {
        PowerManager powerManager = null;
        if (context != null) {
            powerManager = (PowerManager) context.getSystemService("power");
        }
        if (powerManager == null) {
            setInFastScreenOn(false);
            return;
        }
        HwLog.w(TAG, turnOn ? "Start Fast finger turnon screen" : "Stop fast finger turnon screen");
        try {
            powerManager.getClass().getMethod("stopWakeUpReady", new Class[]{Long.TYPE, Boolean.TYPE}).invoke(powerManager, new Object[]{Long.valueOf(SystemClock.uptimeMillis()), Boolean.valueOf(turnOn)});
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: System hasn't stopWakeUpReady method " + e);
        } catch (IllegalArgumentException e2) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: stopWakeUpReady method has wrong parameter " + e2);
        } catch (Exception e3) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: other reflect exception " + e3);
        }
        setInFastScreenOn(false);
    }

    public static void startWakeUpReady(Context context) {
        PowerManager powerManager = null;
        if (context != null) {
            powerManager = (PowerManager) context.getSystemService("power");
        }
        if (powerManager == null) {
            HwLog.w(TAG, "powermanager is null");
            return;
        }
        HwLog.w(TAG, "Fast TurnonScreen start");
        try {
            powerManager.getClass().getMethod("startWakeUpReady", new Class[]{Long.TYPE}).invoke(powerManager, new Object[]{Long.valueOf(SystemClock.uptimeMillis())});
            setInFastScreenOn(true);
            mLastStartWakeUpTime = System.currentTimeMillis();
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: System hasn't startWakeUpReady method " + e);
        } catch (IllegalArgumentException e2) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: startWakeUpReady method has wrong parameter " + e2);
        } catch (Exception e3) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: other reflect exception " + e3);
        }
    }

    public static void setAuthSucceeded(Context context) {
        PowerManager powerManager = null;
        if (context != null) {
            powerManager = (PowerManager) context.getSystemService("power");
        }
        if (powerManager == null) {
            HwLog.w(TAG, "powermanager is null");
            return;
        }
        try {
            powerManager.getClass().getMethod("setAuthSucceeded", new Class[0]).invoke(powerManager, new Object[0]);
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: System hasn't setAuthSucceeded method " + e);
        } catch (IllegalArgumentException e2) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: setAuthSucceeded method has wrong parameter " + e2);
        } catch (Exception e3) {
            HwLog.e(TAG, "LockConfig::PowerManager Value: other reflect exception " + e3);
        }
        HwLog.d(TAG, "setAuthSucceeded");
    }

    public static void doReporterWhenUnlockFingerprintSucceed(Context context, boolean isScreenOff) {
        HwLockScreenReporter.report(context, 127, BuildConfig.FLAVOR);
        long fingerUnlockTime = System.currentTimeMillis() - mLastInputFingerTime;
        if (2000 > fingerUnlockTime && 0 < fingerUnlockTime) {
            HwLockScreenReporter.report(context, 128, "{unlock_time:" + fingerUnlockTime + ", isScreenOff:" + isScreenOff + "}");
        }
        mLastInputFingerTime = 0;
    }

    public static void setLastInputFingerTime() {
        mLastInputFingerTime = System.currentTimeMillis();
    }
}
