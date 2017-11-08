package com.huawei.powergenie.api;

import java.util.ArrayList;

public interface IPolicy {
    int getOldPowerMode();

    int getPowerMode();

    boolean hasGmsApps();

    boolean isChinaMarketProduct();

    boolean isDisabledGsfGms();

    boolean isExtremeModeV2();

    boolean isGmsApp(String str);

    boolean isOffPowerMode();

    boolean isSupportExtrModeV2();

    void processGmsPkgChanged(boolean z);

    void sendExtremeMode(boolean z);

    boolean supportCinemaMode();

    boolean supportGmsGsfPolicy();

    boolean supportScenarioRRC();

    boolean updateConfigList(String str, ArrayList<String> arrayList);

    void updateDisabledGsfGms(boolean z);
}
