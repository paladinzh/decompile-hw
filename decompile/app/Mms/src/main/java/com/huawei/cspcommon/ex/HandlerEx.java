package com.huawei.cspcommon.ex;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.huawei.cspcommon.MLog;

public class HandlerEx extends Handler {
    private IExtendHandler mExtHandler;
    private long mLastFreshTime;
    private String mName;

    public interface IExtendHandler {
        boolean handleMessage(Message message);
    }

    public void setExtendHandler(IExtendHandler extHandler) {
        this.mExtHandler = extHandler;
    }

    public HandlerEx() {
        this(null, false);
    }

    public HandlerEx(Looper looper) {
        this(looper, null, false);
    }

    public HandlerEx(Callback callback, boolean async) {
        super(callback, async);
        this.mName = null;
        this.mLastFreshTime = 0;
        this.mExtHandler = null;
    }

    public HandlerEx(Looper looper, Callback callback, boolean async) {
        this(looper, callback, async, getLooperName(looper));
    }

    public HandlerEx(Looper looper, Callback callback, boolean async, String name) {
        super(looper, callback, async);
        this.mName = null;
        this.mLastFreshTime = 0;
        this.mExtHandler = null;
        this.mName = name;
        if (looper != null) {
            TmoMonitor.getInst().addWatchTarget(name, (Handler) this);
        }
    }

    public static String getLooperName(Looper l) {
        if (l == null) {
            return "Invalide-handler";
        }
        return "HandlerThread " + l.getThread().getName() + " id:" + l.getThread().getId();
    }

    public void dispatchMessage(Message msg) {
        Runnable r = msg.getCallback();
        long time = SystemClock.uptimeMillis();
        if (r != null) {
            r.run();
            time = SystemClock.uptimeMillis() - time;
            if (time > 300) {
                checkRunnerTimeout(r, time);
            }
            return;
        }
        if (this.mExtHandler == null || !this.mExtHandler.handleMessage(msg)) {
            synchronized (this) {
                this.mLastFreshTime = SystemClock.uptimeMillis();
            }
            super.dispatchMessage(msg);
        }
        time = SystemClock.uptimeMillis() - time;
        if (time > getMsgMaxRunningTime(msg)) {
            checkMessageTimeout(msg, time);
        }
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        return super.sendMessageAtTime(msg, uptimeMillis);
    }

    public void sendPeriodMessage(int what, long delay, long periodTime) {
        sendPeriodMessage(obtainMessage(what), delay, periodTime);
    }

    public void sendPeriodMessage(Message msg, long delay, long periodTime) {
        synchronized (this) {
            boolean isTimeExceed = SystemClock.uptimeMillis() - this.mLastFreshTime > periodTime;
        }
        if (!isTimeExceed) {
            removeMessages(msg.what);
            sendMessageDelayed(msg, delay);
        } else if (!hasMessages(msg.what)) {
            sendMessageDelayed(msg, 0);
        }
    }

    private void checkRunnerTimeout(Runnable r, long runTime) {
        MLog.e("CSP_RADAR", "Runnable execute too long time");
        if (r instanceof CheckableRunnable) {
            CheckableRunnable chkRun = (CheckableRunnable) r;
            if (runTime >= chkRun.getMaxRunningTime()) {
                chkRun.onTimeout(runTime);
            }
        }
    }

    private void checkMessageTimeout(Message msg, long runTime) {
        MLog.e("CSP_RADAR", "Message execute too long time." + msg);
    }

    protected long getMsgMaxRunningTime(Message msg) {
        return Long.MAX_VALUE;
    }
}
