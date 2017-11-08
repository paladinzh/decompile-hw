package com.android.settings.pressure.util;

import android.content.Context;
import android.util.Log;
import com.huawei.android.hardware.toucforce.TouchForceManager;

public class PressureUtil {
    public static boolean isSupportPressureHabit(Context c) {
        boolean isSupport = false;
        if (c == null) {
            return false;
        }
        try {
            isSupport = new TouchForceManager(c).isSupportForce();
        } catch (Exception e) {
        }
        return isSupport;
    }

    public static int pressureToLevel(float pressure) {
        if (pressure <= 0.13f) {
            return 1;
        }
        if (pressure > 0.13f && pressure <= 0.18f) {
            return 2;
        }
        if (pressure > 0.18f && pressure <= 0.28f) {
            return 3;
        }
        if (pressure <= 0.28f || pressure > 0.38f) {
            return 5;
        }
        return 4;
    }

    public static float levelToPressure(int level) {
        if (level == 1) {
            return 0.1f;
        }
        if (level == 2) {
            return 0.15f;
        }
        if (level == 3) {
            return 0.25f;
        }
        if (level == 4) {
            return 0.35f;
        }
        return 0.4f;
    }

    public static float getFirstGradePressureLimit(Context context) {
        float limit = 0.1f;
        try {
            Class classz = Class.forName("com.huawei.android.hardware.toucforce.TouchForceManager");
            limit = ((Float) classz.getDeclaredMethod("getPressureLimit", null).invoke(new TouchForceManager(context), null)).floatValue();
            Log.d("PressureUtil", " First grade pressure is successfully get, value is: " + limit);
            return limit;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Log.e("PressureUtil", ": Reflection exception: " + ex.getMessage());
            return limit;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            Log.e("PressureUtil", "Other exception: " + ex2.getMessage());
            return limit;
        }
    }

    public static float getSecondGradePressureLimit(Context context) {
        float limit = 0.5f;
        try {
            Class classz = Class.forName("com.huawei.android.hardware.toucforce.TouchForceManager");
            limit = ((Float) classz.getDeclaredMethod("getPressureLimitSec", null).invoke(new TouchForceManager(context), null)).floatValue();
            Log.d("PressureUtil", " Second grade pressure is successfully get, value is: " + limit);
            return limit;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Log.e("PressureUtil", ": Reflection exception: " + ex.getMessage());
            return limit;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            Log.e("PressureUtil", "Other exception: " + ex2.getMessage());
            return limit;
        }
    }
}
