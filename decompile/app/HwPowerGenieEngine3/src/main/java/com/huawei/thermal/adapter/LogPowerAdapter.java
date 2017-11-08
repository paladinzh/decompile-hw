package com.huawei.thermal.adapter;

import android.util.Log;
import com.huawei.pgmng.log.LogPower;

public final class LogPowerAdapter {
    public static void push(int temperature, String action, String policy) {
        Log.i("LogPowerAdapter", "do thermal temperature:" + temperature + "action:" + action + " value:" + policy + " to PG");
        LogPower.push(145, action, String.valueOf(temperature), policy);
    }

    public static void push(int temperature, String action, String policy, int sensorType) {
        Log.i("LogPowerAdapter", "do thermal temperature:" + temperature + "action:" + action + " value:" + policy + " sensorType:" + sensorType + " to PG");
        LogPower.push(145, action, String.valueOf(temperature), policy, String.valueOf(sensorType).split(""));
    }
}
