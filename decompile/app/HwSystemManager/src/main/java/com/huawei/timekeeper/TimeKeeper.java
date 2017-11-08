package com.huawei.timekeeper;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.timekeeper.store.AppDataStore;
import com.huawei.timekeeper.store.SettingsSecureStore;
import com.huawei.timekeeper.store.Store;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class TimeKeeper extends AbsTimeKeeper {
    private static final int CDD_STRONG_AUTH_THREASHOLD = 150;
    private static final String JSON_ERROR_COUNT = "error";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_START_ELAPSED = "elapsed";
    private static final String JSON_START_RTC = "rtc";
    private static final String LAST_TIME_CHANGED_RTC = "last_time_changed_rtc";
    private static final String PKG_KEYGUARD = "com.android.systemui";
    private static final String PKG_SETTIGNS = "com.android.settings";
    protected static final String TAG = "TimeKeeper";
    protected static final boolean VERBOSE = true;
    private static SparseArray<Rule> sRuleStrategy = new SparseArray();
    private static SparseArray<Store> sStoreStrategy = new SparseArray();
    private static Map<String, TimeKeeper> sTimeKeeperMap = new HashMap();
    private final Context mContext;
    private CountDownTimer mCountDownTimer;
    private int mErrorCount;
    private boolean mIsCountingDown;
    private final String mName;
    private TimeObservable mObservable = new TimeObservable();
    private final Rule mRule;
    private long mStartTimeElapsed;
    private long mStartTimeRTC;
    private long mStopTimeInFuture;
    private final Store mStore;
    private TimeTickInfo mTimeTickInfo = new TimeTickInfo();
    private final int mUserHandle;

    static {
        addStoreStrategy(new AppDataStore());
        addStoreStrategy(new SettingsSecureStore());
        addRuleStrategy(new Rule(0));
        addRuleStrategy(new Rule(1));
    }

    public static boolean addStoreStrategy(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("store is null");
        }
        boolean result;
        synchronized (sStoreStrategy) {
            int mode = store.getMode();
            if (sStoreStrategy.get(mode) == null) {
                sStoreStrategy.put(mode, store);
                result = true;
            } else {
                result = false;
            }
            Log.i(TAG, "addStoreStrategy mode:" + mode + ", result:" + result);
        }
        return result;
    }

    public static boolean addRuleStrategy(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("rule is null");
        }
        boolean result;
        Rule.verify(rule);
        synchronized (sRuleStrategy) {
            int level = rule.getLevel();
            if (sRuleStrategy.get(level) == null) {
                sRuleStrategy.put(level, rule);
                result = true;
            } else {
                result = false;
            }
            Log.i(TAG, "addRuleStrategy level:" + level + ", result:" + result);
        }
        return result;
    }

    private TimeKeeper(Context context, String name, Store store, Rule rule, int userHandle) {
        this.mContext = context;
        this.mName = name;
        this.mStore = store;
        this.mUserHandle = userHandle;
        this.mRule = rule;
        this.mErrorCount = 0;
        resetTime();
    }

    public synchronized boolean restore() {
        String json = this.mStore.restore(this.mContext, this.mUserHandle, this.mName);
        boolean changed = !TextUtils.equals(toJson().toString(), json);
        Log.i(TAG, "restore " + this.mName + ", mUserHandle:" + this.mUserHandle + ", changed:" + changed + ", value:" + json);
        if (changed) {
            if (TextUtils.isEmpty(json)) {
                this.mErrorCount = 0;
                this.mStartTimeRTC = 0;
                this.mStartTimeElapsed = 0;
            } else {
                try {
                    JSONObject obj = new JSONObject(json);
                    if (obj.getInt(JSON_LEVEL) != this.mRule.getLevel()) {
                        Log.e(TAG, "restore json level not match.");
                        return false;
                    }
                    int errorCount = obj.getInt(JSON_ERROR_COUNT);
                    long startTimeRTC = obj.getLong(JSON_START_RTC);
                    long startTimeElapsed = obj.getLong(JSON_START_ELAPSED);
                    this.mErrorCount = errorCount;
                    this.mStartTimeRTC = startTimeRTC;
                    this.mStartTimeElapsed = startTimeElapsed;
                } catch (JSONException e) {
                    Log.e(TAG, "restore JSONException");
                    return false;
                }
            }
            if (this.mStartTimeRTC > 0) {
                restoreCountDown();
            } else {
                dispatchFinish(this.mCountDownTimer);
            }
        }
        return true;
    }

    private synchronized void restoreCountDown() {
        long lockingTime;
        int stage = getCurrentStage();
        int length = this.mRule.getChanceStage().length;
        if (this.mErrorCount >= CDD_STRONG_AUTH_THREASHOLD && isCalledBySystemApp()) {
            lockingTime = 86400000;
        } else if (stage < length) {
            lockingTime = this.mRule.getLockingTimeStage()[stage];
        } else {
            lockingTime = this.mRule.getLockingTimeStage()[length - 1];
        }
        long remainingTime = initRemainingTime(lockingTime);
        if (remainingTime > 0) {
            this.mStopTimeInFuture = SystemClock.elapsedRealtime() + remainingTime;
            triggerCountDown(remainingTime);
        } else {
            resetTime();
            save();
        }
    }

    private synchronized long initRemainingTime(long lockingTime) {
        long timeByRTC;
        long timeByElapsed;
        timeByRTC = calculateRemainingTimeByRTC(this.mContext, this.mStartTimeRTC, lockingTime);
        timeByElapsed = calculateRemainingTimeByElapsed(this.mStartTimeElapsed, lockingTime);
        Log.i(TAG, "initRemainingTime timeByRTC=" + timeByRTC + ", timeByElapsed=" + timeByElapsed);
        return timeByRTC < timeByElapsed ? timeByRTC : timeByElapsed;
    }

    private static long calculateRemainingTimeByRTC(Context context, long startTimeRTC, long lockingTime) {
        long lastTimeChangeRTC = Global.getLong(context.getContentResolver(), LAST_TIME_CHANGED_RTC, 0);
        long nowRTC = System.currentTimeMillis();
        if (0 == lastTimeChangeRTC) {
            return lockingTime;
        }
        if (lastTimeChangeRTC < startTimeRTC) {
            return (startTimeRTC + lockingTime) - nowRTC;
        }
        return (lastTimeChangeRTC + lockingTime) - nowRTC;
    }

    private static long calculateRemainingTimeByElapsed(long startTimeElapsed, long lockingInterval) {
        long nowElapsed = SystemClock.elapsedRealtime();
        if (nowElapsed < startTimeElapsed) {
            return lockingInterval - nowElapsed;
        }
        return lockingInterval - (nowElapsed - startTimeElapsed);
    }

    public static TimeKeeper getInstance(Context context, String name, int level) {
        return getInstance(context, name, level, 0, AbsTimeKeeper.USER_NULL);
    }

    public static TimeKeeper getInstance(Context context, String name, int level, int saveMode) {
        return getInstance(context, name, level, saveMode, AbsTimeKeeper.USER_NULL);
    }

    public static TimeKeeper getInstanceForUser(Context context, String name, int level, int userHandle) {
        return getInstance(context, name, level, 1, userHandle);
    }

    private static TimeKeeper getInstance(Context context, String name, int level, int saveMode, int userHandle) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        } else if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name is empty");
        } else {
            Rule rule;
            synchronized (sRuleStrategy) {
                rule = (Rule) sRuleStrategy.get(level);
            }
            if (rule == null) {
                throw new IllegalArgumentException("rule level " + level + " is not found");
            }
            Store store;
            synchronized (sStoreStrategy) {
                store = (Store) sStoreStrategy.get(saveMode);
            }
            if (store == null) {
                throw new IllegalArgumentException("saveMode " + saveMode + " is not found");
            }
            store.checkPermission(context);
            synchronized (TimeKeeper.class) {
                String storedName = store.getStoredName(context, userHandle, name);
                Log.i(TAG, "getInstance name:" + storedName + ", mode:" + saveMode);
                TimeKeeper timeKeeper = (TimeKeeper) sTimeKeeperMap.get(storedName);
                if (timeKeeper != null) {
                    return timeKeeper;
                }
                timeKeeper = new TimeKeeper(context, storedName, store, rule, userHandle);
                timeKeeper.restore();
                sTimeKeeperMap.put(storedName, timeKeeper);
                return timeKeeper;
            }
        }
    }

    public void registerObserver(TimeObserver observer) {
        this.mObservable.registerObserver(observer);
        synchronized (this) {
            Log.d(TAG, "registerObserver isCountingDown:" + this.mIsCountingDown);
            if (this.mIsCountingDown) {
                long millisUntilFinished = this.mStopTimeInFuture - SystemClock.elapsedRealtime();
                if (millisUntilFinished <= 0) {
                    Log.i(TAG, "registerObserver remaining < 0 finish now :" + millisUntilFinished);
                    dispatchFinish(this.mCountDownTimer);
                } else {
                    this.mTimeTickInfo.setTime(millisUntilFinished);
                    Log.i(TAG, "registerObserver info:" + this.mTimeTickInfo);
                    observer.onTimeTick(this.mTimeTickInfo);
                }
            }
        }
    }

    public void unregisterObserver(TimeObserver observer) {
        Log.d(TAG, "unregisterObserver");
        this.mObservable.unregisterObserver(observer);
    }

    public boolean isObserverRegistered(TimeObserver observer) {
        return this.mObservable.isObserverRegistered(observer);
    }

    public void unregisterAll() {
        Log.d(TAG, "unregisterAll");
        this.mObservable.unregisterAll();
    }

    private boolean isCalledBySystemApp() {
        String appPkg = this.mContext == null ? "" : this.mContext.getApplicationInfo().packageName;
        return !PKG_KEYGUARD.equals(appPkg) ? "com.android.settings".equals(appPkg) : true;
    }

    public synchronized int addErrorCount() {
        int chance;
        if (this.mIsCountingDown) {
            throw new IllegalStateException("It is counting down, can not retry.");
        }
        long lockingTime;
        this.mErrorCount++;
        int stage = getCurrentStage();
        int[] chanceStage = this.mRule.getChanceStage();
        int length = chanceStage.length;
        if (this.mErrorCount >= CDD_STRONG_AUTH_THREASHOLD && isCalledBySystemApp()) {
            chance = 0;
            lockingTime = 86400000;
        } else if (stage < chanceStage.length) {
            chance = chanceStage[stage] - this.mErrorCount;
            lockingTime = this.mRule.getLockingTimeStage()[stage];
        } else {
            int error = (this.mErrorCount - chanceStage[chanceStage.length - 1]) % this.mRule.getChanceAddition();
            chance = error == 0 ? 0 : this.mRule.getChanceAddition() - error;
            lockingTime = this.mRule.getLockingTimeStage()[length - 1];
        }
        Log.i(TAG, "addErrorCount mErrorCount=" + this.mErrorCount + ", chance=" + chance);
        if (chance <= 0) {
            this.mStartTimeRTC = System.currentTimeMillis();
            this.mStartTimeElapsed = SystemClock.elapsedRealtime();
            this.mStopTimeInFuture = SystemClock.elapsedRealtime() + lockingTime;
            triggerCountDown(lockingTime);
        } else {
            resetTime();
        }
        save();
        return chance;
    }

    public void resetErrorCount(Context context) {
        synchronized (this) {
            Log.i(TAG, "resetErrorCount " + this.mName);
            this.mIsCountingDown = false;
            this.mErrorCount = 0;
            resetTime();
            if (this.mCountDownTimer != null) {
                this.mCountDownTimer.cancel();
                this.mCountDownTimer = null;
            }
            remove();
        }
        this.mObservable.dispatchFinish();
    }

    public synchronized int getErrorCount() {
        return this.mErrorCount;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRemainingChance() {
        synchronized (this) {
            if (this.mIsCountingDown) {
                long millisUntilFinished = this.mStopTimeInFuture - SystemClock.elapsedRealtime();
                if (millisUntilFinished <= 0) {
                    Log.i(TAG, "getRemainingChance remaining < 0 finish now :" + millisUntilFinished);
                    dispatchFinish(this.mCountDownTimer);
                } else {
                    return 0;
                }
            }
            int errorCount = this.mErrorCount;
        }
    }

    public synchronized TimeTickInfo getTimeTickInfo() {
        long millisUntilFinished;
        if (this.mIsCountingDown) {
            millisUntilFinished = this.mStopTimeInFuture - SystemClock.elapsedRealtime();
            if (millisUntilFinished <= 0) {
                Log.i(TAG, "getTimeTickInfo remaining < 0 finish now :" + millisUntilFinished);
                dispatchFinish(this.mCountDownTimer);
                millisUntilFinished = 0;
            }
        } else {
            millisUntilFinished = 0;
        }
        this.mTimeTickInfo.setTime(millisUntilFinished);
        Log.d(TAG, "getTimeTickInfo:" + this.mTimeTickInfo);
        return this.mTimeTickInfo;
    }

    private synchronized void resetTime() {
        this.mStartTimeRTC = 0;
        this.mStartTimeElapsed = 0;
        this.mStopTimeInFuture = 0;
    }

    private int getCurrentStage() {
        int[] chanceStage = this.mRule.getChanceStage();
        synchronized (this) {
            int errorCount = this.mErrorCount;
        }
        for (int i = chanceStage.length - 1; i >= 0; i--) {
            if (errorCount > chanceStage[i]) {
                return i + 1;
            }
        }
        return 0;
    }

    private synchronized void save() {
        String json = toJson().toString();
        Log.d(TAG, "save:" + this.mName + ", mode:" + this.mStore.getMode() + ", value:" + json);
        this.mStore.save(this.mContext, this.mUserHandle, this.mName, json);
    }

    private synchronized JSONObject toJson() {
        JSONObject obj;
        obj = new JSONObject();
        try {
            obj.put(JSON_LEVEL, this.mRule.getLevel());
            obj.put(JSON_ERROR_COUNT, this.mErrorCount);
            obj.put(JSON_START_RTC, this.mStartTimeRTC);
            obj.put(JSON_START_ELAPSED, this.mStartTimeElapsed);
        } catch (JSONException e) {
            Log.e(TAG, "toJson error", e);
        }
        return obj;
    }

    private synchronized void remove() {
        Log.d(TAG, "remove:" + this.mName);
        this.mStore.remove(this.mContext, this.mUserHandle, this.mName);
    }

    private synchronized void triggerCountDown(long millisInFuture) {
        Log.i(TAG, "trigerCountDown " + this.mName + ", millisInFuture=" + millisInFuture);
        int timeUnit = getTimeUnitByMillis(millisInFuture);
        this.mIsCountingDown = true;
        setCountDownTimer(timeUnit, millisInFuture);
    }

    private static int getTimeUnitByMillis(long millisInFuture) {
        return (millisInFuture - 1000) / 60000 > 0 ? 1 : 0;
    }

    private synchronized void setCountDownTimer(int timeUnit, long millisInFuture) {
        int extra;
        Log.d(TAG, "setCountDownTimer timeUnit=" + timeUnit);
        long countDownInterval = 1 == timeUnit ? 60000 : 1000;
        if (this.mCountDownTimer != null) {
            this.mCountDownTimer.cancel();
        }
        if (timeUnit == 0) {
            extra = (int) ((1200 - (millisInFuture % 1000)) % 1000);
        } else {
            extra = 0;
        }
        final long j = countDownInterval;
        final int i = timeUnit;
        this.mCountDownTimer = new CountDownTimer(((long) extra) + millisInFuture, countDownInterval) {
            public void onTick(long millisUntilFinished) {
                int remaining = (int) (millisUntilFinished / j);
                TimeKeeper.this.dispatchTick(i, millisUntilFinished - ((long) extra), remaining);
                if (remaining == 1 && i != 0) {
                    Log.d(TimeKeeper.TAG, "onTick change interval from minute to second.");
                    cancel();
                    TimeKeeper.this.setCountDownTimer(0, millisUntilFinished - ((long) extra));
                }
            }

            public void onFinish() {
                TimeKeeper.this.dispatchFinish(this);
            }
        }.start();
    }

    private synchronized void dispatchTick(int timeUnit, long millisUntilFinished, int remaining) {
        boolean dispatch = true;
        if (1 == timeUnit) {
            this.mTimeTickInfo.setTime(millisUntilFinished);
        } else if (remaining == 60) {
            this.mTimeTickInfo.setTime(millisUntilFinished, 0, 1, 0);
        } else if (remaining < 60) {
            this.mTimeTickInfo.setTime(millisUntilFinished, 0, 0, remaining);
        } else {
            dispatch = false;
        }
        if (dispatch) {
            Log.d(TAG, "onTick timeUnit:" + timeUnit + ", info:" + this.mTimeTickInfo);
            this.mObservable.dispatchTick(this.mTimeTickInfo);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void dispatchFinish(CountDownTimer timer) {
        if (timer != null) {
            if (timer == this.mCountDownTimer) {
                Log.i(TAG, "onFinish " + this.mName);
                this.mIsCountingDown = false;
                this.mObservable.dispatchFinish();
                resetTime();
                save();
                this.mCountDownTimer = null;
            } else {
                Log.i(TAG, "onFinish from old CountDownTimer, ignore " + this.mName);
            }
        }
    }

    public synchronized void trigerLockout(long timeToLock) {
        Log.w(TAG, "trigerLockout " + timeToLock);
        if (!this.mIsCountingDown && timeToLock > 0) {
            if (isCalledBySystemApp()) {
                this.mStartTimeRTC = System.currentTimeMillis();
                this.mStartTimeElapsed = SystemClock.elapsedRealtime();
                this.mStopTimeInFuture = SystemClock.elapsedRealtime() + timeToLock;
                triggerCountDown(timeToLock);
                save();
            }
        }
        throw new IllegalArgumentException("lockout time can't be negative");
    }
}
