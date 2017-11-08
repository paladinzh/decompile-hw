package com.huawei.thermal;

import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;

public class Watchdog extends Thread {
    static Handler sMainHandler;
    static Watchdog sWatchdog;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList();
    HandlerChecker mMonitorChecker;

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitors = new ArrayList();
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public boolean isOverdueLocked() {
            if (!this.mCompleted) {
                if (!(SystemClock.uptimeMillis() <= this.mStartTime + this.mWaitMax)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isHalfOverdueLocked() {
            if (!this.mCompleted) {
                if (!(SystemClock.uptimeMillis() <= this.mStartTime + (this.mWaitMax / 2))) {
                    return true;
                }
            }
            return false;
        }

        public int getCompletionStateLocked() {
            int i = 1;
            if (this.mCompleted) {
                return 0;
            }
            int i2;
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            if (latency >= this.mWaitMax / 2) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (i2 == 0) {
                return 1;
            }
            if (latency < this.mWaitMax) {
                i = 0;
            }
            return i == 0 ? 2 : 3;
        }

        public Thread getThread() {
            return this.mHandler.getLooper().getThread();
        }

        public String getName() {
            return this.mName;
        }

        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor != null) {
                return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName() + " on " + this.mName + " (" + getThread().getName() + ")";
            }
            return "Blocked in handler on " + this.mName + " (" + getThread().getName() + ")";
        }

        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (Watchdog.this) {
                    this.mCurrentMonitor = (Monitor) this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    public interface Monitor {
        void monitor();
    }

    public static Watchdog getInstance() {
        if (sWatchdog == null) {
            sWatchdog = new Watchdog();
        }
        return sWatchdog;
    }

    private Watchdog() {
        super("thermal-watchdog");
    }

    public void init(Handler mainHandler) {
        sMainHandler = mainHandler;
        this.mMonitorChecker = new HandlerChecker(sMainHandler, "pg thread", 10000);
        this.mHandlerCheckers.add(this.mMonitorChecker);
    }

    public void addThread(Handler thread) {
        addThread(thread, 10000);
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (isAlive()) {
                Log.w("thermal-watchdog", "Threads can't be added once the Watchdog is running, thread:" + thread + "time:" + timeoutMillis);
                return;
            }
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            state = Math.max(state, ((HandlerChecker) this.mHandlerCheckers.get(i)).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (hc.isOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private ArrayList<HandlerChecker> getHalfBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (hc.isHalfOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private String describeCheckersLocked(ArrayList<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(128);
        for (int i = 0; i < checkers.size(); i++) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(((HandlerChecker) checkers.get(i)).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    public void run() {
        Log.i("thermal-watchdog", "PG Watchdog Start");
        boolean waitedHalf = false;
        while (true) {
            synchronized (this) {
                int i;
                long timeout = 5000;
                for (i = 0; i < this.mHandlerCheckers.size(); i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                long start = SystemClock.uptimeMillis();
                while (true) {
                    if ((timeout <= 0 ? 1 : null) != null) {
                        break;
                    }
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf("thermal-watchdog", e);
                    }
                    timeout = 5000 - (SystemClock.uptimeMillis() - start);
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (waitState == 0) {
                    waitedHalf = false;
                } else if (waitState == 1) {
                } else if (waitState != 2) {
                    blockedCheckers = getBlockedCheckersLocked();
                    Slog.w("thermal-watchdog", "*** PG WATCHDOG KILLING PROCESS: " + describeCheckersLocked(blockedCheckers));
                    for (i = 0; i < blockedCheckers.size(); i++) {
                        Slog.w("thermal-watchdog", ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                        for (StackTraceElement element : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                            Slog.w("thermal-watchdog", "    at " + element);
                        }
                    }
                    Slog.w("thermal-watchdog", "*** GOODBYE!");
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                    waitedHalf = false;
                } else {
                    if (!waitedHalf) {
                        blockedCheckers = getHalfBlockedCheckersLocked();
                        Slog.w("thermal-watchdog", "*** PG WATCHDOG WAITED HALF: " + describeCheckersLocked(blockedCheckers));
                        for (i = 0; i < blockedCheckers.size(); i++) {
                            Slog.w("thermal-watchdog", ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                            for (StackTraceElement element2 : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                                Slog.w("thermal-watchdog", "    at " + element2);
                            }
                        }
                        waitedHalf = true;
                    }
                }
            }
        }
    }
}
