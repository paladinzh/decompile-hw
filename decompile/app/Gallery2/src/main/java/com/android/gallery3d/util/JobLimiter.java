package com.android.gallery3d.util;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.LinkedList;

public class JobLimiter implements FutureListener {
    private final LinkedList<JobWrapper<?>> mJobs = new LinkedList();
    private int mLimit;
    private final ThreadPool mPool;

    private static class JobWrapper<T> implements Future<T>, Job<T> {
        private String mClazz;
        private Future<T> mDelegate;
        private boolean mIsHeavy;
        private Job<T> mJob;
        private FutureListener<T> mListener;
        private T mResult;
        public final int mStartMode;
        private int mState = 0;
        private String mWorkContent;

        public JobWrapper(Job<T> job, FutureListener<T> listener, int startMode) {
            this.mJob = job;
            this.mClazz = this.mJob.clazz();
            this.mIsHeavy = this.mJob.isHeavyJob();
            this.mWorkContent = this.mJob.workContent();
            this.mListener = listener;
            this.mStartMode = startMode;
        }

        public synchronized void setFuture(Future<T> future) {
            if (this.mState == 0) {
                this.mDelegate = future;
            }
        }

        public void cancel() {
            FutureListener futureListener = null;
            synchronized (this) {
                if (this.mState != 1) {
                    futureListener = this.mListener;
                    this.mJob = null;
                    this.mListener = null;
                    if (this.mDelegate != null) {
                        this.mDelegate.cancel();
                        this.mDelegate = null;
                    }
                }
                this.mState = 2;
                this.mResult = null;
                notifyAll();
            }
            if (futureListener != null) {
                futureListener.onFutureDone(this);
            }
        }

        public synchronized boolean isCancelled() {
            return this.mState == 2;
        }

        public boolean isDone() {
            return this.mState != 0;
        }

        public synchronized T get() {
            while (this.mState == 0) {
                Utils.waitWithoutInterrupt(this);
            }
            return this.mResult;
        }

        public void waitDone() {
            get();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public T run(JobContext jc) {
            synchronized (this) {
                if (this.mState == 2) {
                    return null;
                }
                Job<T> job = this.mJob;
            }
            synchronized (this) {
                if (this.mState == 2) {
                    return null;
                }
                this.mState = 1;
                FutureListener<T> listener = this.mListener;
                this.mListener = null;
                this.mJob = null;
                this.mResult = r2;
                notifyAll();
            }
        }

        public boolean needDecodeVideoFromOrigin() {
            boolean needDecodeVideoFromOrigin;
            synchronized (this) {
                Job<T> job = this.mJob;
                needDecodeVideoFromOrigin = job == null ? false : job.needDecodeVideoFromOrigin();
            }
            return needDecodeVideoFromOrigin;
        }

        public boolean isHeavyJob() {
            return this.mIsHeavy;
        }

        public String clazz() {
            return this.mClazz;
        }

        public String workContent() {
            return this.mWorkContent;
        }
    }

    public JobLimiter(ThreadPool pool, int limit) {
        this.mPool = (ThreadPool) Utils.checkNotNull(pool);
        this.mLimit = limit;
    }

    public synchronized <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        return submit(job, listener, 1);
    }

    public synchronized <T> Future<T> submit(Job<T> job, FutureListener<T> listener, int startMode) {
        if (job.needDecodeVideoFromOrigin()) {
            return this.mPool.submit(job, listener, startMode);
        }
        JobWrapper<T> future = new JobWrapper((Job) Utils.checkNotNull(job), listener, startMode);
        this.mJobs.addLast(future);
        submitTasksIfAllowed();
        return future;
    }

    private void submitTasksIfAllowed() {
        while (this.mLimit > 0 && !this.mJobs.isEmpty()) {
            JobWrapper wrapper = (JobWrapper) this.mJobs.removeFirst();
            if (!wrapper.isCancelled()) {
                this.mLimit--;
                wrapper.setFuture(this.mPool.submit(wrapper, this, wrapper.mStartMode));
            }
        }
    }

    public synchronized void onFutureDone(Future future) {
        this.mLimit++;
        submitTasksIfAllowed();
    }
}
