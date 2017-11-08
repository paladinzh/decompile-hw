package com.android.systemui.flashlight;

import android.content.Context;
import com.android.systemui.utils.CompatUtils;
import com.android.systemui.utils.HwLog;
import java.lang.reflect.Method;

public class FlashlightUtils {
    private static final Class<?> CLASS_HwPostCamera = CompatUtils.getClass("com.huawei.hwpostcamera.HwPostCamera");
    public static final boolean HW_POSTCAMERA_SUPPORT;
    private static final Method METHOD_STATIC_switchOffFlash = CompatUtils.getMethod(CLASS_HwPostCamera, "switchOffFlash", new Class[0]);
    private static final Method METHOD_STATIC_switchOnFlash = CompatUtils.getMethod(CLASS_HwPostCamera, "switchOnFlash", new Class[0]);

    static {
        boolean z = false;
        if (CLASS_HwPostCamera != null) {
            z = true;
        }
        HW_POSTCAMERA_SUPPORT = z;
    }

    public static boolean controlCamera(Context context, int state) {
        if (1 == state) {
            try {
                CompatUtils.invoke(null, METHOD_STATIC_switchOnFlash, null);
                HwLog.i("FlashlightUtils", "Open flashlight without camera");
            } catch (Exception e) {
                HwLog.w("FlashlightUtils", "controlCamera::huawei's flashlight is not support, use google's, " + e);
                return false;
            }
        }
        try {
            CompatUtils.invoke(null, METHOD_STATIC_switchOffFlash, null);
            HwLog.i("FlashlightUtils", "Close flashlight without camera");
        } catch (Exception e2) {
            HwLog.w("FlashlightUtils", "controlCamera::huawei's flashlight is not support, use google's, " + e2);
            return false;
        }
        return true;
    }
}
