package com.android.systemui.statusbar.policy;

import android.content.Intent;

public interface HwWifiSignalInterface {
    void handleBroadcastHuawei(Intent intent);

    boolean isWifiCharged();

    boolean isWifiNoInternet();
}
