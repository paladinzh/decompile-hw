package com.huawei.cspcommon.ex;

public interface CheckableRunnable extends Runnable {
    long getMaxRunningTime();

    void onTimeout(long j);
}
