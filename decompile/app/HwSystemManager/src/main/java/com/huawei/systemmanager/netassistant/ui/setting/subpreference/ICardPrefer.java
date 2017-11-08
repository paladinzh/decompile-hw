package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import com.huawei.systemmanager.netassistant.ui.Item.CardItem;

public interface ICardPrefer {
    void refreshPreferShow();

    void setCard(CardItem cardItem);

    void setValueChangedListener(IValueChangedListener iValueChangedListener);
}
