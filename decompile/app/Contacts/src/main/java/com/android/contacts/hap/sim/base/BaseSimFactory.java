package com.android.contacts.hap.sim.base;

import android.content.Context;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimConfigListener;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.model.account.AccountType.DefinitionException;

public class BaseSimFactory extends SimFactory {
    private BaseSimAccountType mSimAccountType;
    private BaseSimConfig mSimConfig = new BaseSimConfig();
    private BaseSimPersistenceManager mSimPerManager;

    public BaseSimFactory(int aSubscription, String aAccountType, Context aContext, SimConfigListener aSimConfigListener) {
        super(aSubscription, aAccountType);
        try {
            this.mSimPerManager = new BaseSimPersistenceManager(SimFactoryManager.getProviderUri(aSubscription));
            this.mSimPerManager.init(aContext);
            this.mSimAccountType = new BaseSimAccountType(aContext, this.mSimConfig, aSubscription, aAccountType);
            this.mSimAccountType.setConfigChangeListener(aSimConfigListener);
        } catch (DefinitionException e) {
            e.printStackTrace();
        }
    }

    public SimPersistanceManager getSimPersistanceManager() {
        return this.mSimPerManager;
    }

    public SimAccountType getSimAccountType() {
        return this.mSimAccountType;
    }

    public SimConfig getSimConfig() {
        return this.mSimConfig;
    }
}
