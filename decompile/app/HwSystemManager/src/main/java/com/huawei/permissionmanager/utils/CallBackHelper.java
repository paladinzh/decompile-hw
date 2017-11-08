package com.huawei.permissionmanager.utils;

import android.app.AlertDialog;

public interface CallBackHelper {
    void addPendingCfg(int i, int i2, int i3, String str);

    void callBackAddRecord(int i, boolean z, AlertDialog alertDialog);

    void callBackRelease(int i, boolean z);

    void removePendingCfg(int i, int i2, int i3, String str);

    void stopService();
}
