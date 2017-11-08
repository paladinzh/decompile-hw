package com.huawei.harassmentinterception.engine;

public class HwEngineCallerManager {
    private static HwEngineCallerManager sInstance = null;
    private HwEngineCaller mCaller = null;

    public static synchronized HwEngineCallerManager getInstance() {
        HwEngineCallerManager hwEngineCallerManager;
        synchronized (HwEngineCallerManager.class) {
            if (sInstance == null) {
                sInstance = new HwEngineCallerManager();
            }
            hwEngineCallerManager = sInstance;
        }
        return hwEngineCallerManager;
    }

    public synchronized HwEngineCaller getEngineCaller() {
        return this.mCaller;
    }

    public synchronized void setEngineCaller(HwEngineCaller caller) {
        this.mCaller = caller;
    }
}
