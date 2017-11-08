package com.android.contacts.hap.sim.base;

import android.content.Context;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.model.account.AccountType.DefinitionException;

public class BaseSimAccountType extends SimAccountType {
    protected BaseSimAccountType(Context context, SimConfig aSimConfig, int aSubscription, String aAccountType) throws DefinitionException {
        super(context, aSimConfig, aSubscription, aAccountType);
        addDataKindStructuredName(context);
        addDataKindDisplayName(context);
        addDataKindPhone(context);
    }
}
