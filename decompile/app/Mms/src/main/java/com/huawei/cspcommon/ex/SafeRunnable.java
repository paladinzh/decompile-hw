package com.huawei.cspcommon.ex;

public abstract class SafeRunnable implements Runnable {
    public abstract void safeRun();

    public void run() {
        safeRun();
    }
}
