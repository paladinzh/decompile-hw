package com.huawei.powergenie.core.app;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.debugtest.DbgUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class AlarmStats {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private final AppManager mAppManager;
    private final Context mContext;
    private final HashMap<String, Integer> mCurScrOffAlarmCount = new HashMap();
    private long mCurScrOffTotalWakeups = 0;
    private final ICoreContext mICoreContext;
    private long mLastWakeupTime = 0;
    private final Object mLock = new Object();
    private final HashMap<String, Long> mScrOffAlarmLastTime = new HashMap();
    private long mScreenOffTime = 0;
    private final HashMap<String, Integer> mTotalScrOffAlarmCount = new HashMap();
    private long mTotalScreenOffDuration = 0;

    protected AlarmStats(ICoreContext pgcontext, AppManager appManager) {
        this.mICoreContext = pgcontext;
        this.mContext = pgcontext.getContext();
        this.mAppManager = appManager;
    }

    protected void handleScreenState(boolean screenOn) {
        if (screenOn) {
            printTopAlarmApps(this.mCurScrOffAlarmCount);
            this.mTotalScreenOffDuration += this.mScreenOffTime;
            this.mScreenOffTime = 0;
            this.mScrOffAlarmLastTime.clear();
            return;
        }
        synchronized (this.mLock) {
            this.mCurScrOffAlarmCount.clear();
        }
        this.mCurScrOffTotalWakeups = 0;
        this.mScreenOffTime = SystemClock.elapsedRealtime();
    }

    protected void handleAlarmStart(String pkgName, int alarmType, String interval, String alarmIntent) {
        if (DEBUG_USB) {
            Log.i("AlarmStats", "alarm app: " + pkgName + ", type: " + alarmType + ",interval: " + interval + ",triggerIntent:" + alarmIntent + ",total wakeups:" + this.mCurScrOffTotalWakeups);
        }
        if (alarmType == 0 || alarmType == 2) {
            long now = SystemClock.elapsedRealtime();
            if (now > this.mLastWakeupTime + 50) {
                this.mCurScrOffTotalWakeups++;
            }
            this.mLastWakeupTime = now;
            if (!("android".equals(pkgName) || "com.android.phone".equals(pkgName))) {
                Long lastTime = (Long) this.mScrOffAlarmLastTime.get(pkgName);
                this.mScrOffAlarmLastTime.put(pkgName, Long.valueOf(now));
                if (lastTime == null || now >= lastTime.longValue() + 50) {
                    statsAlarm(pkgName);
                    handleAlarmAbnormal(pkgName, alarmType);
                } else {
                    if (DEBUG_USB) {
                        Log.i("AlarmStats", "less 50ms and skip wakeup alarm: " + pkgName);
                    }
                }
            }
        }
    }

    private void statsAlarm(String pkgName) {
        synchronized (this.mLock) {
            Integer count = (Integer) this.mCurScrOffAlarmCount.get(pkgName);
            if (count != null) {
                HashMap hashMap = this.mCurScrOffAlarmCount;
                int intValue = count.intValue() + 1;
                count = Integer.valueOf(intValue);
                hashMap.put(pkgName, Integer.valueOf(intValue));
            } else {
                this.mCurScrOffAlarmCount.put(pkgName, Integer.valueOf(1));
            }
            count = (Integer) this.mTotalScrOffAlarmCount.get(pkgName);
            if (count != null) {
                hashMap = this.mTotalScrOffAlarmCount;
                intValue = count.intValue() + 1;
                count = Integer.valueOf(intValue);
                hashMap.put(pkgName, Integer.valueOf(intValue));
            } else {
                this.mTotalScrOffAlarmCount.put(pkgName, Integer.valueOf(1));
            }
        }
    }

    private void handleAlarmAbnormal(String pkgName, int alarmType) {
        int startCnt = getCurScrOffAlarmCount(pkgName);
        int startInterval = getCurScrOffAlarmFreq(pkgName);
        if (isHighPowerAlarm(startCnt, startInterval, pkgName)) {
            if (DEBUG_USB) {
                Log.i("AlarmStats", "high power alarm pkg:" + pkgName + ", repeats:" + startCnt + ", interval:" + startInterval + "s");
            }
            this.mAppManager.processAbnormalPowerApp(pkgName, 0, startCnt, startInterval * 1000, "alarm", false);
        }
    }

    private boolean isHighPowerAlarm(int count, int interval, String pkg) {
        if (this.mAppManager.isAllowedUninstallPkg(pkg)) {
            if (count >= 20 && interval < 180) {
                return true;
            }
        } else if (count >= 12 && interval < 300) {
            return true;
        }
        return false;
    }

    protected long getTotalWakeupsSinceScrOff() {
        return this.mCurScrOffTotalWakeups;
    }

    protected boolean isAlarmFreqEmpty() {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mCurScrOffAlarmCount.size() == 0 && this.mTotalScrOffAlarmCount.size() == 0) {
                z = true;
            }
        }
        return z;
    }

    protected ArrayList<String> getCurScrOffAlarmApps(int topNum, int maxFreq) {
        ArrayList<String> topAlarmPkgList = new ArrayList();
        synchronized (this.mLock) {
            if (this.mCurScrOffAlarmCount.size() > 0) {
                int cnt = 0;
                for (Entry<String, Integer> entry : sort(this.mCurScrOffAlarmCount)) {
                    String pkg = (String) entry.getKey();
                    if (getCurScrOffAlarmFreq(pkg) < maxFreq && !topAlarmPkgList.contains(pkg)) {
                        topAlarmPkgList.add(pkg);
                    }
                    cnt++;
                    if (cnt >= topNum) {
                        break;
                    }
                }
            }
        }
        return topAlarmPkgList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int getCurScrOffAlarmCount(String pkg) {
        synchronized (this.mLock) {
            if (pkg != null) {
                if (this.mCurScrOffAlarmCount.containsKey(pkg)) {
                    int alarmCount = ((Integer) this.mCurScrOffAlarmCount.get(pkg)).intValue();
                    return alarmCount;
                }
            }
        }
    }

    protected int getCurScrOffAlarmFreq(String pkg) {
        int count = getCurScrOffAlarmCount(pkg);
        long interval = SystemClock.elapsedRealtime() - this.mScreenOffTime;
        if (count <= 3 || interval <= 0) {
            return -1;
        }
        return (int) (interval / ((long) (count * 1000)));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int getTotalScrOffAlarmCount(String pkg) {
        synchronized (this.mLock) {
            if (pkg != null) {
                if (this.mTotalScrOffAlarmCount.containsKey(pkg)) {
                    int alarmCount = ((Integer) this.mTotalScrOffAlarmCount.get(pkg)).intValue();
                    return alarmCount;
                }
            }
        }
    }

    protected int getTotalScrOffAlarmFreq(String pkg) {
        int count = getTotalScrOffAlarmCount(pkg);
        long totalScrOff = (SystemClock.elapsedRealtime() - this.mScreenOffTime) + this.mTotalScreenOffDuration;
        if (count <= 3 || totalScrOff <= 0) {
            return -1;
        }
        return (int) (totalScrOff / ((long) (count * 1000)));
    }

    protected ArrayList<String> getTotalScrOffAlarmApps(int topNum, int maxInterval, int minCnt) {
        ArrayList<String> topAlarmPkgList = new ArrayList();
        synchronized (this.mLock) {
            if (this.mTotalScrOffAlarmCount.size() > 0) {
                List<Entry<String, Integer>> pkgList = sort(this.mTotalScrOffAlarmCount);
                int cnt = 0;
                Integer freq = Integer.valueOf(0);
                for (Entry<String, Integer> entry : pkgList) {
                    if (cnt >= topNum) {
                        break;
                    }
                    String pkg = (String) entry.getKey();
                    freq = Integer.valueOf(getTotalScrOffAlarmFreq(pkg));
                    if (freq != null && freq.intValue() < maxInterval) {
                        Integer tmpCnt = (Integer) this.mCurScrOffAlarmCount.get(pkg);
                        if (!(tmpCnt == null || tmpCnt.intValue() <= minCnt || topAlarmPkgList.contains(pkg))) {
                            topAlarmPkgList.add(pkg);
                        }
                    }
                    cnt++;
                }
            }
        }
        return topAlarmPkgList;
    }

    private void printTopAlarmApps(HashMap<String, Integer> alarmMap) {
        long interval = SystemClock.elapsedRealtime() - this.mScreenOffTime;
        if (DEBUG_USB || interval >= 3600000) {
            synchronized (this.mLock) {
                if (alarmMap.size() > 0) {
                    int cnt = 0;
                    List<Entry<String, Integer>> pkgList = sort(alarmMap);
                    for (int index = pkgList.size() - 1; index >= 0; index--) {
                        Entry<String, Integer> entry = (Entry) pkgList.get(index);
                        String pkg = (String) entry.getKey();
                        Log.i("AlarmStats", "Top alarm : " + pkg + ", count = " + ((Integer) entry.getValue()).intValue());
                        cnt++;
                        if (cnt > 10) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private List<Entry<String, Integer>> sort(HashMap<String, Integer> alarmMap) {
        List<Entry<String, Integer>> listData = new ArrayList(alarmMap.entrySet());
        Collections.sort(listData, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                if (o2.getValue() == null || o1.getValue() == null) {
                    return -1;
                }
                return ((Integer) o1.getValue()).compareTo((Integer) o2.getValue());
            }
        });
        return listData;
    }
}
