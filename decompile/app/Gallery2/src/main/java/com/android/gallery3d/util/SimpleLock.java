package com.android.gallery3d.util;

public class SimpleLock {
    private boolean isLocked = false;
    private Object mLock = new Object();

    public void waitUntilNotify() {
        synchronized (this.mLock) {
            this.isLocked = true;
            while (this.isLocked) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    GalleryLog.i("SimpleLock", "Wait lock failed in waitUntilNotify() method.");
                }
            }
        }
        return;
    }

    public void notifyAllWaitingLock() {
        synchronized (this.mLock) {
            this.isLocked = false;
            this.mLock.notifyAll();
        }
    }
}
