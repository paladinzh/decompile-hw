package com.huawei.gallery.data;

import android.os.Process;
import com.android.gallery3d.common.Utils;

public abstract class AutoLoaderThread extends Thread {
    private volatile boolean mActive = true;
    private volatile boolean mDirty = true;

    protected abstract void onLoad();

    public void run() {
        Process.setThreadPriority(10);
        while (this.mActive) {
            synchronized (this) {
                if (!this.mActive || (this.mDirty && !isRenderLock())) {
                    this.mDirty = false;
                    onLoad();
                } else {
                    Utils.waitWithoutInterrupt(this);
                }
            }
        }
    }

    protected boolean isRenderLock() {
        return false;
    }

    public synchronized void notifyDirty() {
        this.mDirty = true;
        notifyAll();
    }

    public synchronized void terminate() {
        this.mActive = false;
        notifyAll();
    }
}
