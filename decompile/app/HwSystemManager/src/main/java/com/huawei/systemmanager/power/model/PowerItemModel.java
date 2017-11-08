package com.huawei.systemmanager.power.model;

public class PowerItemModel {
    private boolean currentState = false;
    private boolean isForWatch;
    private int itemID;
    private int itemOptimizeString;
    private int itemString;
    private int optimizeState;
    private int restoreState;
    private int restoreValue;

    public PowerItemModel(int labelID, int itemString, int itemOptimizeString, boolean currentState, int restoreState, int restoreValue, int optimizeState, boolean forWatch) {
        this.itemID = labelID;
        this.itemString = itemString;
        this.itemOptimizeString = itemOptimizeString;
        this.currentState = currentState;
        this.restoreState = restoreState;
        this.restoreValue = restoreValue;
        this.optimizeState = optimizeState;
        this.isForWatch = forWatch;
    }

    public int getItemID() {
        return this.itemID;
    }

    public int getItemString() {
        return this.itemString;
    }

    public void setItemString(int id) {
        this.itemString = id;
    }

    public int getitemOptimizeString() {
        return this.itemOptimizeString;
    }

    public boolean getCurrentstate() {
        return this.currentState;
    }

    public void setCurrentstate(boolean state) {
        this.currentState = state;
    }

    public int getRestoreState() {
        return this.restoreState;
    }

    public void setRestoreState(int state) {
        this.restoreState = state;
    }

    public int getRestoreValue() {
        return this.restoreValue;
    }

    public void setRestoreValue(int state) {
        this.restoreValue = state;
    }

    public int getOptimizeState() {
        return this.optimizeState;
    }

    public boolean getIsForWatch() {
        return this.isForWatch;
    }
}
