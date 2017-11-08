package com.android.contacts.hap.sim.extended;

import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimFactoryManager;

public class ExtendedSimConfig implements SimConfig {
    private int[] mRecordSize;
    private int mSubscription;

    protected ExtendedSimConfig(int[] aRecordSize, int aSubscription) {
        this.mRecordSize = aRecordSize;
        this.mSubscription = aSubscription;
    }

    public boolean isEmailEnabled() {
        return false;
    }

    public boolean isANREnabled() {
        return false;
    }

    public int getSimCapacity() {
        int simCapacity = SimFactoryManager.getSimMaxCapacity(this.mSubscription);
        if (simCapacity != -1 || this.mRecordSize == null || this.mRecordSize.length <= 2) {
            return simCapacity;
        }
        int size = this.mRecordSize[2];
        SimFactoryManager.storeMaxValueForSim(size, this.mSubscription);
        return size;
    }

    public int getAvailableFreeSpace() {
        return getSimCapacity() - SimFactoryManager.getTotalSIMContactsPresent(this.mSubscription);
    }
}
