package com.huawei.systemmanager.comm.concurrent;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

public class HsmSingleExecutor implements Executor {
    Runnable mActive;
    private final ArrayDeque<Runnable> mTasks = new ArrayDeque();

    public synchronized void execute(final Runnable r) {
        this.mTasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    HsmSingleExecutor.this.scheduleNext();
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
            getExecutor().execute(this.mActive);
        }
    }

    public synchronized void clearAllTask() {
        this.mTasks.clear();
    }

    public synchronized int getDequeTaskNum() {
        return this.mTasks.size();
    }

    protected Executor getExecutor() {
        return HsmExecutor.THREAD_POOL_EXECUTOR;
    }
}
