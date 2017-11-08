package com.huawei.powergenie.core.powerstats;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

public class AppPowerStatsService extends BaseService implements IPowerStats {
    private static Class mHwLog = null;
    private static Method mReport = null;
    private HashMap<Integer, ArrayList<Stats>> mAppStats = new HashMap();
    private PowerStatsHandler mHandler;
    private ICoreContext mICoreContext;
    private long mLastReportTime = SystemClock.elapsedRealtime();
    private int mStatsCount = 0;
    private boolean mUsbConnect = false;

    public class Stats {
        public int mCount;
        public String mPkg;
        public int mType;

        public Stats(int type, String pkg, int count) {
            this.mType = type;
            this.mPkg = pkg;
            this.mCount = count;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("type=").append(this.mType);
            builder.append(" pkg=").append(this.mPkg);
            builder.append(" count=").append(this.mCount);
            return builder.toString();
        }
    }

    public class DurationStats extends Stats {
        public long mDuration;
        public long mLastStartTime;
        public long mTotalDuration = 0;

        public DurationStats(int type, String pkg, long duration, int count, long startTime) {
            super(type, pkg, count);
            this.mDuration = duration;
            this.mLastStartTime = startTime;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append(" duration=").append(this.mDuration + this.mTotalDuration);
            builder.append(" lastStartTime=").append(this.mLastStartTime);
            return builder.toString();
        }
    }

