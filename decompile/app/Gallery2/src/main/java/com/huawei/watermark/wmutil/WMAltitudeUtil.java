package com.huawei.watermark.wmutil;

import com.huawei.watermark.manager.parse.WMElement;

public class WMAltitudeUtil {
    public static int getAltitude(float referencePressure, float currentPressure) {
        return Math.round(44330.0f * (WMElement.CAMERASIZEVALUE1B1 - ((float) Math.pow((double) (currentPressure / referencePressure), 0.19029495120048523d))));
    }
}
