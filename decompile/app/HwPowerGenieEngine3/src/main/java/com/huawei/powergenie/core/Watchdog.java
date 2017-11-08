package com.huawei.powergenie.core;

import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.huawei.powergenie.debugtest.DbgUtils;
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
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        }

        public boolean isHalfOverdueLocked() {
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + (this.mWaitMax / 2);
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            if (latency < this.mWaitMax / 2) {
                return 1;
            }
            if (latency < this.mWaitMax) {
                return 2;
            }
            return 3;
        }

        public Thread getThread() {
            return this.mHandler.getLooper().getThread();
        }

        public String getName() {
            return this.mName;
        }

        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor == null) {
                return "Blocked in handler on " + this.mName + " (" + getThread().getName() + ")";
            }
            return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName() + " on " + this.mName + " (" + getThread().getName() + ")";
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
        super("watchdog");
    }

    public void init(Handler mainHandler) {
        sMainHandler = mainHandler;
        this.mMonitorChecker = new HandlerChecker(sMainHandler, "pg thread", 20000);
        this.mHandlerCheckers.add(this.mMonitorChecker);
    }

    public void addThread(Handler thread) {
        addThread(thread, 20000);
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Threads can't be added once the Watchdog is running");
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
        Log.i("PG_Watchdog", "PG Watchdog Start");
        boolean waitedHalf = false;
        while (true) {
            synchronized (this) {
                int i;
                for (i = 0; i < this.mHandlerCheckers.size(); i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = 10000; timeout > 0; timeout = 10000 - (SystemClock.uptimeMillis() - start)) {
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf("PG_Watchdog", e);
                    }
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (waitState == 0) {
                    waitedHalf = false;
                } else if (waitState != 1) {
                    ArrayList<HandlerChecker> blockedCheckers;
                    if (waitState != 2) {
                        blockedCheckers = getBlockedCheckersLocked();
                        Slog.w("PG_Watchdog", "*** PG WATCHDOG KILLING PROCESS: " + describeCheckersLocked(blockedCheckers));
                        for (i = 0; i < blockedCheckers.size(); i++) {
                            Slog.w("PG_Watchdog", ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                            for (StackTraceElement element : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                                Slog.w("PG_Watchdog", "    at " + element);
                            }
                        }
                        Slog.w("PG_Watchdog", "*** GOODBYE!");
                        Process.killProcess(Process.myPid());
                        System.exit(10);
                        waitedHalf = false;
                    } else if (!waitedHalf) {
                        blockedCheckers = getHalfBlockedCheckersLocked();
                        Slog.w("PG_Watchdog", "*** PG WATCHDOG WAITED HALF: " + describeCheckersLocked(blockedCheckers));
                        for (i = 0; i < blockedCheckers.size(); i++) {
                            Slog.w("PG_Watchdog", ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                            for (StackTraceElement element2 : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                                Slog.w("PG_Watchdog", "    at " + element2);
                            }
                        }
                        waitedHalf = true;
                        DbgUtils.sendNotification("watch dog half", "blocked more than 5 minute");
                    }
                }
            }
        }
    }
}
