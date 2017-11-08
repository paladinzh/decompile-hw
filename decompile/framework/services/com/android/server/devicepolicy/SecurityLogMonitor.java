package com.android.server.devicepolicy;

import android.app.admin.SecurityLog;
import android.app.admin.SecurityLog.SecurityEvent;
import android.os.Process;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SecurityLogMonitor implements Runnable {
    private static final int BUFFER_ENTRIES_MAXIMUM_LEVEL = 10240;
    private static final int BUFFER_ENTRIES_NOTIFICATION_LEVEL = 1024;
    private static final boolean DEBUG = false;
    private static final long POLLING_INTERVAL_MILLISECONDS = TimeUnit.MINUTES.toMillis(1);
    private static final long RATE_LIMIT_INTERVAL_MILLISECONDS = TimeUnit.HOURS.toMillis(2);
    private static final String TAG = "SecurityLogMonitor";
    @GuardedBy("mLock")
    private boolean mAllowedToRetrieve = false;
    private final Lock mLock = new ReentrantLock();
    @GuardedBy("mLock")
    private Thread mMonitorThread = null;
    @GuardedBy("mLock")
    private long mNextAllowedRetrivalTimeMillis = -1;
    @GuardedBy("mLock")
    private ArrayList<SecurityEvent> mPendingLogs = new ArrayList();
    private final DevicePolicyManagerService mService;

    SecurityLogMonitor(DevicePolicyManagerService service) {
        this.mService = service;
    }

    void start() {
        this.mLock.lock();
        try {
            if (this.mMonitorThread == null) {
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = -1;
                this.mMonitorThread = new Thread(this);
                this.mMonitorThread.start();
            }
            this.mLock.unlock();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }

    void stop() {
        this.mLock.lock();
        try {
            if (this.mMonitorThread != null) {
                this.mMonitorThread.interrupt();
                this.mMonitorThread.join(TimeUnit.SECONDS.toMillis(5));
                this.mPendingLogs = new ArrayList();
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = -1;
                this.mMonitorThread = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for thread to stop", e);
        } catch (Throwable th) {
            this.mLock.unlock();
        }
        this.mLock.unlock();
    }

    List<SecurityEvent> retrieveLogs() {
        this.mLock.lock();
        try {
            if (this.mAllowedToRetrieve) {
                this.mAllowedToRetrieve = false;
                this.mNextAllowedRetrivalTimeMillis = System.currentTimeMillis() + RATE_LIMIT_INTERVAL_MILLISECONDS;
                List<SecurityEvent> result = this.mPendingLogs;
                this.mPendingLogs = new ArrayList();
                return result;
            }
            this.mLock.unlock();
            return null;
        } finally {
            this.mLock.unlock();
        }
    }

    public void run() {
        Process.setThreadPriority(10);
        ArrayList<SecurityEvent> logs = new ArrayList();
        long lastLogTimestampNanos = -1;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(POLLING_INTERVAL_MILLISECONDS);
                if (lastLogTimestampNanos < 0) {
                    SecurityLog.readEvents(logs);
                } else {
                    SecurityLog.readEventsSince(1 + lastLogTimestampNanos, logs);
                }
                if (!logs.isEmpty()) {
                    this.mLock.lockInterruptibly();
                    this.mPendingLogs.addAll(logs);
                    if (this.mPendingLogs.size() > BUFFER_ENTRIES_MAXIMUM_LEVEL) {
                        this.mPendingLogs = new ArrayList(this.mPendingLogs.subList(this.mPendingLogs.size() - 5120, this.mPendingLogs.size()));
                    }
                    this.mLock.unlock();
                    lastLogTimestampNanos = ((SecurityEvent) logs.get(logs.size() - 1)).getTimeNanos();
                    logs.clear();
                }
                notifyDeviceOwnerIfNeeded();
            } catch (IOException e) {
                Log.e(TAG, "Failed to read security log", e);
            } catch (InterruptedException e2) {
                Log.i(TAG, "Thread interrupted, exiting.", e2);
                return;
            } catch (Throwable th) {
                this.mLock.unlock();
            }
        }
    }

    private void notifyDeviceOwnerIfNeeded() throws InterruptedException {
        boolean z = false;
        boolean allowToRetrieveNow = false;
        this.mLock.lockInterruptibly();
        try {
            int logSize = this.mPendingLogs.size();
            if (logSize >= 1024) {
                allowToRetrieveNow = true;
            } else if (logSize > 0) {
                if (this.mNextAllowedRetrivalTimeMillis == -1 || System.currentTimeMillis() >= this.mNextAllowedRetrivalTimeMillis) {
                    allowToRetrieveNow = true;
                }
            }
            z = !this.mAllowedToRetrieve ? allowToRetrieveNow : false;
            this.mAllowedToRetrieve = allowToRetrieveNow;
            if (z) {
                this.mService.sendDeviceOwnerCommand("android.app.action.SECURITY_LOGS_AVAILABLE", null);
            }
        } finally {
            this.mLock.unlock();
        }
    }
}
