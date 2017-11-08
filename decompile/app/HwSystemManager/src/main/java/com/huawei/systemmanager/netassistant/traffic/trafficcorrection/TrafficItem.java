package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

class TrafficItem {
    public static final long TRAFFIC_UNSET = -1;
    protected long mLeftTraffic = -1;
    protected long mTotalTraffic = -1;
    protected long mUsedTraffic = -1;

    TrafficItem() {
    }

    public long getTrafficUsed() {
        if (this.mUsedTraffic != -1) {
            return this.mUsedTraffic;
        }
        if (this.mLeftTraffic == -1 || this.mTotalTraffic == -1) {
            return -1;
        }
        return this.mTotalTraffic - this.mLeftTraffic;
    }

    public void setTotalTraffic(long totalTraffic) {
        this.mTotalTraffic = totalTraffic;
    }

    public void setUsedTraffic(long usedTraffic) {
        this.mUsedTraffic = usedTraffic;
    }

    public void setLeftTraffic(long leftTraffic) {
        this.mLeftTraffic = leftTraffic;
    }

    public boolean isTrafficValid() {
        if (this.mUsedTraffic != -1) {
            return true;
        }
        if (this.mTotalTraffic == -1 || this.mLeftTraffic == -1) {
            return false;
        }
        return true;
    }

    public boolean isTotalTraffictSet() {
        return this.mTotalTraffic != -1;
    }

    public boolean isUsedTraffictSet() {
        return this.mUsedTraffic != -1;
    }

    public boolean isLeftTraffictSet() {
        return this.mLeftTraffic != -1;
    }
}
