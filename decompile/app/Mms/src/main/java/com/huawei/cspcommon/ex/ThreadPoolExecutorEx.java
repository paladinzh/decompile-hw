package com.huawei.cspcommon.ex;

import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.MLog.LogEx;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorEx extends ThreadPoolExecutor {
    private HashMap<Runnable, Thread> mCurrentTasks = new HashMap();

    public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public String getName() {
        return "Unamed ThreadPoolExecutor";
    }

    public String toString() {
        return super.toString() + ";  " + getName();
    }

    public void execute(Runnable command) {
        try {
            super.execute(command);
        } catch (Exception e) {
            MLog.wtf("CSP_RADAR", "Fatal Exception in Runnale " + getName(), e);
        } catch (Throwable e2) {
            MLog.wtf("CSP_RADAR", "Fatal Throwable in Runnale " + getName(), e2);
        }
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        unmarkCurrentTask(r);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        markCurrentTask(t, r);
    }

    private void markCurrentTask(Thread t, Runnable r) {
        synchronized (this.mCurrentTasks) {
            this.mCurrentTasks.put(r, t);
        }
    }

    private void unmarkCurrentTask(Runnable r) {
        synchronized (this.mCurrentTasks) {
            this.mCurrentTasks.remove(r);
        }
    }

    protected String logRunningInfo() {
        StringBuilder sb = new StringBuilder("NamedThreadPoolExecutor-").append(getName());
        synchronized (this.mCurrentTasks) {
            for (Entry<Runnable, Thread> ent : this.mCurrentTasks.entrySet()) {
                getRunningInfo((Runnable) ent.getKey(), (Thread) ent.getValue(), sb);
            }
        }
        return sb.toString();
    }

    private final String getRunningInfo(Runnable r, Thread thr, StringBuilder sb) {
        sb.append(" runner:").append(r);
        if (thr == null) {
            sb.append(". Can't get running info");
            return sb.toString();
        }
        sb.append(". ").append(LogEx.getTraceInfo(thr, 0, 8, "", new Object[0]));
        return sb.toString();
    }
}
