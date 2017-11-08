package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.analytics.DataCollector;
import com.android.systemui.statusbar.StatusBarState;
import fyusion.vislib.BuildConfig;
import java.io.PrintWriter;

public class FalsingManager implements SensorEventListener {
    private static final int[] CLASSIFIER_SENSORS = new int[]{8};
    private static final int[] COLLECTOR_SENSORS = new int[]{1, 4, 8, 5, 11};
    private static FalsingManager sInstance = null;
    private final AccessibilityManager mAccessibilityManager;
    private boolean mBouncerOn = false;
    private final Context mContext;
    private final DataCollector mDataCollector;
    private boolean mEnforceBouncer = false;
    private final Handler mHandler = new Handler();
    private final HumanInteractionClassifier mHumanInteractionClassifier;
    private boolean mScreenOn;
    private final SensorManager mSensorManager;
    private boolean mSessionActive = false;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            FalsingManager.this.updateConfiguration();
        }
    };
    private int mState = 0;

    private FalsingManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService(SensorManager.class);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mDataCollector = DataCollector.getInstance(this.mContext);
        this.mHumanInteractionClassifier = HumanInteractionClassifier.getInstance(this.mContext);
        this.mScreenOn = ((PowerManager) context.getSystemService(PowerManager.class)).isInteractive();
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("falsing_manager_enforce_bouncer"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static FalsingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FalsingManager(context);
        }
        return sInstance;
    }

    private void updateConfiguration() {
        boolean z = false;
        if (Secure.getInt(this.mContext.getContentResolver(), "falsing_manager_enforce_bouncer", 0) != 0) {
            z = true;
        }
        this.mEnforceBouncer = z;
    }

    private boolean shouldSessionBeActive() {
        if (FalsingLog.ENABLED) {
        }
        if (isEnabled() && this.mScreenOn && this.mState == 1) {
            return true;
        }
        return false;
    }

    private boolean sessionEntrypoint() {
        if (this.mSessionActive || !shouldSessionBeActive()) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(boolean force) {
        if (!this.mSessionActive) {
            return;
        }
        if (force || !shouldSessionBeActive()) {
            this.mSessionActive = false;
            this.mSensorManager.unregisterListener(this);
        }
    }

    private void onSessionStart() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSessionStart", "classifierEnabled=" + isClassiferEnabled());
        }
        this.mBouncerOn = false;
        this.mSessionActive = true;
        if (this.mHumanInteractionClassifier.isEnabled()) {
            registerSensors(CLASSIFIER_SENSORS);
        }
        if (this.mDataCollector.isEnabled()) {
            registerSensors(COLLECTOR_SENSORS);
        }
    }

    private void registerSensors(int[] sensors) {
        for (int sensorType : sensors) {
            Sensor s = this.mSensorManager.getDefaultSensor(sensorType);
            if (s != null) {
                this.mSensorManager.registerListener(this, s, 1);
            }
        }
    }

    public boolean isClassiferEnabled() {
        return this.mHumanInteractionClassifier.isEnabled();
    }

    private boolean isEnabled() {
        return !this.mHumanInteractionClassifier.isEnabled() ? this.mDataCollector.isEnabled() : true;
    }

    public boolean isFalseTouch() {
        int i = 1;
        if (FalsingLog.ENABLED && !this.mSessionActive && ((PowerManager) this.mContext.getSystemService(PowerManager.class)).isInteractive()) {
            String str = "isFalseTouch";
            StringBuilder append = new StringBuilder().append("Session is not active, yet there's a query for a false touch.").append(" enabled=").append(isEnabled() ? 1 : 0).append(" mScreenOn=");
            if (!this.mScreenOn) {
                i = 0;
            }
            FalsingLog.wtf(str, append.append(i).append(" mState=").append(StatusBarState.toShortString(this.mState)).toString());
        }
        if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
            return false;
        }
        return this.mHumanInteractionClassifier.isFalseTouch();
    }

    public synchronized void onSensorChanged(SensorEvent event) {
        this.mDataCollector.onSensorChanged(event);
        this.mHumanInteractionClassifier.onSensorChanged(event);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        this.mDataCollector.onAccuracyChanged(sensor, accuracy);
    }

    public boolean shouldEnforceBouncer() {
        return this.mEnforceBouncer;
    }

    public void setStatusBarState(int state) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("setStatusBarState", "from=" + StatusBarState.toShortString(this.mState) + " to=" + StatusBarState.toShortString(state));
        }
        this.mState = state;
        if (shouldSessionBeActive()) {
            sessionEntrypoint();
        } else {
            sessionExitpoint(false);
        }
    }

    public void onScreenTurningOn() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenTurningOn", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenTurningOn();
        }
    }

    public void onScreenOnFromTouch() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOnFromTouch", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenOnFromTouch();
        }
    }

    public void onScreenOff() {
        if (FalsingLog.ENABLED) {
            int i;
            String str = "onScreenOff";
            StringBuilder append = new StringBuilder().append("from=");
            if (this.mScreenOn) {
                i = 1;
            } else {
                i = 0;
            }
            FalsingLog.i(str, append.append(i).toString());
        }
        this.mDataCollector.onScreenOff();
        this.mScreenOn = false;
        sessionExitpoint(false);
    }

    public void onSucccessfulUnlock() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSucccessfulUnlock", BuildConfig.FLAVOR);
        }
        this.mDataCollector.onSucccessfulUnlock();
    }

    public void onBouncerShown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerShown", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (!this.mBouncerOn) {
            this.mBouncerOn = true;
            this.mDataCollector.onBouncerShown();
        }
    }

    public void onBouncerHidden() {
        if (FalsingLog.ENABLED) {
            int i;
            String str = "onBouncerHidden";
            StringBuilder append = new StringBuilder().append("from=");
            if (this.mBouncerOn) {
                i = 1;
            } else {
                i = 0;
            }
            FalsingLog.i(str, append.append(i).toString());
        }
        if (this.mBouncerOn) {
            this.mBouncerOn = false;
            this.mDataCollector.onBouncerHidden();
        }
    }

    public void onQsDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onQsDown", BuildConfig.FLAVOR);
        }
        this.mHumanInteractionClassifier.setType(0);
        this.mDataCollector.onQsDown();
    }

    public void setQsExpanded(boolean expanded) {
        this.mDataCollector.setQsExpanded(expanded);
    }

    public void onTrackingStarted() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onTrackingStarted", BuildConfig.FLAVOR);
        }
        this.mHumanInteractionClassifier.setType(4);
        this.mDataCollector.onTrackingStarted();
    }

    public void onTrackingStopped() {
        this.mDataCollector.onTrackingStopped();
    }

    public void onNotificationActive() {
        this.mDataCollector.onNotificationActive();
    }

    public void onNotificationDoubleTap() {
        this.mDataCollector.onNotificationDoubleTap();
    }

    public void setNotificationExpanded() {
        this.mDataCollector.setNotificationExpanded();
    }

    public void onNotificatonStartDraggingDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDraggingDown", BuildConfig.FLAVOR);
        }
        this.mHumanInteractionClassifier.setType(2);
        this.mDataCollector.onNotificatonStartDraggingDown();
    }

    public void onNotificatonStopDraggingDown() {
        this.mDataCollector.onNotificatonStopDraggingDown();
    }

    public void onNotificationDismissed() {
        this.mDataCollector.onNotificationDismissed();
    }

    public void onNotificatonStartDismissing() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDismissing", BuildConfig.FLAVOR);
        }
        this.mHumanInteractionClassifier.setType(1);
        this.mDataCollector.onNotificatonStartDismissing();
    }

    public void onNotificatonStopDismissing() {
        this.mDataCollector.onNotificatonStopDismissing();
    }

    public void onUnlockHintStarted() {
        this.mDataCollector.onUnlockHintStarted();
    }

    public void onTouchEvent(MotionEvent event, int width, int height) {
        if (this.mSessionActive && !this.mBouncerOn) {
            this.mDataCollector.onTouchEvent(event, width, height);
            this.mHumanInteractionClassifier.onTouchEvent(event);
        }
    }

    public void dump(PrintWriter pw) {
        int i;
        int i2 = 1;
        pw.println("FALSING MANAGER");
        pw.print("classifierEnabled=");
        if (isClassiferEnabled()) {
            i = 1;
        } else {
            i = 0;
        }
        pw.println(i);
        pw.print("mSessionActive=");
        if (this.mSessionActive) {
            i = 1;
        } else {
            i = 0;
        }
        pw.println(i);
        pw.print("mBouncerOn=");
        if (this.mSessionActive) {
            i = 1;
        } else {
            i = 0;
        }
        pw.println(i);
        pw.print("mState=");
        pw.println(StatusBarState.toShortString(this.mState));
        pw.print("mScreenOn=");
        if (!this.mScreenOn) {
            i2 = 0;
        }
        pw.println(i2);
        pw.println();
    }
}
