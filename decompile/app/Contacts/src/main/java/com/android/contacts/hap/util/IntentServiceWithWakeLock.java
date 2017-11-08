package com.android.contacts.hap.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.android.contacts.util.HwLog;

public abstract class IntentServiceWithWakeLock extends IntentService {
    private static WakeLock lockStatic = null;

    protected abstract void doWakefulWork(Intent intent);

    private static synchronized WakeLock getLock(Context context) {
        WakeLock wakeLock;
        synchronized (IntentServiceWithWakeLock.class) {
            if (lockStatic == null) {
                lockStatic = ((PowerManager) context.getApplicationContext().getSystemService("power")).newWakeLock(1, "com.android.contacts.hap.util.IntentServiceWithWakeLock");
                lockStatic.setReferenceCounted(true);
            }
            wakeLock = lockStatic;
        }
        return wakeLock;
    }

    public IntentServiceWithWakeLock(String name) {
        super(name);
    }

    protected final void onHandleIntent(Intent intent) {
        try {
            if (!getLock(this).isHeld()) {
                getLock(this).acquire();
            }
            doWakefulWork(intent);
            if (getLock(this).isHeld()) {
                try {
                    getLock(this).release();
                } catch (Throwable th) {
                    HwLog.e("IntentServiceWithWakeLock", "Wakelock Release error:" + th.getMessage());
                }
            }
        } catch (Throwable th2) {
            HwLog.e("IntentServiceWithWakeLock", "Wakelock Release error:" + th2.getMessage());
        }
    }
}
