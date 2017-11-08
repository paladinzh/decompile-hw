package com.huawei.systemmanager.hsmstat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Normal;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.hsmstat.perioddata.BootStartupPeriodData;
import com.huawei.systemmanager.hsmstat.perioddata.CloudRecommendPeriodData;
import com.huawei.systemmanager.hsmstat.perioddata.IPeriodData;
import com.huawei.systemmanager.hsmstat.perioddata.TrafficPackagePeriodData;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

class HsmStatBinderHandler extends Handler {
    private static final Executor EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;
    static final int MSG_ACTIVITY_ONCREATE = 20;
    static final int MSG_ACTIVITY_ONDESTROY = 25;
    static final int MSG_ACTIVITY_ONPAUSE = 24;
    static final int MSG_ACTIVITY_ONRESUME = 22;
    static final int MSG_GOTO_BACKGROUND = 32;
    static final int MSG_GOTO_FROGROUND = 30;
    static final int MSG_SCREEN_OFF = 37;
    static final int MSG_STAT_E = 1;
    static final int MSG_STAT_EVENT = 3;
    static final int MSG_STAT_R = 7;
    static final int MSG_UI_PROCESS_DIE = 34;
    private static final long SECDUAL_TIME_ROUND = 1800000;
    private static final int STAT_R_COUNT = 20;
    private static final long STAT_R_DELAY = 30000;
    private static final String TAG = "HsmStat_info_DcBinderHandler";
    boolean foreground;
    private ScheduleStat mBootupScheduleStat = new ScheduleStat(new BootStartupPeriodData());
    private ScheduleStat mCloudRecommendStat = new ScheduleStat(new CloudRecommendPeriodData());
    final Context mContext;
    String mCurrentActivity = "";
    private int mEventCount;
    final SharedPreferences mPreferences;
    final IHsmStat mSt;
    private AtomicBoolean mStarted = new AtomicBoolean();
    private ScheduleStat mTrafficPackage = new ScheduleStat(new TrafficPackagePeriodData());

    private class ScheduleStat implements Runnable {
        IPeriodData mPData = null;

        public ScheduleStat(IPeriodData data) {
            this.mPData = data;
        }

        protected boolean checkShouldStat() {
            if (this.mPData == null) {
                HwLog.e(HsmStatBinderHandler.TAG, "checkShouldStat false because of invalid mPData!");
                return false;
            }
            String preferenceKey = this.mPData.getSharePreferenceKey();
            long interval = this.mPData.getIntervalTime();
            boolean shouldStat = false;
            long lastStatTime = HsmStatBinderHandler.this.mPreferences.getLong(preferenceKey, -1);
            long currentTime = System.currentTimeMillis();
            if (lastStatTime < 0) {
                shouldStat = true;
            }
            long distance = currentTime - lastStatTime;
            if (distance >= 0 && distance <= interval) {
                if (Math.abs(distance - interval) < 1800000) {
                }
                if (shouldStat) {
                    return false;
                }
                HsmStatBinderHandler.this.mPreferences.edit().putLong(preferenceKey, currentTime).commit();
                return true;
            }
            shouldStat = true;
            if (shouldStat) {
                return false;
            }
            HsmStatBinderHandler.this.mPreferences.edit().putLong(preferenceKey, currentTime).commit();
            return true;
        }

        public void run() {
            if (checkShouldStat()) {
                List<StatEntry> entryList = this.mPData.getRecordData(HsmStatBinderHandler.this.mContext);
                if (entryList != null) {
                    for (StatEntry entry : entryList) {
                        HsmStatBinderHandler.this.obtainMessage(1, entry).sendToTarget();
                    }
                    return;
                }
                HwLog.w(HsmStatBinderHandler.TAG, "getRecordData return null!");
            }
        }
    }

    HsmStatBinderHandler(Context ctx, IHsmStat st) {
        this.mContext = ctx;
        this.mSt = st;
        this.mPreferences = ctx.getSharedPreferences(HsmStatConst.SHARE_PREFERENCE_NAME, 0);
    }

    void start() {
        this.mStarted.set(true);
    }

    void stop() {
        this.mStarted.set(false);
        removeCallbacksAndMessages(null);
    }

