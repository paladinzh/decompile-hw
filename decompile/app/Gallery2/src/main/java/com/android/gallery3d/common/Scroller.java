package com.android.gallery3d.common;

import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;

public class Scroller {
    private static float ALPHA = 800.0f;
    private static float DECELERATION_RATE = ((float) (Math.log(0.75d) / Math.log(0.9d)));
    private static float END_TENSION = (WMElement.CAMERASIZEVALUE1B1 - START_TENSION);
    private static final float[] SPLINE = new float[101];
    private static float START_TENSION = 0.4f;
    private static float sViscousFluidNormalize;
    private static float sViscousFluidScale = 8.0f;

    static {
        float x_min = 0.0f;
        for (int i = 0; i <= 100; i++) {
            float x;
            float coef;
            float t = ((float) i) / 100.0f;
            float x_max = WMElement.CAMERASIZEVALUE1B1;
            while (true) {
                x = x_min + ((x_max - x_min) / 2.0f);
                coef = (MapConfig.MIN_ZOOM * x) * (WMElement.CAMERASIZEVALUE1B1 - x);
                float tx = ((((WMElement.CAMERASIZEVALUE1B1 - x) * START_TENSION) + (END_TENSION * x)) * coef) + ((x * x) * x);
                if (((double) Math.abs(tx - t)) < 1.0E-5d) {
                    break;
                } else if (tx > t) {
                    x_max = x;
                } else {
                    x_min = x;
                }
            }
            SPLINE[i] = coef + ((x * x) * x);
        }
        SPLINE[100] = WMElement.CAMERASIZEVALUE1B1;
        sViscousFluidNormalize = WMElement.CAMERASIZEVALUE1B1;
        sViscousFluidNormalize = WMElement.CAMERASIZEVALUE1B1 / viscousFluid(WMElement.CAMERASIZEVALUE1B1);
    }

    static float viscousFluid(float x) {
        x *= sViscousFluidScale;
        if (x < WMElement.CAMERASIZEVALUE1B1) {
            x -= WMElement.CAMERASIZEVALUE1B1 - ((float) Math.exp((double) (-x)));
        } else {
            x = 0.36787945f + (0.63212055f * (WMElement.CAMERASIZEVALUE1B1 - ((float) Math.exp((double) (WMElement.CAMERASIZEVALUE1B1 - x)))));
        }
        return x * sViscousFluidNormalize;
    }
}
