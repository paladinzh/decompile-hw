package com.huawei.keyguard.events;

import android.content.Context;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.events.TimeZoneFinder.TimeZoneListener;
import com.huawei.keyguard.util.OsUtils;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeZoneManager {
    private static TimeZoneManager sInstance;
    private static long sLastTime;
    private HashSet<TimeZoneListener> mListeners;
    private TimeZoneTask mTimeZoneTask;
    private TimeZone sDefaultTimeZone;
    private TimeZone sLastTimeZone;

    private class TimeZoneTask implements Runnable {
        private final AtomicBoolean mCancelled = new AtomicBoolean();
        Context mContext;

        public TimeZoneTask(Context context) {
            this.mCancelled.set(false);
        }

        public void execute() {
            GlobalContext.getSerialExecutor().execute(this);
        }

        public void run() {
            final TimeZone timeZone = checkTimeZoneInBackground(this.mContext);
            GlobalContext.getUIHandler().post(new Runnable() {
                public void run() {
                    if (timeZone != null) {
                        TimeZoneManager.this.setCacheTimeZone(timeZone);
                        TimeZoneManager.sLastTime = System.currentTimeMillis();
                    }
                    if (!TimeZoneTask.this.isCancelled()) {
                        for (TimeZoneListener listener : TimeZoneManager.this.mListeners) {
                            listener.onTimeZoneChange(null, timeZone);
                        }
                        TimeZoneManager.this.mTimeZoneTask = null;
                    }
                }
            });
        }

        protected TimeZone checkTimeZoneInBackground(Context context) {
            if (this.mContext == null) {
                return null;
            }
            TimeZone tz = new NitzTimeZoneFinder().getTimeZone(context);
            if (tz != null) {
                return tz;
            }
            if (isCancelled()) {
                return null;
            }
            tz = new MncTimeZoneFinder(null).getTimeZone(this.mContext);
            if (tz != null) {
                return tz;
            }
            return null;
        }

        private final void cancel(boolean mayInterrupt) {
            this.mCancelled.set(true);
        }

        private final boolean isCancelled() {
            return this.mCancelled.get();
        }
    }

    private TimeZoneManager() {
    }

    public static synchronized TimeZoneManager getInstance() {
        TimeZoneManager timeZoneManager;
        synchronized (TimeZoneManager.class) {
            if (sInstance == null) {
                sInstance = new TimeZoneManager();
            }
            timeZoneManager = sInstance;
        }
        return timeZoneManager;
    }

    public void setCacheTimeZone(TimeZone timeZone) {
        this.sLastTimeZone = timeZone;
    }

    public void clearCacheTimeZone() {
        setCacheTimeZone(null);
    }

    public void registerTimeZoneListener(Context context, TimeZoneListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new HashSet();
        }
        this.mListeners.add(listener);
        if (this.sLastTimeZone == null || System.currentTimeMillis() - sLastTime >= 1800000) {
            if (this.mTimeZoneTask != null) {
                this.mTimeZoneTask.cancel(true);
            }
            this.mTimeZoneTask = new TimeZoneTask(context);
            this.mTimeZoneTask.execute();
        }
    }

    public void reFinderTimeZone(Context context) {
        if (this.mTimeZoneTask == null) {
            this.mTimeZoneTask = new TimeZoneTask(context);
            this.mTimeZoneTask.execute();
        }
    }

    public void unregisterTimeZoneListener(TimeZoneListener listener) {
        if (this.mListeners != null) {
            this.mListeners.remove(listener);
        }
        if (this.mListeners != null && this.mListeners.size() == 0 && this.mTimeZoneTask != null) {
            this.mTimeZoneTask.cancel(true);
        }
    }

    public TimeZone getDefaultTimeZone(Context ctx) {
        String sDefTimezoneId = OsUtils.getSystemString(ctx, "keyguard_default_time_zone");
        if (!(sDefTimezoneId == null || sDefTimezoneId.length() == 0)) {
            this.sDefaultTimeZone = TimeZone.getTimeZone(sDefTimezoneId);
        }
        return this.sDefaultTimeZone;
    }

    public void setDefaultTimeZone(Context ctx, String sTzId) {
        if (sTzId == null || sTzId.length() == 0) {
            this.sDefaultTimeZone = null;
        } else {
            this.sDefaultTimeZone = TimeZone.getTimeZone(sTzId);
        }
    }
}
