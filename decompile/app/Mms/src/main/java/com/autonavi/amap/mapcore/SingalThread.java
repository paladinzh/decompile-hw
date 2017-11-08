package com.autonavi.amap.mapcore;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingalThread extends Thread {
    private boolean isWaiting = true;
    String logTag = "SingalThread";
    private final Lock mLock = new ReentrantLock();
    private final Condition mWaiting = this.mLock.newCondition();

    void doWait() throws InterruptedException {
        this.mLock.lock();
        this.isWaiting = true;
        this.mWaiting.await();
        this.mLock.unlock();
    }

    void doAwake() {
        if (this.isWaiting) {
            this.mLock.lock();
            this.isWaiting = false;
            this.mWaiting.signal();
            this.mLock.unlock();
        }
    }
}
