package com.huawei.keyguard;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalContext {
    private static Handler mBackgroundHandler = null;
    private static Handler mUIHandler;
    private static Context sContext;
    private static final ThreadPoolExecutor sDefaultExecutor = new ThreadPoolExecutor(3, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue(), sDefaultThreadFactory);
    private static final ThreadFactory sDefaultThreadFactory = new XThreadFactory("KeyguardThreadFactory");
    private static long sMainTID = -1;
    private static final SerialExecutor sSerialExecutor = new SerialExecutor(sDefaultExecutor);

    private static class SafeRunnable implements Runnable {
        private Runnable mUnsafeRunner;

        public SafeRunnable(Runnable r) {
            this.mUnsafeRunner = r;
            if (this.mUnsafeRunner == null) {
                HwLog.e("GlobalContext", "Create SafeRunnable with invalide object : ", new Exception());
            }
        }

        public void run() {
            try {
                if (this.mUnsafeRunner != null) {
                    this.mUnsafeRunner.run();
                }
            } catch (Exception e) {
                HwLog.wtf("GlobalContext", "Fatal Exception in Runnale " + this, e);
            } catch (Throwable e2) {
                HwLog.wtf("GlobalContext", "Fatal Throwable in Runnale " + this, e2);
            }
        }
    }

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
                HwLog.w("GlobalContext", "has too many work tobe execute " + waitTask);
            }
            this.mTasks.offer(r);
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            Runnable runnable = (Runnable) this.mTasks.poll();
            this.mActive = runnable;
            if (!(runnable == null || this.mExecuteEnv.isShutdown())) {
                this.mExecuteEnv.execute(this);
            }
        }
    }

    private static class XThreadFactory implements ThreadFactory {
        private final AtomicInteger mCount = new AtomicInteger(1);
        private final String mTag;

        public XThreadFactory(String tag) {
            this.mTag = tag;
        }

        public Thread newThread(Runnable r) {
            String threadName = BuildConfig.FLAVOR;
            threadName = this.mTag + "-" + this.mCount.getAndIncrement() + ". @" + r;
            Thread t = new Thread(new SafeRunnable(r), threadName);
            if (t.getPriority() != 1) {
                t.setPriority(1);
            }
            HwLog.d(this.mTag, "XThreadFactory create: " + threadName + " " + t.getId());
            return t;
        }
    }

    public static void setContext(Context context) {
        if (sContext == null) {
            sContext = context;
        }
        sMainTID = Looper.getMainLooper().getThread().getId();
    }

    public static boolean isRunningInUI() {
        if (sMainTID == -1) {
            sMainTID = Looper.getMainLooper().getThread().getId();
        }
        return Thread.currentThread().getId() == sMainTID;
    }

    public static SerialExecutor getSerialExecutor() {
        return sSerialExecutor;
    }

    public static ThreadPoolExecutor getPoolExecutor() {
        return sDefaultExecutor;
    }

    public static Context getContext() {
        return sContext;
    }

    public static TelephonyManager getTelephonyManager(Context context) {
        return TelephonyManager.from(context);
    }

    public static MSimTelephonyManager getMSimTelephonyManager(Context context) {
        return MSimTelephonyManager.from(context);
    }

    public static Handler getUIHandler() {
        Handler handler;
        synchronized (GlobalContext.class) {
            if (mUIHandler == null) {
                mUIHandler = new Handler(Looper.getMainLooper());
            }
            handler = mUIHandler;
        }
        return handler;
    }

    public static Handler getBackgroundHandler() {
        Handler handler;
        synchronized (GlobalContext.class) {
            if (mBackgroundHandler == null) {
                HandlerThread backgroundThread = new HandlerThread("KG_Background_Handle_Thread", 1);
                backgroundThread.start();
                mBackgroundHandler = new Handler(backgroundThread.getLooper());
            }
            handler = mBackgroundHandler;
        }
        return handler;
    }
}
