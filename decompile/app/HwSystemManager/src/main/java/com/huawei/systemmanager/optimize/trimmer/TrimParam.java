package com.huawei.systemmanager.optimize.trimmer;

import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrimParam {
    private Context mCotnext;
    private boolean mIsOnekeyclean;
    private boolean mKeepForeground;
    private AtomicBoolean mNeedRefreshRecentTask = new AtomicBoolean(false);
    private long mStartTime = System.currentTimeMillis();

    private TrimParam() {
    }

    public Context getContext() {
        return this.mCotnext;
    }

    public boolean isKeepForeground() {
        return this.mKeepForeground;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public boolean isOnekeyclean() {
        return this.mIsOnekeyclean;
    }

    public void updateRecentTask() {
        this.mNeedRefreshRecentTask.set(true);
    }

    public boolean consumeRecentTaskFlag() {
        return this.mNeedRefreshRecentTask.getAndSet(false);
    }

    public static TrimParam createSystemuiTrimParam(Context ctx, long startTime) {
        TrimParam param = new TrimParam();
        param.mCotnext = ctx;
        param.mKeepForeground = false;
        param.mStartTime = startTime;
        param.mIsOnekeyclean = false;
        return param;
    }

    public static TrimParam createOnekeycleanParam(Context ctx, boolean keepForeground) {
        TrimParam param = new TrimParam();
        param.mCotnext = ctx;
        param.mKeepForeground = keepForeground;
        param.mStartTime = System.currentTimeMillis();
        param.mIsOnekeyclean = true;
        return param;
    }
}
