package com.huawei.thermal.config;

import android.util.Log;

public class SensorTempAction {
    private int mLevelClear = -100000;
    private int mLevelTrigger = -100000;
    public int mNumThresholds = 0;
    public int mPostiveMiniClrTemperature = Integer.MAX_VALUE;
    public final String mSensorName;
    public final int mSensorType;
    public final TriggerAction[] mTriggerAction = new TriggerAction[8];

    public SensorTempAction(int sensorType, String sensorName) {
        this.mSensorType = sensorType;
        this.mSensorName = sensorName;
        this.mNumThresholds = 0;
    }

    public boolean addTriggerAction(TriggerAction triggerAction) {
        boolean ret;
        if (triggerAction.mLevelTrigger > this.mLevelTrigger && triggerAction.mLevelClear > this.mLevelClear && this.mNumThresholds < 8) {
            this.mTriggerAction[this.mNumThresholds] = triggerAction;
            this.mNumThresholds++;
            ret = true;
        } else {
            ret = false;
            Log.e("SensorTempAction", "thermal config error, because of the format of file");
        }
        if (triggerAction.mLevelClear > 0 && this.mPostiveMiniClrTemperature > triggerAction.mLevelClear) {
            this.mPostiveMiniClrTemperature = triggerAction.mLevelClear;
        }
        this.mLevelTrigger = triggerAction.mLevelTrigger;
        this.mLevelClear = triggerAction.mLevelClear;
        return ret;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" Sensor Type =").append(this.mSensorType);
        builder.append(" Sensor Name =").append(this.mSensorName);
        builder.append(" mPostiveMiniClrTemperature = ").append("" + this.mPostiveMiniClrTemperature);
        for (int i = 0; i < this.mNumThresholds; i++) {
            builder.append(" Action List=").append(this.mTriggerAction[i].toString());
        }
        return builder.toString();
    }
}
