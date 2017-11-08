package com.android.contacts.hap.sim;

public interface SimConfig {
    int getAvailableFreeSpace();

    int getSimCapacity();

    boolean isANREnabled();

    boolean isEmailEnabled();
}
