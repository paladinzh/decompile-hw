package com.android.contacts.hap.provider;

import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustContactsAppProviderImpl extends HwCustContactsAppProvider {
    public boolean addYellowPagesContactInList() {
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }
}
