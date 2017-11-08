package com.android.mms.attachment.utils;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import com.huawei.cspcommon.MLog;

public class WakeLockHelper {
    private final Object mLock = new Object();
    private final int mMyPid;
    private WakeLock mWakeLock;
    private final String mWakeLockId;

    public WakeLockHelper(String wakeLockId) {
        this.mWakeLockId = wakeLockId;
        this.mMyPid = Process.myPid();
    }

    public void acquire(Context context, Intent intent, int opcode) {
        synchronized (this.mLock) {
            if (this.mWakeLock == null) {
                this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.mWakeLockId);
            }
        }
        this.mWakeLock.acquire();
        intent.putExtra("pid", this.mMyPid);
    }

    public void release(Intent intent, int opcode) {
        if (this.mMyPid == intent.getIntExtra("pid", -1)) {
            try {
                this.mWakeLock.release();
            } catch (RuntimeException e) {
                MLog.e("WakeLockHelper", "KeepAliveService.onHandleIntent exit crash " + intent + " " + intent.getAction() + " opcode: " + opcode + " sWakeLock: " + this.mWakeLock + " isHeld: " + (this.mWakeLock == null ? "(null)" : Boolean.valueOf(this.mWakeLock.isHeld())));
            }
        }
    }
}
