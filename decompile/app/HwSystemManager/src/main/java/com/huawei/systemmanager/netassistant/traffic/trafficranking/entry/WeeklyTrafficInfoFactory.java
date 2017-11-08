package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

public class WeeklyTrafficInfoFactory extends ITrafficInfoFactory {
    public AbsTrafficAppInfo create(int uid) {
        return new WeeklyTrafficInfo(uid);
    }
}
