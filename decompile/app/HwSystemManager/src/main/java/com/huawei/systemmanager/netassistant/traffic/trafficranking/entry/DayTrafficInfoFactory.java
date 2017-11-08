package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

public class DayTrafficInfoFactory extends ITrafficInfoFactory {
    public AbsTrafficAppInfo create(int uid) {
        return new DayTrafficInfo(uid);
    }
}
