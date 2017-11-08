package com.android.deskclock;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.android.util.Log;

public class AlarmAlertWakeLock {
    private static WakeLock mFullWakeLock;
    private static WakeLock sBrightWakeLock;
    private static WakeLock sCpuWakeLock;

    public static WakeLock createBrightScreenWakeLock(Context context) {
        return ((PowerManager) context.getSystemService("power")).newWakeLock(10, "deskcover");
    }

    public static synchronized void acquireBrightScreenWakeLock(Context context) {
        synchronized (AlarmAlertWakeLock.class) {
            if (sBrightWakeLock != null) {
                return;
            }
            sBrightWakeLock = createBrightScreenWakeLock(context);
            sBrightWakeLock.acquire();
        }
    }

    public static synchronized void releaseBrightLock() {
        synchronized (AlarmAlertWakeLock.class) {
            if (sBrightWakeLock != null) {
                if (sBrightWakeLock.isHeld()) {
                    try {
                        sBrightWakeLock.release();
                    } catch (RuntimeException e) {
                        Log.w("AlarmAlertWakeLock", "release wakelock exception = " + e.getMessage());
                    }
                }
                sBrightWakeLock = null;
            }
        }
    }

    public static WakeLock createPartialWakeLock(Context context) {
        return ((PowerManager) context.getSystemService("power")).newWakeLock(268435457, "DeskClock");
    }

    public static synchronized void acquireCpuWakeLock(Context context) {
        synchronized (AlarmAlertWakeLock.class) {
            if (sCpuWakeLock != null) {
                return;
            }
            sCpuWakeLock = createPartialWakeLock(context);
            sCpuWakeLock.acquire();
        }
    }

    public static synchronized void releaseCpuLock() {
        synchronized (AlarmAlertWakeLock.class) {
            if (sCpuWakeLock != null) {
                if (sCpuWakeLock.isHeld()) {
                    try {
                        sCpuWakeLock.release();
                    } catch (RuntimeException e) {
                        Log.w("AlarmAlertWakeLock", "release wakelock exception = " + e.getMessage());
                    }
                }
                sCpuWakeLock = null;
            }
        }
    }

    public static WakeLock createFullWakeLock(Context context) {
        return ((PowerManager) context.getSystemService("power")).newWakeLock(805306394, "AlarmsMainActivity");
    }

    public static synchronized void acquireFullWakeLock() {
        synchronized (AlarmAlertWakeLock.class) {
            if (mFullWakeLock != null) {
                return;
            }
            mFullWakeLock = createFullWakeLock(DeskClockApplication.getDeskClockApplication());
            mFullWakeLock.acquire();
        }
    }

    public static synchronized void releaseFullLock() {
        synchronized (AlarmAlertWakeLock.class) {
            if (mFullWakeLock != null) {
                if (mFullWakeLock.isHeld()) {
                    try {
                        mFullWakeLock.release();
                    } catch (RuntimeException e) {
                        Log.w("AlarmAlertWakeLock", "release wakelock exception = " + e.getMessage());
                    }
                }
                mFullWakeLock = null;
            }
        }
    }
}
