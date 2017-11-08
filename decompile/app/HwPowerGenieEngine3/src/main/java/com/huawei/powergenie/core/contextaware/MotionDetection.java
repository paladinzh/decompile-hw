package com.huawei.powergenie.core.contextaware;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import com.android.location.provider.ActivityChangedEvent;
import com.android.location.provider.ActivityRecognitionEvent;
import com.android.location.provider.ActivityRecognitionProvider;
import com.android.location.provider.ActivityRecognitionProvider.Sink;
import com.huawei.android.location.activityrecognition.HwActivityChangedEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognition;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionHardwareSink;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionServiceConnection;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.ArrayList;
import java.util.List;

public final class MotionDetection {
    private static final boolean DEBUG = Log.isLoggable("MotionDetection", 2);
    private Sink mActivityRecognitionListener = new Sink() {
        public void onActivityChanged(ActivityChangedEvent event) {
            MotionDetection.this.processActivityChangedEvent(event);
        }
    };
    private ActivityRecognitionProvider mActivityRecognitionProvider;
    private HwActivityRecognitionServiceConnection mArServiceConnect = new HwActivityRecognitionServiceConnection() {
        public void onServiceConnected() {
            Log.d("MotionDetection", "onServiceConnected()");
            MotionDetection.this.mIsHwArConnectedOk = true;
            if (MotionDetection.this.mIsStart) {
                MotionDetection.this.startHwMotionDetection(30);
            }
        }

        public void onServiceDisconnected() {
            Log.d("MotionDetection", "onServiceDisconnected()");
            MotionDetection.this.mIsHwArConnectedOk = false;
            if (MotionDetection.this.mIsStart) {
                MotionDetection.this.connectionHwArService();
            }
        }
    };
    private final List<String> mCaredMotionActivities = new ArrayList<String>() {
        {
            add("android.activity_recognition.running");
            add("android.activity_recognition.walking");
            add("android.activity_recognition.on_bicycle");
            add("android.activity_recognition.in_vehicle");
        }
    };
    private Context mContext = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    synchronized (MotionDetection.this) {
                        if (MotionDetection.DEBUG) {
                            Log.i("MotionDetection", "handle MSG_CHECK_REALLY_STILL_EXIT");
                        }
                        MotionDetection.this.checkReallyStillExitLocked(15);
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private HwActivityRecognition mHwAR;
    private HwActivityRecognitionHardwareSink mHwArSink = new HwActivityRecognitionHardwareSink() {
        public void onActivityChanged(HwActivityChangedEvent event) {
            MotionDetection.this.processActivityChangedEvent(event);
        }
    };
    private boolean mIsActivitySupported = false;
    private boolean mIsHwArConnectedOk = false;
    private boolean mIsMotionActivitiesEnabled = false;
    private boolean mIsStart = false;
    private long mLastStillExitTime = 0;
    private long mReportLatencySec = 30;
    private int mState = 0;
    private final UserStateManager mUserStateManager;

    public MotionDetection(Context context, UserStateManager userStateManager) {
        this.mUserStateManager = userStateManager;
        initActivityRecognitionProvider();
        if (!this.mIsActivitySupported) {
            this.mHwAR = new HwActivityRecognition(context);
            connectionHwArService();
        }
        this.mContext = context;
    }

    private void connectionHwArService() {
        if (!this.mIsHwArConnectedOk && this.mHwAR != null) {
            Log.i("MotionDetection", "Connection HwArService...");
            this.mHwAR.connectService(this.mHwArSink, this.mArServiceConnect);
        }
    }

    private void initActivityRecognitionProvider() {
        try {
            this.mActivityRecognitionProvider = PGActivityRecognitionService.getActivityRecognitionProvider();
            if (this.mActivityRecognitionProvider == null) {
                Log.e("MotionDetection", "Error creating Activity-Recognition Provider");
                return;
            }
            this.mIsActivitySupported = this.mActivityRecognitionProvider.isActivitySupported("android.activity_recognition.still");
            if (this.mIsActivitySupported) {
                Log.d("MotionDetection", "registerSink");
                this.mActivityRecognitionProvider.registerSink(this.mActivityRecognitionListener);
            } else {
                Log.e("MotionDetection", "Activity-Recognition not supports STILL.");
            }
        } catch (RemoteException e) {
            Log.e("MotionDetection", "RemoteException found!");
        }
    }

    protected boolean startMotionDetection(long reportLatencySec) {
        if (!this.mIsStart) {
            this.mIsStart = true;
            if (this.mIsActivitySupported) {
                startOriginalMotionDetection(reportLatencySec);
            } else {
                startHwMotionDetection(reportLatencySec);
            }
        }
        return true;
    }

    private boolean startOriginalMotionDetection(long reportLatencySec) {
        if (this.mActivityRecognitionProvider == null) {
            initActivityRecognitionProvider();
        }
        try {
            if (this.mIsActivitySupported) {
                Log.i("MotionDetection", "enableActivityEvent reportLatencySec: " + reportLatencySec);
                long reportLatencyNs = ((reportLatencySec * 1000) * 1000) * 1000;
                this.mActivityRecognitionProvider.enableActivityEvent("android.activity_recognition.still", 1, reportLatencyNs);
                this.mActivityRecognitionProvider.enableActivityEvent("android.activity_recognition.still", 2, reportLatencyNs);
            } else {
                Log.w("MotionDetection", "Activity Recognition is not supported to start it.");
            }
        } catch (RemoteException e) {
            Log.e("MotionDetection", "RemoteException found!");
        }
        return true;
    }

    private boolean startHwMotionDetection(long reportLatencySec) {
        connectionHwArService();
        boolean z = false;
        this.mReportLatencySec = reportLatencySec;
        synchronized (this) {
            if (!(this.mHwAR == null || this.mHwAR.getSupportedActivities() == null)) {
                if (this.mHwAR.getSupportedActivities().length == 0) {
                    Log.e("MotionDetection", "no any supported activities.");
                    return false;
                }
                try {
                    Log.i("MotionDetection", "startHwMotionDetection");
                    long reportLatencyNs = ((reportLatencySec * 1000) * 1000) * 1000;
                    z = this.mHwAR.enableActivityEvent("android.activity_recognition.still", 1, reportLatencyNs) ? this.mHwAR.enableActivityEvent("android.activity_recognition.still", 2, reportLatencyNs) : false;
                } catch (Exception e) {
                    Log.e("MotionDetection", "startMotionDetection exception !");
                }
            }
            this.mState = 1;
            this.mIsMotionActivitiesEnabled = false;
            return z;
        }
    }

    protected void stopMotionDetection() {
        if (this.mIsStart) {
            this.mIsStart = false;
            if (this.mIsActivitySupported) {
                stopOriginalMotionDetection();
            } else {
                stopHwMotionDetection();
            }
        }
    }

    private void stopOriginalMotionDetection() {
        try {
            if (this.mIsActivitySupported) {
                Log.i("MotionDetection", "disableActivityEvent");
                this.mActivityRecognitionProvider.disableActivityEvent("android.activity_recognition.still", 1);
                this.mActivityRecognitionProvider.disableActivityEvent("android.activity_recognition.still", 2);
                this.mUserStateManager.stopUserStationary();
                return;
            }
            Log.w("MotionDetection", "Activity Recognition is not supported to stop it.");
        } catch (RemoteException e) {
            Log.e("MotionDetection", "RemoteException found!");
        }
    }

    private void stopHwMotionDetection() {
        synchronized (this) {
            if (this.mHwAR != null) {
                try {
                    Log.i("MotionDetection", "stopHwMotionDetection");
                    this.mHwAR.disableActivityEvent("android.activity_recognition.still", 1);
                    this.mHwAR.disableActivityEvent("android.activity_recognition.still", 2);
                    disableMotionActivities(false);
                    this.mUserStateManager.stopUserStationary();
                } catch (Exception e) {
                    Log.e("MotionDetection", "stopMotionDetection exception !");
                }
            }
            this.mState = 0;
        }
    }

    private void processActivityChangedEvent(ActivityChangedEvent activityEvent) {
        if (activityEvent == null) {
            Log.w("MotionDetection", "activityEvent null");
            return;
        }
        Log.i("MotionDetection", "onActivityChanged: " + activityEvent);
        boolean enterStill = false;
        boolean isEffectiveEvent = false;
        long lastestTimestamp = 0;
        for (ActivityRecognitionEvent event : activityEvent.getActivityRecognitionEvents()) {
            if ("android.activity_recognition.still".equals(event.getActivity()) && event.getTimestampNs() > lastestTimestamp) {
                if (1 == event.getEventType()) {
                    enterStill = true;
                    isEffectiveEvent = true;
                    lastestTimestamp = event.getTimestampNs();
                } else if (2 == event.getEventType()) {
                    enterStill = false;
                    isEffectiveEvent = true;
                    lastestTimestamp = event.getTimestampNs();
                }
            }
        }
        if (isEffectiveEvent) {
            if (enterStill) {
                Log.i("MotionDetection", "start stationary.");
                this.mUserStateManager.startUserStationary();
            } else {
                Log.i("MotionDetection", "start walking.");
                this.mUserStateManager.stopUserStationary();
            }
            return;
        }
        Log.i("MotionDetection", "ineffective event.");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processActivityChangedEvent(HwActivityChangedEvent activityEvent) {
        if (activityEvent == null) {
            Log.w("MotionDetection", "activityEvent null");
            return;
        }
        Log.i("MotionDetection", "onActivityChanged: " + activityEvent);
        HwActivityRecognitionEvent lastEvent = null;
        for (HwActivityRecognitionEvent event : activityEvent.getActivityRecognitionEvents()) {
            if (lastEvent == null) {
                lastEvent = event;
            } else if (event.getTimestampNs() > lastEvent.getTimestampNs()) {
                lastEvent = event;
            }
        }
        if (lastEvent == null) {
            Log.i("MotionDetection", "ineffective event.");
            return;
        }
        synchronized (this) {
            Log.i("MotionDetection", "lastEvent evnt: " + lastEvent.getActivity() + ", type:" + lastEvent.getEventType() + ", current state: " + stateToString(this.mState));
            if (DEBUG) {
                Log.i("MotionDetection", "now: " + SystemClock.elapsedRealtimeNanos());
            }
            if (this.mState == 0) {
            } else if ("android.activity_recognition.still".equals(lastEvent.getActivity())) {
                if (lastEvent.getEventType() == 1) {
                    disableMotionActivities(true);
                    if (this.mState != 2) {
                        Log.i("MotionDetection", "start stationary.");
                        this.mState = 2;
                        this.mUserStateManager.startUserStationary();
                        if (DEBUG) {
                            vibrate(100);
                        }
                    } else {
                        Log.i("MotionDetection", "now is already stationary,do nothing.");
                    }
                } else if (lastEvent.getEventType() == 2) {
                    if (this.mState != 3) {
                        Log.i("MotionDetection", "rev still_exit,need double check");
                        enableMotionActivities(5);
                    } else if (DEBUG) {
                        Log.i("MotionDetection", "rev still_exit,but now is already walking.");
                    }
                }
            } else if (this.mCaredMotionActivities.contains(lastEvent.getActivity())) {
                disableMotionActivities(true);
                if (this.mState != 3) {
                    Log.i("MotionDetection", "start walking.");
                    this.mState = 3;
                    this.mUserStateManager.stopUserStationary();
                    if (DEBUG) {
                        vibrate(1000);
                    }
                } else {
                    Log.i("MotionDetection", "now is already walking,do nothing.");
                }
            }
        }
    }

    void checkReallyStillExitLocked(long window) {
        if (this.mState != 0) {
            if (this.mState != 3) {
                long interval = SystemClock.elapsedRealtimeNanos() - this.mLastStillExitTime;
                if (DEBUG) {
                    Log.i("MotionDetection", "interval(s):" + (interval / 1000000000));
                }
                if (interval > window * 1000000000) {
                    Log.i("MotionDetection", "start walking, for not rev still_enter: " + (interval / 1000000000) + "s");
                    this.mState = 3;
                    disableMotionActivities(true);
                    this.mUserStateManager.stopUserStationary();
                    if (DEBUG) {
                        vibrate(1000);
                    }
                } else {
                    if (DEBUG) {
                        Log.i("MotionDetection", "need delay to check if really still exit");
                    }
                    this.mHandler.sendEmptyMessageDelayed(100, 2000);
                }
            } else if (DEBUG) {
                Log.i("MotionDetection", "checkReallyStillExitLocked:now is already walking,do nothing");
            }
        }
    }

    private boolean enableMotionActivities(long reportLatencySec) {
        int i = 1;
        boolean isStillEnable = true;
        if (!this.mIsMotionActivitiesEnabled) {
            Log.i("MotionDetection", "enableMotionActivities, t: " + reportLatencySec);
            long reportLatencyNs = ((1000 * reportLatencySec) * 1000) * 1000;
            for (String activity : this.mCaredMotionActivities) {
                i &= this.mHwAR.enableActivityEvent(activity, 1, reportLatencyNs);
                if (DEBUG) {
                    Log.i("MotionDetection", "enable motion activities:" + activity + ", ret: " + i);
                }
            }
            this.mHwAR.disableActivityEvent("android.activity_recognition.still", 1);
            this.mHwAR.disableActivityEvent("android.activity_recognition.still", 2);
            isStillEnable = this.mHwAR.enableActivityEvent("android.activity_recognition.still", 1, reportLatencyNs) & this.mHwAR.enableActivityEvent("android.activity_recognition.still", 2, reportLatencyNs);
            if (DEBUG) {
                Log.i("MotionDetection", "enable still activities,ret: " + isStillEnable);
            }
            this.mLastStillExitTime = SystemClock.elapsedRealtimeNanos();
            this.mHandler.sendEmptyMessageDelayed(100, 2000);
            this.mIsMotionActivitiesEnabled = true;
        }
        return i & isStillEnable;
    }

    private void disableMotionActivities(boolean restoreStillActivity) {
        if (this.mIsMotionActivitiesEnabled) {
            Log.i("MotionDetection", "disableMotionActivities");
            this.mHandler.removeMessages(100);
            for (String activity : this.mCaredMotionActivities) {
                this.mHwAR.disableActivityEvent(activity, 1);
            }
            if (restoreStillActivity) {
                long reportLatencyNs = ((this.mReportLatencySec * 1000) * 1000) * 1000;
                this.mHwAR.disableActivityEvent("android.activity_recognition.still", 1);
                this.mHwAR.disableActivityEvent("android.activity_recognition.still", 2);
                boolean isEnable = this.mHwAR.enableActivityEvent("android.activity_recognition.still", 1, reportLatencyNs) & this.mHwAR.enableActivityEvent("android.activity_recognition.still", 2, reportLatencyNs);
                if (DEBUG) {
                    Log.i("MotionDetection", "reportLatencySec: " + this.mReportLatencySec + ", restore still activity,ret: " + isEnable);
                }
            }
            this.mIsMotionActivitiesEnabled = false;
        }
    }

    private void vibrate(long time) {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(time);
    }

    private String stateToString(int state) {
        switch (state) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
                return "not_monitor";
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return "not_known";
            case NativeAdapter.PLATFORM_HI /*2*/:
                return "stationary";
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return "walking";
            default:
                return "Err";
        }
    }
}
