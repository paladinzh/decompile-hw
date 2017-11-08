package com.android.contacts.util;

import android.os.AsyncTask;
import android.os.Looper;
import com.android.contacts.test.NeededForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AsyncTaskExecutors {
    private static final int CORE_POOL_SIZE = (CPU_COUNT + 1);
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
    public static final SerialExecutor SERIAL_EXECUTOR = new SerialExecutor();
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory, new DiscardOldestPolicy());
    private static AsyncTaskExecutorFactory mInjectedAsyncTaskExecutorFactory = null;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Contacts AsyncTask #" + this.mCount.getAndIncrement());
        }
    };

    public interface AsyncTaskExecutorFactory {
        AsyncTaskExecutor createAsyncTaskExeuctor();
    }

    private static class SerialExecutor implements Executor {
        Runnable mActive;
        final ArrayDeque<Runnable> mTasks;

        private SerialExecutor() {
            this.mTasks = new ArrayDeque();
        }

        public synchronized void execute(final Runnable r) {
            this.mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        SerialExecutor.this.scheduleNext();
                    }
                }
            });
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            Runnable runnable = (Runnable) this.mTasks.poll();
            this.mActive = runnable;
            if (runnable != null) {
                AsyncTaskExecutors.THREAD_POOL_EXECUTOR.execute(this.mActive);
            }
        }
    }

    private static class SimpleAsyncTaskExecutor implements AsyncTaskExecutor {
        private final Executor mExecutor;

        public SimpleAsyncTaskExecutor(Executor executor) {
            this.mExecutor = executor;
        }

        public <T> AsyncTask<T, ?, ?> submit(Object identifer, AsyncTask<T, ?, ?> task, T... params) {
            AsyncTaskExecutors.checkCalledFromUiThread();
            return task.executeOnExecutor(this.mExecutor, params);
        }
    }

    public static AsyncTaskExecutor createAsyncTaskExecutor() {
        synchronized (AsyncTaskExecutors.class) {
            if (mInjectedAsyncTaskExecutorFactory != null) {
                AsyncTaskExecutor createAsyncTaskExeuctor = mInjectedAsyncTaskExecutorFactory.createAsyncTaskExeuctor();
                return createAsyncTaskExeuctor;
            }
            createAsyncTaskExeuctor = new SimpleAsyncTaskExecutor(AsyncTask.SERIAL_EXECUTOR);
            return createAsyncTaskExeuctor;
        }
    }

    public static AsyncTaskExecutor createThreadPoolExecutor() {
        synchronized (AsyncTaskExecutors.class) {
            if (mInjectedAsyncTaskExecutorFactory != null) {
                AsyncTaskExecutor createAsyncTaskExeuctor = mInjectedAsyncTaskExecutorFactory.createAsyncTaskExeuctor();
                return createAsyncTaskExeuctor;
            }
            createAsyncTaskExeuctor = new SimpleAsyncTaskExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return createAsyncTaskExeuctor;
        }
    }

    @NeededForTesting
    public static void setFactoryForTest(AsyncTaskExecutorFactory factory) {
        synchronized (AsyncTaskExecutors.class) {
            mInjectedAsyncTaskExecutorFactory = factory;
        }
    }

    public static void checkCalledFromUiThread() {
        Preconditions.checkState(Thread.currentThread() == Looper.getMainLooper().getThread(), "submit method must be called from ui thread, was: " + Thread.currentThread());
    }

    public static AsyncTaskExecutor createThreadPoolExecutorDiscardOldestPolicy() {
        return new SimpleAsyncTaskExecutor(THREAD_POOL_EXECUTOR);
    }
}
