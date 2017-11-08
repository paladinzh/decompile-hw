package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel;

import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.ICodeName;
import java.util.List;

public interface FstOperatorSetView {
    void finishOperatorSet();

    void setBrand(String str);

    void setCity(String str);

    void setOperator(String str);

    void setProvince(String str);

    void showBrandDialog(List<ICodeName> list, int i);

    void showCityDialog(List<ICodeName> list, int i);

    void showOperatorDialog(List<ICodeName> list, int i);

    void showProvinceDialog(List<ICodeName> list, int i);
}
