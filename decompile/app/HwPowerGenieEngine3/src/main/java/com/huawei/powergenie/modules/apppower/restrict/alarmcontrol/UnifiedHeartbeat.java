package com.huawei.powergenie.modules.apppower.restrict.alarmcontrol;

import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.policy.IntelligentProvider;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.ArrayList;
import java.util.List;

public final class UnifiedHeartbeat {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private ArrayList<String> mAdjustAlarmPkgs = new ArrayList();
    private ArrayList<AdjustItem> mAdjustItemList = new ArrayList();
    private ArrayList<String> mBlacklistPkgs = new ArrayList();
    private ArrayList<String> mClockAppPkgs = null;
    private final Context mContext;
    private ArrayList<String> mDBHistoryAlarmPkgs = null;
    private boolean mDeepUserState = false;
    private boolean mHasStart = false;
    private final IAppManager mIAppManager;
    private final IAppPowerAction mIAppPowerAction;
    private final IAppType mIAppType;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private ArrayList<String> mImAppsList = null;
    private boolean mIsNeedUpdateAdjust = false;
    private ArrayList<String> mLauncherList = null;
    private boolean mLongScreenOff = false;
    private ArrayList<String> mScreenLockList = null;
    private long mStartAdjustTime = 0;

    class AdjustItem {
        private int mAdjustMode;
        private ArrayList<String> mAdjustPkgList;
        private int mAlarmLevel;
        private boolean mIsNeedAdjust;

        private AdjustItem(String pkg, int level, int mode) {
            this.mAlarmLevel = level;
            this.mAdjustMode = mode;
            this.mAdjustPkgList = new ArrayList();
            this.mAdjustPkgList.add(pkg);
        }

        private void addPkgToList(String pkg) {
            synchronized (this.mAdjustPkgList) {
                if (!this.mAdjustPkgList.contains(pkg)) {
                    this.mAdjustPkgList.add(pkg);
                }
            }
        }

        private void removePkgFromList(String pkg) {
            synchronized (this.mAdjustPkgList) {
                if (this.mAdjustPkgList.contains(pkg)) {
                    this.mAdjustPkgList.remove(pkg);
                }
            }
        }

        private boolean isInItemList(String pkg) {
            synchronized (this.mAdjustPkgList) {
                if (this.mAdjustPkgList.contains(pkg)) {
                    return true;
                }
                return false;
            }
        }

        private boolean matched(int level, int mode) {
            if (level == this.mAlarmLevel && mode == this.mAdjustMode) {
                return true;
            }
            return false;
        }
    }

    protected UnifiedHeartbeat(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
        Log.i("UnifiedHeartbeat", "support unified heartbeat.");
    }

    protected void handleAppsAlarm(String pkg, int alarmType, String interval, String alarmIntent) {
        if (this.mICoreContext.isScreenOff()) {
            checkAlarmAdjust(pkg);
            checkToStartSmartAlarm();
        }
    }

    protected void handlePowerContected() {
        if (this.mICoreContext.isScreenOff() && !DEBUG_USB) {
            Log.i("UnifiedHeartbeat", "Power connected, stop alarm adjust.");
            stopSmartAlarm();
        }
    }

    protected void handlePowerDiscontected() {
        if (this.mICoreContext.isScreenOff() && !DEBUG_USB) {
            Log.i("UnifiedHeartbeat", "Power disconnected, start alarm adjust.");
            checkToStartSmartAlarm();
        }
    }

    protected void handleScreenState(boolean screenOn) {
        if (screenOn) {
            this.mLongScreenOff = false;
            this.mDeepUserState = false;
            stopSmartAlarm();
        }
    }

    private void checkAlarmAdjust(String pkg) {
        if (this.mHasStart && !isNeedFilte(pkg)) {
            int alarmFreq = this.mIAppManager.getCurScrOffAlarmFreq(pkg);
            synchronized (this.mAdjustAlarmPkgs) {
                if (this.mAdjustAlarmPkgs.contains(pkg)) {
                    if (alarmFreq >= 600) {
                        removeAdjustPkg(pkg);
                    }
                } else if (this.mIAppManager.getCurScrOffAlarmApps(10, 180).contains(pkg)) {
                    addPeriodAdjustAlarms(pkg, true);
                    this.mAdjustAlarmPkgs.add(pkg);
                }
            }
        }
    }

