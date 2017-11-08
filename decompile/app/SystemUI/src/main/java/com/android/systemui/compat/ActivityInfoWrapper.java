package com.android.systemui.compat;

import com.android.systemui.utils.CompatUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActivityInfoWrapper {
    private static final Class<?> CLASS_ActivityInfoEx = CompatUtils.getClass("android.content.pm.ActivityInfoEx");
    public static final int CONFIG_HWTHEME = CompatUtils.objectToInt(CompatUtils.getFieldValue(null, FIELD_CONFIG_HWTHEME));
    private static final Field FIELD_CONFIG_HWTHEME = CompatUtils.getField(CLASS_ActivityInfoEx, "CONFIG_HWTHEME");
    private static final Method METHOD_STATIC_activityInfoConfigToNative = CompatUtils.getMethod(CLASS_ActivityInfoEx, "activityInfoConfigToNative", Integer.TYPE);

    public static boolean isValid() {
        return CLASS_ActivityInfoEx != null;
    }

    public static int activityInfoConfigToNative(int input) {
        if (!isValid()) {
            return 0;
        }
        return ((Integer) CompatUtils.invoke(null, METHOD_STATIC_activityInfoConfigToNative, Integer.valueOf(input))).intValue();
    }

    public static boolean isThemeChanged(int configChanges) {
        return (CONFIG_HWTHEME & configChanges) != 0;
    }
}
