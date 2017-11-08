package com.android.settings;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.huawei.android.app.HwSdLockManager;
import java.util.HashMap;

public class SdCardLockUtils {
    private static HwSdLockManager sHwSdLockManager;

    public static HwSdLockManager getHwSdLockManager(Context context) {
        if (sHwSdLockManager == null && context != null) {
            try {
                sHwSdLockManager = new HwSdLockManager(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sHwSdLockManager;
    }

    public static boolean isFeatureSupported(Context context) {
        return getHwSdLockManager(context) != null;
    }

    public static boolean isSdCardPresent(Context context) {
        boolean z = true;
        if (!isFeatureSupported(context)) {
            return false;
        }
        int state = sHwSdLockManager.getSDLockState();
        if (!(671 == state || 670 == state || 639 == state)) {
            z = false;
        }
        return z;
    }

    public static boolean isSdCardUnlocked(Context context) {
        boolean z = false;
        if (!isFeatureSupported(context)) {
            return false;
        }
        if (671 == sHwSdLockManager.getSDLockState()) {
            z = true;
        }
        return z;
    }

    public static boolean isPasswordProtected(Context context) {
        boolean z = true;
        if (!isFeatureSupported(context)) {
            return false;
        }
        int state = sHwSdLockManager.getSDLockState();
        if (!(671 == state || 670 == state)) {
            z = false;
        }
        return z;
    }

    public static boolean isSdCardBusy(Context context) {
        boolean z = false;
        if (!isFeatureSupported(context)) {
            return false;
        }
        if (645 == sHwSdLockManager.getSDLockState()) {
            z = true;
        }
        return z;
    }

    public static boolean setSDLockPassword(Context context, String password) {
        if (isFeatureSupported(context)) {
            int state = sHwSdLockManager.getSDLockState();
            int responseCode = sHwSdLockManager.setSDLockPassword(password);
            if (responseCode == 200) {
                if (671 == state || 670 == state) {
                    Toast.makeText(context, context.getString(2131628192), 1).show();
                }
                return true;
            }
            if (671 == state || 670 == state) {
                Toast.makeText(context, context.getString(2131628191), 1).show();
            } else {
                Toast.makeText(context, context.getString(2131628190), 1).show();
            }
            HashMap<Short, Object> map = new HashMap();
            map.put(Short.valueOf((short) 0), Integer.valueOf(responseCode));
            map.put(Short.valueOf((short) 1), Integer.valueOf(state));
            RadarReporter.reportRadar(907018009, map);
        }
        return false;
    }

    public static boolean unlockSDCard(Context context, String password) {
        if (isFeatureSupported(context)) {
            int responseCode = sHwSdLockManager.unlockSDCard(password);
            if (responseCode == 200) {
                return true;
            }
            HashMap<Short, Object> map = new HashMap();
            map.put(Short.valueOf((short) 0), Integer.valueOf(responseCode));
            RadarReporter.reportRadar(907018011, map);
        }
        return false;
    }

    public static boolean clearSDLockPassword(Context context) {
        if (isFeatureSupported(context)) {
            int responseCode = sHwSdLockManager.clearSDLockPassword();
            if (responseCode == 200) {
                return true;
            }
            Toast.makeText(context, context.getString(2131628194), 1).show();
            HashMap<Short, Object> map = new HashMap();
            map.put(Short.valueOf((short) 0), Integer.valueOf(responseCode));
            RadarReporter.reportRadar(907018010, map);
        }
        return true;
    }

    public static void eraseSDLock(Context context) {
        if (!Utils.isMonkeyRunning() && isFeatureSupported(context)) {
            new TimeKeeperAdapter(context, "sdcard_lock_" + getCurrentSDCardId(context), true).resetErrorCount(context);
            Log.d("SdCardLockUtils", "eraseSDLock try to reset error count");
            sHwSdLockManager.eraseSDLock();
        }
    }

    public static String getCurrentSDCardId(Context context) {
        if (isFeatureSupported(context)) {
            return sHwSdLockManager.getSDCardId();
        }
        return null;
    }
}
