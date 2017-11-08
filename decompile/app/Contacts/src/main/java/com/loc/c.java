package com.loc;

import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;

/* compiled from: AMapLocationManager */
/* synthetic */ class c {
    static final /* synthetic */ int[] a = new int[AMapLocationMode.values().length];

    static {
        try {
            a[AMapLocationMode.Battery_Saving.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[AMapLocationMode.Device_Sensors.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[AMapLocationMode.Hight_Accuracy.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
