package com.huawei.powergenie.api;

import android.content.Context;

public interface IThermal {
    int getCurThermalStep();

    String getThermalInterface(String str);

    int getThermalTemp(int i);

    void notifyUsePfmcThermalPolicy(boolean z);

    void notifyVRMode(boolean z);

    void sendThermalComUIEvent(Context context, String str, String str2);

    void sendThermalUIEvent(Context context, String str);

    void sendVRWarningLevel(String str, int i);

    boolean setCameraFps(int i);

    boolean setChargeHotLimit(int i, int i2);

    void setChargingLimit(int i, String str);

    boolean setFlashLimit(boolean z, boolean z2);

    boolean setIspLimit(int i);

    boolean setPAFallback(boolean z);

    void setWlanLimit(int i, String str);
}
