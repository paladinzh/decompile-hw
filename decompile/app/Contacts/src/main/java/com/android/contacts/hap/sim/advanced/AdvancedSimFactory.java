package com.android.contacts.hap.sim.advanced;

import android.content.Context;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimConfigListener;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.model.account.AccountType.DefinitionException;

public class AdvancedSimFactory extends SimFactory {
    private AdvancedSimConfig mAdvancedSimConfig;
    private AdvancedSimAccountType mSimAccountType;
    private AdvancedSimPersistenceManager mSimPerManager;

    public AdvancedSimFactory(int aSubscription, String aAccountType, Context context, SimConfigListener aSimConfigListener, int[] aSimRecordSize) {
        super(aSubscription, aAccountType);
        this.mAdvancedSimConfig = new AdvancedSimConfig(aSimRecordSize, aSubscription);
        try {
            this.mSimPerManager = new AdvancedSimPersistenceManager(this.mAdvancedSimConfig, SimFactoryManager.getProviderUri(aSubscription), aAccountType);
            this.mSimPerManager.init(context);
            this.mSimAccountType = new AdvancedSimAccountType(context, this.mAdvancedSimConfig, aSubscription, aAccountType);
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
        return this.mAdvancedSimConfig;
    }
}
