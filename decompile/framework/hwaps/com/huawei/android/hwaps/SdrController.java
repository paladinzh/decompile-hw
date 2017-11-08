package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.os.SystemClock;
import android.os.SystemProperties;

public class SdrController {
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_MENU = 82;
    public static final int KEYCODE_POWER = 26;
    private static final String TAG = "SdrController";
    private static boolean mIsTurnOnSSR;
    private static SdrController sInstance = null;
    public float mRatio = 2.0f;

    static {
        boolean z = false;
        if (16384 == (SystemProperties.getInt("sys.aps.support", 0) & 16384)) {
            z = true;
        }
        mIsTurnOnSSR = z;
    }

    public static synchronized SdrController getInstance() {
        SdrController sdrController;
        synchronized (SdrController.class) {
            if (sInstance == null) {
                sInstance = new SdrController();
            }
            sdrController = sInstance;
        }
        return sdrController;
    }

    public static boolean isSupportApsSdr() {
        if (2048 == (SystemProperties.getInt("sys.aps.support", 0) & 2048)) {
            return true;
        }
        ApsCommon.logI(TAG, "SDR: control: Dcr module is not supported");
        return false;
    }

    public void startSdr() {
        HwApsInterface.nativeStartSdr(this.mRatio);
        ApsCommon.logD(TAG, "SDR: control: start zoom");
    }

    public void stopSdr() {
        HwApsInterface.nativeStopSdr();
        ApsCommon.logD(TAG, "SDR: control: stop zoom");
    }

    public void stopSdrImmediately() {
        HwApsInterface.nativeStopSdrImmediately();
        ApsCommon.logD(TAG, "SDR: control: stop zoom immediately");
    }

    public void setSdrRatio(float ratio) {
        this.mRatio = ratio;
        HwApsInterface.nativeSetSdrRatio(ratio);
        ApsCommon.logD(TAG, "SDR: control: setSdrRatio  : " + this.mRatio);
    }

    public float getCurrentSdrRatio() {
        return HwApsInterface.nativeGetCurrentSdrRatio();
    }

    public boolean IsSdrCase() {
        boolean isSdrCase = HwApsInterface.nativeIsSdrCase();
        ApsCommon.logD(TAG, "SDR: control: check if sdr can be run. [result:" + isSdrCase + "]");
        return isSdrCase;
    }

    public static boolean StopSdrForSpecial(String info, int keyCode) {
        if (-1 != keyCode && mIsTurnOnSSR) {
            setPropertyForKeyCode(keyCode);
        }
        return true;
    }

    private static void setPropertyForKeyCode(int keyCode) {
        final String msg = Long.toString(SystemClock.uptimeMillis()) + "|" + Integer.toString(keyCode);
        new Thread(new Runnable() {
            public void run() {
                SystemProperties.set("sys.aps.keycode", msg);
            }
        }).start();
        ApsCommon.logD(TAG, "SDR: Controller, setPropertyForKeyCode, keycode: " + keyCode + ", msg: " + msg);
    }
}
