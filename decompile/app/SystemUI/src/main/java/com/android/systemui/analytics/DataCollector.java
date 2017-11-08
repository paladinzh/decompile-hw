package com.android.systemui.analytics;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.MotionEvent;
import com.google.protobuf.nano.MessageNano;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataCollector implements SensorEventListener {
    private static DataCollector sInstance = null;
    private boolean mCollectBadTouches = false;
    private final Context mContext;
    private boolean mCornerSwiping = false;
    private SensorLoggerSession mCurrentSession = null;
    private boolean mEnableCollector = false;
    private final Handler mHandler = new Handler();
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            DataCollector.this.updateConfiguration();
        }
    };
    private boolean mTimeoutActive = false;
    private boolean mTrackingStarted = false;

    private DataCollector(Context context) {
        this.mContext = context;
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("data_collector_enable"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("data_collector_collect_bad_touches"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static DataCollector getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataCollector(context);
        }
        return sInstance;
    }

    private void updateConfiguration() {
        boolean z;
        boolean z2 = false;
        if (!Build.IS_DEBUGGABLE || Secure.getInt(this.mContext.getContentResolver(), "data_collector_enable", 0) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mEnableCollector = z;
        if (this.mEnableCollector && Secure.getInt(this.mContext.getContentResolver(), "data_collector_collect_bad_touches", 0) != 0) {
            z2 = true;
        }
        this.mCollectBadTouches = z2;
    }

    private boolean sessionEntrypoint() {
        if (!this.mEnableCollector || this.mCurrentSession != null) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(int result) {
        if (this.mEnableCollector && this.mCurrentSession != null) {
            onSessionEnd(result);
        }
    }

    private void onSessionStart() {
        this.mCornerSwiping = false;
        this.mTrackingStarted = false;
        this.mCurrentSession = new SensorLoggerSession(System.currentTimeMillis(), System.nanoTime());
    }

    private void onSessionEnd(int result) {
        SensorLoggerSession session = this.mCurrentSession;
        this.mCurrentSession = null;
        session.end(System.currentTimeMillis(), result);
        queueSession(session);
    }

    private void queueSession(final SensorLoggerSession currentSession) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                byte[] b = MessageNano.toByteArray(currentSession.toProto());
                String dir = DataCollector.this.mContext.getFilesDir().getAbsolutePath();
                if (currentSession.getResult() == 1) {
                    dir = dir + "/good_touches";
                } else if (DataCollector.this.mCollectBadTouches) {
                    dir = dir + "/bad_touches";
                } else {
                    return;
                }
                File file = new File(dir);
                file.mkdir();
                try {
                    new FileOutputStream(new File(file, "trace_" + System.currentTimeMillis())).write(b);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public synchronized void onSensorChanged(SensorEvent event) {
        if (this.mEnableCollector && this.mCurrentSession != null) {
            this.mCurrentSession.addSensorEvent(event, System.nanoTime());
            enforceTimeout();
        }
    }

    private void enforceTimeout() {
        if (this.mTimeoutActive && System.currentTimeMillis() - this.mCurrentSession.getStartTimestampMillis() > 11000) {
            onSessionEnd(2);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean isEnabled() {
        return this.mEnableCollector;
    }

    public void onScreenTurningOn() {
        if (sessionEntrypoint()) {
            addEvent(0);
        }
    }

    public void onScreenOnFromTouch() {
        if (sessionEntrypoint()) {
            addEvent(1);
        }
    }

    public void onScreenOff() {
        addEvent(2);
        sessionExitpoint(0);
    }

    public void onSucccessfulUnlock() {
        addEvent(3);
        sessionExitpoint(1);
    }

    public void onBouncerShown() {
        addEvent(4);
    }

    public void onBouncerHidden() {
        addEvent(5);
    }

    public void onQsDown() {
        addEvent(6);
    }

    public void setQsExpanded(boolean expanded) {
        if (expanded) {
            addEvent(7);
        } else {
            addEvent(8);
        }
    }

    public void onTrackingStarted() {
        this.mTrackingStarted = true;
        addEvent(9);
    }

    public void onTrackingStopped() {
        if (this.mTrackingStarted) {
            this.mTrackingStarted = false;
            addEvent(10);
        }
    }

    public void onNotificationActive() {
        addEvent(11);
    }

    public void onNotificationDoubleTap() {
        addEvent(13);
    }

    public void setNotificationExpanded() {
        addEvent(14);
    }

    public void onNotificatonStartDraggingDown() {
        addEvent(16);
    }

    public void onNotificatonStopDraggingDown() {
        addEvent(17);
    }

    public void onNotificationDismissed() {
        addEvent(18);
    }

    public void onNotificatonStartDismissing() {
        addEvent(19);
    }

    public void onNotificatonStopDismissing() {
        addEvent(20);
    }

    public void onUnlockHintStarted() {
        addEvent(26);
    }

    public void onTouchEvent(MotionEvent event, int width, int height) {
        if (this.mCurrentSession != null) {
            this.mCurrentSession.addMotionEvent(event);
            this.mCurrentSession.setTouchArea(width, height);
            enforceTimeout();
        }
    }

    private void addEvent(int eventType) {
        if (this.mEnableCollector && this.mCurrentSession != null) {
            this.mCurrentSession.addPhoneEvent(eventType, System.nanoTime());
        }
    }
}
