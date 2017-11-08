package com.android.contacts;

import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustGroupListLoaderImpl extends HwCustGroupListLoader {
    public String addCustomProjectionCondition() {
        if (HwCustContactFeatureUtils.isSupportIceEmergencyContacts()) {
            return " AND group_visible != " + String.valueOf(2);
        }
        return "";
    }
}
