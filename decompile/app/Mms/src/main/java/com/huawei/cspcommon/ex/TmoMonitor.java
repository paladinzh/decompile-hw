package com.huawei.cspcommon.ex;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.MLog.LogEx;
import com.huawei.cspcommon.ex.ThreadEx.TaskStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

public class TmoMonitor {
    private static String ARG_TARGET_NAME = "executor_name";
    private static long DELAY_BACK_GROUND = 3000;
    private static long HEART_BEAT_INTERVAL = 20000;
    private static TmoMonitor inst = new TmoMonitor();
    private volatile boolean mIsSleep = false;
    private MoniterHandler mMonitor;
    private HashMap<Object, Integer> mTmoTargets = new HashMap();
    private HashMap<String, Object> mWatchedTargets = new HashMap();

    private class MoniterHandler extends Handler {
        private Map<String, Integer> mCachedMsgs;

        private MoniterHandler(Looper looper) {
            super(looper);
            this.mCachedMsgs = new HashMap();
        }

        public void wakeUp() {
            long interval = 0;
            for (Entry<String, Integer> e : this.mCachedMsgs.entrySet()) {
                sendMessageDelayed(getMsg(((Integer) e.getValue()).intValue(), (String) e.getKey()), TmoMonitor.HEART_BEAT_INTERVAL + interval);
                interval += TmoMonitor.DELAY_BACK_GROUND;
            }
        }

        public void handleMessage(Message msg) {
            String key = msg.getData().getString(TmoMonitor.ARG_TARGET_NAME, "");
            if (TmoMonitor.this.mIsSleep) {
                this.mCachedMsgs.put(key, Integer.valueOf(msg.what));
                MLog.d("TmoMonitor", "Cache monitor msg as screen_off");
            } else if (TextUtils.isEmpty(key)) {
                MLog.e("TmoMonitor", "Monitor thread handler invalid message" + key);
            } else {
                boolean chkState = false;
                switch (msg.what) {
                    case 1:
                        chkState = TmoMonitor.this.checkHandler(key);
                        break;
                    case 2:
                        chkState = TmoMonitor.this.checkExecutor(key);
                        break;
                    case 3:
                        chkState = TmoMonitor.this.checkTaskStack(key);
                        break;
                }
                if (chkState) {
                    sendMessageDelayed(getMsg(msg.what, key), TmoMonitor.HEART_BEAT_INTERVAL);
                }
            }
        }

        private Message getMsg(int type, String key) {
            Message nextMsg = obtainMessage(type);
            nextMsg.getData().putString(TmoMonitor.ARG_TARGET_NAME, key);
            return nextMsg;
        }
    }

    public static TmoMonitor getInst() {
        return inst;
    }

    private TmoMonitor() {
        HandlerThread monitorThread = new HandlerThread("Monitor Hanlder", 19);
        monitorThread.start();
        this.mMonitor = new MoniterHandler(monitorThread.getLooper());
    }

    public void stopWatch() {
        this.mIsSleep = true;
    }

    public void startWatch() {
        this.mIsSleep = false;
        this.mMonitor.wakeUp();
    }

    public void addFutureTask(Runnable r, long delay) {
        MLog.v("TmoMonitor", "Tmo add a future task " + r);
        this.mMonitor.postDelayed(r, delay);
    }

    public void addWatchTarget(String name, Handler h) {
        addWatchTarget(name, 1, h);
    }

    public void addWatchTarget(String name, Executor h) {
        addWatchTarget(name, 2, h);
    }

    public void addWatchTarget(String name, TaskStack h) {
        addWatchTarget(name, 3, h);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean addWatchTarget(String name, int type, Object s) {
        synchronized (this.mWatchedTargets) {
            if (this.mWatchedTargets.containsKey(name)) {
                MLog.e("TmoMonitor", "Add already observed target for " + name + " TaskStack " + s);
                return false;
            }
            this.mWatchedTargets.put(name, s);
        }
    }

    private int checkWarningThreshold(Object obj, boolean isTimeout) {
        Integer cnt = (Integer) this.mTmoTargets.get(obj);
        if (cnt == null) {
            if (isTimeout) {
                this.mTmoTargets.put(obj, Integer.valueOf(1));
            }
            cnt = Integer.valueOf(0);
        } else if (isTimeout) {
            cnt = Integer.valueOf(cnt.intValue() + 1);
            this.mTmoTargets.put(obj, cnt);
        } else {
            this.mTmoTargets.remove(obj);
            cnt = Integer.valueOf(0);
        }
        return cnt.intValue();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkHandler(String name) {
        synchronized (this.mWatchedTargets) {
            Handler observed = this.mWatchedTargets.get(name);
            if (observed == null) {
                Log.e("TmoMonitor", "Check invalide Hanlder " + name);
                return false;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkExecutor(String name) {
        synchronized (this.mWatchedTargets) {
            Executor observed = this.mWatchedTargets.get(name);
            if (observed == null) {
                Log.e("TmoMonitor", "Check invalide executor: " + name);
                return false;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkTaskStack(String name) {
        synchronized (this.mWatchedTargets) {
            TaskStack observed = this.mWatchedTargets.get(name);
            if (observed == null) {
                Log.e("TmoMonitor", "Check invalide executor: " + name);
                return false;
            }
        }
    }

    private static void logExecutorInfo(Executor executor, String mark) {
        MLog.e("TmoMonitor", "executor " + mark + " is not responce. " + executor);
        if (executor instanceof ThreadPoolExecutorEx) {
            MLog.e("TmoMonitor", ((ThreadPoolExecutorEx) executor).logRunningInfo());
            return;
        }
        Map<Thread, StackTraceElement[]> allStacks = Thread.getAllStackTraces();
        StringBuilder sb = new StringBuilder();
        for (Entry<Thread, StackTraceElement[]> e : allStacks.entrySet()) {
            sb.append("Dump thread ").append(((Thread) e.getKey()).getName()).append("id [").append(((Thread) e.getKey()).getId()).append("]");
            LogEx.getTraceInfo((StackTraceElement[]) e.getValue(), 0, 8, sb);
            MLog.e("TmoMonitor", sb.toString());
            sb.replace(0, sb.length(), "");
        }
    }

    private static void logHandlerInfo(Handler h, String key) {
        MLog.e("TmoMonitor", LogEx.getTraceInfo(h.getLooper().getThread(), 0, 8, "TmoMonitor checked Handler ANR." + key, new Object[0]));
    }

    private static void logTaskStackInfo(TaskStack s, String key) {
        MLog.e("TmoMonitor", s.getDebugInfo("TmoMonitor checked TaskStack ANR"));
    }
}
