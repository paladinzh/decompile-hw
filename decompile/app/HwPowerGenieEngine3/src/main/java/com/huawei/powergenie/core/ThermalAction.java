package com.huawei.powergenie.core;

import android.util.Log;

public final class ThermalAction extends PowerAction {
    private static ThermalAction sPool;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    private String mAction;
    boolean mInPool = false;
    private String mPackageName;
    private String mSensorName;
    private int mSensorType;
    private int mTemperature;
    private String mValue;
    ThermalAction next;

    protected ThermalAction() {
        super(0, 0);
    }

    public void resetAs(int actionId, long timestamp, String name, int type, int temperature, String action, String value) {
        super.reset(actionId, timestamp);
        this.mSensorType = type;
        this.mSensorName = name;
        this.mTemperature = temperature;
        this.mAction = action;
        this.mPackageName = action;
        this.mValue = value;
    }

    public static ThermalAction obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                ThermalAction m = sPool;
                sPool = m.next;
                m.next = null;
                m.mInPool = false;
                sPoolSize--;
                return m;
            }
            Log.i("ThermalAction", "new ThermalAction");
            return new ThermalAction();
        }
    }

    public void recycle() {
        if (this.mInPool) {
            Log.e("ThermalAction", "This ThermalAction cannot be recycled because it is still in sPool.");
        }
        synchronized (sPoolSync) {
            if (sPoolSize < 2) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
                this.mInPool = true;
            }
        }
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public int getType() {
        if (super.getType() >= 0) {
            return super.getType();
        }
        return 3;
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
        builder.append(" action id=").append(getActionId());
        builder.append(" sensor type=").append(this.mSensorType);
        builder.append(" action name=").append(this.mSensorName);
        builder.append(" sensor temperature=").append(this.mTemperature);
        builder.append(" action =").append(this.mAction);
        builder.append(" policy =").append(this.mValue);
        return builder.toString();
    }
}
