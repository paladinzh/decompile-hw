package com.huawei.systemmanager.comm.concurrent;

import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutorUtil {
    public static final int CORE_POOL_SIZE = (CPU_COUNT + 1);
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    public static final long KEEP_ALIVE = 3;
    public static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
    public static final int MAX_POOL_SIZE = 256;

    public static class HsmRejectedExecutionHandler implements RejectedExecutionHandler {
        private String mExecutorName;

        public HsmRejectedExecutionHandler(String executorName) {
            this.mExecutorName = executorName;
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            HwLog.e(this.mExecutorName, "RejectedExecutionHandler called current number of runnable is too many!You need to check if current there is something wrong?");
            if (!executor.isShutdown()) {
                HwLog.i(this.mExecutorName, "Create a new thread");
                new Thread(r, this.mExecutorName + "#reject thread").start();
            }
        }
    }

    public static class HsmThreadFactory implements ThreadFactory {
        private final AtomicLong mCount = new AtomicLong(1);
        private String mExecutorName;

        public HsmThreadFactory(String executorName) {
            this.mExecutorName = executorName;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, this.mExecutorName + " #" + this.mCount.getAndIncrement());
        }
    }

    private ExecutorUtil() {
    }

    public static ThreadPoolExecutor createNormalExecutor(String executorName) {
        return createNormalExecutor(executorName, false);
    }

    public static ThreadPoolExecutor createNormalExecutor(String executorName, boolean allowCoreThreadTimeOunt) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 3, TimeUnit.SECONDS, new LinkedBlockingQueue(256), new HsmThreadFactory(executorName), new HsmRejectedExecutionHandler(executorName));
        if (allowCoreThreadTimeOunt) {
            executor.allowCoreThreadTimeOut(allowCoreThreadTimeOunt);
        }
        return executor;
    }
}
