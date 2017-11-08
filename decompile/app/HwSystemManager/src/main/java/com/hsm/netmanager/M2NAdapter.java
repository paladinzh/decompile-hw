package com.hsm.netmanager;

import android.telephony.SubscriptionManager;

public class M2NAdapter {
    public static int getDefaultDataSubscriptionId() {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    public static int getDefaultSmsSubscriptionId() {
        return SubscriptionManager.getDefaultSmsSubscriptionId();
    }
}
