package com.huawei.systemmanager.netassistant.traffic.trafficranking.control;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.common.collect.Lists;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.AbsTrafficAppInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.DayTrafficInfoFactory;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.ITrafficInfoFactory;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.MonthTrafficInfoFactory;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.WeeklyTrafficInfoFactory;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class TrafficRankingManager {
    private static final String TAG = TrafficRankingManager.class.getSimpleName();
    private static TrafficRankingManager sInstance;
    private INetAssistantService mService;

    private TrafficRankingManager() {
        checkService();
    }

    private void checkService() {
        if (this.mService == null) {
            this.mService = Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT));
        }
    }

    public static synchronized TrafficRankingManager getDefault() {
        TrafficRankingManager trafficRankingManager;
        synchronized (TrafficRankingManager.class) {
            if (sInstance == null) {
                sInstance = new TrafficRankingManager();
            }
            trafficRankingManager = sInstance;
        }
        return trafficRankingManager;
    }

    public List<AbsTrafficAppInfo> getDailyTrafficInfoList(ITrafficInfoFactory factory, String imsi, int mPeriod) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, this.mService.getPeriodMobileTrafficAppList(imsi, DateUtil.getDayStartTimeMills(), DateUtil.getCurrentTimeMills()), null);
        } catch (RemoteException e) {
            HwLog.d(TAG, "getDailyTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getDailyTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }

    public List<AbsTrafficAppInfo> getMonthTrafficInfoList(ITrafficInfoFactory factory, String imsi, int mPeriod) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, this.mService.getPeriodMobileTrafficAppList(imsi, DateUtil.getMonthStartTimeMills(imsi), DateUtil.getCurrentTimeMills()), null);
        } catch (RemoteException e) {
            HwLog.d(TAG, "getMonthTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getMonthTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }

    public List<AbsTrafficAppInfo> getWeeklyTrafficInfoList(ITrafficInfoFactory factory, String imsi, int mPeriod) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, this.mService.getPeriodMobileTrafficAppList(imsi, DateUtil.getWeekStartTimeMills(imsi), DateUtil.getCurrentTimeMills()), null);
        } catch (RemoteException e) {
            HwLog.d(TAG, "getMonthTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getMonthTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }

    public void initNoTrafficApp(List<AbsTrafficAppInfo> list, String imsi) {
        if (list != null && list.size() != 0 && !TextUtils.isEmpty(imsi)) {
            NoTrafficAppDbInfo noTrafficAppDbInfo = new NoTrafficAppDbInfo(imsi);
            noTrafficAppDbInfo.initDbData();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                AbsTrafficAppInfo info = (AbsTrafficAppInfo) list.get(i);
                info.setChecked(noTrafficAppDbInfo.isNoTrafficApp(info.getUid()));
            }
        }
    }

    private List<AbsTrafficAppInfo> mergeMobileAndWifiItem(ITrafficInfoFactory factory, List<ParcelableAppItem> mobileAppInfoList, List<ParcelableAppItem> wifiAppInfoList) {
        SparseArray<AbsTrafficAppInfo> sa = new SparseArray();
        if (mobileAppInfoList != null) {
            for (ParcelableAppItem mobileAppItem : mobileAppInfoList) {
                AbsTrafficAppInfo appInfo = factory.create(mobileAppItem.key);
                appInfo.setMobileTraffic(mobileAppItem.mobiletotal);
                HwLog.d(TAG, "mergeMobileAndWifiItem add mobile info to SparseArray. mobile traffic = " + mobileAppItem.mobiletotal);
                sa.put(mobileAppItem.key, appInfo);
            }
        }
        if (wifiAppInfoList != null) {
            for (ParcelableAppItem wifiAppItem : wifiAppInfoList) {
                appInfo = (AbsTrafficAppInfo) sa.get(wifiAppItem.key);
                if (appInfo == null) {
                    HwLog.d(TAG, "the info of " + wifiAppItem.key + " is not in SparseArray");
                    appInfo = factory.create(wifiAppItem.key);
                }
                appInfo.setWifiTraffic(wifiAppItem.wifitotal);
                HwLog.d(TAG, "mergeMobileAndWifiItem add wifi info to SparseArray. wifi traffic = " + wifiAppItem.wifitotal);
                sa.put(wifiAppItem.key, appInfo);
            }
        }
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        for (int i = 0; i < sa.size(); i++) {
            list.add((AbsTrafficAppInfo) sa.valueAt(i));
        }
        return list;
    }

    public List<AbsTrafficAppInfo> getDailyTrafficInfoList(DayTrafficInfoFactory factory, int period) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, null, this.mService.getPeriodWifiTrafficAppList(DateUtil.getDayStartTimeMills(), DateUtil.getCurrentTimeMills()));
        } catch (RemoteException e) {
            HwLog.d(TAG, "getDailyTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getDailyTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }

    public List<AbsTrafficAppInfo> getWeeklyTrafficInfoList(WeeklyTrafficInfoFactory factory, int period) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, null, this.mService.getPeriodWifiTrafficAppList(DateUtil.getWeekStartTimeMills(null), DateUtil.getCurrentTimeMills()));
        } catch (RemoteException e) {
            HwLog.d(TAG, "getWeeklyTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getWeeklyTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }

    public List<AbsTrafficAppInfo> getMonthTrafficInfoList(MonthTrafficInfoFactory factory, int period) {
        checkService();
        List<AbsTrafficAppInfo> list = Lists.newArrayList();
        try {
            list = mergeMobileAndWifiItem(factory, null, this.mService.getPeriodWifiTrafficAppList(DateUtil.getMonthStartTimeMills(null), DateUtil.getCurrentTimeMills()));
        } catch (RemoteException e) {
            HwLog.d(TAG, "getMonthTrafficInfoList failed");
            e.printStackTrace();
        } catch (Exception e1) {
            HwLog.d(TAG, "getMonthTrafficInfoList error ");
            e1.printStackTrace();
        }
        return list;
    }
}
