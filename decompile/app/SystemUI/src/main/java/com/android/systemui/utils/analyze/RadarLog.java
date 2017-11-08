package com.android.systemui.utils.analyze;

import android.util.HwLogException;
import android.util.LogException;

public class RadarLog {
    private static final LogException sLogException = HwLogException.getInstance();

    public static void logRadar(int bugType, int sceneCode, String bodyMsg) {
        logRadar(67, bugType, sceneCode, bodyMsg);
    }

    public static void logRadar(int level, int bugType, int sceneCode, String bodyMsg) {
        logRadar(level, 7, bugType, sceneCode, bodyMsg);
    }

    public static void logRadar(int level, int mask, int bugType, int sceneCode, String bodyMsg) {
        sLogException.msg("app-SystemUI", level, mask, header("5.0.1", bugType, sceneCode), bodyMsg);
    }

    private static String header(String apkVersion, int bugType, int sceneCode) {
        return "Package: com.android.systemui\nAPK version: " + apkVersion + "\n" + "Bug type: " + bugType + "\n" + "Scene def: " + sceneCode + "\n";
    }
}