    public boolean statImmediately(StatEntry entry) {
        HwLog.i(HsmStatConst.TAG_EVENT, "stat immediately");
        if (TextUtils.isEmpty(entry.key)) {
            HwLog.e(HsmStatConst.TAG, "entry key is empty!");
            return false;
        }
        if (TextUtils.isEmpty(entry.value)) {
            entry.value = " ";
        }
        HwLog.i(HsmStatConst.TAG_EVENT, SqlMarker.QUOTATION + entry.key + "\",\"" + entry.value + SqlMarker.QUOTATION);
        this.mSt.eStat(entry.key, entry.value);
        return this.mSt.rStat();
    }

    boolean statR() {
        if (!this.mStarted.get()) {
            HwLog.i(TAG, "HsmStatBinderHandler is not started, stat r ignore");
            return false;
        } else if (this.foreground) {
            HwLog.i(HsmStatConst.TAG_EVENT, "stat r");
            this.mEventCount = 0;
            return this.mSt.rStat();
        } else {
            HwLog.i(HsmStatConst.TAG_EVENT, "current is not foreground, did not stat r!");
            return false;
        }
    }

    public void handleMessage(Message msg) {
        if (!this.mStarted.get()) {
            HwLog.i(TAG, "DcBinderHandler is not started, do not handler message");
        } else if (!handlerActivityAction(msg) && !handlerProcessAction(msg) && handlerNormalAction(msg)) {
        }
    }

    private boolean handlerActivityAction(Message msg) {
        switch (msg.what) {
            case 20:
            case 22:
                StatEntry entry = msg.obj;
                enterActivity(entry.key, entry.value);
                break;
            case 24:
                leaveActivity(((StatEntry) msg.obj).key);
                break;
            case 25:
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handlerProcessAction(Message msg) {
        switch (msg.what) {
            case 30:
                break;
            case 32:
                leaveSystemManager();
                break;
            case 34:
                leaveSystemManager();
                break;
            case 37:
                leaveSystemManager();
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handlerNormalAction(Message msg) {
        switch (msg.what) {
            case 1:
                StatEntry entry = msg.obj;
                if (!TextUtils.isEmpty(entry.key)) {
                    if (TextUtils.isEmpty(entry.value)) {
                        entry.value = " ";
                    }
                    HwLog.i(HsmStatConst.TAG_EVENT, SqlMarker.QUOTATION + entry.key + "\",\"" + entry.value + SqlMarker.QUOTATION);
                    this.mSt.eStat(entry.key, entry.value);
                    this.mEventCount++;
                    removeMessages(7);
                    if (this.mEventCount < 20) {
                        sendEmptyMessageDelayed(7, STAT_R_DELAY);
                        break;
                    }
                    statR();
                    break;
                }
                HwLog.e(HsmStatConst.TAG, "entry key is empty!");
                break;
            case 3:
                break;
            case 7:
                statR();
                break;
            default:
                return false;
        }
        return true;
    }

    private void leaveSystemManager() {
        if (this.foreground) {
            this.mCurrentActivity = "";
            this.foreground = false;
            obtainMessage(1, new StatEntry(Normal.ACTION_LEAVE_SYSTEMMANAGER, "")).sendToTarget();
        }
    }

    private void enterActivity(String acName, String from) {
        if (!this.mCurrentActivity.equals(acName)) {
            this.mCurrentActivity = acName;
            boolean fromOutSide = !this.foreground;
            this.foreground = true;
            obtainMessage(1, new StatEntry(Normal.ACTION_ENTER_ACTIVITY, HsmStatConst.constructEnterAcValue(HsmStatConst.cutActivityName(acName), from, fromOutSide))).sendToTarget();
        }
    }

    private void leaveActivity(String acName) {
        if (this.mCurrentActivity.equals(acName)) {
            this.mCurrentActivity = "";
        }
        obtainMessage(1, new StatEntry("la", HsmStatConst.cutActivityName(acName))).sendToTarget();
    }

    void statRDially() {
        EXECUTOR.execute(this.mBootupScheduleStat);
        EXECUTOR.execute(this.mTrafficPackage);
        EXECUTOR.execute(this.mCloudRecommendStat);
    }
}
