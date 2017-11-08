package com.huawei.systemmanager.mainscreen.detector.task;

import android.content.Context;
import android.os.SystemClock;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DetectTask {
    public static final int MAX_PROGRESS = 100;
    public static final String TAG = "DetectTask";
    private static final int TASK_STATUS_FINISH = 3;
    private static final int TASK_STATUS_INIT = 0;
    private static final int TASK_STATUS_RUNNING = 1;
    private AtomicBoolean mCanceled = new AtomicBoolean(false);
    protected Context mContext;
    private DetectTaskListener mListener;
    private volatile float mProgress;
    private AtomicInteger mRunStatus = new AtomicInteger(0);
    private long mScanCostTime = -1;
    private long mStartTime = 0;
    private Object mTag;

    protected abstract void doTask();

    public abstract String getTaskName();

    public abstract int getWeight();

    public DetectTask(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean execute() {
        if (1 == this.mRunStatus.get()) {
            return false;
        }
        this.mRunStatus.set(1);
        this.mStartTime = SystemClock.elapsedRealtime();
        getExecutor().execute(new Runnable() {
            public void run() {
                HwLog.i(DetectTask.TAG, "do task begin. And the task name is " + DetectTask.this.getTaskName());
                DetectTask.this.doTask();
                HwLog.i(DetectTask.TAG, "do task end. And the task name is " + DetectTask.this.getTaskName());
            }
        });
        return true;
    }

    protected Executor getExecutor() {
        return HsmExecutor.THREAD_POOL_EXECUTOR;
    }

    public boolean isFinish() {
        return this.mRunStatus.get() == 3;
    }

    public boolean isRunning() {
        return this.mRunStatus.get() == 1;
    }

    public void cancel() {
        HwLog.i(getTaskName(), "cancel called!");
        setCanceled();
    }

    public void setListener(DetectTaskListener listener) {
        this.mListener = listener;
    }

    public void publishTaskStart() {
        HwLog.i(getTaskName(), "publishTaskStart!");
        if (this.mListener != null) {
            this.mListener.onStart(this);
        }
    }

    public void publishItemFount(DetectItem item) {
        if (this.mListener != null) {
            this.mListener.onItemFount(this, item);
        }
    }

    public void publishTaskFinish() {
        if (isFinish()) {
            HwLog.i(getTaskName(), "publishTaskFinish failed, its already finished");
            return;
        }
        this.mRunStatus.set(3);
        this.mScanCostTime = SystemClock.elapsedRealtime() - this.mStartTime;
        HwLog.i(getTaskName(), "publishTaskFinish, cost time:" + this.mScanCostTime);
        saveScanTime();
        if (this.mListener != null) {
            this.mListener.onTaskFinish(this);
        }
    }

    public void publishProgressChange(String itemName, float progress) {
        this.mProgress = progress;
        if (this.mListener != null) {
            this.mListener.onProgressChange(this, itemName, progress);
        }
    }

    public float getProgress() {
        return this.mProgress;
    }

    protected void setCanceled() {
        this.mCanceled.set(true);
    }

    public boolean isCanceled() {
        return this.mCanceled.get();
    }

    public boolean saveScanTime() {
        if (this.mScanCostTime <= 0) {
            HwLog.w(TAG, "save time error, mScanCostTime=" + this.mScanCostTime);
            return false;
        }
        HwLog.i(TAG, getTaskName() + " saveScanTime time:" + this.mScanCostTime);
        getContext().getSharedPreferences("systemmanagerscan", 0).edit().putLong(getTaskName(), this.mScanCostTime).commit();
        return true;
    }

    public void destory() {
        HwLog.i(TAG, "destroy called!");
        if (isRunning()) {
            cancel();
        }
    }

    public boolean isEnable() {
        return true;
    }

    protected void setTag(Object tag) {
        this.mTag = tag;
    }

    protected Object getTag() {
        return this.mTag;
    }
}
