package com.android.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import com.android.deskclock.MotionManager;

public class WeakenVolume extends Thread {
    private Context mContext = null;
    private boolean mIsRun = true;
    private MotionManager motionManager = null;
    private int pickupType = 0;
    private int sleepTime = 200;
    private int volume = 0;

    public WeakenVolume(Context context, int systemVolume, int type, MotionManager manager) {
        this.volume = systemVolume;
        this.pickupType = type;
        this.mContext = context;
        this.motionManager = manager;
    }

    public void setRun(boolean isRun) {
        this.mIsRun = isRun;
    }

    public void stopThread() {
        if (isAlive()) {
            Log.d("WeakenVolume", "WeakenVolume->stopThread : mIsRun = " + this.mIsRun);
            interrupt();
        }
    }

    public void run() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.sleepTime = 1500 / (this.volume == 0 ? 1 : this.volume);
        Log.i("WeakenVolume", "WeakenVolume->run : volume = " + this.volume + " sleep time = " + this.sleepTime);
        for (int volumeTemp = this.volume; volumeTemp >= 1; volumeTemp--) {
            audioManager.setStreamVolume(4, volumeTemp, 0);
            SystemClock.sleep((long) this.sleepTime);
            if (!this.mIsRun) {
                Log.w("WeakenVolume", "WeakenVolume->run : force break.");
                break;
            }
        }
        Log.w("WeakenVolume", "WeakenVolume->run : normal break.");
        switch (this.pickupType) {
            case 1:
                if (this.motionManager != null) {
                    this.motionManager.stopAlarmPickupReduceGestureListener();
                    return;
                }
                return;
            case 2:
                Utils.updateSystemVolume(this.mContext, this.volume);
                if (this.motionManager != null) {
                    this.motionManager.stopTimerPickupReduceGestureListener();
                    return;
                }
                return;
            default:
                return;
        }
    }
}
