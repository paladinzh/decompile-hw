package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter;

import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;

public interface FstPackageSetPresenter {
    void finishPackageSet(float f, int i, int i2, boolean z, SimProfileDes simProfileDes);

    void init();

    void save(float f, int i, int i2, boolean z, SimProfileDes simProfileDes);
}
