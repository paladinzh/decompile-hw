package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

public class MonthTrafficInfoFactory extends ITrafficInfoFactory {
    public AbsTrafficAppInfo create(int uid) {
        return new MonthTrafficInfo(uid);
    }
}
