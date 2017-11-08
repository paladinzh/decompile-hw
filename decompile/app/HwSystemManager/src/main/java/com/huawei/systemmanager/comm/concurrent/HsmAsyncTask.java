package com.huawei.systemmanager.comm.concurrent;

import android.os.AsyncTask;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public abstract class HsmAsyncTask<Params, Progress, Result> {
    private static final ThreadPoolExecutor PARALLEL_EXECUTOR = HsmExecutor.THREAD_POOL_EXECUTOR;
    private static final String TAG = HsmAsyncTask.class.getSimpleName();
    private static final AtomicLong sId = new AtomicLong(1);
    private volatile boolean mCancelled;
    private final String mFlag = (getClass().getSimpleName() + "#" + sId.getAndIncrement() + "#");
    private final InnerTask<Params, Progress, Result> mInnerTask = new InnerTask(this);

    private static class InnerTask<Params2, Progress2, Result2> extends AsyncTask<Params2, Progress2, Result2> {
        private final HsmAsyncTask<Params2, Progress2, Result2> mOwner;
        private ThreadPoolExecutor mThreadPool = HsmAsyncTask.PARALLEL_EXECUTOR;

        public InnerTask(HsmAsyncTask<Params2, Progress2, Result2> owner) {
            this.mOwner = owner;
        }

        public AsyncTask<Params2, Progress2, Result2> executeInThreadPool(ThreadPoolExecutor threadPool, Params2... params) {
            this.mThreadPool = threadPool;
            return executeOnExecutor(threadPool, params);
        }

        protected Result2 doInBackground(Params2... params) {
            long start = System.currentTimeMillis();
            HwLog.d(HsmAsyncTask.TAG, this.mOwner.getflag() + "start in thread:" + Thread.currentThread() + ", current active tasksize:" + ((long) this.mThreadPool.getActiveCount()) + ", queue size:" + this.mThreadPool.getQueue().size());
            Result2 res = this.mOwner.doInBackground(params);
            HwLog.d(HsmAsyncTask.TAG, this.mOwner.getflag() + " end, cost time:" + (System.currentTimeMillis() - start));
            return res;
        }

        public void onCancelled(Result2 result) {
            HwLog.i(HsmAsyncTask.TAG, this.mOwner.getflag() + " was canceled");
            this.mOwner.onCancelled(result);
        }

        public void onPostExecute(Result2 result) {
            if (this.mOwner.mCancelled) {
                HwLog.i(HsmAsyncTask.TAG, this.mOwner.getflag() + " was canceled");
                this.mOwner.onCancelled(result);
                return;
            }
            this.mOwner.onSuccess(result);
        }
    }

    protected abstract Result doInBackground(Params... paramsArr);

    public final void cancel(boolean mayInterruptIfRunning) {
        this.mCancelled = true;
        this.mInnerTask.cancel(mayInterruptIfRunning);
    }

    protected void onCancelled(Result result) {
    }

    protected void onSuccess(Result result) {
    }

    public final Result get() throws InterruptedException, ExecutionException {
        return this.mInnerTask.get();
    }

    public final String getflag() {
        return this.mFlag;
    }

    public final HsmAsyncTask<Params, Progress, Result> executeParallel(Params... params) {
        this.mInnerTask.executeInThreadPool(PARALLEL_EXECUTOR, params);
        return this;
    }
}
