package com.huawei.thermal.policy;

public final class BatteryAction extends Action {
    private String mAction;
    private int mBatteryLevel;
    private String mValue;

    public BatteryAction(int batterylevel, String action, String policy) {
        this.mBatteryLevel = batterylevel;
        this.mAction = action;
        this.mValue = policy;
    }

    public int getBatteryLevel() {
        return this.mBatteryLevel;
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
        builder.append(" sensor temperature=").append(this.mBatteryLevel);
        builder.append(" action =").append(this.mAction);
        builder.append(" policy =").append(this.mValue);
        return builder.toString();
    }
}
