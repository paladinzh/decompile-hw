package com.huawei.systemmanager.netassistant.task;

import android.annotation.TargetApi;
import android.net.HwNetworkPolicyManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.huawei.systemmanager.Task;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;

@TargetApi(22)
public class ServiceStateMonitor extends Task {
    public static final String TAG = "ServiceStateMonitor";
    private HandlerThread mHandlerThread;
    boolean mIsListening;
    TelephonyManager mPhone;
    SparseArray<MobilePhoneStateListener> mPhoneListeners = new SparseArray();
    private final OnSubscriptionsChangedListener mSubscriptionListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            if (ServiceStateMonitor.this.mIsListening) {
                HwLog.i(ServiceStateMonitor.TAG, "OnSubscriptionsChangedListener onSubscriptionsChanged()");
                ServiceStateMonitor.this.updatePhoneListeners();
            }
        }
    };
    SubscriptionManager mSubscriptionManager;

    private class MobilePhoneStateListener extends PhoneStateListener {
        boolean mIsRoaming;
        int mSubId;

        public MobilePhoneStateListener(int subId, boolean isRoaming, Looper looper) {
            super(subId, looper);
            this.mSubId = subId;
            this.mIsRoaming = isRoaming;
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            boolean isRoaming = serviceState.getDataRoaming();
            SubscriptionInfo info = ServiceStateMonitor.this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
            HwLog.i(ServiceStateMonitor.TAG, "service state changed, isRoaming = " + isRoaming);
            if (info != null && this.mSubId == info.getSubscriptionId()) {
                HwNetworkPolicyManager.from(GlobalContext.getContext()).forceUpdatePolicy(isRoaming);
            }
            this.mIsRoaming = isRoaming;
        }
    }

    public String getName() {
        return TAG;
    }

    public void init() {
        this.mSubscriptionManager = SubscriptionManager.from(GlobalContext.getContext());
        this.mPhone = (TelephonyManager) GlobalContext.getContext().getSystemService("phone");
        this.mHandlerThread = new HandlerThread(TAG, 10);
        this.mHandlerThread.start();
    }

    public void registerListener() {
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mIsListening = true;
        updatePhoneListeners();
    }

    public void unRegisterListener() {
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        unRegisterPhoneListeners();
        this.mIsListening = false;
    }

    public void destory() {
        this.mHandlerThread.quit();
    }

    private void updatePhoneListeners() {
        HwLog.i(TAG, "updatePhoneListeners");
        List<SubscriptionInfo> subscriptionInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfos == null) {
            HwLog.i(TAG, "sub infos is null");
            subscriptionInfos = Collections.emptyList();
        }
        for (SubscriptionInfo info : subscriptionInfos) {
            int subId = info.getSubscriptionId();
            if (((MobilePhoneStateListener) this.mPhoneListeners.get(info.getSubscriptionId())) == null) {
                boolean isRoaming = this.mPhone.isNetworkRoaming(subId);
                HwLog.i(TAG, "add phone listener, subid = " + subId + " roaming = " + isRoaming);
                MobilePhoneStateListener listener = new MobilePhoneStateListener(subId, isRoaming, this.mHandlerThread.getLooper());
                this.mPhone.listen(listener, 1);
                this.mPhoneListeners.put(subId, listener);
            }
        }
    }

    private void unRegisterPhoneListeners() {
        for (int i = 0; i < this.mPhoneListeners.size(); i++) {
            this.mPhone.listen((PhoneStateListener) this.mPhoneListeners.valueAt(i), 0);
        }
        this.mPhoneListeners.clear();
    }

    public void onHandleMessage(Message msg) {
    }

    public boolean getRoamingStateBySubId(int subId) {
        MobilePhoneStateListener listener = (MobilePhoneStateListener) this.mPhoneListeners.get(subId);
        if (listener != null) {
            return listener.mIsRoaming;
        }
        return false;
    }
}
