package com.android.settings.deviceinfo;

import android.util.Log;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ModeSwitchExecutor {
    private int mCapacity;
    private int mInterval;
    private boolean mIsRunning;
    private ReentrantLock mLock;
    private Runnable mLooper;
    private int mPendingTaskNum;
    private Condition mTaskAllowedCond;
    private Condition mTaskArrivedCond;
    private LinkedBlockingQueue<Task> mTaskQueue;
    private Thread mWorker;

    private class Looper implements Runnable {
        private Looper() {
        }

        public void run() {
            Log.d("ModeSwitchExecutor", "looper running....");
            while (ModeSwitchExecutor.this.mIsRunning) {
                Task task = ModeSwitchExecutor.this.acquireTask();
                if (task != null) {
                    try {
                        ModeSwitchExecutor.this.executeTask(task);
                    } catch (Throwable t) {
                        Log.e("ModeSwitchExecutor", "Failed to execute task! task = " + task);
                        t.printStackTrace();
                    }
                    ModeSwitchExecutor.this.blockTask();
                }
            }
            ModeSwitchExecutor.this.finishLatestTask();
            Log.d("ModeSwitchExecutor", "looper finished.");
        }
    }

    public static abstract class Task {
        public abstract void execute();
    }

    public ModeSwitchExecutor(int capacity, int interval) {
        if (capacity <= 0 || interval < 0) {
            Log.w("ModeSwitchExecutor", "invalid arguments detected, capacity = " + capacity + ", interval = " + interval);
        }
        if (capacity <= 0) {
            capacity = 10;
        }
        this.mCapacity = capacity;
        if (interval < 0) {
            interval = 500;
        }
        this.mInterval = interval;
        this.mPendingTaskNum = 0;
        this.mIsRunning = true;
        this.mLooper = new Looper();
        this.mWorker = new Thread(this.mLooper);
        this.mLock = new ReentrantLock();
        this.mTaskArrivedCond = this.mLock.newCondition();
        this.mTaskAllowedCond = this.mLock.newCondition();
        this.mTaskQueue = new LinkedBlockingQueue();
    }

    public ModeSwitchExecutor() {
        this(10, 500);
    }

    public void start() {
        Log.d("ModeSwitchExecutor", "ModeSwitchExecutor start...");
        this.mWorker.start();
        Log.d("ModeSwitchExecutor", "ModeSwitchExecutor started.");
    }

    public void stop() {
        Log.d("ModeSwitchExecutor", "ModeSwitchExecutor stop...");
        this.mIsRunning = false;
        this.mLock.lock();
        try {
            this.mTaskAllowedCond.signalAll();
            this.mTaskArrivedCond.signalAll();
            Log.d("ModeSwitchExecutor", "ModeSwitchExecutor stopped.");
        } finally {
            this.mLock.unlock();
        }
    }

    public void submit(Task task) {
        if (task == null) {
            Log.e("ModeSwitchExecutor", "submit null task!");
            throw new IllegalArgumentException("Null task!");
        }
        this.mLock.lock();
        try {
            if (this.mPendingTaskNum >= this.mCapacity) {
                Log.d("ModeSwitchExecutor", "overflow, mPendingTaskNum = " + this.mPendingTaskNum + ", mCapacity = " + this.mCapacity);
                this.mTaskQueue.clear();
                if (this.mTaskQueue.offer(task)) {
                    this.mPendingTaskNum = 1;
                } else {
                    Log.e("ModeSwitchExecutor", "failed to offer task = " + task);
                    this.mPendingTaskNum = 0;
                }
            } else if (this.mTaskQueue.offer(task)) {
                this.mPendingTaskNum++;
            } else {
                Log.e("ModeSwitchExecutor", "failed to offer task = " + task);
            }
            this.mTaskArrivedCond.signalAll();
            Log.d("ModeSwitchExecutor", "Task submitted, task = " + task);
        } finally {
            this.mLock.unlock();
        }
    }

    private Task getLatestTask() {
        Task task = null;
        Iterator<Task> it = this.mTaskQueue.iterator();
        while (it.hasNext()) {
            task = (Task) it.next();
        }
        return task;
    }

    private Task acquireTask() {
        Task task = null;
        this.mLock.lock();
        if (this.mPendingTaskNum > 0) {
            task = getLatestTask();
            Log.d("ModeSwitchExecutor", "task cleared, latest task = " + task + ", mPendingTaskNum = " + this.mPendingTaskNum);
            this.mTaskQueue.clear();
            this.mPendingTaskNum = 0;
        } else {
            while (this.mIsRunning && this.mPendingTaskNum <= 0) {
                try {
                    this.mTaskArrivedCond.await();
                } catch (InterruptedException e) {
                    Log.e("ModeSwitchExecutor", "Interrupted during task waiting!");
                    e.printStackTrace();
                } finally {
                    this.mLock.unlock();
                }
            }
            if (this.mIsRunning) {
                task = (Task) this.mTaskQueue.poll();
                this.mPendingTaskNum--;
                if (this.mPendingTaskNum < 0) {
                    Log.w("ModeSwitchExecutor", "Error occurred, invalid mPendingTaskNum = " + this.mPendingTaskNum + ", task = " + task + ", queue size = " + this.mTaskQueue.size());
                    this.mPendingTaskNum = 0;
                }
            }
        }
        Log.d("ModeSwitchExecutor", "task acquired, task = " + task);
        return task;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void blockTask() {
        this.mLock.lock();
        try {
            if (this.mTaskAllowedCond.await((long) this.mInterval, TimeUnit.MILLISECONDS)) {
                Log.d("ModeSwitchExecutor", "block tast wait finish!");
            }
            this.mLock.unlock();
        } catch (InterruptedException e) {
            Log.e("ModeSwitchExecutor", "Interrupted during task blocking!");
            e.printStackTrace();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }

    private void executeTask(Task task) {
        long start = System.currentTimeMillis();
        Log.d("ModeSwitchExecutor", "[tuning] task = " + task + " start at " + start);
        task.execute();
        Log.d("ModeSwitchExecutor", "[tuning] task finished, task = " + task + ", execution time = " + (System.currentTimeMillis() - start));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishLatestTask() {
        this.mLock.lock();
        try {
            Task task = getLatestTask();
            if (task != null) {
                executeTask(task);
            }
            this.mLock.unlock();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }
}
