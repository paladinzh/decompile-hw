package com.huawei.systemmanager.netassistant.traffic.trafficranking.entry;

public abstract class ITrafficInfoFactory {
    public abstract AbsTrafficAppInfo create(int i);

    protected AbsTrafficAppInfo createInfo(AbsTrafficAppInfo info) {
        return null;
    }
}