    private void checkToStartSmartAlarm() {
        if (this.mHasStart) {
            if (SystemClock.elapsedRealtime() - this.mStartAdjustTime >= 600000) {
                updateSmartAlarm();
                this.mStartAdjustTime = SystemClock.elapsedRealtime();
            }
        } else if (this.mIDeviceState.getScrOffDuration() >= 60000 && !this.mIPolicy.isOffPowerMode() && (DEBUG_USB || !this.mIDeviceState.isCharging())) {
            startSmartAlarm();
        }
    }

    private void startSmartAlarm() {
        if (initAlarmAdjustList()) {
            if (!this.mHasStart && this.mAdjustItemList.size() > 0) {
                this.mHasStart = true;
                Log.i("UnifiedHeartbeat", "start alarm adjust.");
                this.mStartAdjustTime = SystemClock.elapsedRealtime();
                periodAdjustAlarms();
            }
            return;
        }
        Log.i("UnifiedHeartbeat", "No app alarm need to adjsut, just return.");
    }

    protected void stopSmartAlarm() {
        if (this.mHasStart) {
            this.mHasStart = false;
            Log.i("UnifiedHeartbeat", "stop alarm adjust.");
            removeAllPeriodAdjustAlarms();
        }
        synchronized (this.mAdjustItemList) {
            this.mAdjustItemList.clear();
        }
        synchronized (this.mAdjustAlarmPkgs) {
            this.mAdjustAlarmPkgs.clear();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateSmartAlarm() {
        ArrayList<String> topAlarmPkgs = this.mIAppManager.getCurScrOffAlarmApps(10, 180);
        synchronized (this.mAdjustAlarmPkgs) {
            for (String name : topAlarmPkgs) {
                if (!(this.mAdjustAlarmPkgs.contains(name) || isNeedFilte(name))) {
                    this.mAdjustAlarmPkgs.add(name);
                }
            }
            if (this.mAdjustAlarmPkgs.size() == 0) {
                return;
            }
            updateDeviceState();
            for (String pkg : this.mAdjustAlarmPkgs) {
                addPeriodAdjustAlarms(pkg, false);
            }
        }
    }

    private ArrayList<String> getTopAlarmPkgs(int topNum) {
        if (this.mIAppManager.isAlarmFreqEmpty()) {
            return null;
        }
        if (this.mDBHistoryAlarmPkgs == null) {
            this.mDBHistoryAlarmPkgs = getDBHistoryAlarmApps(200, topNum);
        }
        if (this.mBlacklistPkgs.size() == 0) {
            getBlackListAlarmApps(this.mBlacklistPkgs);
        }
        ArrayList<String> topAlarmsList = new ArrayList();
        if (this.mDBHistoryAlarmPkgs != null && this.mDBHistoryAlarmPkgs.size() > 0) {
            topAlarmsList.addAll(this.mDBHistoryAlarmPkgs);
        }
        for (String pkgName : this.mBlacklistPkgs) {
            if (this.mIAppManager.getTotalScrOffAlarmCount(pkgName) > 0 && !topAlarmsList.contains(pkgName)) {
                topAlarmsList.add(pkgName);
            }
        }
        for (String name : this.mIAppManager.getTotalScrOffAlarmApps(topNum, 600, 50)) {
            if (!topAlarmsList.contains(name)) {
                topAlarmsList.add(name);
            }
        }
        for (String name2 : this.mIAppManager.getCurScrOffAlarmApps(topNum, 180)) {
            if (!topAlarmsList.contains(name2)) {
                topAlarmsList.add(name2);
            }
        }
        return topAlarmsList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> getDBHistoryAlarmApps(int minWkNum, int topNum) {
        ArrayList<String> pkgList = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(IntelligentProvider.APP_SCROFF_URI, new String[]{"appName"}, "wakeups > " + minWkNum, null, "wakeups DESC");
        if (cursor == null) {
            Log.w("UnifiedHeartbeat", "selection table is not exist.");
            return null;
        }
        int num = 0;
        try {
            int pkgColId = cursor.getColumnIndex("appName");
            while (cursor.moveToNext() && num < topNum) {
                String pkgName = cursor.getString(pkgColId);
                if (pkgName != null) {
                    pkgList.add(pkgName);
                    num++;
                }
            }
            cursor.close();
        } catch (RuntimeException ex) {
            Log.e("UnifiedHeartbeat", "RuntimeException:", ex);
        } catch (Throwable th) {
            cursor.close();
        }
        return pkgList;
    }

    private void getBlackListAlarmApps(ArrayList<String> blacklist) {
        blacklist.add("com.huawei.appmarket");
        blacklist.add("com.tencent.mm");
        blacklist.add("com.tencent.mobileqq");
        blacklist.add("com.sina.weibo");
        blacklist.add("com.sohu.newsclient");
        blacklist.add("com.netease.newsreader.activity");
        blacklist.add("com.qiyi.video");
        blacklist.add("vStudio.Android.Camera360");
        blacklist.add("com.Qunar");
        blacklist.add("com.youdao.dict");
    }

    private long getAlarmAdjustPeriod(int level) {
        switch (level) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return 300000;
            case NativeAdapter.PLATFORM_HI /*2*/:
                return 300000;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return 600000;
            case 4:
                return 900000;
            case 5:
                return 1800000;
            case 6:
                return 604800000;
            default:
                return 0;
        }
    }

    private boolean isNeedFilte(String pkgName) {
        if (pkgName == null) {
            return true;
        }
        if (this.mBlacklistPkgs.contains(pkgName)) {
            return false;
        }
        if (this.mClockAppPkgs == null) {
            this.mClockAppPkgs = this.mIAppType.getAppsByType(10);
        }
        if ((this.mClockAppPkgs != null && this.mClockAppPkgs.contains(pkgName)) || pkgName.contains("clock") || pkgName.contains("alarm")) {
            return true;
        }
        if (pkgName.contains("whatsapp")) {
            Log.i("UnifiedHeartbeat", "not handle: whatsapp");
            return true;
        } else if (pkgName.contains("google")) {
            Log.i("UnifiedHeartbeat", "not handle: google app: " + pkgName);
            return true;
        } else if (!pkgName.startsWith("com.xdja")) {
            return this.mIAppManager.isSystemApp(this.mContext, pkgName) && (pkgName.startsWith("com.android") || pkgName.contains(".huawei.") || this.mIAppManager.getSignature(this.mContext, pkgName) != 0);
        } else {
            Log.i("UnifiedHeartbeat", "not handle: com.xdja");
            return true;
        }
    }

    private boolean isDeepUserState() {
        return false;
    }

    private void updateAppTypeList() {
        this.mClockAppPkgs = this.mIAppType.getAppsByType(10);
        this.mScreenLockList = this.mIAppType.getAppsByType(9);
        this.mLauncherList = this.mIAppType.getAppsByType(1);
        this.mImAppsList = this.mIAppType.getAppsByType(11);
    }

    private void updateDeviceState() {
        if (this.mIDeviceState.getScrOffDuration() >= 600000) {
            this.mLongScreenOff = true;
        }
        this.mDeepUserState = isDeepUserState();
    }

    private boolean initAlarmAdjustList() {
        ArrayList<String> topAlarmPkgs = getTopAlarmPkgs(5);
        if (topAlarmPkgs == null || topAlarmPkgs.size() == 0) {
            return false;
        }
        synchronized (this.mAdjustAlarmPkgs) {
            for (String pkg : topAlarmPkgs) {
                if (!(this.mAdjustAlarmPkgs.contains(pkg) || isNeedFilte(pkg))) {
                    this.mAdjustAlarmPkgs.add(pkg);
                }
            }
            if (this.mAdjustAlarmPkgs.size() == 0) {
                return false;
            }
            updateDeviceState();
            updateAppTypeList();
            for (String pkg2 : this.mAdjustAlarmPkgs) {
                addPeriodAdjustAlarms(pkg2, false);
            }
            return true;
        }
    }

    private void addPeriodAdjustAlarms(String pkg, boolean addAbnormal) {
        int mode;
        int level;
        if (this.mImAppsList != null && this.mImAppsList.contains(pkg)) {
            if (this.mLongScreenOff && this.mDeepUserState) {
                mode = 1;
            } else {
                mode = 0;
            }
            level = 2;
        } else if ((this.mScreenLockList != null && this.mScreenLockList.contains(pkg)) || ((this.mLauncherList != null && this.mLauncherList.contains(pkg)) || (pkg != null && (pkg.contains("weibo") || pkg.contains("news"))))) {
            if (this.mLongScreenOff && this.mDeepUserState) {
                level = 3;
            } else {
                level = 2;
            }
            mode = 2;
        } else if (this.mBlacklistPkgs.contains(pkg)) {
            level = 2;
            mode = 2;
        } else {
            level = 2;
            mode = 1;
        }
        AdjustItem item = addToAlarmAdjustList(level, mode, pkg, addAbnormal);
        if (addAbnormal) {
            periodAdjustAlarms(item);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private AdjustItem addToAlarmAdjustList(int level, int mode, String pkg, boolean addAbnormalAdjust) {
        AdjustItem adjustItem = null;
        synchronized (this.mAdjustItemList) {
            for (AdjustItem item : this.mAdjustItemList) {
                AdjustItem item2;
                if (item2.isInItemList(pkg)) {
                    if (item2.matched(level, mode)) {
                        return null;
                    }
                    item2.removePkgFromList(pkg);
                }
            }
            boolean isMatched = false;
            for (AdjustItem item22 : this.mAdjustItemList) {
                if (item22.matched(level, mode)) {
                    isMatched = true;
                    if (!item22.isInItemList(pkg)) {
                        item22.addPkgToList(pkg);
                        item22.mIsNeedAdjust = true;
                        if (!addAbnormalAdjust) {
                            this.mIsNeedUpdateAdjust = true;
                        }
                        adjustItem = item22;
                    }
                    if (!isMatched) {
                        item22 = new AdjustItem(pkg, level, mode);
                        item22.mIsNeedAdjust = true;
                        this.mAdjustItemList.add(item22);
                        if (!addAbnormalAdjust) {
                            this.mIsNeedUpdateAdjust = true;
                        }
                        adjustItem = item22;
                    }
                }
            }
            if (isMatched) {
                item22 = new AdjustItem(pkg, level, mode);
                item22.mIsNeedAdjust = true;
                this.mAdjustItemList.add(item22);
                if (addAbnormalAdjust) {
                    this.mIsNeedUpdateAdjust = true;
                }
                adjustItem = item22;
            }
        }
    }

    private void periodAdjustAlarms() {
        synchronized (this.mAdjustItemList) {
            if (this.mAdjustItemList.size() > 0) {
                for (AdjustItem item : this.mAdjustItemList) {
                    periodAdjustAlarms(item);
                }
            }
        }
        this.mIsNeedUpdateAdjust = false;
    }

    private void periodAdjustAlarms(AdjustItem item) {
        if (item != null && item.mIsNeedAdjust) {
            long adjustPeriod = getAlarmAdjustPeriod(item.mAlarmLevel);
            this.mIAppPowerAction.periodAdjustAlarms(item.mAdjustPkgList, 0, adjustPeriod, item.mAdjustMode);
            item.mIsNeedAdjust = false;
            Log.i("UnifiedHeartbeat", "AdjustAlarms period = " + adjustPeriod + " adjust mode = " + item.mAdjustMode + " pkg = " + item.mAdjustPkgList);
        }
    }

    private void removeAdjustPkg(String pkg) {
        synchronized (this.mAdjustItemList) {
            for (AdjustItem item : this.mAdjustItemList) {
                if (item.isInItemList(pkg)) {
                    item.removePkgFromList(pkg);
                    break;
                }
            }
        }
        List<String> removeAdjust = new ArrayList();
        removeAdjust.add(pkg);
        removePeriodAdjustAlarms(removeAdjust, 0);
        this.mAdjustAlarmPkgs.remove(pkg);
    }

    private void removePeriodAdjustAlarms(List<String> pkgList, int type) {
        this.mIAppPowerAction.removePeriodAdjustAlarms(pkgList, type);
    }

    private void removeAllPeriodAdjustAlarms() {
        Log.i("UnifiedHeartbeat", "remove all period adjust alarms !");
        this.mIAppPowerAction.removeAllPeriodAdjustAlarms();
    }
}
