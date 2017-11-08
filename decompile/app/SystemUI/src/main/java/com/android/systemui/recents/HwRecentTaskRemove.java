package com.android.systemui.recents;

import android.app.ActivityManager.RecentTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.HwRecentsView;
import com.android.systemui.utils.HwLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HwRecentTaskRemove {
    private static HwRecentTaskRemove hwRecentTaskRemove;
    private Context mContext;
    private Thread mRemoveTaskThread = null;
    private long mRequestRemoveTaskClockTime = 0;
    private long mRequestRemoveTaskSystemTime = 0;

    private class RemoveTaskThread extends Thread {
        CountDownLatch latch = new CountDownLatch(1);
        Context mContextInThread = null;
        long mRemoveInitMemorySize;
        long mRemoveOldAvailMemorySize;
        BroadcastReceiver mRemoveTaskOverReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("HwRecentTaskRemove", "enter mRemoveTaskOverReceiver onReceive");
                if (intent == null) {
                    Log.e("HwRecentTaskRemove", "in mRemoveTaskOverReceiver intent is null");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Log.e("HwRecentTaskRemove", "in mRemoveTaskOverReceiver action is null");
                    return;
                }
                if ("com.huawei.systemmanager.action.REPLY_TRIM_ALL".equals(action)) {
                    Log.d("HwRecentTaskRemove", "get a remove task finish rsp, rspId:" + intent.getLongExtra("request_id", 0));
                    RemoveTaskThread.this.latch.countDown();
                } else {
                    Log.e("HwRecentTaskRemove", "unKnow action in mRemoveTaskOverReceiver:" + action);
                }
            }
        };
        private long mStartRemoveTaskClockTime = 0;

        public RemoveTaskThread(Context context, long availMemorySize, long initMemorySize) {
            super("RemoveTaskThread_" + new SimpleDateFormat("HH:MM:ss SSS", Locale.getDefault()).format(new Date()));
            this.mContextInThread = context;
            this.mRemoveOldAvailMemorySize = availMemorySize;
            this.mRemoveInitMemorySize = initMemorySize;
        }

        public void run() {
            if (HwRecentTaskRemove.this.needRequestCallTrimProcess(this.mStartRemoveTaskClockTime)) {
                Log.d("HwRecentTaskRemove", "RemoveTaskThread " + this + ", availMemorySize:" + this.mRemoveOldAvailMemorySize + ", initMemorySize:" + this.mRemoveInitMemorySize + ", start!!");
                this.mStartRemoveTaskClockTime = SystemClock.elapsedRealtime();
                removeTaskProcess();
                HwRecentsView.showMemoryToast(HwRecentTaskRemove.this.mContext, this.mRemoveOldAvailMemorySize, this.mRemoveInitMemorySize);
                Log.d("HwRecentTaskRemove", "RemoveTaskThread " + this + ", depose over!");
            } else {
                Log.e("HwRecentTaskRemove", "cannot trigeTrimProcess, mStartRemoveTaskClockTime:" + this.mStartRemoveTaskClockTime + ",mRequestRemoveTaskClockTime:" + HwRecentTaskRemove.this.geRequestRemoveTaskClockTime() + ",SystemClock.elapsedRealtime:" + SystemClock.elapsedRealtime());
            }
            HwRecentTaskRemove.this.releaseRemoveTask();
        }

        private void removeTaskProcess() {
            this.latch = new CountDownLatch(1);
            registerReceiver();
            Log.d("HwRecentTaskRemove", "ready to send broadcast for trim process");
            long startRequestRemoveTaskTime = System.currentTimeMillis();
            this.mContextInThread.sendBroadcast(getRemoveTaskRequestIntent(), "com.huawei.android.launcher.permission.ONEKEYCLEAN");
            try {
                if (this.latch.await(20000, TimeUnit.MILLISECONDS)) {
                    Log.e("HwRecentTaskRemove", "wait for remove task timeout!! time:20000ms");
                }
            } catch (InterruptedException e) {
            }
            unregisterReceiver();
            Log.d("HwRecentTaskRemove", "after remove all task, useTime:" + (System.currentTimeMillis() - startRequestRemoveTaskTime) + "ms");
        }

        private Intent getRemoveTaskRequestIntent() {
            long currentRequestId = System.currentTimeMillis();
            Log.d("HwRecentTaskRemove", "gener requestId:" + currentRequestId + " for remove all task");
            return new Intent("com.huawei.systemmanager.action.REQUEST_TRIM_ALL").putExtra("request_id", currentRequestId).putExtra("start_time", HwRecentTaskRemove.this.getRemoveTaskTime());
        }

        private void registerReceiver() {
            this.mContextInThread.registerReceiver(this.mRemoveTaskOverReceiver, new IntentFilter("com.huawei.systemmanager.action.REPLY_TRIM_ALL"), "com.android.systemui.permission.removeTask", null);
        }

        private void unregisterReceiver() {
            this.mContextInThread.unregisterReceiver(this.mRemoveTaskOverReceiver);
        }
    }

    public static synchronized HwRecentTaskRemove getInstance(Context context) {
        synchronized (HwRecentTaskRemove.class) {
            if (hwRecentTaskRemove != null) {
                HwRecentTaskRemove hwRecentTaskRemove = hwRecentTaskRemove;
                return hwRecentTaskRemove;
            }
            hwRecentTaskRemove = new HwRecentTaskRemove(context);
            hwRecentTaskRemove = hwRecentTaskRemove;
            return hwRecentTaskRemove;
        }
    }

    public void sendRemoveTaskToSystemManager(Task task) {
        if (task == null || task.key == null) {
            HwLog.i("HwRecentTaskRemove", "(task == null || task.key == null), return");
            return;
        }
        HwLog.i("HwRecentTaskRemove", "remove task send broadcast packageName=" + task.packageName + ", userId=" + task.key.userId);
        Intent intent = new Intent("huawei.intent.action.hsm_remove_pkg");
        intent.putExtra("pkg_name", task.packageName);
        intent.putExtra("userid", task.key.userId);
        intent.setPackage("com.huawei.systemmanager");
        this.mContext.sendBroadcast(intent);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void notifyRemoveStart(long clickTime) {
        Log.i("HwRecentTaskRemove", "notifyRemoveStart called, clickTime:" + clickTime);
        if (this.mRemoveTaskThread != null) {
            if (this.mRemoveTaskThread.isAlive()) {
                Log.d("HwRecentTaskRemove", "is already running,just wait!");
            } else {
                if (clickTime > 0) {
                    this.mRequestRemoveTaskSystemTime = clickTime;
                }
                this.mRemoveTaskThread.start();
            }
        }
    }

    private synchronized long getRemoveTaskTime() {
        return this.mRequestRemoveTaskSystemTime;
    }

    private HwRecentTaskRemove(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public synchronized void setRemoveTaskCondition(long oldAvailMemorySize, long initMemorySize) {
        Log.d("HwRecentTaskRemove", "enter startRemoveTask()");
        this.mRequestRemoveTaskClockTime = SystemClock.elapsedRealtime();
        this.mRequestRemoveTaskSystemTime = System.currentTimeMillis();
        if (isInRemoveTask()) {
            Log.d("HwRecentTaskRemove", "already in remove task!");
        } else {
            this.mRemoveTaskThread = new RemoveTaskThread(this.mContext, oldAvailMemorySize, initMemorySize);
        }
    }

    private synchronized void releaseRemoveTask() {
        this.mRemoveTaskThread = null;
    }

    private synchronized boolean needRequestCallTrimProcess(long lastRequestTrimTime) {
        boolean z = false;
        synchronized (this) {
            if (this.mRequestRemoveTaskClockTime >= lastRequestTrimTime && this.mRequestRemoveTaskClockTime <= SystemClock.elapsedRealtime()) {
                z = true;
            }
        }
        return z;
    }

    private synchronized long geRequestRemoveTaskClockTime() {
        return this.mRequestRemoveTaskClockTime;
    }

    public synchronized boolean willRemovedTask(RecentTaskInfo task) {
        if (isInRemoveTask()) {
            Log.d("HwRecentTaskRemove", "in willRemovedTask:" + isInRemoveTask() + ", task:" + task.id + ",activeTime:" + task.lastActiveTime + ",requestTime:" + this.mRequestRemoveTaskSystemTime + ", less:" + (this.mRequestRemoveTaskSystemTime - task.lastActiveTime) + ", absTime:" + Math.abs(task.lastActiveTime - this.mRequestRemoveTaskSystemTime));
            if (task.lastActiveTime <= this.mRequestRemoveTaskSystemTime) {
                return true;
            }
        }
        return false;
    }

    private synchronized boolean isInRemoveTask() {
        boolean z = false;
        synchronized (this) {
            if (this.mRemoveTaskThread != null && SystemClock.elapsedRealtime() - this.mRequestRemoveTaskClockTime < 20000) {
                z = true;
            }
        }
        return z;
    }
}
