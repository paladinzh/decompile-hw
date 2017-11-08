package com.huawei.thermal.policy;

import android.util.Log;

public final class ThermalAction extends Action {
    private static ThermalAction sPool;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    private String mAction;
    boolean mInPool = false;
    private String mSensorName;
    private int mSensorType;
    private int mTemperature;
    private long mTimestamp;
    private String mValue;
    ThermalAction next;

    public void resetAs(long timestamp, String name, int type, int temperature, String action, String value) {
        this.mTimestamp = timestamp;
        this.mSensorType = type;
        this.mSensorName = name;
        this.mTemperature = temperature;
        this.mAction = action;
        this.mValue = value;
    }

    public static ThermalAction obtain() {
        synchronized (sPoolSync) {
            if (sPool == null) {
                Log.i("ThermalAction", "new ThermalAction");
                return new ThermalAction();
            }
            ThermalAction m = sPool;
            sPool = m.next;
            m.next = null;
            m.mInPool = false;
            sPoolSize--;
            return m;
        }
    }

    public int getSensorType() {
        return this.mSensorType;
    }

    public String getSensorName() {
        return this.mSensorName;
    }

    public int getTemperature() {
        return this.mTemperature;
    }

    public String getAction() {
        return this.mAction;
    }

    public String getValue() {
        return this.mValue;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" sensor type=").append(this.mSensorType);
        builder.append(" action name=").append(this.mSensorName);
        builder.append(" sensor temperature=").append(this.mTemperature);
        builder.append(" action =").append(this.mAction);
        builder.append(" policy =").append(this.mValue);
        return builder.toString();
    }
}
