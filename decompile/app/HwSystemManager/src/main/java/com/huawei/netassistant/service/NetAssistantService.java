package com.huawei.netassistant.service;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.netassistant.analyse.TrafficAnalyseManager;
import com.huawei.netassistant.analyse.TrafficAutoAdjust;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.calculator.CalculateTrafficMethod;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableDailyTrafficItem;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.netassistant.common.SimCardSettingsInfo;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.db.NetAssistantStore;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.NotifyPreference;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.TrafficPackageSettings;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.TrafficPackageSettings.Tables;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficState;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class NetAssistantService extends Stub implements HsmService {
    public static final String NET_ASSISTANT = "com.huawei.netassistant.service.netassistantservice";
    public static final String TAG = "NetAssistantService";
    private static final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        }
    };
    private TrafficAnalyseManager mAnalyseManager;
    private CalculateTrafficManager mCalculateTrafficManager;
    private Context mContext;
    private NetAssistantDBManager mDBManager;
    private Bundle mSimProfileDesMap = new Bundle();

    public NetAssistantService(Context context) {
        HwLog.i(TAG, "/NetAssistantService start up");
        this.mContext = context;
        registerForBroadcasts();
        this.mCalculateTrafficManager = CalculateTrafficManager.getInstance();
        this.mDBManager = NetAssistantDBManager.getInstance();
        initPackageData();
        NetAppManager.transeDataFromBefore();
        this.mAnalyseManager = TrafficAnalyseManager.getInstance();
        this.mAnalyseManager.analyseScreenLock();
    }

    private void initPackageData() {
        ContentResolver cr = this.mContext.getContentResolver();
        if (CursorHelper.checkCursorValidAndClose(cr.query(new Tables().getUri(), null, null, null, null))) {
            HwLog.i(TAG, "package set time has init");
            return;
        }
        Cursor cursor = cr.query(SettingTable.getContentUri(), NetAssistantStore.getSettingColumns(), null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            while (cursor.moveToNext()) {
                String imsi = cursor.getString(1);
                if (!TextUtils.isEmpty(imsi)) {
                    new TrafficPackageSettings(imsi).save(null);
                    HwLog.i(TAG, "init package set time");
                }
            }
            CursorHelper.closeCursor(cursor);
            HwLog.i(TAG, "init package set time complete");
            return;
        }
        HwLog.e(TAG, "cursor valid");
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        if (this.mContext != null) {
            this.mContext.registerReceiver(mReceiver, intentFilter);
        }
    }

    private void unregisterForBroadcasts() {
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(mReceiver);
        }
    }

    private void enforceCallingPermission() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public long getMonthMobileTotalBytes(String imsi) throws RemoteException {
        enforceCallingPermission();
        int yearMonth = DateUtil.getYearMonth(imsi);
        int trafficState = TrafficState.getCurrentTrafficState(imsi);
        HwLog.i(TAG, "Traffic state = " + trafficState);
        return ITrafficInfo.create(imsi, yearMonth, trafficState).getTrafficData().getNormalUsedTraffic();
    }

    public long getMonthWifiTotalBytes() throws RemoteException {
        enforceCallingPermission();
        return this.mCalculateTrafficManager.getMonthSpendTraffic(NetworkTemplate.buildTemplateWifiWildcard(), null);
    }

    public List getAbnormalWifiAppList() throws RemoteException {
        enforceCallingPermission();
        return new ArrayList();
    }

    public List getAbnormalMobileAppList(String imsi) throws RemoteException {
        enforceCallingPermission();
        return new ArrayList();
    }

    public long getMonthWifiBackBytes() throws RemoteException {
        enforceCallingPermission();
        NetworkTemplate template = NetworkTemplate.buildTemplateWifiWildcard();
        long currentTime = DateUtil.getCurrentTimeMills();
        return CalculateTrafficMethod.getBackGroundBytesByAll(CalculateTrafficMethod.getAllUidBytesInPeriod(template, DateUtil.getMonthStartTimeMills(null), currentTime, currentTime));
    }

    public long getMonthWifiForeBytes() throws RemoteException {
        enforceCallingPermission();
        NetworkTemplate template = NetworkTemplate.buildTemplateWifiWildcard();
        long currentTime = DateUtil.getCurrentTimeMills();
        return CalculateTrafficMethod.getForeGroundBytesByAll(CalculateTrafficMethod.getAllUidBytesInPeriod(template, DateUtil.getMonthStartTimeMills(null), currentTime, currentTime));
    }

    public long getPeriodMobileTotalBytes(String imsi, long start, long end) throws RemoteException {
        enforceCallingPermission();
        return CalculateTrafficMethod.getTotalBytesInPeriod(NetworkTemplate.buildTemplateMobileAll(imsi), start, end);
    }

    public long getPeriodWifiTotalBytes(long start, long end) throws RemoteException {
        enforceCallingPermission();
        return CalculateTrafficMethod.getTotalBytesInPeriod(NetworkTemplate.buildTemplateWifiWildcard(), start, end);
    }

    public long getTodayMobileTotalBytes(String imsi) throws RemoteException {
        enforceCallingPermission();
        CalculateTrafficManager.getInstance().getStatsSession().forceUpdate();
        return this.mCalculateTrafficManager.getTodaySpendTraffic(NetworkTemplate.buildTemplateMobileAll(imsi));
    }

    public long getTodayWifiTotalBytes() throws RemoteException {
        enforceCallingPermission();
        return this.mCalculateTrafficManager.getTodaySpendTraffic(NetworkTemplate.buildTemplateWifiWildcard());
    }

    public boolean setAdjustItemInfo(String imsi, int adjustType, long value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setAdjustItemInfo(imsi, adjustType, value - this.mCalculateTrafficManager.getMonthSpendTraffic(NetworkTemplate.buildTemplateMobileAll(imsi), imsi));
    }

    public boolean setProvinceInfo(String imsi, String provinceCode, String cityCode) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setProvinceInfo(imsi, provinceCode, cityCode);
    }

    public boolean setOperatorInfo(String imsi, String providerCode, String brandCode) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setOperatorInfo(imsi, providerCode, brandCode);
    }

    public boolean setSettingTotalPackage(String imsi, long value) throws RemoteException {
        enforceCallingPermission();
        String mainImsi = TrafficAnalyseManager.getInstance().getMainCardImsi();
        String secImsi = TrafficAnalyseManager.getInstance().getSecondaryCardImsi();
        float mainCardTotal = (float) this.mDBManager.getSettingTotalPackage(mainImsi);
        float secCardTotal = (float) this.mDBManager.getSettingTotalPackage(secImsi);
        if (TextUtils.equals(mainImsi, imsi)) {
            if (secCardTotal >= 0.0f) {
                this.mDBManager.setSettingSpeedNotify(mainImsi, this.mDBManager.getSettingSpeedNotify(secImsi));
            }
        } else if (TextUtils.equals(secImsi, imsi) && mainCardTotal >= 0.0f) {
            this.mDBManager.setSettingSpeedNotify(secImsi, this.mDBManager.getSettingSpeedNotify(mainImsi));
        }
        boolean result = this.mDBManager.setSettingTotalPackage(imsi, value);
        TrafficAnalyseManager.getInstance().initAutoAdjustAlarm(imsi);
        return result;
    }

    public boolean setSettingBeginDate(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingBeginDate(imsi, value);
    }

    public boolean setSettingRegularAdjustType(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        if (value == 0) {
            TrafficAutoAdjust.getInstance().cancelAutoAdjustAlarm(this.mContext, imsi, TrafficAnalyseManager.getInstance().isMainCard(imsi));
        } else {
            long currTime = DateUtil.getCurrentTimeMills();
            this.mDBManager.setSettingRegularAdjustBeginTime(imsi, currTime);
            TrafficAutoAdjust.getInstance().startAutoAdjustAlarm(this.mContext, value, imsi, currTime, currTime, TrafficAnalyseManager.getInstance().isMainCard(imsi));
        }
        return this.mDBManager.setSettingRegularAdjustType(imsi, value);
    }

    public boolean setSettingExcessMontyType(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingExcessMontyType(imsi, value);
    }

    public boolean setSettingOverMarkMonth(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingOverMarkMonth(imsi, value);
    }

    public boolean setSettingOverMarkDay(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingOverMarkDay(imsi, value);
    }

    public boolean setSettingUnlockScreen(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingUnlockScreen(imsi, value);
    }

    public boolean setSettingNotify(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setSettingNotify(imsi, value);
    }

    public boolean setSettingSpeedNotify(String imsi, int value) throws RemoteException {
        enforceCallingPermission();
        String mainImsi = TrafficAnalyseManager.getInstance().getMainCardImsi();
        String secImsi = TrafficAnalyseManager.getInstance().getSecondaryCardImsi();
        boolean res = false;
        if (mainImsi != null && this.mDBManager.getSettingTotalPackage(mainImsi) >= 0) {
            res = this.mDBManager.setSettingSpeedNotify(mainImsi, value);
        }
        if (secImsi == null || this.mDBManager.getSettingTotalPackage(secImsi) < 0) {
            return res;
        }
        return this.mDBManager.setSettingSpeedNotify(secImsi, value);
    }

    public long getSettingTotalPackage(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingColumnsLongInfo(imsi, 2);
    }

    public int getSettingBeginDate(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingBeginDate(imsi);
    }

    public int getSettingRegularAdjustType(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingRegularAdjustType(imsi);
    }

    public int getSettingExcessMontyType(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingExcessMontyType(imsi);
    }

    public int getSettingOverMarkMonth(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingOverMarkMonth(imsi);
    }

    public int getSettingOverMarkDay(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingOverMarkDay(imsi);
    }

    public int getSettingUnlockScreen(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingUnlockScreen(imsi);
    }

    public int getSettingNotify(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingNotify(imsi);
    }

    public int getSettingSpeedNotify(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSettingSpeedNotify(imsi);
    }

    public long getAdjustPackageValue(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustPackageValue(imsi);
    }

    public SimCardSettingsInfo getSimCardSettingsInfo(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getSimCardSettingsInfo(imsi);
    }

    public long getAdjustDate(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustDate(imsi);
    }

    public String getAdjustProvince(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustProvince(imsi);
    }

    public String getAdjustCity(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustCity(imsi);
    }

    public String getAdjustProvider(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustProvider(imsi);
    }

    public String getAdjustBrand(String imsi) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.getAdjustBrand(imsi);
    }

    public boolean setNetAccessInfo(int uid, int setNetAccessInfos) throws RemoteException {
        enforceCallingPermission();
        return this.mDBManager.setNetAccessInfos(uid, setNetAccessInfos);
    }

    public void sendAdjustSMS(String imsi) throws RemoteException {
        enforceCallingPermission();
        getMonthTrafficDailyDetailList(imsi);
        TrafficAnalyseManager.getInstance().startAutoAdjust(imsi);
    }

    public List getMonth4GMobileAppList(String imsi) throws RemoteException {
        enforceCallingPermission();
        NetworkTemplate template = NetworkTemplate.buildTemplateMobile4g(imsi);
        long monthStartMills = DateUtil.getMonthStartTimeMills(imsi);
        long currentTimeMills = DateUtil.getCurrentTimeMills();
        return CalculateTrafficMethod.getAllUidBytesInPeriod(template, monthStartMills, currentTimeMills, currentTimeMills);
    }

    public List getPeriodMobileTrafficAppList(String imsi, long startTime, long endTime) {
        enforceCallingPermission();
        return CalculateTrafficMethod.getAllUidBytesInPeriod(NetworkTemplate.buildTemplateMobileAll(imsi), startTime, endTime, endTime);
    }

    public List getPeriodWifiTrafficAppList(long startTime, long endTime) {
        enforceCallingPermission();
        return CalculateTrafficMethod.getAllUidBytesInPeriod(NetworkTemplate.buildTemplateWifiWildcard(), startTime, endTime, endTime);
    }

    public List getMonthTrafficDailyDetailList(String imsi) throws RemoteException {
        enforceCallingPermission();
        CalculateTrafficManager.getInstance().getStatsSession().forceUpdate();
        NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(imsi);
        ArrayList<ParcelableDailyTrafficItem> items = new ArrayList();
        if (TextUtils.isEmpty(imsi)) {
            return items;
        }
        int accountDay = DateUtil.getCycleDayFromDB(imsi);
        long monthStartMills = DateUtil.getMonthStartTimeMills(imsi);
        long monthEndMills = DateUtil.monthEnd(accountDay);
        long dayStartMills = monthStartMills;
        for (long dayEndMills = monthStartMills + 86400000; dayEndMills <= monthEndMills; dayEndMills += 86400000) {
            items.add(new ParcelableDailyTrafficItem(Utility.getLocaleNumber(DateUtil.convertDayStartMillsToDate(dayStartMills)), this.mCalculateTrafficManager.getDaySpendTraffic(template, dayStartMills, dayEndMills)));
            dayStartMills = dayEndMills;
        }
        return items;
    }

    public String getSimCardOperatorName(int slot) throws RemoteException {
        SimCardInfo scInfo;
        enforceCallingPermission();
        PhoneSimCardInfo pscInfo = SimCardManager.getInstance().getPhoneSimCardInfo();
        switch (slot) {
            case 0:
                scInfo = pscInfo.getCardInfoSlot0();
                break;
            case 1:
                scInfo = pscInfo.getCardInfoSlot1();
                break;
            default:
                scInfo = pscInfo.getDataUsedSimCard();
                break;
        }
        if (scInfo != null) {
            return scInfo.getOperatorName();
        }
        return null;
    }

    public void init() {
    }

    public void startSpeedUpdate() throws RemoteException {
        enforceCallingPermission();
    }

    public void stopSpeedUpdate() throws RemoteException {
        enforceCallingPermission();
    }

    public void onDestroy() {
        unregisterForBroadcasts();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    public void clearDailyWarnPreference(String imsi) throws RemoteException {
        enforceCallingPermission();
        NotifyPreference.getInstance().clearDailyWarnPreference(imsi);
    }

    public void clearMonthLimitPreference(String imsi) throws RemoteException {
        enforceCallingPermission();
        NotifyPreference.getInstance().clearMonthLimitPreference(imsi);
    }

    public void clearMonthWarnPreference(String imsi) throws RemoteException {
        enforceCallingPermission();
        NotifyPreference.getInstance().clearMonthWarnPreference(imsi);
    }

    public List getMonthPerDayTraffic(NetworkTemplate template, int uid) throws RemoteException {
        enforceCallingPermission();
        CalculateTrafficManager.getInstance().getStatsSession().forceUpdate();
        ArrayList<ParcelableDailyTrafficItem> items = new ArrayList();
        if (template == null) {
            HwLog.w(TAG, "getMonthPerDayTraffic template is null");
            return items;
        }
        long current = DateUtil.getCurrentTimeMills();
        long start = DateUtil.getDayStartTimeMills();
        long end = current;
        for (int count = 1; count <= 30; count++) {
            long bytes = CalculateTrafficMethod.getTotalBytesByUid(template, uid, start, end, current);
            String date = Utility.getLocaleNumber(DateUtil.convertDayStartMillsToDate(start));
            items.add(0, new ParcelableDailyTrafficItem(date, bytes));
            end = start;
            start -= 86400000;
            HwLog.d(TAG, "count = " + count + "bytes = " + bytes + "date = " + date);
        }
        return items;
    }

    public List getDayPerHourTraffic(NetworkTemplate template, int uid) throws RemoteException {
        enforceCallingPermission();
        CalculateTrafficManager.getInstance().getStatsSession().forceUpdate();
        ArrayList<ParcelableDailyTrafficItem> items = new ArrayList();
        if (template == null) {
            HwLog.w(TAG, "getDayPerHourTraffic template is null");
            return items;
        }
        long current = DateUtil.getCurrentTimeMills();
        long start = current;
        long end = current;
        for (int count = 1; count <= 24; count++) {
            long bytes = CalculateTrafficMethod.getTotalBytesByUid(template, uid, start, end, current);
            String date = Utility.getLocaleNumber(DateUtil.convertHourMillsToDate(start));
            items.add(0, new ParcelableDailyTrafficItem(date, bytes));
            end = start;
            start -= 3600000;
            HwLog.d(TAG, "count = " + count + "bytes = " + bytes + "date = " + date);
        }
        return items;
    }

    public long getForeGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) throws RemoteException {
        enforceCallingPermission();
        return CalculateTrafficMethod.getForeGroundBytesByUid(template, uid, start, end, now);
    }

    public long getBackGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) throws RemoteException {
        enforceCallingPermission();
        return CalculateTrafficMethod.getBackGroundBytesByUid(template, uid, start, end, now);
    }

    public void putSimProfileDes(SimProfileDes info) {
        enforceCallingPermission();
        HwLog.i(TAG, "put sim profile des");
        if (info == null) {
            HwLog.w(TAG, "putSimProfileDes info is null");
        } else {
            this.mSimProfileDesMap.putParcelable(info.imsi, info);
        }
    }

    public Bundle getSimProfileDes() {
        enforceCallingPermission();
        HwLog.i(TAG, "getSimProfileDes count : " + this.mSimProfileDesMap.size());
        return this.mSimProfileDesMap;
    }

    public long getMonthlyTotalBytes(String imsi) {
        enforceCallingPermission();
        long monthlyLimit = NatSettingManager.getLimitNotifyByte(imsi);
        HwLog.i(TAG, "getMonthlyTotalBytes = " + monthlyLimit);
        return monthlyLimit;
    }

    public int getNetworkUsageDays(String imsi) {
        enforceCallingPermission();
        return (int) DateUtil.getNetworkUsageDays(imsi);
    }
}
