package com.android.contacts.util;

import android.content.Context;
import com.google.android.gms.R;

public class HwCustPhoneCapabilityTester {
    public boolean includeSipFeature(Context context) {
        return context.getResources().getBoolean(R.bool.config_sip_enabled);
    }
}
