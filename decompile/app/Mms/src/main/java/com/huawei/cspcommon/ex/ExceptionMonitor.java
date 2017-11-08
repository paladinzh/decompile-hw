package com.huawei.cspcommon.ex;

import com.huawei.cspcommon.MLog;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

public class ExceptionMonitor {
    private static WrapperedExceptionHandler sExceptionHandler = null;

    public interface IExceptionChecker {
        boolean checkException(Thread thread, Throwable th);
    }

    private static class WrapperedExceptionHandler implements UncaughtExceptionHandler {
        private static final Runnable sAsycOomChecker = new Runnable() {
            public void run() {
                MLog.d("ExceptionMonitor", "Check a main thread OOM in background");
                MemCollector.logMemInfo();
            }
        };
        ArrayList<IExceptionChecker> mAppsChecker;
        UncaughtExceptionHandler mHandler;

        private WrapperedExceptionHandler(UncaughtExceptionHandler ueh) {
            this.mHandler = null;
            this.mAppsChecker = new ArrayList();
            this.mHandler = ueh;
        }

        private void addAppExceptionHandler(IExceptionChecker h) {
            synchronized (this.mAppsChecker) {
                this.mAppsChecker.add(h);
            }
        }

        public void uncaughtException(Thread thread, Throwable ex) {
            if (!checkException(thread, ex)) {
                MLog.wtf("ExceptionMonitor", "Have a uncaughtException " + ex.getMessage());
            }
            if (this.mHandler != null) {
                this.mHandler.uncaughtException(thread, ex);
            }
        }

        private boolean checkException(Thread thread, Throwable ex) {
            synchronized (this.mAppsChecker) {
                for (IExceptionChecker chk : this.mAppsChecker) {
                    if (chk.checkException(thread, ex)) {
                        return true;
                    }
                }
                return checkUnProcedException(thread, ex);
            }
        }

        private boolean checkUnProcedException(Thread thread, Throwable ex) {
            MLog.e("ExceptionMonitor", "CspExceptionChecker " + ex.getClass().getName() + " in thread " + thread, ex);
            return true;
        }
    }

    public static void init() {
        sExceptionHandler = new WrapperedExceptionHandler(Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(sExceptionHandler);
    }

    public static final void setExtendExceptionChecker(IExceptionChecker checker) {
        if (sExceptionHandler != null) {
            sExceptionHandler.addAppExceptionHandler(checker);
        }
    }

    public static final void checkExcption(Throwable ex) {
        if (sExceptionHandler != null) {
            sExceptionHandler.checkException(Thread.currentThread(), ex);
        }
    }
}
