package com.huawei.thermal.config;

import java.util.HashMap;

public class ActionItem {
    private int mActionSize = 0;
    public final HashMap<String, String> mActions = new HashMap();
    public int mBatLevelClear = -1;
    public int mBatLevelTrigger = -1;

    public void setBatTrigger(int battery) {
        this.mBatLevelTrigger = battery;
    }

    public void setBatClear(int battery) {
        this.mBatLevelClear = battery;
    }

    public boolean addAction(String action, String value) {
        if (this.mActionSize >= 16) {
            return false;
        }
        this.mActions.put(action, value);
        this.mActionSize++;
        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" mBatLevelTrigger =").append(this.mBatLevelTrigger);
        builder.append(" mBatLevelClear =").append(this.mBatLevelClear);
        builder.append(" action =").append(this.mActions.toString());
        return builder.toString();
    }
}
