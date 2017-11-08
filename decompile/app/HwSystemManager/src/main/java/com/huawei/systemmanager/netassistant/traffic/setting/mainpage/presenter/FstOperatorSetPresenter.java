package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter;

import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.ICodeName;

public interface FstOperatorSetPresenter {
    void changeBrand();

    void changeCity();

    void changeOperator();

    void changeProvince();

    void finishOperatorSet();

    void onBrandSet(ICodeName iCodeName);

    void onCitySet(ICodeName iCodeName);

    void onOperatorSet(ICodeName iCodeName);

    void onProvinceSet(ICodeName iCodeName);

    void showDefaultView();
}
