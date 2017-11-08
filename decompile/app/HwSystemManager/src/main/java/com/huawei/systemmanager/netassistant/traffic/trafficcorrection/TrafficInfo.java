package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import com.huawei.systemmanager.util.HwLog;

public class TrafficInfo {
    private static final String TAG = TrafficInfo.class.getSimpleName();
    public TrafficItem m4GItem;
    public TrafficItem mLeisureItem;
    public TrafficItem mNormalItem;
    private String querryCode;
    private String querryPort;
    private int simIndex;

    public TrafficInfo(TrafficItem mNormalItem, TrafficItem m4GItem, TrafficItem leisureItem, int simIndex, String querryCode, String querryPort) {
        this.mNormalItem = mNormalItem;
        this.m4GItem = m4GItem;
        this.mLeisureItem = leisureItem;
        this.simIndex = simIndex;
        this.querryCode = querryCode;
        this.querryPort = querryPort;
    }

    public boolean isReadyToAnalysis() {
        HwLog.i(TAG, "isReadyToAnalysis index = " + this.simIndex + " normal total:" + this.mNormalItem.mTotalTraffic + "  used:" + this.mNormalItem.mUsedTraffic + " left:" + this.mNormalItem.mLeftTraffic);
        HwLog.i(TAG, "isReadyToAnalysis index = " + this.simIndex + " 4g total:" + this.m4GItem.mTotalTraffic + "  used:" + this.m4GItem.mUsedTraffic + " left:" + this.m4GItem.mLeftTraffic);
        if (this.mNormalItem.isTrafficValid() || this.m4GItem.isTrafficValid()) {
            return true;
        }
        return false;
    }

    public long getTrafficResult() {
        long traffoc4glUsed = this.m4GItem.getTrafficUsed();
        if (traffoc4glUsed == -1) {
            traffoc4glUsed = 0;
        }
        long trafficNormalUsed = this.mNormalItem.getTrafficUsed();
        if (trafficNormalUsed == -1) {
            trafficNormalUsed = 0;
        }
        return trafficNormalUsed + traffoc4glUsed;
    }

    public int getSimIndex() {
        return this.simIndex;
    }

    public String getQuerryCode() {
        return this.querryCode;
    }

    public String getQuerryPort() {
        return this.querryPort;
    }

    private boolean hasOnlyNormalLeft() {
        return this.mNormalItem.isLeftTraffictSet() ? hasNo4GTraffic() : false;
    }

    private boolean hasNo4GTraffic() {
        return (this.m4GItem.isTotalTraffictSet() || this.m4GItem.isUsedTraffictSet() || this.m4GItem.isLeftTraffictSet()) ? false : true;
    }

    private boolean hasNoTotalTraffic() {
        return this.mNormalItem.isLeftTraffictSet() ? this.m4GItem.isLeftTraffictSet() : false;
    }

    private boolean hasOnly4GLeft() {
        return this.m4GItem.isLeftTraffictSet() ? hasNoNormalTraffic() : false;
    }

    private boolean hasNoNormalTraffic() {
        return (this.mNormalItem.isTotalTraffictSet() || this.mNormalItem.isUsedTraffictSet() || this.mNormalItem.isLeftTraffictSet()) ? false : true;
    }

    public boolean isReadyToLocalAnalysis() {
        return (hasOnlyNormalLeft() || hasNoTotalTraffic()) ? true : hasOnly4GLeft();
    }
}
