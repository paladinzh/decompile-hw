package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel;

import java.util.List;

public interface FstPackageSetView {
    void finishPackageSet();

    void setStartDayEntries(List<String> list);

    void setTrafficUnitEntries(List<String> list);

    void showDefaultView(int i, int i2, boolean z);

    void showOtherPackageSet();
}
