package com.huawei.timekeeper;

import android.content.Context;

public abstract class AbsTimeKeeper {
    public static final long DAY = 86400000;
    public static final long HOUR = 3600000;
    public static final long MINUTE = 60000;
    public static final int RADIX_MINUTE_TO_HOUR = 60;
    public static final int RADIX_SECOND_TO_MINUTE = 60;
    public static final int SAVE_MODE_APP_DATA = 0;
    public static final int SAVE_MODE_SETTINGS_SECURE = 1;
    public static final long SECOND = 1000;
    public static final int SECURITY_HIGH = 1;
    public static final int SECURITY_LOW = 0;
    public static final int TIME_UNIT_MINUTE = 1;
    public static final int TIME_UNIT_SECOND = 0;
    public static final int USER_NULL = -10000;

    public abstract int addErrorCount();

    public abstract int getErrorCount();

    public abstract int getRemainingChance();

    public abstract TimeTickInfo getTimeTickInfo();

    public abstract boolean isObserverRegistered(TimeObserver timeObserver);

    public abstract void registerObserver(TimeObserver timeObserver);

    public abstract void resetErrorCount(Context context);

    public abstract void unregisterAll();

    public abstract void unregisterObserver(TimeObserver timeObserver);

    public boolean restore() {
        return true;
    }

    public void trigerLockout(long timeToLock) {
    }
}
