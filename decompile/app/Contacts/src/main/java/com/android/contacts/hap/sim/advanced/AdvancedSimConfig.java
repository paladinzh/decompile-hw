package com.android.contacts.hap.sim.advanced;

import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimFactoryManager;

public class AdvancedSimConfig implements SimConfig {
    int[] mRecordSize = null;
    private int mSubscription;

    protected AdvancedSimConfig(int[] aRecordSize, int aSubscription) {
        this.mRecordSize = aRecordSize;
        this.mSubscription = aSubscription;
    }

    public boolean isEmailEnabled() {
        boolean z = true;
        if (this.mRecordSize == null || this.mRecordSize.length <= 3) {
            return false;
        }
        if (this.mRecordSize[3] != 1) {
            z = false;
        }
        return z;
    }

    public boolean isANREnabled() {
        boolean z = false;
        if (this.mRecordSize == null || this.mRecordSize.length <= 6) {
            return false;
        }
        if (this.mRecordSize[6] == 2) {
            z = true;
        }
        return z;
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
