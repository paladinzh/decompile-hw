package com.huawei.timekeeper;

import android.content.Context;

public abstract class AbsTimeKeeper {
    public abstract int addErrorCount();

    public abstract int getErrorCount();

    public abstract int getRemainingChance();

    public abstract TimeTickInfo getTimeTickInfo();

    public abstract boolean isObserverRegistered(TimeObserver timeObserver);

    public abstract void registerObserver(TimeObserver timeObserver);

    public abstract void resetErrorCount(Context context);

    public abstract void unregisterAll();

    public abstract void unregisterObserver(TimeObserver timeObserver);

    public void trigerLockout(long timeToLock) {
    }
}
