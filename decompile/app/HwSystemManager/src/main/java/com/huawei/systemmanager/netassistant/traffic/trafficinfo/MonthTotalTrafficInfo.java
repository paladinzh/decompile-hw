package com.huawei.systemmanager.netassistant.traffic.trafficinfo;

import android.text.TextUtils;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.ExtraTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.util.HwLog;

public class MonthTotalTrafficInfo {
    private static final String TAG = "MonthTotalTrafficInfo";
    long extraTotalPackage;
    long lastMonthLeftTraffic;
    String mImsi;
    long monthTotalTraffic;
    long totalPackage;

    private MonthTotalTrafficInfo(String imsi) {
        this.mImsi = imsi;
    }

    public static MonthTotalTrafficInfo create(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            HwLog.e(TAG, "no active traffic card");
            return null;
        }
        TrafficPackageSettings pkgSetting = new TrafficPackageSettings(imsi);
        pkgSetting.get();
        MonthTotalTrafficInfo info = new MonthTotalTrafficInfo(imsi);
        if (DateUtil.beforeThisMonth(pkgSetting.initTimeMills, info.mImsi)) {
            return null;
        }
        info.totalPackage = NetAssistantDBManager.getInstance().getSettingTotalPackage(imsi);
        if (info.totalPackage < 0) {
            HwLog.e(TAG, "no package set");
            return null;
        }
        info.extraTotalPackage = new ExtraTrafficSetting(imsi).get().getPackage();
        info.lastMonthLeftTraffic = NetAssistantDBManager.getInstance().getMonthLimitByte(imsi) - ITrafficInfo.create(imsi, DateUtil.getLastYearMonth(), 301).getTraffic();
        if (info.lastMonthLeftTraffic < 0) {
            info.lastMonthLeftTraffic = 0;
        }
        info.monthTotalTraffic = (info.totalPackage + info.extraTotalPackage) + info.lastMonthLeftTraffic;
        HwLog.i(TAG, "total package = " + info.totalPackage + " extraTotalPackage = " + info.extraTotalPackage + " lastLeftTraffic = " + info.lastMonthLeftTraffic + " monthTotalTraffic = " + info.monthTotalTraffic);
        return info;
    }

    public void save() {
        HsmStat.statE(Events.E_NETASSISTANT_TRAFFIC_MONTHLY_TOTAL_RESET);
        if (TextUtils.isEmpty(this.mImsi)) {
            HwLog.e(TAG, "no active traffic card");
        } else {
            NetAssistantDBManager.getInstance().setMonthLimitByte(this.mImsi, this.monthTotalTraffic, true);
        }
    }
}
