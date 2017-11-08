package com.fyusion.sdk.camera.util;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.internal.analytics.Fyulytics;

/* compiled from: Unknown */
public class a {
    private static boolean a = false;
    private static boolean b = false;
    private static boolean c = false;

    public static boolean a() {
        return VERSION.SDK_INT >= 21;
    }

    public static boolean b() {
        if (a()) {
            d();
        }
        return a;
    }

    public static boolean c() {
        if (a()) {
            d();
        }
        return b;
    }

    @RequiresApi(api = 21)
    private static void d() {
        if (!c) {
            CameraManager cameraManager = (CameraManager) FyuseSDK.getContext().getSystemService("camera");
            try {
                for (String cameraCharacteristics : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics2 = cameraManager.getCameraCharacteristics(cameraCharacteristics);
                    int intValue = ((Integer) cameraCharacteristics2.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)).intValue();
                    if (intValue != 2 && intValue >= 0) {
                        if (((Integer) cameraCharacteristics2.get(CameraCharacteristics.LENS_FACING)).intValue() == 1) {
                            a = true;
                        } else if (((Integer) cameraCharacteristics2.get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                            b = true;
                        }
                    }
                }
                c = true;
            } catch (CameraAccessException e) {
                DLog.w(Fyulytics.TAG, "Checking for FyusionCamera2 Support failed :: " + e.getMessage());
            }
        }
    }
}
