package com.android.settings;

import android.content.Context;
import android.util.Log;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;

public class TimeKeeperAdapter {
    private TimerCallback mCallback;
    private String mName;
    private boolean mNeedRestore = false;
    TimeObserver mObeserver = new TimeObserver() {
        public void onTimeTick(TimeTickInfo info) {
            if (TimeKeeperAdapter.this.mCallback != null) {
                TimeKeeperAdapter.this.mCallback.onTimeTick(TimeKeeperAdapter.this.parseTimeInfo(info));
            }
        }

        public void onTimeFinish() {
            if (TimeKeeperAdapter.this.mCallback != null) {
                Log.d("TimeKeeperAdapter", "time tick finished.");
                TimeKeeperAdapter.this.mCallback.onTimeFinish();
            }
        }
    };
    private TimeKeeper mTimeKeeper;

    public interface TimerCallback {
        void onTimeFinish();

        void onTimeTick(TimerRemainMsg timerRemainMsg);
    }

    public static class TimerRemainMsg {
        int mRemainTime = -1;
        int mStringResourceId = -1;

        public TimerRemainMsg(int resourceId, int time) {
            this.mStringResourceId = resourceId;
            this.mRemainTime = time;
        }
    }

    private TimerRemainMsg parseTimeInfo(TimeTickInfo info) {
        if (info.getHour() != 0) {
            return new TimerRemainMsg(2131689537, info.getHour());
        }
        if (info.getMinute() != 0) {
            return new TimerRemainMsg(2131689536, info.getMinute());
        }
        if (info.getSecond() != 0) {
            return new TimerRemainMsg(2131689535, info.getSecond());
        }
        return null;
    }

    public TimeKeeperAdapter(Context context, String name, boolean needRestore) {
        this.mNeedRestore = needRestore;
        this.mName = name;
        this.mTimeKeeper = TimeKeeper.getInstance(context, name, 0, 1);
        isAvailable();
    }

    private boolean isAvailable() {
        if (this.mTimeKeeper == null) {
            Log.e("TimeKeeperAdapter", "mTimeTicker is null");
            return false;
        }
        Log.d("TimeKeeperAdapter", "Time keepr name is: " + this.mName);
        if (this.mNeedRestore) {
            this.mTimeKeeper.restore();
        }
        return true;
    }

    public void registerObserver(TimerCallback callback) {
        if (!isAvailable()) {
            return;
        }
        if (this.mTimeKeeper.isObserverRegistered(this.mObeserver)) {
            Log.d("TimeKeeperAdapter", "mObeserver has registered already.");
            return;
        }
        this.mTimeKeeper.registerObserver(this.mObeserver);
        this.mCallback = callback;
    }

    public void unregisterObserver() {
        if (isAvailable() && this.mTimeKeeper.isObserverRegistered(this.mObeserver)) {
            this.mTimeKeeper.unregisterObserver(this.mObeserver);
        }
    }

    public void resetErrorCount(Context context) {
        if (isAvailable()) {
            this.mTimeKeeper.resetErrorCount(context);
            Log.d("TimeKeeperAdapter", "reset error count");
            if (getRemainingChance() <= 0) {
                Log.e("TimeKeeperAdapter", "mTimeKeeper reset failed.");
            }
        }
    }

    public int addErrorCount() {
        if (!isAvailable()) {
            return 0;
        }
        try {
            int chanceLeft = this.mTimeKeeper.addErrorCount();
            Log.d("TimeKeeperAdapter", "remaining chance after adding error count is: " + chanceLeft);
            return chanceLeft;
        } catch (IllegalStateException ex) {
            Log.e("TimeKeeperAdapter", ex.getMessage());
            return 0;
        }
    }

    public int getRemainingChance() {
        if (!isAvailable()) {
            return 0;
        }
        int remainingChance = this.mTimeKeeper.getRemainingChance();
        Log.d("TimeKeeperAdapter", "remaining chance is: " + remainingChance);
        return remainingChance;
    }

    public TimerRemainMsg getTimerRemainMsg() {
        if (isAvailable()) {
            return parseTimeInfo(this.mTimeKeeper.getTimeTickInfo());
        }
        return null;
    }
}
