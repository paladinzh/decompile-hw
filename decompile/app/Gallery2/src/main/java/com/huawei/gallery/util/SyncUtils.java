package com.huawei.gallery.util;

import android.os.ConditionVariable;

public class SyncUtils {
    public static ConditionVariable runWithConditionVariable(final Runnable runnable) {
        final ConditionVariable lock = new ConditionVariable(false);
        new Thread(new Runnable() {
            public void run() {
                runnable.run();
                lock.open();
            }
        }).start();
        return lock;
    }
}
