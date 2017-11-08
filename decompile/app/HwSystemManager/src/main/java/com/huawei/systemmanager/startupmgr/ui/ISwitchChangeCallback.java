package com.huawei.systemmanager.startupmgr.ui;

public interface ISwitchChangeCallback {
    void allOpSwitchChangeCancelled(boolean z);

    void allOpSwitchChanged(boolean z);

    void itemSwitchChangeCancelled(int i, boolean z);

    void itemSwitchChanged(int i, boolean z);
}
