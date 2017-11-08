package com.android.contacts.hap.sim.extended;

import android.content.Context;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.model.account.AccountType.DefinitionException;

public class ExtendedSimAccountType extends SimAccountType {
    protected ExtendedSimAccountType(Context context, SimConfig aSimConfig, int aSubscription, String aAccountType) throws DefinitionException {
        super(context, aSimConfig, aSubscription, aAccountType);
        addDataKindStructuredName(context);
        addDataKindDisplayName(context);
        addDataKindPhone(context);
        addRingtone(context);
        this.mWritable = true;
    }
}
