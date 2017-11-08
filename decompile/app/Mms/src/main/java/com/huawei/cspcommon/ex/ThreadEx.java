package com.huawei.cspcommon.ex;

import android.util.Log;
import android.util.SparseArray;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.MLog.LogEx;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadEx {
    private static final ThreadPoolExecutorEx sDefaultExecutor = new ThreadPoolExecutorEx(4, 6, 5, TimeUnit.SECONDS, new LinkedBlockingQueue(), getDefaultThreadFactory());
    private static final ThreadFactory sDefaultThreadFactory = new XThreadFactory("ApkThreadFactory");
    private static final SerialExecutor sNetworkExecutor = new SerialExecutor(sDefaultExecutor);
    private static final SerialExecutor sSerialExecutor = new SerialExecutor(sDefaultExecutor);

    public static class SerialExecutor implements Executor, Runnable {
        Runnable mActive;
        ThreadPoolExecutor mExecuteEnv;
        final SparseArray<Runnable> mTaskMarker;
        final ArrayDeque<Runnable> mTasks = new ArrayDeque();

        public SerialExecutor(ThreadPoolExecutor runEnv) {
            this.mExecuteEnv = runEnv;
            this.mTaskMarker = new SparseArray();
        }

        public void run() {
            try {
                Runnable task;
                synchronized (this) {
                    task = this.mActive;
                }
                if (task != null) {
                    task.run();
                }
                scheduleNext();
            } catch (Throwable th) {
                scheduleNext();
            }
        }

        public synchronized void execute(Runnable r) {
            int waitTask = this.mTasks.size();
            if (waitTask > 3) {
                MLog.w("Mms-SerialExecutor", "has too many work tobe execute " + waitTask);
            }
            this.mTasks.offer(r);
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        public synchronized void execute(int token, Runnable r) {
            removeTask(token);
            this.mTaskMarker.append(token, r);
            execute(r);
        }

        protected synchronized void scheduleNext() {
            Runnable runnable = (Runnable) this.mTasks.poll();
            this.mActive = runnable;
            if (!(runnable == null || this.mExecuteEnv.isShutdown())) {
                this.mExecuteEnv.execute(this);
            }
        }

        public synchronized void removeTask(Runnable r) {
            if (r != null) {
                this.mTasks.remove(r);
            }
        }

        public synchronized void removeTask(int token) {
            removeTask((Runnable) this.mTaskMarker.get(token));
        }
    }

    private static class SimpleSafeRunnable extends SafeRunnable {
        private final Runnable mRun;

        SimpleSafeRunnable(Runnable r) {
            this.mRun = r;
        }

        public void safeRun() {
            this.mRun.run();
        }
    }

    public static class TaskStack {
        private boolean mEmpty = true;
        private final Object mIowaitObj = new Object();
        private final LinkedList<Runnable> mThingsToLoad = new LinkedList();
        Thread mWorkerThread;

        public void interrupt() {
            this.mWorkerThread.interrupt();
        }

        public TaskStack(String name) {
            this.mWorkerThread = new Thread(new SafeRunnable() {
                public void safeRun() {
                    while (true) {
                        Runnable runnable = null;
                        synchronized (TaskStack.this.mThingsToLoad) {
                            if (TaskStack.this.mThingsToLoad.size() == 0) {
                                TaskStack.this.setEmpty(true);
                                try {
                                    TaskStack.this.mThingsToLoad.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                            if (TaskStack.this.mThingsToLoad.size() > 0) {
                                runnable = (Runnable) TaskStack.this.mThingsToLoad.removeFirst();
                            }
                        }
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                }
            }, "TaskStack-" + name);
            TmoMonitor.getInst().addWatchTarget(name + "-tid=" + this.mWorkerThread.getId(), this);
        }

        public void setEmpty(boolean empty) {
            synchronized (this.mIowaitObj) {
                this.mEmpty = empty;
                if (empty) {
                    this.mIowaitObj.notifyAll();
                }
            }
        }

        public void waitForIdle() {
            long begin = System.currentTimeMillis();
            while (true) {
                synchronized (this.mIowaitObj) {
                    if (this.mEmpty) {
                        Log.i("CSP_TaskStack", "waitForIdle cost " + (System.currentTimeMillis() - begin) + " ms");
                        return;
                    }
                    try {
                        this.mIowaitObj.wait();
                    } catch (InterruptedException e) {
                        Log.e("CSP_TaskStack", "waitForIdle ocuur exception::" + e);
                    }
                }
            }
        }

        public void start(int priority) {
            this.mWorkerThread.setPriority(priority);
            this.mWorkerThread.start();
        }

        public void clear() {
            synchronized (this.mThingsToLoad) {
                MLog.v("CSP_TaskStack", "mThingsToLoad.clear ");
                this.mThingsToLoad.clear();
            }
        }

        public void push(Runnable r, boolean emergency) {
            synchronized (this.mThingsToLoad) {
                if (emergency) {
                    this.mThingsToLoad.addFirst(r);
                } else {
                    this.mThingsToLoad.addLast(r);
                }
                this.mThingsToLoad.notifyAll();
                setEmpty(false);
            }
        }

        public int getQueuedSize() {
            int size;
            synchronized (this.mThingsToLoad) {
                size = this.mThingsToLoad.size();
            }
            return size;
        }

        public String getDebugInfo(String prefix) {
            StringBuilder sb = new StringBuilder(prefix).append(" TaskStack:").append(this.mWorkerThread.getName()).append(" queue-size:").append(getQueuedSize());
            LogEx.getTraceInfo(this.mWorkerThread.getStackTrace(), 0, 8, sb);
            return sb.toString();
        }
    }

    private static class XThreadFactory implements ThreadFactory {
        private final AtomicInteger mCount = new AtomicInteger(1);
        private final String mTag;

        public XThreadFactory(String tag) {
            this.mTag = tag;
        }

        public Thread newThread(Runnable r) {
            String threadName = "";
            threadName = this.mTag + "-" + this.mCount.getAndIncrement() + ". @" + r + " @" + LogEx.getCallerInfo(3);
            MLog.d(this.mTag, "XThreadFactory create: " + threadName);
            Thread t = new Thread(r instanceof SafeRunnable ? r : new SimpleSafeRunnable(r), threadName);
            if (t.getPriority() != 1) {
                t.setPriority(1);
            }
            return t;
        }
    }

    public static Executor getSerialExecutor() {
        return sSerialExecutor;
    }

    public static Executor getNetworkExecutor() {
        return sNetworkExecutor;
    }

    public static Executor getDefaultExecutor() {
        return sDefaultExecutor;
    }

    public static void execute(Runnable r) {
        if (!sDefaultExecutor.isShutdown()) {
            sDefaultExecutor.execute(r);
        }
    }

    public static ThreadFactory getDefaultThreadFactory() {
        return sDefaultThreadFactory;
    }

    public static Executor createExecutor(int corePool, int maxPool, int keepTime, String tag) {
        int i = corePool;
        int i2 = maxPool;
        Executor e = new ThreadPoolExecutorEx(i, i2, (long) keepTime, TimeUnit.SECONDS, new LinkedBlockingQueue(), new XThreadFactory(tag));
        TmoMonitor.getInst().addWatchTarget(tag, e);
        return e;
    }
}
