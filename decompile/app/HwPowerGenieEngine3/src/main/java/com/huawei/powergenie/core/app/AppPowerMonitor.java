package com.huawei.powergenie.core.app;

import android.content.Context;
import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.Utils;
import com.huawei.powergenie.core.policy.DBWrapper;
import com.huawei.powergenie.core.policy.DBWrapper.AppScrOffItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public final class AppPowerMonitor {
    private HashMap<Integer, AppOrigData> mAppOrigDataList = new HashMap();
    private int mCollectCount = 0;
    private Context mContext;
    protected DBWrapper mDBWrapper;
    private long mDbUpdateTime = SystemClock.elapsedRealtime();
    private IAppManager mIAppManager;
    private final ICoreContext mICoreContext;
    protected final IDeviceState mIDeviceState;
    private HashMap<String, AppScrOffItem> mNewAppScroffItems = new HashMap();
    private long mScreenOffTime = System.currentTimeMillis();
    private INetworkStatsService mStatsService;
    private INetworkStatsSession mStatsSession;
    private NetworkTemplate mTemplateMobileAll;
    private NetworkTemplate mTemplateWifiWildcard;

    static final class AppOrigData {
        long mGpsTime = 0;
        long mWifiScan = 0;
        long mWkTime = 0;

        AppOrigData() {
        }
    }

    public AppPowerMonitor(ICoreContext context) {
        this.mICoreContext = context;
        this.mIAppManager = (IAppManager) context.getService("appmamager");
        this.mDBWrapper = new DBWrapper(context.getContext());
        this.mIDeviceState = (IDeviceState) context.getService("device");
        this.mContext = this.mICoreContext.getContext();
        this.mStatsService = Stub.asInterface(ServiceManager.getService("netstats"));
    }

    protected void handleBootComplete() {
        this.mTemplateMobileAll = NetworkTemplate.buildTemplateMobileAll(SystemProperties.get("test.subscriberid", TelephonyManager.from(this.mContext).getSubscriberId()));
        this.mTemplateWifiWildcard = NetworkTemplate.buildTemplateWifiWildcard();
    }

    protected void handleScreenOn() {
        if (System.currentTimeMillis() - this.mScreenOffTime >= 900000) {
            collectAppScrOffInfo();
            if (SystemClock.elapsedRealtime() - this.mDbUpdateTime >= 86400000) {
                writeAppScrOffItemDB(false);
                this.mDbUpdateTime = SystemClock.elapsedRealtime();
            }
        }
    }

    protected void handleScreenOff() {
        this.mScreenOffTime = System.currentTimeMillis();
    }

    protected void handleShutdown() {
        writeAppScrOffItemDB(true);
    }

    protected void handlePackageState(boolean added, String pkgName) {
        if (!added) {
            this.mNewAppScroffItems.remove(pkgName);
        }
    }

    private void collectAppScrOffInfo() {
        this.mCollectCount++;
        ArrayList<String> runingAppList = this.mIAppManager.getRuningApp(this.mContext);
        if (runingAppList != null) {
            for (String pkgName : runingAppList) {
                if (isInAppControlList(pkgName)) {
                    if (this.mStatsSession == null && !this.mIDeviceState.isShutdown()) {
                        try {
                            if (this.mStatsService != null) {
                                this.mStatsSession = this.mStatsService.openSession();
                            }
                        } catch (RemoteException e) {
                            Log.e("AppPowerMonitor", "RemoteException:", e);
                        } catch (Exception e2) {
                            Log.e("AppPowerMonitor", "Open session Exception!");
                        }
                    }
                    if (this.mNewAppScroffItems.containsKey(pkgName)) {
                        updateScroffItem(pkgName, (AppScrOffItem) this.mNewAppScroffItems.get(pkgName));
                    } else {
                        AppScrOffItem scrOffItem = new AppScrOffItem();
                        updateScroffItem(pkgName, scrOffItem);
                        this.mNewAppScroffItems.put(pkgName, scrOffItem);
                    }
                }
            }
            if (this.mStatsSession != null) {
                TrafficStats.closeQuietly(this.mStatsSession);
                this.mStatsSession = null;
            }
        }
    }

    private void updateScroffItem(String pkgName, AppScrOffItem scrOffItem) {
        int appUid = this.mIAppManager.getUidByPkgFromOwner(pkgName);
        scrOffItem.appName = pkgName;
        Integer count = Integer.valueOf(this.mIAppManager.getCurScrOffAlarmCount(pkgName));
        scrOffItem.wakeups = (long) (count != null ? count.intValue() : 0);
        if (!this.mIDeviceState.isShutdown()) {
            long now = System.currentTimeMillis();
            scrOffItem.mobileRx = getDataConsumByUid(appUid, 1, 0, this.mScreenOffTime, now, now) + scrOffItem.mobileRx;
            scrOffItem.mobileTx = getDataConsumByUid(appUid, 1, 1, this.mScreenOffTime, now, now) + scrOffItem.mobileTx;
            scrOffItem.wifiRx = getDataConsumByUid(appUid, 0, 0, this.mScreenOffTime, now, now) + scrOffItem.wifiRx;
            scrOffItem.wifiTx = getDataConsumByUid(appUid, 0, 1, this.mScreenOffTime, now, now) + scrOffItem.wifiTx;
        }
        if (this.mAppOrigDataList.containsKey(Integer.valueOf(appUid))) {
            AppOrigData appOrigData = (AppOrigData) this.mAppOrigDataList.get(Integer.valueOf(appUid));
            scrOffItem.wkTime = (this.mIDeviceState.getWkTimeByUidPid(appUid, -1) - appOrigData.mWkTime) + scrOffItem.wkTime;
            long wifiScanTime = this.mIDeviceState.getWifiScanTime(appUid) - appOrigData.mWifiScan;
            scrOffItem.wifiScan += wifiScanTime;
            long useGpsTime = this.mIDeviceState.getGpsTime(appUid) - appOrigData.mGpsTime;
            scrOffItem.gpsTime += useGpsTime;
            if (wifiScanTime >= 60000) {
                String logMsg = pkgName + "\t" + wifiScanTime;
                Log.i("AppPowerMonitor", "WIFI_SCAN_SCROFF: " + pkgName + " time: " + wifiScanTime);
            }
            if (useGpsTime >= 60000) {
                Log.i("AppPowerMonitor", "USE_GPS_SCROFF: " + pkgName + " time: " + useGpsTime);
            }
        }
    }

    private void writeAppScrOffItemDB(boolean forceWriteDb) {
        Log.d("AppPowerMonitor", "writeAppScrOffItemDB size: " + this.mNewAppScroffItems.size());
        HashMap<String, AppScrOffItem> appScrOffItem;
        AppScrOffItem item;
        String pkgName;
        if (this.mNewAppScroffItems.size() <= 0) {
            if (!forceWriteDb) {
            }
            appScrOffItem = this.mDBWrapper.getAppScrOffItems();
            for (Entry entry : this.mNewAppScroffItems.entrySet()) {
                item = (AppScrOffItem) entry.getValue();
                pkgName = (String) entry.getKey();
                if (checkUpdateItem(item)) {
                    item.bgCpuTime = 0;
                    item.updateTime = Utils.formatDate(System.currentTimeMillis());
                    if (appScrOffItem == null) {
                    }
                    this.mDBWrapper.addAppScrOffItem(item);
                }
            }
            this.mNewAppScroffItems.clear();
            this.mCollectCount = 0;
        } else if (forceWriteDb || this.mCollectCount >= 10) {
            appScrOffItem = this.mDBWrapper.getAppScrOffItems();
            for (Entry entry2 : this.mNewAppScroffItems.entrySet()) {
                item = (AppScrOffItem) entry2.getValue();
                pkgName = (String) entry2.getKey();
                if (checkUpdateItem(item)) {
                    item.bgCpuTime = 0;
                    item.updateTime = Utils.formatDate(System.currentTimeMillis());
                    if (appScrOffItem == null && appScrOffItem.containsKey(item.appName)) {
                        AppScrOffItem updateItem = (AppScrOffItem) appScrOffItem.get(item.appName);
                        item.wakeups += updateItem.wakeups;
                        item.bgCpuTime += updateItem.bgCpuTime;
                        item.wkTime += updateItem.wkTime;
                        item.wifiScan += updateItem.wifiScan;
                        item.gpsTime += updateItem.gpsTime;
                        item.mobileRx += updateItem.mobileRx;
                        item.mobileTx += updateItem.mobileTx;
                        item.wifiRx += updateItem.wifiRx;
                        item.wifiTx += updateItem.wifiTx;
                        item.wkTime += updateItem.wkTime;
                        item.wifiScan += updateItem.wifiScan;
                        item.gpsTime += updateItem.gpsTime;
                        this.mDBWrapper.updateAppScrOffItem(item);
                    } else {
                        this.mDBWrapper.addAppScrOffItem(item);
                    }
                }
            }
            this.mNewAppScroffItems.clear();
            this.mCollectCount = 0;
        }
    }

    private boolean checkUpdateItem(AppScrOffItem item) {
        if (item == null) {
            return false;
        }
        if (item.wakeups == 0 && item.bgCpuTime == 0 && item.bgUseTime == 0 && item.wkTime == 0 && item.wifiScan == 0 && item.gpsTime == 0 && item.mobileRx == 0 && item.mobileTx == 0 && item.wifiRx == 0 && item.wifiTx == 0) {
            return false;
        }
        return true;
    }

    private boolean isInAppControlList(String pkg) {
        if (this.mIAppManager.isCleanProtectApp(pkg) || this.mIAppManager.isCleanUnprotectApp(pkg) || this.mIAppManager.isStandbyProtectApp(pkg) || this.mIAppManager.isStandbyUnprotectApp(pkg)) {
            return true;
        }
        return false;
    }

    private long getDataConsumByUid(int uid, int dataType, int dataFlag, long start, long end, long now) {
        long data = 0;
        NetworkTemplate template = null;
        if (1 == dataType) {
            try {
                template = this.mTemplateMobileAll;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AppPowerMonitor", "Exception:", e);
            }
        } else if (dataType == 0) {
            template = this.mTemplateWifiWildcard;
        }
        if (template == null) {
            return 0;
        }
        NetworkStatsHistory detailDefault;
        NetworkStatsHistory.Entry entryDefault;
        long entryDf;
        NetworkStatsHistory detailForeground;
        NetworkStatsHistory.Entry entryForeground;
        long entryFg;
        if (dataFlag == 0) {
            detailDefault = collectHistoryForUid(template, uid, 0, null);
            entryDefault = null;
            if (detailDefault != null) {
                entryDefault = detailDefault.getValues(start, end, now, null);
            }
            entryDf = 0;
            if (entryDefault != null) {
                entryDf = entryDefault.rxBytes;
            }
            detailForeground = collectHistoryForUid(template, uid, 1, null);
            entryForeground = null;
            if (detailForeground != null) {
                entryForeground = detailForeground.getValues(start, end, now, null);
            }
            entryFg = 0;
            if (entryForeground != null) {
                entryFg = entryForeground.rxBytes;
            }
            data = entryDf + entryFg;
        } else if (1 == dataFlag) {
            detailDefault = collectHistoryForUid(template, uid, 0, null);
            entryDefault = null;
            if (detailDefault != null) {
                entryDefault = detailDefault.getValues(start, end, now, null);
            }
            entryDf = 0;
            if (entryDefault != null) {
                entryDf = entryDefault.txBytes;
            }
            detailForeground = collectHistoryForUid(template, uid, 1, null);
            entryForeground = null;
            if (detailForeground != null) {
                entryForeground = detailForeground.getValues(start, end, now, null);
            }
            entryFg = 0;
            if (entryForeground != null) {
                entryFg = entryForeground.txBytes;
            }
            data = entryDf + entryFg;
        }
        return data;
    }

    private NetworkStatsHistory collectHistoryForUid(NetworkTemplate template, int uid, int set, NetworkStatsHistory existing) throws RemoteException {
        INetworkStatsSession statsSession = this.mStatsSession;
        if (statsSession == null) {
            return null;
        }
        NetworkStatsHistory history = null;
        try {
            history = statsSession.getHistoryForUid(template, uid, set, 0, 10);
        } catch (Exception e) {
            Log.e("AppPowerMonitor", "Exception: collectHistoryForUid");
        }
        return history;
    }
}
