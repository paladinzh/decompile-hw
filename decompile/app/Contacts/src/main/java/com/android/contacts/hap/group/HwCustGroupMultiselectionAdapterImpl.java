package com.android.contacts.hap.group;

import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustGroupMultiselectionAdapterImpl extends HwCustGroupMultiselectionAdapter {
    public void addCustomSelectionCondition(StringBuilder selection) {
        if (HwCustContactFeatureUtils.isSupportIceEmergencyContacts()) {
            selection.append(" AND group_visible != ").append(String.valueOf(2));
        }
    }
}
