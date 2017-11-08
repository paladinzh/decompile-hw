package com.huawei.systemmanager.comm.grule;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorChecker;

public class GRuleManager {
    private static GRuleManager mInstance;
    private MonitorChecker monitorChecker = new MonitorChecker();

    public static synchronized GRuleManager getInstance() {
        GRuleManager gRuleManager;
        synchronized (GRuleManager.class) {
            if (mInstance == null) {
                mInstance = new GRuleManager();
            }
            gRuleManager = mInstance;
        }
        return gRuleManager;
    }

    public boolean shouldMonitor(Context context, String scenarioKey, String pkgName) {
        return this.monitorChecker.shouldMonitor(context, scenarioKey, pkgName);
    }
}
