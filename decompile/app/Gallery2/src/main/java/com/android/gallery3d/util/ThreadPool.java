package com.android.gallery3d.util;

import com.android.gallery3d.util.BusinessRadar.BugType;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    public static final JobContext JOB_CONTEXT_STUB = new JobContextStub();
    ResourceCounter mAdamCounter;
    ResourceCounter mCpuCounter;
    ResourceCounter mEveCounter;
    private final Executor mExecutor;
    ResourceCounter mHumanCounter;
    ResourceCounter mNetworkCounter;
    ResourceCounter mVideoCounter;
    private final Executor mVideoExecutor;
    ResourceCounter mVirtualCounter;

    public interface Job<T> {
        String clazz();

        boolean isHeavyJob();

        boolean needDecodeVideoFromOrigin();

        T run(JobContext jobContext);

        String workContent();
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface JobContext {
        boolean isCancelled();

        void setCancelListener(CancelListener cancelListener);

        boolean setMode(int i);
    }

    private static class JobContextStub implements JobContext {
        private JobContextStub() {
        }

        public boolean isCancelled() {
            return false;
        }

        public void setCancelListener(CancelListener listener) {
        }

        public boolean setMode(int mode) {
            return true;
        }
    }

    private static class ResourceCounter {
        public int value;

        public ResourceCounter(int v) {
            this.value = v;
        }
    }

    private class Worker<T> implements Runnable, Future<T>, JobContext {
        private CancelListener mCancelListener;
        private volatile boolean mIsCancelled;
        private boolean mIsDone;
        private Job<T> mJob;
        private FutureListener<T> mListener;
        private int mMode;
        private T mResult;
        private int mStartMode;
        private ResourceCounter mWaitOnResource;

        public Worker(Job<T> job, FutureListener<T> listener, int startMode) {
            this.mJob = job;
            this.mListener = listener;
            this.mStartMode = startMode;
        }

        public void run() {
            Object result = null;
            if (setMode(this.mStartMode) && !isCancelled()) {
                try {
                    long start = System.currentTimeMillis();
                    result = this.mJob.run(this);
                    long duration = System.currentTimeMillis() - start;
                    if (duration > 5000) {
                        String className = this.mJob.clazz();
                        String workContent = this.mJob.workContent();
                        if (!this.mJob.isHeavyJob()) {
                            GalleryLog.w("Worker", "Job:" + className + " has run over " + duration + " ms." + " mode:" + this.mMode + ", free cpu:" + ThreadPool.this.mCpuCounter.value + ", free network:" + ThreadPool.this.mNetworkCounter.value + ", workContent:" + workContent);
                            BusinessRadar.report(BugType.JOB_TIME_OUT, String.format("job[%s] run %s ms. work content: %s", new Object[]{this.mJob.clazz(), Long.valueOf(duration), this.mJob.workContent()}));
                        }
                    }
                } catch (Throwable ex) {
                    GalleryLog.w("Worker", "Exception in running a job." + ex.getMessage());
                }
            }
            synchronized (this) {
                setMode(0);
                this.mResult = result;
                this.mIsDone = true;
                notifyAll();
            }
            if (this.mListener != null) {
                this.mListener.onFutureDone(this);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void cancel() {
            if (!this.mIsCancelled) {
                this.mIsCancelled = true;
                if (this.mWaitOnResource != null) {
                    synchronized (this.mWaitOnResource) {
                        this.mWaitOnResource.notifyAll();
                    }
                }
                if (this.mCancelListener != null) {
                    this.mCancelListener.onCancel();
                }
            }
        }

        public boolean isCancelled() {
            return this.mIsCancelled;
        }

        public synchronized boolean isDone() {
            return this.mIsDone;
        }

        public synchronized T get() {
            while (!this.mIsDone) {
                try {
                    wait();
                } catch (Exception ex) {
                    GalleryLog.w("Worker", "ingore exception." + ex.getMessage());
                }
            }
            return this.mResult;
        }

        public void waitDone() {
            get();
        }

        public synchronized void setCancelListener(CancelListener listener) {
            this.mCancelListener = listener;
            if (this.mIsCancelled && this.mCancelListener != null) {
                this.mCancelListener.onCancel();
            }
        }

        public boolean setMode(int mode) {
            ResourceCounter rc = modeToCounter(this.mMode);
            if (rc != null) {
                releaseResource(rc);
            }
            this.mMode = 0;
            rc = modeToCounter(mode);
            if (rc != null) {
                if (!acquireResource(rc)) {
                    return false;
                }
                this.mMode = mode;
            }
            return true;
        }

        private ResourceCounter modeToCounter(int mode) {
            if (mode == 1) {
                return ThreadPool.this.mCpuCounter;
            }
            if (mode == 2) {
                return ThreadPool.this.mNetworkCounter;
            }
            if (mode == 3) {
                return ThreadPool.this.mAdamCounter;
            }
            if (mode == 4) {
                return ThreadPool.this.mEveCounter;
            }
            if (mode == 5) {
                return ThreadPool.this.mHumanCounter;
            }
            if (mode == 6) {
                return ThreadPool.this.mVirtualCounter;
            }
            if (mode == 7) {
                return ThreadPool.this.mVideoCounter;
            }
            return null;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean acquireResource(ResourceCounter counter) {
            while (true) {
                synchronized (this) {
                    if (this.mIsCancelled) {
                        this.mWaitOnResource = null;
                        return false;
                    }
                    this.mWaitOnResource = counter;
                }
            }
        }

        private void releaseResource(ResourceCounter counter) {
            synchronized (counter) {
                counter.value++;
                counter.notifyAll();
            }
        }
    }

    public ThreadPool() {
        this(8, 16);
    }

    public ThreadPool(int initPoolSize, int maxPoolSize) {
        this.mCpuCounter = new ResourceCounter(4);
        this.mNetworkCounter = new ResourceCounter(2);
        this.mAdamCounter = new ResourceCounter(2);
        this.mEveCounter = new ResourceCounter(1);
        this.mHumanCounter = new ResourceCounter(1);
        this.mVirtualCounter = new ResourceCounter(6);
        this.mVideoCounter = new ResourceCounter(2);
        this.mExecutor = new ThreadPoolExecutor(initPoolSize, maxPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("thread-pool", 10));
        this.mVideoExecutor = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("thread-pool-video", 10));
    }

    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener, int startMode) {
        Executor executor = this.mExecutor;
        if (job.needDecodeVideoFromOrigin()) {
            executor = this.mVideoExecutor;
            startMode = 7;
        }
        Worker<T> w = new Worker(job, listener, startMode);
        executor.execute(w);
        return w;
    }

    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        return submit(job, listener, 1);
    }

    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }
}
