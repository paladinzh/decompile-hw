package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import com.huawei.systemmanager.util.HwLog;

public abstract class ITrafficInfo {
    private static final String TAG = ITrafficInfo.class.getSimpleName();
    String mImsi;
    int mMonth;
    TrafficStatisticsInfo tableInfo;
    long trafficBytes;

    public static class TrafficData {
        boolean isOverData;
        long resetTraffic;
        String trafficLeftData;
        String trafficLeftUnit;
        String trafficStateMessage;
        String trafficUsedData;
        String trafficUsedMessage;
        String trafficUsedUnit;

        public boolean isOverData() {
            return this.isOverData;
        }

        public void setOverData(boolean isOverData) {
            this.isOverData = isOverData;
        }

        public String getTrafficUsedData() {
            return this.trafficUsedData;
        }

        public void setTrafficUsedData(String trafficUsedData) {
            this.trafficUsedData = trafficUsedData;
        }

        public String getTrafficUsedUnit() {
            return this.trafficUsedUnit;
        }

        public void setTrafficUsedUnit(String trafficUsedUnit) {
            this.trafficUsedUnit = trafficUsedUnit;
        }

        public String getTrafficLeftData() {
            return this.trafficLeftData;
        }

        public void setTrafficLeftData(String trafficLeftData) {
            this.trafficLeftData = trafficLeftData;
        }

        public String getTrafficLeftUnit() {
            return this.trafficLeftUnit;
        }

        public void setTrafficLeftUnit(String trafficLeftUnit) {
            this.trafficLeftUnit = trafficLeftUnit;
        }

        public String getTrafficUsedMessage() {
            return this.trafficUsedMessage;
        }

        public void setTrafficUsedMessage(String trafficUsedMessage) {
            this.trafficUsedMessage = trafficUsedMessage;
        }

        public String getTrafficStateMessage() {
            return this.trafficStateMessage;
        }

        public void setTrafficStateMessage(String trafficStateMessage) {
            this.trafficStateMessage = trafficStateMessage;
        }

        public long getNormalUsedTraffic() {
            return this.resetTraffic;
        }

        public void setNormalUsedTraffic(long resetTraffic) {
            this.resetTraffic = resetTraffic;
        }
    }

    public abstract long getLeftTraffic();

    public abstract long getTotalLimit();

    public abstract TrafficData getTrafficData();

    public abstract int getType();

    protected abstract void notifyUI();

    public ITrafficInfo(String imsi, int month) {
        this.mImsi = imsi;
        this.mMonth = month;
    }

    public long getTraffic() {
        long traffic = this.tableInfo.getTraffic();
        return traffic > 0 ? traffic : 0;
    }

    public void updateBytes(long updateByte) {
        HwLog.i(TAG, "ITrafficInfo create " + getType() + "updateBytes = " + updateByte);
        this.tableInfo.setTraffic(updateByte);
        this.tableInfo.save(null);
    }

    protected void onCreate() {
        this.tableInfo = new TrafficStatisticsInfo(this.mImsi, this.mMonth, getType());
        this.tableInfo.get();
        this.trafficBytes = this.tableInfo.getTraffic();
    }

    public static ITrafficInfo create(String imsi, int month, int tarfficType) {
        ITrafficInfo trafficInfo;
        switch (tarfficType) {
            case 302:
                trafficInfo = new LeisureTraffic(imsi, month);
                break;
            case 303:
                trafficInfo = new RoamingTraffic(imsi, month);
                break;
            default:
                trafficInfo = new NormalTraffic(imsi, month);
                break;
        }
        trafficInfo.onCreate();
        return trafficInfo;
    }
}
