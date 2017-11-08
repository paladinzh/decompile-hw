package com.android.contacts.hap.sim;

public abstract class SimFactory {
    protected String mAccountType;
    protected int mSubscription;

    public abstract SimAccountType getSimAccountType();

    public abstract SimConfig getSimConfig();

    public abstract SimPersistanceManager getSimPersistanceManager();

    protected SimFactory(int aSubscription, String aAccountType) {
        this.mSubscription = aSubscription;
        this.mAccountType = aAccountType;
    }
}
