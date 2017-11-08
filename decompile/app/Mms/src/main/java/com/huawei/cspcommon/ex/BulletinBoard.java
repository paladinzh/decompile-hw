package com.huawei.cspcommon.ex;

import android.os.SystemClock;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class BulletinBoard {
    private static BulletinBoard mBB = new BulletinBoard();
    private HashMap<String, BulletinTask> mMsgs = new HashMap();
    private Runnable mTodoEventChecker = new Runnable() {
        public void run() {
            List<String> otEvents = new ArrayList();
            long now = SystemClock.uptimeMillis();
            synchronized (BulletinBoard.this.mTodoEvents) {
                for (Entry<String, Long> e : BulletinBoard.this.mTodoEvents.entrySet()) {
                    if (((Long) e.getValue()).longValue() > now) {
                        otEvents.add((String) e.getKey());
                    }
                }
                for (String s : otEvents) {
                    BulletinBoard.this.mTodoEvents.remove(s);
                }
                boolean hasTodos = BulletinBoard.this.mTodoEvents.size() > 0;
            }
            for (String s2 : otEvents) {
                MLog.e("bulletin", "Check undo thing timeout " + s2);
            }
            if (hasTodos) {
                TmoMonitor.getInst().addFutureTask(BulletinBoard.this.mTodoEventChecker, 2000);
            }
        }
    };
    private HashMap<String, Long> mTodoEvents = new HashMap();

    private static class BulletinTask implements CheckableRunnable {
        long mFinishTime;
        long mStartTime;
        String mTaskName;
        private long mWaitTime;

        private BulletinTask(String taskName) {
            this.mWaitTime = 1000;
            this.mTaskName = taskName;
        }

        public void run() {
            BulletinBoard.mBB.removeTask(this.mTaskName);
            this.mFinishTime = SystemClock.uptimeMillis();
        }

        public String toString() {
            return this.mTaskName;
        }

        private void reset() {
            this.mStartTime = SystemClock.uptimeMillis();
        }

        public long getMaxRunningTime() {
            return this.mWaitTime;
        }

        public void onTimeout(long runTime) {
            MLog.w("BulletinTask", "BulletinTask " + this.mTaskName + " time out, start " + this.mStartTime + " use " + (this.mFinishTime - this.mStartTime));
        }
    }

    public static BulletinBoard getInst() {
        return mBB;
    }

    public Runnable getBulletinTask(String name) {
        BulletinTask b;
        synchronized (this.mMsgs) {
            b = (BulletinTask) this.mMsgs.get(name);
            if (b == null) {
                b = new BulletinTask(name);
                this.mMsgs.put(name, b);
            }
            b.reset();
        }
        return b;
    }

    public void removeTask(String taskName) {
        synchronized (this.mMsgs) {
            this.mMsgs.remove(taskName);
            this.mMsgs.notifyAll();
        }
    }

    public boolean checkAnr(String key, long delay) {
        synchronized (this.mMsgs) {
            if (this.mMsgs.containsKey(key)) {
                long waited;
                long startTime = SystemClock.uptimeMillis();
                do {
                    try {
                        this.mMsgs.wait(delay);
                    } catch (InterruptedException e) {
                    }
                    if (this.mMsgs.containsKey(key)) {
                        waited = SystemClock.uptimeMillis() - startTime;
                    } else {
                        return false;
                    }
                } while (waited <= delay);
                MLog.w("bulletin", "wait for task " + key + " timeout. lasting " + waited);
                return true;
            }
            return false;
        }
    }
}
