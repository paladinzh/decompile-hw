package com.huawei.powergenie.modules.resgovernor;

import com.huawei.powergenie.api.ICoreContext;

public abstract class ScalingFreqAdapter {
    protected abstract int getFreq(int i);

    protected abstract boolean setFreq(int i, int i2);

    protected abstract boolean setFreq(int[] iArr, int[] iArr2);

    public static ScalingFreqAdapter getFreqAdapter(ICoreContext context) {
        if (context.isHisiPlatform()) {
            return ScalingFreqAdapterHisi.getInstance(context.getContext());
        }
        return null;
    }

    public boolean setLCpuMax(int value) {
        return setFreq(2, value);
    }

    public boolean setLCpuMin(int value) {
        return setFreq(1, value);
    }

    public boolean setBCpuMax(int value) {
        return setFreq(5, value);
    }

    public boolean setBCpuMin(int value) {
        return setFreq(4, value);
    }

    public boolean setGpuMax(int value) {
        return setFreq(8, value);
    }

    public boolean setGpuMin(int value) {
        return setFreq(7, value);
    }

    public boolean setDdrMax(int value) {
        return setFreq(11, value);
    }

    public boolean setDdrMin(int value) {
        return setFreq(10, value);
    }

    public boolean setIpaTemp(int value) {
        return setFreq(18, value);
    }

    public boolean setIpaPower(int value) {
        return setFreq(19, value);
    }

    public boolean setIpaSwitch(int value) {
        return setFreq(17, value);
    }

    public boolean setForkOnClaster(int value) {
        return setFreq(20, value);
    }

    public boolean setBoost(int value) {
        return setFreq(38, value);
    }

    public boolean setHmp(int type, int state, int thresholdUp, int thresholdDown) {
        types = new int[4];
        int[] values = new int[]{15, type, 16, state};
        types[2] = 13;
        values[2] = thresholdUp;
        types[3] = 14;
        values[3] = thresholdDown;
        return setFreq(types, values);
    }

    public int getDefLCpuMin() {
        return getFreq(21);
    }

    public int getDefLCpuMax() {
        return getFreq(22);
    }

    public int getDefBCpuMin() {
        return getFreq(23);
    }

    public int getDefBCpuMax() {
        return getFreq(24);
    }

    public int getDefGpuMin() {
        return getFreq(25);
    }

    public int getDefGpuMax() {
        return getFreq(26);
    }

    public int getDefDdrMin() {
        return getFreq(27);
    }

    public int getDefDdrMax() {
        return getFreq(28);
    }
}
