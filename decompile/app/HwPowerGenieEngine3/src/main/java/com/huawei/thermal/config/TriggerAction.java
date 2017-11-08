package com.huawei.thermal.config;

import java.util.HashMap;

public class TriggerAction {
    public final ActionItem[] mActionItems;
    protected int mActionSize;
    public boolean[] mActionState;
    public HashMap<String, String> mActions;
    public int mLevelClear;
    public final HashMap<Integer, Integer> mLevelClearBattery;
    public int mLevelTrigger;
    protected int mNumActionItems;

    public TriggerAction() {
        this.mActionSize = 0;
        this.mActionItems = new ActionItem[8];
        this.mActionState = new boolean[8];
        this.mNumActionItems = 0;
        this.mLevelTrigger = -100000;
        this.mLevelClear = -100000;
        this.mLevelClearBattery = new HashMap();
        this.mActions = null;
        this.mActionSize = 0;
        this.mNumActionItems = 0;
    }

    public void setTrigger(int temp) {
        this.mLevelTrigger = temp;
    }

    public void setClear(int temp) {
        this.mLevelClear = temp;
    }

    public void addBatteryClear(int sensor, int temp) {
        this.mLevelClearBattery.put(Integer.valueOf(sensor), Integer.valueOf(temp));
    }

    public boolean addAction(String action, String value) {
        if (this.mActions == null) {
            this.mActions = new HashMap();
        }
        if (this.mActionSize >= 16) {
            return false;
        }
        this.mActions.put(action, value);
        this.mActionSize++;
        return true;
    }

    public void addActionItem(ActionItem actionItem) {
        this.mActionItems[this.mNumActionItems] = actionItem;
        this.mActionState[this.mNumActionItems] = false;
        this.mNumActionItems++;
    }

    public int getActionItemsNum() {
        return this.mNumActionItems;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" thresholds =").append(this.mLevelTrigger);
        builder.append(" thresholds_clr =").append(this.mLevelClear);
        builder.append(" thresholds_clr_battery =").append(this.mLevelClearBattery.toString());
        for (int i = 0; i < this.mNumActionItems; i++) {
            builder.append(" Action List=").append(this.mActionItems[i].toString());
            builder.append(" Action state =").append(this.mActionState[i]);
        }
        if (this.mActions != null) {
            builder.append(" no config battery, action =").append(this.mActions.toString());
        }
        return builder.toString();
    }
}
