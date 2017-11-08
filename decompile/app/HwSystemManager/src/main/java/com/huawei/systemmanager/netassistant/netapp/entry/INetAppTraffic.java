package com.huawei.systemmanager.netassistant.netapp.entry;

import com.huawei.systemmanager.comparator.SizeComparator;

public interface INetAppTraffic {
    public static final SizeComparator<INetAppTraffic> TRAFFIC_APP_MOBILE_COMPARATOR = new SizeComparator<INetAppTraffic>() {
        public long getKey(INetAppTraffic t) {
            return t.getMobileTraffic();
        }
    };

    long getMobileTraffic();

    long getWifiTraffic();
}
