package com.huawei.mms.ui;

import com.huawei.mms.ui.MultiModeListView.EditHandler;

public interface EmuiListViewListener {
    EditHandler getHandler(int i);

    int getHintColor(int i, int i2);

    String getHintText(int i, int i2);

    void onEnterEditMode();

    void onExitEditMode();
}
