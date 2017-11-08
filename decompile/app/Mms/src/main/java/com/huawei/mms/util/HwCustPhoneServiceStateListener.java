package com.huawei.mms.util;

import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.mms.MmsApp;
import com.huawei.android.telephony.PhoneStateListenerEx;

public class HwCustPhoneServiceStateListener extends PhoneStateListenerEx {
    static final String TAG = "HwCustPhoneServiceStateListener";
    private static boolean mIsListenerSet;
    private static HwCustPhoneServiceStateListener sInstance = new HwCustPhoneServiceStateListener(0);
    private boolean mIsInit;
    private ServiceState mState;

    private HwCustPhoneServiceStateListener(int subscription) {
        super(subscription);
        setSubscription(this, subscription);
    }

    public static void startListeningServiceState() {
        if (!mIsListenerSet) {
            TelephonyManager mSimTelephonyManager = MmsApp.getDefaultTelephonyManager();
            if (mSimTelephonyManager != null) {
                mSimTelephonyManager.listen(sInstance, 1);
                mIsListenerSet = true;
            }
        }
    }

    public static void stopListeningServiceState() {
        if (mIsListenerSet) {
            TelephonyManager mSimTelephonyManager = MmsApp.getDefaultTelephonyManager();
            if (mSimTelephonyManager != null) {
                mSimTelephonyManager.listen(sInstance, 0);
                mIsListenerSet = false;
            }
            sInstance.setInit(false);
        }
    }

    public static boolean isNetworkNotAvailable() {
        if (sInstance.isInit()) {
            return !sInstance.isNoService() ? sInstance.isEmergencyOnly() : true;
        }
        Log.d(TAG, "serviceState listener is not registered");
        return false;
    }

    public void onServiceStateChanged(ServiceState serviceState) {
        this.mState = serviceState;
        this.mIsInit = true;
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

    private boolean isInit() {
        return this.mIsInit;
    }

    private void setInit(boolean flag) {
        this.mIsInit = flag;
    }
}
