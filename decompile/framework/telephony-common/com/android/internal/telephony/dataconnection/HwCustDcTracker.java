package com.android.internal.telephony.dataconnection;

import android.os.Message;

public class HwCustDcTracker {
    public HwCustDcTracker(DcTracker dcTracker) {
    }

    public void handleCustMessage(Message msg) {
    }

    public boolean isPSAllowedByFdn() {
        return true;
    }

    public void registerForFdn() {
    }

    public void unregisterForFdn() {
    }

    public ApnSetting getPrefMmsApnForVoWifi(ApnContext apnContext, int radioTech, String operator, ApnSetting apnSetting) {
        return apnSetting;
    }

    public boolean usePrefApnForIwlanNetwork(String operator, String apnType, int radioTech) {
        return true;
    }
}
