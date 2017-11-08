package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

public class DayTrafficInfo extends AbsTrafficAppInfo {
    DayTrafficInfo(int uid) {
        super(uid);
        this.wifiTraffic = 0;
        this.mobileTraffic = 0;
    }

    public int getAppPeriod() {
        return 2;
    }
}
