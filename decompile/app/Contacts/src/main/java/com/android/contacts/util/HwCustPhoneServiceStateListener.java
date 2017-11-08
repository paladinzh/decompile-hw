package com.android.contacts.util;

import android.telephony.ServiceState;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.huawei.android.telephony.PhoneStateListenerEx;

public class HwCustPhoneServiceStateListener extends PhoneStateListenerEx {
    private static boolean mIsListenerSet;
    private static HwCustPhoneServiceStateListener sInstance = new HwCustPhoneServiceStateListener(0);
    private boolean mIsInitState;
    private ServiceState mState;

    private HwCustPhoneServiceStateListener(int subscription) {
        super(subscription);
        setSubscription(this, subscription);
    }

    public static void startListeningServiceState() {
        if (!mIsListenerSet) {
            SimFactoryManager.listenPhoneState(sInstance, 1);
            mIsListenerSet = true;
        }
    }

    public static void stopListeningServiceState() {
        if (mIsListenerSet) {
            SimFactoryManager.listenPhoneState(sInstance, 0);
            sInstance.mIsInitState = false;
            mIsListenerSet = false;
        }
    }

    public static boolean isNetworkNotAvailable() {
        if (!sInstance.mIsInitState) {
            return false;
        }
        return !sInstance.isNoService() ? sInstance.isEmergencyOnly() : true;
    }

    public void onServiceStateChanged(ServiceState serviceState) {
        this.mState = serviceState;
        this.mIsInitState = true;
    }

    private int getState() {
        if (this.mState != null) {
            return this.mState.getState();
        }
        return 3;
    }

    private boolean isEmergencyOnly() {
        if (this.mState != null) {
            return this.mState.isEmergencyOnly();
        }
        return false;
    }

    private boolean isNoService() {
        return getState() != 0;
    }
}
