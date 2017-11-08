package com.android.deskclock;

import android.content.Context;
import com.android.util.Log;
import com.huawei.motiondetection.MotionDetectionListener;
import com.huawei.motiondetection.MotionDetectionManager;
import com.huawei.motiondetection.MotionRecoResult;

public class MotionManager {
    private static MotionManager mMotionManager;
    private static boolean mSupportMotion;
    private MotionDetectionManager mAlarmMDManager;
    private ClockMotionDetectionListener mAlarmMotionDetectionListener;
    private Context mContext;
    private Object mLock = new Object();
    private MotionDetectionManager mTimerMDManager;
    private ClockMotionDetectionListener mTimerMotionDetectionListener;

    public static class ClockMotionDetectionListener implements MotionDetectionListener {
        private MotionListener mMotionListener;

        public ClockMotionDetectionListener(MotionListener motionListener) {
            this.mMotionListener = motionListener;
        }

        public void notifyMotionRecoResult(MotionRecoResult mrecoRes) {
            if (mrecoRes.mMotionType == 102) {
                if (this.mMotionListener != null) {
                    this.mMotionListener.pickupReduce();
                    Log.i("MotionManager", "notifyMotionRecoResult : pickupReduce");
                }
            } else if (mrecoRes.mMotionType == 202 && this.mMotionListener != null) {
                this.mMotionListener.flipMute();
                Log.i("MotionManager", "notifyMotionRecoResult : flipMute");
            }
        }
    }

    public interface MotionListener {
        void flipMute();

        void pickupReduce();
    }

    private MotionManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized MotionManager getInstance(Context context) {
        MotionManager motionManager;
        synchronized (MotionManager.class) {
            mSupportMotion = isMotionClassExists("com.huawei.motiondetection.MotionDetectionManager") ? isMotionRecoApkExist(context) : false;
            if (mMotionManager == null) {
                mMotionManager = new MotionManager(context);
                Log.i("MotionManager", "getInstance : new MotionManager");
            }
            motionManager = mMotionManager;
        }
        return motionManager;
    }

    public static boolean isMotionRecoApkExist(Context context) {
        if (MotionDetectionManager.isMotionRecoApkExist(context)) {
            return true;
        }
        Log.i("MotionManager", "isMotionRecoApkExist : Motion service not installed, it can not do motion recognize.");
        return false;
    }

    private static boolean isMotionClassExists(String motionClsName) {
        try {
            Class.forName(motionClsName);
            return true;
        } catch (ClassNotFoundException e) {
            Log.e("MotionManager", "isMotionClassExists : ClassNotFoundException = " + e.getMessage());
            return false;
        }
    }

    public void startTimerGestureListener(MotionListener motionListener, boolean pickupReduce) {
        Log.i("MotionManager", "startTimerGestureListener : pickupReduce = " + pickupReduce);
        if (mSupportMotion) {
            this.mTimerMotionDetectionListener = new ClockMotionDetectionListener(motionListener);
            this.mTimerMDManager = new MotionDetectionManager(this.mContext);
            this.mTimerMDManager.addMotionListener(this.mTimerMotionDetectionListener);
            this.mTimerMDManager.startMotionAppsReco(202);
            if (pickupReduce) {
                this.mTimerMDManager.startMotionAppsReco(102);
            }
        }
    }

    public void stopTimerFlipMuteGestureListener() {
        Log.i("MotionManager", "stopTimerFlipMuteGestureListener");
        if (this.mTimerMDManager != null) {
            this.mTimerMDManager.stopMotionAppsReco(202);
        }
    }

    public void stopTimerPickupReduceGestureListener() {
        Log.i("MotionManager", "stopTimerPickupReduceGestureListener");
        if (this.mTimerMDManager != null) {
            this.mTimerMDManager.stopMotionAppsReco(102);
        }
    }

    public void stopTimerGestureListener() {
        Log.i("MotionManager", "stopTimerGestureListener");
        if (this.mTimerMDManager != null) {
            this.mTimerMDManager.stopMotionAppsReco(102);
            this.mTimerMDManager.stopMotionAppsReco(202);
            this.mTimerMDManager.removeMotionListener(this.mTimerMotionDetectionListener);
            this.mTimerMotionDetectionListener = null;
            this.mTimerMDManager.destroy();
            this.mTimerMDManager = null;
        }
    }

    public void startAlarmGestureListener(MotionListener motionListener, boolean pickupReduce) {
        Log.i("MotionManager", "startAlarmGestureListener : pickupReduce = " + pickupReduce);
        if (mSupportMotion) {
            synchronized (this.mLock) {
                this.mAlarmMotionDetectionListener = new ClockMotionDetectionListener(motionListener);
                this.mAlarmMDManager = new MotionDetectionManager(this.mContext);
                this.mAlarmMDManager.addMotionListener(this.mAlarmMotionDetectionListener);
                this.mAlarmMDManager.startMotionAppsReco(202);
                if (pickupReduce) {
                    this.mAlarmMDManager.startMotionAppsReco(102);
                }
            }
        }
    }

    public void stopAlarmFlipMuteGestureListener() {
        Log.i("MotionManager", "stopAlarmFlipMuteGestureListener");
        synchronized (this.mLock) {
            if (this.mAlarmMDManager != null) {
                this.mAlarmMDManager.stopMotionAppsReco(202);
            }
        }
    }

    public void stopAlarmPickupReduceGestureListener() {
        Log.i("MotionManager", "stopAlarmPickupReduceGestureListener");
        synchronized (this.mLock) {
            if (this.mAlarmMDManager != null) {
                this.mAlarmMDManager.stopMotionAppsReco(102);
            }
        }
    }

    public void stopAlarmGestureListener() {
        Log.i("MotionManager", "stopAlarmGestureListener");
        synchronized (this.mLock) {
            if (this.mAlarmMDManager != null) {
                this.mAlarmMDManager.stopMotionAppsReco(102);
                this.mAlarmMDManager.stopMotionAppsReco(202);
                this.mAlarmMDManager.removeMotionListener(this.mAlarmMotionDetectionListener);
                this.mAlarmMotionDetectionListener = null;
                this.mAlarmMDManager.destroy();
                this.mAlarmMDManager = null;
            }
        }
    }
}
