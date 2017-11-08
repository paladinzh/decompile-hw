package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;

class ActivityMetricsLogger {
    private static final long INVALID_START_TIME = -1;
    private static final String TAG = "ActivityManager";
    private static final String[] TRON_WINDOW_STATE_VARZ_STRINGS = new String[]{"window_time_0", "window_time_1", "window_time_2"};
    private static final int WINDOW_STATE_FREEFORM = 2;
    private static final int WINDOW_STATE_INVALID = -1;
    private static final int WINDOW_STATE_SIDE_BY_SIDE = 1;
    private static final int WINDOW_STATE_STANDARD = 0;
    private final Context mContext;
    private long mCurrentTransitionStartTime = -1;
    private long mLastLogTimeSecs = (SystemClock.elapsedRealtime() / 1000);
    private boolean mLoggedStartingWindowDrawn;
    private boolean mLoggedTransitionStarting;
    private boolean mLoggedWindowsDrawn;
    private final ActivityStackSupervisor mSupervisor;
    private int mWindowState = 0;

    ActivityMetricsLogger(ActivityStackSupervisor supervisor, Context context) {
        this.mSupervisor = supervisor;
        this.mContext = context;
    }

    void logWindowState() {
        long now = SystemClock.elapsedRealtime() / 1000;
        if (this.mWindowState != -1) {
            MetricsLogger.count(this.mContext, TRON_WINDOW_STATE_VARZ_STRINGS[this.mWindowState], (int) (now - this.mLastLogTimeSecs));
        }
        this.mLastLogTimeSecs = now;
        ActivityStack stack = this.mSupervisor.getStack(3);
        if (stack == null || stack.getStackVisibilityLocked(null) == 0) {
            this.mWindowState = -1;
            stack = this.mSupervisor.getFocusedStack();
            if (stack.mStackId == 4) {
                stack = this.mSupervisor.findStackBehind(stack);
            }
            if (stack.mStackId == 0 || stack.mStackId == 1) {
                this.mWindowState = 0;
            } else if (stack.mStackId == 3) {
                Slog.wtf(TAG, "Docked stack shouldn't be the focused stack, because it reported not being visible.");
                this.mWindowState = -1;
            } else if (stack.mStackId == 2) {
                this.mWindowState = 2;
            } else if (StackId.isStaticStack(stack.mStackId)) {
                throw new IllegalStateException("Unknown stack=" + stack);
            }
            return;
        }
        this.mWindowState = 1;
    }

    void notifyActivityLaunching() {
        this.mCurrentTransitionStartTime = System.currentTimeMillis();
    }

    void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity) {
        String str;
        boolean processSwitch = true;
        ProcessRecord processRecord = null;
        if (launchedActivity != null) {
            processRecord = (ProcessRecord) this.mSupervisor.mService.mProcessNames.get(launchedActivity.processName, launchedActivity.appInfo.uid);
        }
        boolean processRunning = processRecord != null;
        if (launchedActivity != null) {
            str = launchedActivity.shortComponentName;
        } else {
            str = null;
        }
        if (processRecord != null && hasStartedActivity(processRecord, launchedActivity)) {
            processSwitch = false;
        }
        notifyActivityLaunched(resultCode, str, processRunning, processSwitch);
    }

    private boolean hasStartedActivity(ProcessRecord record, ActivityRecord launchedActivity) {
        ArrayList<ActivityRecord> activities = record.activities;
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activity = (ActivityRecord) activities.get(i);
            if (launchedActivity != activity && !activity.stopped) {
                return true;
            }
        }
        return false;
    }

    private void notifyActivityLaunched(int resultCode, String componentName, boolean processRunning, boolean processSwitch) {
        if (resultCode < 0 || componentName == null || !processSwitch) {
            reset();
            return;
        }
        MetricsLogger.action(this.mContext, 323, componentName);
        MetricsLogger.action(this.mContext, 324, processRunning);
        MetricsLogger.action(this.mContext, 325, (int) (SystemClock.uptimeMillis() / 1000));
    }

    void notifyWindowsDrawn() {
        if (isTransitionActive() && !this.mLoggedWindowsDrawn) {
            MetricsLogger.action(this.mContext, 322, calculateCurrentDelay());
            this.mLoggedWindowsDrawn = true;
            if (this.mLoggedTransitionStarting) {
                reset();
            }
        }
    }

    void notifyStartingWindowDrawn() {
        if (isTransitionActive() && !this.mLoggedStartingWindowDrawn) {
            this.mLoggedStartingWindowDrawn = true;
            MetricsLogger.action(this.mContext, 321, calculateCurrentDelay());
        }
    }

    void notifyTransitionStarting(int reason) {
        if (isTransitionActive() && !this.mLoggedTransitionStarting) {
            MetricsLogger.action(this.mContext, 320, reason);
            MetricsLogger.action(this.mContext, 319, calculateCurrentDelay());
            this.mLoggedTransitionStarting = true;
            if (this.mLoggedWindowsDrawn) {
                reset();
            }
        }
    }

    private boolean isTransitionActive() {
        return this.mCurrentTransitionStartTime != -1;
    }

    private void reset() {
        this.mCurrentTransitionStartTime = -1;
        this.mLoggedWindowsDrawn = false;
        this.mLoggedTransitionStarting = false;
        this.mLoggedStartingWindowDrawn = false;
    }

    private int calculateCurrentDelay() {
        return (int) (System.currentTimeMillis() - this.mCurrentTransitionStartTime);
    }
}
