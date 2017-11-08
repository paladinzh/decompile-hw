package com.android.contacts.util;

import android.content.Context;
import android.os.SystemProperties;

public class HwCustPhoneCapabilityTesterImpl extends HwCustPhoneCapabilityTester {
    public boolean includeSipFeature(Context context) {
        return SystemProperties.getBoolean("ro.config.support_internetCall", super.includeSipFeature(context));
    }
}
