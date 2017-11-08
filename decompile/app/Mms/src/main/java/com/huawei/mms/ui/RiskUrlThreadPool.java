package com.huawei.mms.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiskUrlThreadPool {
    private static volatile ExecutorService sRiskUrlThreadPool = null;

    public static ExecutorService getDefault() {
        if (sRiskUrlThreadPool == null) {
            sRiskUrlThreadPool = Executors.newSingleThreadExecutor();
        }
        return sRiskUrlThreadPool;
    }
}
