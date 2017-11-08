package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;

public class StartActivityScenarioUtils {
    private static final String AUTH_SCENARIO = "auth_scenario";
    public static final int SCENARIO_BIND_FINGERPRINT = 4;
    public static final int SCENARIO_ENTER_APPLOCK = 1;
    public static final int SCENARIO_INVALID = -1;
    public static final int SCENARIO_LAUNCH_UNLOCKED_APP = 3;
    public static final int SCENARIO_RELOCK_APPLOCK = 2;
    private static int sScenario = -1;

    public static int getAuthScenario(Context context, int dftScenario) {
        return sScenario;
    }

    public static void setAuthScenario(Context context, int value) {
        sScenario = value;
    }
}
