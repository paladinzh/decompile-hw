package com.huawei.gallery.util;

import android.app.Activity;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.lang.reflect.InvocationTargetException;

public class ScreenController {
    private Activity mActivity;
    private float mBrightness = 0.7f;
    private String mBrightnessLevel = SystemProperties.get("ro.hwcamera.brightness_range", "");

    public ScreenController(Activity activity) {
        int brightnessTemp;
        float f;
        this.mActivity = activity;
        if (this.mBrightnessLevel.equals("")) {
            brightnessTemp = transformBrightness(255, 4, 240);
        } else {
            String[] tmp = this.mBrightnessLevel.split(",");
            if (tmp.length == 2) {
                brightnessTemp = transformBrightness(Utils.parseIntSafely(tmp[1], 255), Utils.parseIntSafely(tmp[0], 4), 240);
            } else {
                brightnessTemp = transformBrightness(255, 4, 240);
            }
        }
        if (brightnessTemp != -1) {
            f = ((float) brightnessTemp) / 255.0f;
        } else {
            f = this.mBrightness;
        }
        this.mBrightness = f;
    }

    public void onResume() {
        setScreenBrightness(System.getInt(this.mActivity.getContentResolver(), "screen_brightness_mode", 0) == 1 ? this.mBrightness : GroundOverlayOptions.NO_DIMENSION);
    }

    public void onPause() {
        setScreenBrightness(GroundOverlayOptions.NO_DIMENSION);
    }

    private void setScreenBrightness(float brightness) {
        Window win = this.mActivity.getWindow();
        LayoutParams winParams = win.getAttributes();
        winParams.screenBrightness = brightness;
        win.setAttributes(winParams);
    }

    private int transformBrightness(int max, int min, int level) {
        GalleryLog.i("ScreenController", "app: transformBrightness: max = " + max + ";min = " + min + "; level = " + level);
        int brightness = -1;
        PowerManager pm = (PowerManager) this.mActivity.getSystemService("power");
        try {
            brightness = ((Integer) pm.getClass().getMethod("transformBrightness", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}).invoke(pm, new Object[]{Integer.valueOf(max), Integer.valueOf(min), Integer.valueOf(level)})).intValue();
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        } catch (NoSuchMethodException e) {
            GalleryLog.w("ScreenController", "transformBrightness,NoSuchMethodException");
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        } catch (IllegalAccessException e2) {
            GalleryLog.w("ScreenController", "transformBrightness,IllegalAccessException");
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        } catch (IllegalArgumentException e3) {
            GalleryLog.w("ScreenController", "transformBrightness,IllegalArgumentException");
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        } catch (InvocationTargetException e4) {
            GalleryLog.w("ScreenController", "transformBrightness,InvocationTargetException");
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        } catch (Throwable th) {
            GalleryLog.i("ScreenController", "app: transformBrightness: brightness = " + brightness);
            return brightness;
        }
    }
}
