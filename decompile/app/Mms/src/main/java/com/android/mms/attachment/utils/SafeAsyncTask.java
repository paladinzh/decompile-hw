package com.android.mms.attachment.utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.SystemClock;
import com.android.mms.attachment.Factory;
import com.huawei.cspcommon.MLog;

public abstract class SafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static WakeLockHelper sWakeLock = new WakeLockHelper("bugle_safe_async_task_wakelock");
    private final boolean mCancelExecutionOnTimeout;
    private final long mMaxExecutionTimeMillis;

    protected abstract Result doInBackgroundTimed(Params... paramsArr);

    public SafeAsyncTask() {
        this(10000, false);
    }

    public SafeAsyncTask(long maxTimeMillis, boolean cancelExecutionOnTimeout) {
        this.mMaxExecutionTimeMillis = maxTimeMillis;
        this.mCancelExecutionOnTimeout = cancelExecutionOnTimeout;
    }

    public final SafeAsyncTask<Params, Progress, Result> executeOnThreadPool(Params... params) {
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    protected final Result doInBackground(Params... params) {
        if (this.mCancelExecutionOnTimeout) {
            ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                public void run() {
                    if (SafeAsyncTask.this.getStatus() == Status.RUNNING) {
                        MLog.w("SafeAsyncTask", String.format("%s timed out and is canceled", new Object[]{this}));
                        SafeAsyncTask.this.cancel(true);
                    }
                }
            }, this.mMaxExecutionTimeMillis);
        }
        long startTime = SystemClock.elapsedRealtime();
        try {
            Result doInBackgroundTimed = doInBackgroundTimed(params);
            if (SystemClock.elapsedRealtime() - startTime > this.mMaxExecutionTimeMillis) {
                MLog.w("SafeAsyncTask", String.format("%s took %dms", new Object[]{this, Long.valueOf(SystemClock.elapsedRealtime() - startTime)}));
            }
            return doInBackgroundTimed;
        } catch (Throwable th) {
            if (SystemClock.elapsedRealtime() - startTime > this.mMaxExecutionTimeMillis) {
                MLog.w("SafeAsyncTask", String.format("%s took %dms", new Object[]{this, Long.valueOf(SystemClock.elapsedRealtime() - startTime)}));
            }
        }
    }

    protected void onPostExecute(Result result) {
    }

    public static void executeOnThreadPool(Runnable runnable) {
        executeOnThreadPool(runnable, false);
    }

    public static void executeOnThreadPool(final Runnable runnable, boolean withWakeLock) {
        if (withWakeLock) {
            final Intent intent = new Intent();
            sWakeLock.acquire(Factory.get().getApplicationContext(), intent, 1000);
            THREAD_POOL_EXECUTOR.execute(new Runnable() {
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        SafeAsyncTask.sWakeLock.release(intent, 1000);
                    }
                }
            });
            return;
        }
        THREAD_POOL_EXECUTOR.execute(runnable);
    }
}
