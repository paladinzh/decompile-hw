package com.huawei.systemmanager.comm.concurrent;

import android.os.SystemClock;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class HsmExecutor implements Executor {
    private static final String TAG = "HsmExecutor";
    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = ExecutorUtil.createNormalExecutor(TAG);

    private static final class RunnableWrapper implements Runnable {
        private static final AtomicLong sTaskId = new AtomicLong(1);
        private final String mFlag;
        private final Runnable mRunnable;

        private RunnableWrapper(Runnable r, String taskName) {
            this.mRunnable = r;
            this.mFlag = taskName + "#" + sTaskId.incrementAndGet() + "#";
        }

        public void run() {
            long start = SystemClock.elapsedRealtime();
            HwLog.d(HsmExecutor.TAG, "begin runnable:" + this.mFlag);
            this.mRunnable.run();
            HwLog.d(HsmExecutor.TAG, "end runnable:" + this.mFlag + ",cost time:" + (SystemClock.elapsedRealtime() - start));
        }
    }

    private HsmExecutor() {
    }

    public void execute(Runnable command) {
        if (command == null) {
            HwLog.e(TAG, "execute, parma is null!");
        } else {
            THREAD_POOL_EXECUTOR.execute(command);
        }
    }

    public static void executeTask(Runnable command, String taskName) {
        if (command == null || taskName == null) {
            HwLog.e(TAG, "executeTask, parma is null!");
            return;
        }
        THREAD_POOL_EXECUTOR.execute(new RunnableWrapper(command, taskName));
    }
}
