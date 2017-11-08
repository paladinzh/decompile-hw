package com.android.contacts.hap.sim.base;

import com.android.contacts.hap.sim.SimConfig;

public class BaseSimConfig implements SimConfig {
    public boolean isEmailEnabled() {
        return false;
    }

    public boolean isANREnabled() {
        return false;
    }

    public int getSimCapacity() {
        return 0;
    }

    public int getAvailableFreeSpace() {
        return 0;
    }
}
