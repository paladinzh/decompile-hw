package com.huawei.thermal.event;

public final class ThermalEvent extends Event {
    private int mSensorTemp;
    private int mSensorType;

    public ThermalEvent(int evtID) {
        super(evtID);
        this.mSensorType = -1;
        this.mSensorTemp = -100000;
    }

    public ThermalEvent(int evtID, int type, int temp) {
        super(evtID);
        this.mSensorType = type;
        this.mSensorTemp = temp;
    }

    public void resetAs(ThermalEvent evt) {
        super.setEventId(evt.getEventId());
        this.mSensorType = evt.getSensorType();
        this.mSensorTemp = evt.getSensorTemp();
    }

    public int getType() {
        return 2;
    }

    public int getSensorType() {
        return this.mSensorType;
    }

    public int getSensorTemp() {
        return this.mSensorTemp;
    }

    public void setSensorType(int type) {
        this.mSensorType = type;
    }

    public void setSensorTemp(int temp) {
        this.mSensorTemp = temp;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" EventID =").append(getEventId());
        builder.append(" Time =").append(getTimeStamp());
        builder.append(" SensorType =").append(this.mSensorType);
        builder.append(" SensorTemp =").append(this.mSensorTemp);
        return builder.toString();
    }
}
