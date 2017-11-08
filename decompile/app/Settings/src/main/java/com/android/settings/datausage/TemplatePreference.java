package com.android.settings.datausage;

import android.net.INetworkStatsService;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.INetworkManagementService;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.settingslib.NetworkPolicyEditor;

public interface TemplatePreference {

    public static class NetworkServices {
        INetworkManagementService mNetworkService;
        NetworkPolicyEditor mPolicyEditor;
        NetworkPolicyManager mPolicyManager;
        INetworkStatsService mStatsService;
        SubscriptionManager mSubscriptionManager;
        TelephonyManager mTelephonyManager;
        UserManager mUserManager;
    }

    void setTemplate(NetworkTemplate networkTemplate, int i, NetworkServices networkServices);
}
