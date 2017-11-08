package com.huawei.keyguard.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.Build;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class HwUnlockUtils {
    private static final HashMap<String, Integer> GRAVITY_MAP = new HashMap();
    private static final HashMap<String, Integer> TYPE_MAP = new HashMap();
    private static String sChargingType = "text";
    private static Point sDisplayPoint = new Point(-1, -1);
    private static String sMusicTextType = "multi";

    static {
        GRAVITY_MAP.put("center", Integer.valueOf(17));
        GRAVITY_MAP.put("left", Integer.valueOf(8388611));
        GRAVITY_MAP.put("right", Integer.valueOf(8388613));
        TYPE_MAP.put("bold", Integer.valueOf(1));
        TYPE_MAP.put("normal", Integer.valueOf(0));
        TYPE_MAP.put("italic", Integer.valueOf(2));
    }

    public static int getGravity(String name) {
        return ((Integer) GRAVITY_MAP.get(name)).intValue();
    }

    public static int getTypeface(String name) {
        return ((Integer) TYPE_MAP.get(name)).intValue();
    }

    public static boolean isTablet() {
        return SystemProperties.getBoolean("lockscreen.rot_override", false);
    }

    public static boolean isKeyguardAudioVideoEnable() {
        return SystemProperties.getBoolean("ro.config.keyguard_audio_video", false);
    }

    public static Intent getCameraIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName("com.huawei.camera", "com.huawei.camera.controller.CameraActivity");
        intent.setFlags(805306368);
        return intent;
    }

    public static boolean isUnlockIntent(Intent intent) {
        if (intent == null) {
            HwLog.i("Utils", "null == intent");
            return false;
        }
        HwLog.i("Utils", "intent = " + intent.getAction());
        if ("com.huawei.intent.action.LOCKSCREEN_SET".equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    public static void setChargingType(String chargingType) {
        if ("number".equalsIgnoreCase(chargingType)) {
            sChargingType = "number";
        } else if ("percent".equalsIgnoreCase(chargingType)) {
            sChargingType = "percent";
        } else {
            sChargingType = "text";
        }
    }

    public static String getChargingType() {
        return sChargingType;
    }

    public static void setMusicTextType(String musicTextType) {
        if ("single".equalsIgnoreCase(musicTextType)) {
            sMusicTextType = "single";
        } else {
            sMusicTextType = "multi";
        }
    }

    public static String getMusicTextType() {
        return sMusicTextType;
    }

    public static void vibrate(Context context) {
        if (context == null) {
            HwLog.d("HwLockScreenUtils", "vibrate context is null");
            return;
        }
        if (1 == OsUtils.getSystemInt(context, "haptic_feedback_enabled", 1)) {
            HwLog.d("HwLockScreenUtils", "Settings.System.HAPTIC_FEEDBACK_ENABLED:  1");
            ((Vibrator) context.getSystemService("vibrator")).vibrate(30);
        }
    }

    public static boolean isDualClockEnabled(Context context) {
        boolean z = true;
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "isDualClockEnabled context is null");
            return false;
        } else if (HwKeyguardUpdateMonitor.getInstance(context).isRestrictAsEncrypt()) {
            HwLog.w("HwLockScreenUtils", "isDualClockEnabled isRestrictAsEncrypt");
            return false;
        } else {
            try {
                if (OsUtils.getSystemInt(context, "dual_clocks", 1) != 1) {
                    z = false;
                }
                return z;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static Boolean isStepCounterEnabled(Context context) {
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "isStepCounterEnabled context is null");
            return Boolean.valueOf(false);
        }
        boolean bHaveStepCounter;
        if (hasPackageInfo(context.getPackageManager(), "com.huawei.health")) {
            bHaveStepCounter = true;
        } else {
            bHaveStepCounter = hasPackageInfo(context.getPackageManager(), "com.huawei.bone");
        }
        boolean bSettingsEnable = 1 == Global.getInt(context.getContentResolver(), "pedemeter_enabled", 1);
        if (!bHaveStepCounter) {
            bSettingsEnable = false;
        }
        return Boolean.valueOf(bSettingsEnable);
    }

    public static boolean hasPackageInfo(PackageManager manager, String pkgName) {
        try {
            manager.getPackageInfo(pkgName, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isSupportOrientation() {
        return SystemProperties.getBoolean("lockscreen.rot_override", false);
    }

    public static boolean isLandscape(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "isLandscape failed context is null");
            return false;
        }
        if (isSupportOrientation() && context.getResources().getConfiguration().orientation == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isSecure(Context context) {
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "isSecure context is null");
            return false;
        }
        KeyguardManager km = (KeyguardManager) context.getSystemService("keyguard");
        if (km != null) {
            return km.isKeyguardSecure();
        }
        HwLog.w("HwLockScreenUtils", "isSecure keyguardManager is null");
        return false;
    }

    public static Point resetPoint(Context context) {
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "resetPoint context is null");
            return null;
        }
        Point pointBlocked;
        synchronized (HwUnlockUtils.class) {
            if (sDisplayPoint.x > 0 && sDisplayPoint.y > 0) {
                if (sDisplayPoint.x > sDisplayPoint.y) {
                }
                pointBlocked = getPointBlocked(context);
            }
            HwLog.w("HwLockScreenUtils", "resetPoint: " + sDisplayPoint.x + " - " + sDisplayPoint.y);
            sDisplayPoint = new Point(-1, -1);
            pointBlocked = getPointBlocked(context);
        }
        return pointBlocked;
    }

    public static Point getPoint(Context context) {
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "getPoint context is null");
            return null;
        }
        Point pointBlocked;
        synchronized (HwUnlockUtils.class) {
            pointBlocked = getPointBlocked(context);
        }
        return pointBlocked;
    }

    private static Point getPointBlocked(Context context) {
        boolean useCache = true;
        int orientation = context.getResources().getConfiguration().orientation;
        if (!(orientation == 1 || orientation == 2)) {
            useCache = false;
        }
        Point curPoint = useCache ? sDisplayPoint : new Point();
        if (sDisplayPoint.x <= 0 || !useCache) {
            WindowManager wm = (WindowManager) context.getSystemService("window");
            Display display = wm == null ? null : wm.getDefaultDisplay();
            if (display == null) {
                return null;
            }
            display.getRealSize(curPoint);
            if (orientation == 2) {
                int lx = sDisplayPoint.x;
                sDisplayPoint.x = curPoint.y;
                sDisplayPoint.y = lx;
            }
        }
        if (orientation == 2) {
            return new Point(sDisplayPoint.y, sDisplayPoint.x);
        }
        if (!useCache) {
            HwLog.d("HwLockScreenUtils", "getPoint with special oritation " + curPoint);
        }
        return curPoint;
    }

    public static float getRealXdpi(Context context) {
        if (context == null) {
            HwLog.w("HwLockScreenUtils", "getRealXdpi context is null");
            return -1.0f;
        }
        String deviceName = getDeviceName();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        if (windowManager == null) {
            HwLog.w("HwLockScreenUtils", "getRealXdpi windowManager is null");
            return -1.0f;
        }
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        if (deviceName == null || deviceName.indexOf("PE-") <= -1) {
            return displayMetrics.xdpi;
        }
        return 400.0f;
    }

    public static String getDeviceName() {
        String deviceName = Build.MODEL;
        try {
            deviceName = URLEncoder.encode(deviceName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return deviceName;
    }
}