    private final class PowerStatsHandler extends Handler {
        public PowerStatsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10000:
                    if (AppPowerStatsService.this.canReportStats()) {
                        AppPowerStatsService.this.reportPowerStats();
                        return;
                    }
                    return;
                case 10001:
                    AppPowerStatsService.this.reportPowerStats();
                    return;
                default:
                    return;
            }
        }
    }

    public class ReasonStats extends Stats {
        public String mReason;

        public ReasonStats(int type, String pkg, String reason, int count) {
            super(type, pkg, count);
            this.mReason = reason;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append(" reason=").append(this.mReason);
            return builder.toString();
        }
    }

    public AppPowerStatsService(ICoreContext context) {
        this.mICoreContext = context;
    }

    public void start() {
        HandlerThread thread = new HandlerThread("PowerStats", 10);
        thread.start();
        this.mHandler = new PowerStatsHandler(thread.getLooper());
    }

    public void onInputMsgEvent(MsgEvent event) {
        switch (event.getEventId()) {
            case 300:
                if (this.mStatsCount >= 100) {
                    Log.i("PowerStats", "power stats up to " + this.mStatsCount + ", force report when scrOn");
                    forceReport();
                    return;
                }
                return;
            case 303:
                forceReport();
                return;
            case 310:
                this.mUsbConnect = true;
                checkReport(5000);
                return;
            case 311:
                this.mUsbConnect = false;
                return;
            default:
                return;
        }
    }

    private void forceReport() {
        this.mHandler.removeMessages(10000);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10001), 100);
    }

    private void checkReport(long delay) {
        this.mHandler.removeMessages(10000);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10000), delay);
    }

    private void reportPowerStats() {
        synchronized (this.mAppStats) {
            for (Entry entry : this.mAppStats.entrySet()) {
                for (Stats sts : (ArrayList) entry.getValue()) {
                    int statsType = getStatsType(sts.mType);
                    if (statsType == 1) {
                        reportAppPowerCtrl(String.format("type=%s package=%s count=%s", new Object[]{Integer.valueOf(sts.mType), sts.mPkg, Integer.valueOf(sts.mCount)}));
                    } else if (statsType == 3) {
                        DurationStats ds = (DurationStats) sts;
                        reportAppPowerCtrl(String.format("type=%s package=%s duration=%s count=%s", new Object[]{Integer.valueOf(ds.mType), ds.mPkg, Long.valueOf(ds.mDuration + ds.mTotalDuration), Integer.valueOf(ds.mCount)}));
                    } else if (statsType == 2) {
                        ReasonStats rs = (ReasonStats) sts;
                        reportAppPowerCtrl(String.format("type=%s package=%s reason=%s count=%s", new Object[]{Integer.valueOf(rs.mType), rs.mPkg, rs.mReason, Integer.valueOf(rs.mCount)}));
                    }
                }
            }
            this.mAppStats.clear();
            this.mStatsCount = 0;
        }
    }

    private boolean canReportStats() {
        if (!this.mUsbConnect || this.mStatsCount == 0) {
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastReportTime < 14400000) {
            return false;
        }
        this.mLastReportTime = now;
        return true;
    }

    public void iStats(int type, ArrayList<String> pkgs) {
        reportAppPowerCtrl("type=" + type + " packages=" + pkgs);
    }

    public void iStats(int type, String pkg, int count) {
        updateAppStats(type, pkg, null, count, -1, -1);
    }

    public void iStats(int type, String pkg, int count, String reason) {
        updateAppStats(type, pkg, reason, count, -1, -1);
    }

    public void iStats(int type, String pkg, int count, long duration, long startTime) {
        updateAppStats(type, pkg, null, count, duration, startTime);
    }

    private int getStatsType(int type) {
        switch (type) {
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
            case 6:
            case 7:
                return 3;
            case 4:
                return 2;
            case 5:
                return 2;
            default:
                Log.e("PowerStats", "error unknown type:" + type);
                return 1;
        }
    }

    private void updateAppStats(int type, String pkg, String reason, int count, long duration, long startTime) {
        synchronized (this.mAppStats) {
            ArrayList<Stats> appStats;
            if (this.mAppStats.containsKey(Integer.valueOf(type))) {
                appStats = (ArrayList) this.mAppStats.get(Integer.valueOf(type));
            } else {
                appStats = new ArrayList();
                this.mAppStats.put(Integer.valueOf(type), appStats);
            }
            int index = getStats(type, pkg, reason, appStats);
            if (index >= 0) {
                updateStats((Stats) appStats.get(index), reason, count, duration, startTime);
            } else {
                appStats.add(newStats(type, pkg, reason, count, duration, startTime));
                this.mStatsCount++;
            }
            if (this.mStatsCount >= 200) {
                Log.i("PowerStats", "power stats up to " + this.mStatsCount + ", force report");
                forceReport();
            }
        }
    }

    private void updateStats(Stats sts, String reason, int count, long duration, long startTime) {
        int statsType = getStatsType(sts.mType);
        if (statsType == 1) {
            sts.mCount += count;
        } else if (statsType == 3) {
            DurationStats ds = (DurationStats) sts;
            if (startTime == -1) {
                ds.mDuration += duration;
            } else {
                if (ds.mLastStartTime != startTime) {
                    ds.mTotalDuration += ds.mDuration;
                    ds.mLastStartTime = startTime;
                }
                ds.mDuration = duration;
            }
            ds.mCount += count;
        } else if (statsType == 2) {
            ReasonStats rs = (ReasonStats) sts;
            rs.mCount += count;
        }
    }

    private int getStats(int type, String pkg, String reason, ArrayList<Stats> inAppStats) {
        int size = inAppStats.size();
        for (int index = 0; index < size; index++) {
            Stats sts = (Stats) inAppStats.get(index);
            if (sts.mType == type && sts.mPkg.equals(pkg)) {
                int statsType = getStatsType(type);
                if (statsType == 1 || statsType == 3) {
                    return index;
                }
                if (statsType == 2 && reason.equals(((ReasonStats) sts).mReason)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private Stats newStats(int type, String pkg, String reason, int count, long duration, long startTime) {
        int statsType = getStatsType(type);
        if (statsType == 1) {
            return new Stats(type, pkg, count);
        }
        if (statsType == 3) {
            return new DurationStats(type, pkg, duration, count, startTime);
        }
        if (statsType == 2) {
            return new ReasonStats(type, pkg, reason, count);
        }
        return null;
    }

    private void reportAppPowerCtrl(String msg) {
        report("BDAT_TAG_APPS_POWER_CONTROL", msg);
    }

    private void report(String tag, String msg) {
        if (mHwLog == null) {
            try {
                mHwLog = Class.forName("android.util.HwLog");
            } catch (ClassNotFoundException e) {
                Log.w("PowerStats", "not found class: android.util.HwLog");
            }
        }
        if (mHwLog != null) {
            try {
                if (mReport == null) {
                    mReport = mHwLog.getMethod("bdatd", new Class[]{String.class, String.class});
                }
                if (mReport != null) {
                    mReport.invoke(mHwLog, new Object[]{tag, msg});
                }
            } catch (Exception e2) {
                Log.w("PowerStats", "NoSuchMethod: bdatd(String tag, String msg)");
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (Arrays.asList(args).contains("stats")) {
            pw.println("\nPOWER GENIUS (dumpsys powergenius stats)");
            pw.println("\t stats count:" + this.mStatsCount);
            for (Entry entry : this.mAppStats.entrySet()) {
                for (Stats sts : (ArrayList) entry.getValue()) {
                    pw.println("\t stats:" + sts);
                }
            }
        }
    }
}
