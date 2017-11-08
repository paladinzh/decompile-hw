package com.huawei.keyguard.data;

public class StepCounterInfo {
    private static StepCounterInfo sInst = new StepCounterInfo();
    private boolean mEnableCounter;
    private boolean mEnableCounterChanged;
    private boolean mStepCounterShowEnable = true;
    private int mStepsCount;

    public static StepCounterInfo getInst() {
        return sInst;
    }

    public int getStepsCount() {
        return this.mStepsCount;
    }

    public void setStepsCount(int stepsCount) {
        this.mStepsCount = stepsCount;
    }

    public boolean getEnableCounterChanged() {
        return this.mEnableCounterChanged;
    }

    public boolean getEnableCounter() {
        return this.mEnableCounter;
    }

    public void setEnableCounterChanged(boolean enableCounterChanged) {
        this.mEnableCounterChanged = enableCounterChanged;
    }

    public void setEnableCounter(boolean enableCounter) {
        this.mEnableCounter = enableCounter;
    }

    public void setStepInfoShowEnable(boolean enableCounterShow) {
        this.mStepCounterShowEnable = enableCounterShow;
    }

    public void clearStepInfo() {
        this.mStepsCount = 0;
        this.mEnableCounterChanged = false;
        this.mEnableCounter = false;
        this.mStepCounterShowEnable = false;
    }
}
