package com.amap.api.mapcore;

public class AutoTestConfig {
    public static final int CompassViewId;
    public static final int MyLocationViewId;
    public static final int ScaleControlsViewId;
    public static final int ZoomControllerViewId;
    private static int a;

    static {
        a = 900000000;
        int i = a;
        a = i + 1;
        ZoomControllerViewId = i;
        i = a;
        a = i + 1;
        ScaleControlsViewId = i;
        i = a;
        a = i + 1;
        MyLocationViewId = i;
        i = a;
        a = i + 1;
        CompassViewId = i;
    }
}
