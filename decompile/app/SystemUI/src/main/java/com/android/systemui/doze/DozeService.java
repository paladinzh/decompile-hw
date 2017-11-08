package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.AudioAttributes.Builder;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;
import android.service.dreams.DreamService;
import android.util.Log;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.doze.DozeHost.Callback;
import com.android.systemui.doze.DozeHost.PulseCallback;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.DozeParameters.PulseSchedule;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Date;

public class DozeService extends DreamService {
    private static final boolean DEBUG = Log.isLoggable("DozeService", 3);
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.doze.pulse".equals(intent.getAction())) {
                if (DozeService.DEBUG) {
                    Log.d(DozeService.this.mTag, "Received pulse intent");
                }
                DozeService.this.requestPulse(0);
            }
            if ("com.android.systemui.doze.notification_pulse".equals(intent.getAction())) {
                long instance = intent.getLongExtra("instance", -1);
                if (DozeService.DEBUG) {
                    Log.d(DozeService.this.mTag, "Received notification pulse intent instance=" + instance);
                }
                DozeLog.traceNotificationPulse(instance);
                DozeService.this.requestPulse(1);
                DozeService.this.rescheduleNotificationPulse(DozeService.this.mNotificationLightOn);
            }
            if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
                DozeService.this.mCarMode = true;
                if (DozeService.this.mCarMode && DozeService.this.mDreaming) {
                    DozeService.this.finishForCarMode();
                }
            }
        }
    };
    private boolean mBroadcastReceiverRegistered;
    private boolean mCarMode;
    private final Context mContext = this;
    private boolean mDisplayStateSupported;
    private final DozeParameters mDozeParameters = new DozeParameters(this.mContext);
    private boolean mDreaming;
    private long mEarliestPulseDueToLight;
    private final Handler mHandler = new Handler();
    private DozeHost mHost;
    private final Callback mHostCallback = new Callback() {
        public void onNewNotifications() {
            if (DozeService.DEBUG) {
                Log.d(DozeService.this.mTag, "onNewNotifications (noop)");
            }
        }

        public void onBuzzBeepBlinked() {
            if (DozeService.DEBUG) {
                Log.d(DozeService.this.mTag, "onBuzzBeepBlinked");
            }
            DozeService.this.updateNotificationPulse(System.currentTimeMillis());
        }

        public void onNotificationLight(boolean on) {
            if (DozeService.DEBUG) {
                Log.d(DozeService.this.mTag, "onNotificationLight on=" + on);
            }
            if (DozeService.this.mNotificationLightOn != on) {
                DozeService.this.mNotificationLightOn = on;
                if (DozeService.this.mNotificationLightOn) {
                    DozeService.this.updateNotificationPulseDueToLight();
                }
            }
        }

        public void onPowerSaveChanged(boolean active) {
            DozeService.this.mPowerSaveActive = active;
            if (DozeService.this.mPowerSaveActive && DozeService.this.mDreaming) {
                DozeService.this.finishToSavePower();
            }
        }
    };
    private long mLastScheduleResetTime;
    private boolean mNotificationLightOn;
    private long mNotificationPulseTime;
    private TriggerSensor mPickupSensor;
    private PowerManager mPowerManager;
    private boolean mPowerSaveActive;
    private boolean mPulsing;
    private int mScheduleResetsRemaining;
    private SensorManager mSensors;
    private TriggerSensor mSigMotionSensor;
    private final String mTag = String.format("DozeService.%08x", new Object[]{Integer.valueOf(hashCode())});
    private UiModeManager mUiModeManager;
    private WakeLock mWakeLock;

    private abstract class ProximityCheck implements SensorEventListener, Runnable {
        private boolean mFinished;
        private float mMaxRange;
        private boolean mRegistered;
        private final String mTag;

        public abstract void onProximityResult(int i);

        private ProximityCheck() {
            this.mTag = DozeService.this.mTag + ".ProximityCheck";
        }

        public void check() {
            if (!this.mFinished && !this.mRegistered) {
                Sensor sensor = DozeService.this.mSensors.getDefaultSensor(8);
                if (sensor == null) {
                    if (DozeService.DEBUG) {
                        Log.d(this.mTag, "No sensor found");
                    }
                    finishWithResult(0);
                    return;
                }
                DozeService.this.mPickupSensor.setDisabled(true);
                this.mMaxRange = sensor.getMaximumRange();
                DozeService.this.mSensors.registerListener(this, sensor, 3, 0, DozeService.this.mHandler);
                DozeService.this.mHandler.postDelayed(this, 500);
                this.mRegistered = true;
            }
        }

        public void onSensorChanged(SensorEvent event) {
            int i = 1;
            boolean isNear = false;
            if (event.values.length == 0) {
                if (DozeService.DEBUG) {
                    Log.d(this.mTag, "Event has no values!");
                }
                finishWithResult(0);
                return;
            }
            if (DozeService.DEBUG) {
                Log.d(this.mTag, "Event: value=" + event.values[0] + " max=" + this.mMaxRange);
            }
            if (event.values[0] < this.mMaxRange) {
                isNear = true;
            }
            if (!isNear) {
                i = 2;
            }
            finishWithResult(i);
        }

        public void run() {
            if (DozeService.DEBUG) {
                Log.d(this.mTag, "No event received before timeout");
            }
            finishWithResult(0);
        }

        private void finishWithResult(int result) {
            if (!this.mFinished) {
                if (this.mRegistered) {
                    DozeService.this.mHandler.removeCallbacks(this);
                    DozeService.this.mSensors.unregisterListener(this);
                    DozeService.this.mPickupSensor.setDisabled(false);
                    this.mRegistered = false;
                }
                onProximityResult(result);
                this.mFinished = true;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class TriggerSensor extends TriggerEventListener {
        private final boolean mConfigured;
        private final boolean mDebugVibrate;
        private boolean mDisabled;
        private final int mPulseReason;
        private boolean mRegistered;
        private boolean mRequested;
        private final Sensor mSensor;

        public TriggerSensor(int type, boolean configured, boolean debugVibrate, int pulseReason) {
            this.mSensor = DozeService.this.mSensors.getDefaultSensor(type);
            this.mConfigured = configured;
            this.mDebugVibrate = debugVibrate;
            this.mPulseReason = pulseReason;
        }

        public void setListening(boolean listen) {
            if (this.mRequested != listen) {
                this.mRequested = listen;
                updateListener();
            }
        }

        public void setDisabled(boolean disabled) {
            if (this.mDisabled != disabled) {
                this.mDisabled = disabled;
                updateListener();
            }
        }

        private void updateListener() {
            if (this.mConfigured && this.mSensor != null) {
                if (this.mRequested && !this.mDisabled && !this.mRegistered) {
                    this.mRegistered = DozeService.this.mSensors.requestTriggerSensor(this, this.mSensor);
                    if (DozeService.DEBUG) {
                        Log.d(DozeService.this.mTag, "requestTriggerSensor " + this.mRegistered);
                    }
                } else if (this.mRegistered) {
                    boolean rt = DozeService.this.mSensors.cancelTriggerSensor(this, this.mSensor);
                    if (DozeService.DEBUG) {
                        Log.d(DozeService.this.mTag, "cancelTriggerSensor " + rt);
                    }
                    this.mRegistered = false;
                }
            }
        }

        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mDebugVibrate=" + this.mDebugVibrate + ", mSensor=" + this.mSensor + "}";
        }

        public void onTrigger(TriggerEvent event) {
            boolean withinVibrationThreshold = false;
            DozeService.this.mWakeLock.acquire();
            try {
                if (DozeService.DEBUG) {
                    Log.d(DozeService.this.mTag, "onTrigger: " + DozeService.triggerEventToString(event));
                }
                if (this.mDebugVibrate) {
                    Vibrator v = (Vibrator) DozeService.this.mContext.getSystemService("vibrator");
                    if (v != null) {
                        v.vibrate(1000, new Builder().setContentType(4).setUsage(13).build());
                    }
                }
                this.mRegistered = false;
                DozeService.this.requestPulse(this.mPulseReason);
                updateListener();
                if (System.currentTimeMillis() - DozeService.this.mNotificationPulseTime < ((long) DozeService.this.mDozeParameters.getPickupVibrationThreshold())) {
                    withinVibrationThreshold = true;
                }
                if (!withinVibrationThreshold) {
                    DozeService.this.resetNotificationResets();
                } else if (DozeService.DEBUG) {
                    Log.d(DozeService.this.mTag, "Not resetting schedule, recent notification");
                }
                if (this.mSensor.getType() == 25) {
                    DozeLog.tracePickupPulse(withinVibrationThreshold);
                }
                DozeService.this.mWakeLock.release();
            } catch (Throwable th) {
                DozeService.this.mWakeLock.release();
            }
        }
    }

    public DozeService() {
        if (DEBUG) {
            Log.d(this.mTag, "new DozeService()");
        }
        setDebug(DEBUG);
    }

    protected void dumpOnHandler(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dumpOnHandler(fd, pw, args);
        pw.print("  mDreaming: ");
        pw.println(this.mDreaming);
        pw.print("  mPulsing: ");
        pw.println(this.mPulsing);
        pw.print("  mWakeLock: held=");
        pw.println(this.mWakeLock.isHeld());
        pw.print("  mHost: ");
        pw.println(this.mHost);
        pw.print("  mBroadcastReceiverRegistered: ");
        pw.println(this.mBroadcastReceiverRegistered);
        pw.print("  mSigMotionSensor: ");
        pw.println(this.mSigMotionSensor);
        pw.print("  mPickupSensor:");
        pw.println(this.mPickupSensor);
        pw.print("  mDisplayStateSupported: ");
        pw.println(this.mDisplayStateSupported);
        pw.print("  mNotificationLightOn: ");
        pw.println(this.mNotificationLightOn);
        pw.print("  mPowerSaveActive: ");
        pw.println(this.mPowerSaveActive);
        pw.print("  mCarMode: ");
        pw.println(this.mCarMode);
        pw.print("  mNotificationPulseTime: ");
        pw.println(this.mNotificationPulseTime);
        pw.print("  mScheduleResetsRemaining: ");
        pw.println(this.mScheduleResetsRemaining);
        this.mDozeParameters.dump(pw);
    }

    public void onCreate() {
        if (DEBUG) {
            Log.d(this.mTag, "onCreate");
        }
        super.onCreate();
        if (getApplication() instanceof SystemUIApplication) {
            this.mHost = (DozeHost) ((SystemUIApplication) getApplication()).getComponent(DozeHost.class);
        }
        if (this.mHost == null) {
            Log.w("DozeService", "No doze service host found.");
        }
        setWindowless(true);
        this.mSensors = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSigMotionSensor = new TriggerSensor(17, this.mDozeParameters.getPulseOnSigMotion(), this.mDozeParameters.getVibrateOnSigMotion(), 2);
        this.mPickupSensor = new TriggerSensor(25, this.mDozeParameters.getPulseOnPickup(), this.mDozeParameters.getVibrateOnPickup(), 3);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "DozeService");
        this.mWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDisplayStateSupported = this.mDozeParameters.getDisplayStateSupported();
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService("uimode");
        turnDisplayOff();
    }

    public void onAttachedToWindow() {
        if (DEBUG) {
            Log.d(this.mTag, "onAttachedToWindow");
        }
        super.onAttachedToWindow();
    }

    public void onDreamingStarted() {
        super.onDreamingStarted();
        if (this.mHost == null) {
            finish();
            return;
        }
        this.mPowerSaveActive = this.mHost.isPowerSaveActive();
        this.mCarMode = this.mUiModeManager.getCurrentModeType() == 3;
        if (DEBUG) {
            Log.d(this.mTag, "onDreamingStarted canDoze=" + canDoze() + " mPowerSaveActive=" + this.mPowerSaveActive + " mCarMode=" + this.mCarMode);
        }
        if (this.mPowerSaveActive) {
            finishToSavePower();
        } else if (this.mCarMode) {
            finishForCarMode();
        } else {
            this.mDreaming = true;
            rescheduleNotificationPulse(false);
            this.mEarliestPulseDueToLight = System.currentTimeMillis() + 10000;
            listenForPulseSignals(true);
            this.mHost.startDozing(new Runnable() {
                public void run() {
                    if (DozeService.this.mDreaming) {
                        DozeService.this.startDozing();
                    }
                }
            });
        }
    }

    public void onDreamingStopped() {
        if (DEBUG) {
            Log.d(this.mTag, "onDreamingStopped isDozing=" + isDozing());
        }
        super.onDreamingStopped();
        if (this.mHost != null) {
            this.mDreaming = false;
            listenForPulseSignals(false);
            this.mHost.stopDozing();
        }
    }

    private void requestPulse(int reason) {
        if (!(this.mHost == null || !this.mDreaming || this.mPulsing)) {
            this.mWakeLock.acquire();
            this.mPulsing = true;
            if (this.mDozeParameters.getProxCheckBeforePulse()) {
                boolean nonBlocking;
                final long start = SystemClock.uptimeMillis();
                if (reason == 3) {
                    nonBlocking = this.mDozeParameters.getPickupPerformsProxCheck();
                } else {
                    nonBlocking = false;
                }
                if (nonBlocking) {
                    continuePulsing(reason);
                }
                final DozeService dozeService = this;
                final int i = reason;
                new ProximityCheck(this) {
                    public void onProximityResult(int result) {
                        boolean isNear = result == 1;
                        DozeLog.traceProximityResult(dozeService.mContext, isNear, SystemClock.uptimeMillis() - start, i);
                        if (!nonBlocking) {
                            if (isNear) {
                                dozeService.mPulsing = false;
                                dozeService.mWakeLock.release();
                                return;
                            }
                            dozeService.continuePulsing(i);
                        }
                    }
                }.check();
            } else {
                continuePulsing(reason);
            }
        }
    }

    private void continuePulsing(int reason) {
        if (this.mHost.isPulsingBlocked()) {
            this.mPulsing = false;
            this.mWakeLock.release();
            return;
        }
        this.mHost.pulseWhileDozing(new PulseCallback() {
            public void onPulseStarted() {
                if (DozeService.this.mPulsing && DozeService.this.mDreaming) {
                    DozeService.this.turnDisplayOn();
                }
            }

            public void onPulseFinished() {
                if (DozeService.this.mPulsing && DozeService.this.mDreaming) {
                    DozeService.this.mPulsing = false;
                    DozeService.this.turnDisplayOff();
                }
                DozeService.this.mWakeLock.release();
            }
        }, reason);
    }

    private void turnDisplayOff() {
        if (DEBUG) {
            Log.d(this.mTag, "Display off");
        }
        setDozeScreenState(1);
    }

    private void turnDisplayOn() {
        if (DEBUG) {
            Log.d(this.mTag, "Display on");
        }
        setDozeScreenState(this.mDisplayStateSupported ? 3 : 2);
    }

    private void finishToSavePower() {
        Log.w(this.mTag, "Exiting ambient mode due to low power battery saver");
        finish();
    }

    private void finishForCarMode() {
        Log.w(this.mTag, "Exiting ambient mode, not allowed in car mode");
        finish();
    }

    private void listenForPulseSignals(boolean listen) {
        if (DEBUG) {
            Log.d(this.mTag, "listenForPulseSignals: " + listen);
        }
        this.mSigMotionSensor.setListening(listen);
        this.mPickupSensor.setListening(listen);
        listenForBroadcasts(listen);
        listenForNotifications(listen);
    }

    private void listenForBroadcasts(boolean listen) {
        if (listen) {
            IntentFilter filter = new IntentFilter("com.android.systemui.doze.pulse");
            filter.addAction("com.android.systemui.doze.notification_pulse");
            filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
            this.mBroadcastReceiverRegistered = true;
            return;
        }
        if (this.mBroadcastReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
        this.mBroadcastReceiverRegistered = false;
    }

    private void listenForNotifications(boolean listen) {
        if (listen) {
            resetNotificationResets();
            this.mHost.addCallback(this.mHostCallback);
            this.mNotificationLightOn = this.mHost.isNotificationLightOn();
            if (this.mNotificationLightOn) {
                updateNotificationPulseDueToLight();
                return;
            }
            return;
        }
        this.mHost.removeCallback(this.mHostCallback);
    }

    private void resetNotificationResets() {
        if (DEBUG) {
            Log.d(this.mTag, "resetNotificationResets");
        }
        this.mScheduleResetsRemaining = this.mDozeParameters.getPulseScheduleResets();
    }

    private void updateNotificationPulseDueToLight() {
        updateNotificationPulse(Math.max(System.currentTimeMillis(), this.mEarliestPulseDueToLight));
    }

    private void updateNotificationPulse(long notificationTimeMs) {
        if (DEBUG) {
            Log.d(this.mTag, "updateNotificationPulse notificationTimeMs=" + notificationTimeMs);
        }
        if (!this.mDozeParameters.getPulseOnNotifications()) {
            return;
        }
        if (this.mScheduleResetsRemaining <= 0) {
            if (DEBUG) {
                Log.d(this.mTag, "No more schedule resets remaining");
            }
            return;
        }
        long pulseDuration = (long) this.mDozeParameters.getPulseDuration(false);
        boolean pulseImmediately = System.currentTimeMillis() >= notificationTimeMs;
        if (notificationTimeMs - this.mLastScheduleResetTime >= pulseDuration) {
            this.mScheduleResetsRemaining--;
            this.mLastScheduleResetTime = notificationTimeMs;
        } else if (!pulseImmediately) {
            if (DEBUG) {
                Log.d(this.mTag, "Recently updated, not resetting schedule");
            }
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "mScheduleResetsRemaining = " + this.mScheduleResetsRemaining);
        }
        this.mNotificationPulseTime = notificationTimeMs;
        if (pulseImmediately) {
            DozeLog.traceNotificationPulse(0);
            requestPulse(1);
        }
        rescheduleNotificationPulse(true);
    }

    private PendingIntent notificationPulseIntent(long instance) {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.doze.notification_pulse").setPackage(getPackageName()).putExtra("instance", instance).setFlags(268435456), 134217728);
    }

    private void rescheduleNotificationPulse(boolean predicate) {
        if (DEBUG) {
            Log.d(this.mTag, "rescheduleNotificationPulse predicate=" + predicate);
        }
        this.mAlarmManager.cancel(notificationPulseIntent(0));
        if (predicate) {
            PulseSchedule schedule = this.mDozeParameters.getPulseSchedule();
            if (schedule == null) {
                if (DEBUG) {
                    Log.d(this.mTag, "  don't reschedule: schedule is null");
                }
                return;
            }
            long now = System.currentTimeMillis();
            long time = schedule.getNextTime(now, this.mNotificationPulseTime);
            if (time <= 0) {
                if (DEBUG) {
                    Log.d(this.mTag, "  don't reschedule: time is " + time);
                }
                return;
            }
            long delta = time - now;
            if (delta <= 0) {
                if (DEBUG) {
                    Log.d(this.mTag, "  don't reschedule: delta is " + delta);
                }
                return;
            }
            long instance = time - this.mNotificationPulseTime;
            if (DEBUG) {
                Log.d(this.mTag, "Scheduling pulse " + instance + " in " + delta + "ms for " + new Date(time));
            }
            this.mAlarmManager.setExact(0, time, notificationPulseIntent(instance));
            return;
        }
        if (DEBUG) {
            Log.d(this.mTag, "  don't reschedule: predicate is false");
        }
    }

    private static String triggerEventToString(TriggerEvent event) {
        if (event == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("TriggerEvent[").append(event.timestamp).append(',').append(event.sensor.getName());
        if (event.values != null) {
            for (float append : event.values) {
                sb.append(',').append(append);
            }
        }
        return sb.append(']').toString();
    }
}
