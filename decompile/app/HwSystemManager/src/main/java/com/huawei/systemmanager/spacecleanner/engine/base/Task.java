package com.huawei.systemmanager.spacecleanner.engine.base;

import android.content.Context;
import android.os.SystemClock;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Task {
    public static final int STATE_CANCELED = 9;
    public static final int STATE_COMPLETE = 7;
    public static final int STATE_PENDING = 1;
    public static final int STATE_RUNNING = 5;
    private static final String TAG = "BaseTask";
    private AtomicBoolean mCanceled = new AtomicBoolean(false);
    private final Context mContext;
    private AtomicInteger mCurrentState = new AtomicInteger(1);
    private ExecutorService mExecutor;
    private boolean mHasNotifyEnd = false;
    private TaskListener mListener;
    protected ScanParams mParams;
    private int mProgress;
    private Object mTag;
    private long mTaskCostTime;
    private long mTaskStartTime;

    public interface TaskListener {
        void onItemUpdate(Task task, Trash trash);

        void onProgressUpdate(Task task, int i, String str);

        void onTaskEnd(Task task, boolean z);

        void onTaskStart(Task task);
    }

    public abstract List<Integer> getSupportTrashType();

    public abstract String getTaskName();

    public abstract int getType();

    public abstract boolean isNormal();

    public Task(Context ctx) {
        this.mContext = ctx;
    }

    public final void start(ScanParams p) {
        if (setState(5)) {
            this.mTaskStartTime = SystemClock.elapsedRealtime();
            this.mParams = p;
            startWork(p);
            return;
        }
        HwLog.w(getTaskName(), "start, setState failed!");
    }

    protected void startWork(final ScanParams p) {
        Runnable task = new Runnable() {
            public void run() {
                HwLog.i(Task.this.getTaskName(), "do task begin");
                Task.this.onPublishStart();
                Task.this.doTask(p);
                HwLog.i(Task.this.getTaskName(), "do task end");
            }
        };
        if (this.mExecutor != null) {
            this.mExecutor.execute(task);
        } else {
            HwLog.e(getTaskName(), "no executor set!");
        }
    }

    public void cancel() {
        if (isEnd()) {
            HwLog.i(getTaskName(), "cancel called, but its already end, ignore the command");
            return;
        }
        HwLog.i(getTaskName(), "cancel called, set canceled!");
        this.mCanceled.set(true);
        onPublishEnd();
    }

    public ScanParams getParams() {
        return this.mParams;
    }

    protected void doTask(ScanParams p) {
    }

    protected void onPublishStart() {
        if (!(this.mTaskStartTime == 0 || this.mListener == null)) {
            this.mListener.onTaskStart(this);
        }
    }

    protected void onPublishProgress(int progress, String info) {
        this.mProgress = progress;
        if (this.mListener != null) {
            this.mListener.onProgressUpdate(this, progress, info);
        }
    }

    protected void onPublishItemUpdate(Trash item) {
        if (this.mListener != null) {
            this.mListener.onItemUpdate(this, item);
        }
    }

    protected void onPublishEnd() {
        boolean canceled = this.mCanceled.get();
        if (setState(canceled ? 9 : 7)) {
            this.mTaskCostTime = SystemClock.elapsedRealtime() - this.mTaskStartTime;
            if (this.mListener != null) {
                this.mListener.onTaskEnd(this, canceled);
            }
            return;
        }
        HwLog.w(TAG, getTaskName() + " onPublishEnd, set stat failed, current state is:" + getState());
    }

    public void setListener(TaskListener l) {
        this.mListener = l;
    }

    public long getTaskCostTime() {
        return this.mTaskCostTime;
    }

    protected boolean setState(int newState) {
        synchronized (this) {
            if (isEnd()) {
                HwLog.i(getTaskName(), "is already end, ignore setState: " + newState);
                return false;
            } else if (this.mCurrentState.get() >= newState) {
                return false;
            } else {
                this.mCurrentState.set(newState);
                return true;
            }
        }
    }

    protected void setCanceled(boolean canceled) {
        this.mCanceled.set(canceled);
    }

    public int getState() {
        int i;
        synchronized (this) {
            i = this.mCurrentState.get();
        }
        return i;
    }

    public boolean isEnd() {
        boolean z;
        synchronized (this) {
            z = this.mCurrentState.get() >= 7;
        }
        return z;
    }

    public boolean isNotifyEnd() {
        return this.mHasNotifyEnd;
    }

    public void setNotifyEnd(boolean isEnd) {
        this.mHasNotifyEnd = isEnd;
    }

    public boolean isCanceled() {
        return this.mCanceled.get();
    }

    public List<Trash> getResult() {
        return null;
    }

    public void setExecutor(ExecutorService excutor) {
        this.mExecutor = excutor;
    }

    public ExecutorService getExecutor() {
        return this.mExecutor;
    }

    public int getProgress() {
        return this.mProgress;
    }

    protected int getWeight() {
        return 0;
    }

    public boolean isSupportByTrashType(int trashType) {
        int supportTrash = 0;
        for (Integer type : getSupportTrashType()) {
            supportTrash |= type.intValue();
        }
        if ((trashType & supportTrash) != 0) {
            return true;
        }
        return false;
    }

    protected final void setTag(Object tag) {
        this.mTag = tag;
    }

    protected final Object getTag() {
        return this.mTag;
    }

    public Context getContext() {
        return this.mContext;
    }

    protected static final String getStateName(int state) {
        String stateStr = "";
        switch (state) {
            case 1:
                return "state_pending";
            case 5:
                return "state_running";
            case 7:
                return "state_complete";
            case 9:
                return "state_canceled";
            default:
                return "state_unknow:" + String.valueOf(state);
        }
    }
}
