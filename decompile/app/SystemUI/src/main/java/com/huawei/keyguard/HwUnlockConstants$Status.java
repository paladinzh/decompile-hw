package com.huawei.keyguard;

public enum HwUnlockConstants$Status {
    Normal(true),
    NetworkLocked(true),
    SimMissing(true),
    SimMissingLocked(false),
    SimPukLocked(false),
    SimLocked(true),
    NetworkSubsetLocked(true),
    CorporateLocked(true),
    ServiceProviderLocked(true),
    SimSimLocked(true),
    RuimNetwork1Locked(true),
    RuimNetwork2Locked(true),
    RuimHrpdLocked(true),
    RuimCorporateLocked(true),
    RuimServiceProviderLocked(true),
    RuimRuimLocked(true),
    NetworkPukLocked(true),
    NetworkSubsetPukLocked(true),
    CorporatePukLocked(true),
    ServiceProviderPukLocked(true),
    CardNotReady(true),
    CardDeActived(true);
    
    private final boolean mShowStatusLines;

    private HwUnlockConstants$Status(boolean mShowStatusLines) {
        this.mShowStatusLines = mShowStatusLines;
    }
}
