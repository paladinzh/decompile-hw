package com.android.contacts.hap.sim.extended;

import android.content.Context;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimConfigListener;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.model.account.AccountType.DefinitionException;

public class ExtendedSimFactory extends SimFactory {
    private ExtendedSimAccountType mSimAccountType;
    private ExtendedSimConfig mSimConfig;
    private ExtendedSimPersistenceManager mSimPerManager;

    public ExtendedSimFactory(int aSubscription, String aAccountType, Context context, SimConfigListener aSimConfigListener, int[] aSimRecordSize) {
        super(aSubscription, aAccountType);
        this.mSimConfig = new ExtendedSimConfig(aSimRecordSize, aSubscription);
        try {
            this.mSimPerManager = new ExtendedSimPersistenceManager(this.mSimConfig, SimFactoryManager.getProviderUri(aSubscription), aAccountType);
            this.mSimPerManager.init(context);
            this.mSimAccountType = new ExtendedSimAccountType(context, this.mSimConfig, aSubscription, aAccountType);
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
