package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import java.util.List;

public interface RoamingTrafficView {
    void dismissLoadingDialog();

    void showLoadingDialog();

    void showTrafficList(List<RoamingAppInfo> list);

    void syncBackgroundHeadCheckBox();

    void syncRoamingHeadCheckBox();
}
