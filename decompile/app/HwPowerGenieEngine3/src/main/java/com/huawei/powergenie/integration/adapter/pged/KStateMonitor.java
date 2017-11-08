package com.huawei.powergenie.integration.adapter.pged;

import java.util.ArrayList;

public interface KStateMonitor {
    void onKStateEvent(int i, int i2, String str, ArrayList<Integer> arrayList);
}
