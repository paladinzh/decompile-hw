package com.android.contacts.hap.rcs.dialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.view.MenuItem;
import com.android.contacts.calllog.CallLogDetailFragment;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LogConfig;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.Capabilities;

public class RcsCallLogDetailFramentHelper {
    private boolean isNoNamePhonenumber = true;
    private CapabilityService mCapabilityService;
    private CallLogDetailFragment mFragment;
    private boolean mLoginStatus = false;
    private String mNumber = "";
    private String mPhoneNumber = "";
    private IfMsgplusCb mRcsCallback = new Stub() {
        public void handleEvent(int wEvent, Bundle bundle) {
            HwLog.i("RcsCallLogDetailFramentHelper", "handleEvent");
            Message msg = RcsCallLogDetailFramentHelper.this.mRcseEventHandler.obtainMessage(wEvent);
            msg.obj = bundle;
            RcsCallLogDetailFramentHelper.this.mRcseEventHandler.sendMessage(msg);
        }
    };
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            if (LogConfig.HWDBG) {
                HwLog.d("RcsCallLogDetailFramentHelper", " handleMessage " + msg.what);
            }
            switch (status) {
                case 1508:
                    HwLog.i("RcsCallLogDetailFramentHelper", "update view");
                    RcsCallLogDetailFramentHelper.this.updateCapability(msg);
                    return;
                default:
                    return;
            }
        }
    };
    private MenuItem precallItem = null;
    private LoginStatusReceiver statusReceiver = null;

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean z;
            Bundle extras = intent.getExtras();
            int newStatus = extras == null ? 0 : extras.getInt("new_status");
            RcsCallLogDetailFramentHelper rcsCallLogDetailFramentHelper = RcsCallLogDetailFramentHelper.this;
            if (newStatus == 1) {
                z = true;
            } else {
                z = false;
            }
            rcsCallLogDetailFramentHelper.mLoginStatus = z;
            HwLog.i("RcsCallLogDetailFramentHelper", "newStatus = " + newStatus);
            if (RcsCallLogDetailFramentHelper.this.mNumber != null && 1 == newStatus) {
                RcsCallLogDetailFramentHelper.this.login();
            }
            if (!(RcsCallLogDetailFramentHelper.this.mNumber == null || 1 == newStatus || RcsCallLogDetailFramentHelper.this.mFragment == null || RcsCallLogDetailFramentHelper.this.mFragment.getActivity() == null)) {
                RcsCallLogDetailFramentHelper.this.mFragment.getAdapter().setRcsCallActionState(0);
                RcsCallLogDetailFramentHelper.this.mFragment.getCallLogList().invalidateViews();
            }
            if (RcsCallLogDetailFramentHelper.this.mFragment == null) {
                if (LogConfig.HWDBG) {
                    HwLog.d("RcsCallLogDetailFramentHelper", "mFragment = null");
                }
                return;
            }
            if (!(RcsCallLogDetailFramentHelper.this.mNumber == null || 1 == newStatus || RcsCallLogDetailFramentHelper.this.precallItem == null)) {
                RcsCallLogDetailFramentHelper.this.precallItem.setVisible(true);
                RcsCallLogDetailFramentHelper.this.precallItem.setEnabled(false);
            }
        }
    }

    private void login() {
        if (this.mCapabilityService != null) {
            this.mCapabilityService.checkRcsServiceBind();
        }
        if (this.isNoNamePhonenumber) {
            sendRcsQuestCapability(this.mNumber);
        } else {
            sendPreCallCap(this.mPhoneNumber);
        }
        if (LogConfig.HWDBG) {
            HwLog.d("RcsCallLogDetailFramentHelper", "login status changed,sendRcsQuestCapability");
        }
    }

    public void handleCustomizationsOnCreate(CallLogDetailFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && fragment != null) {
            this.mFragment = fragment;
            registerRCSReceiver(fragment.getActivity());
            if (this.mCapabilityService == null) {
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            if (this.mCapabilityService != null) {
                this.mCapabilityService.setRcsCallBack(Integer.valueOf(1508), this.mRcsCallback);
                this.mCapabilityService.checkRcsServiceBind();
                this.mLoginStatus = this.mCapabilityService.getLoginState();
                HwLog.i("RcsCallLogDetailFramentHelper", "onCreate status = " + this.mLoginStatus);
            }
        }
    }

    public void handleCustomizationsOnDestroy(Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.statusReceiver != null && context != null && this.mCapabilityService != null) {
            context.unregisterReceiver(this.statusReceiver);
            HwLog.i("RcsCallLogDetailFramentHelper", "remove callback");
            this.mCapabilityService.removeRcsCallBack(Integer.valueOf(1508), this.mRcsCallback);
        }
    }

    public void setRcsCallView(MenuItem item, String phoneNumber, boolean isNoNameDetail) {
        this.precallItem = item;
        this.precallItem.setVisible(false);
        if (this.mLoginStatus) {
            this.precallItem.setVisible(false);
        } else {
            this.precallItem.setVisible(true);
            this.precallItem.setEnabled(false);
        }
        this.mPhoneNumber = phoneNumber;
        this.isNoNamePhonenumber = isNoNameDetail;
    }

    private void registerRCSReceiver(Context context) {
        if (this.statusReceiver == null) {
            this.statusReceiver = new LoginStatusReceiver();
        }
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.loginstatus");
        if (context != null) {
            HwLog.i("RcsCallLogDetailFramentHelper", "register");
            context.registerReceiver(this.statusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    public void sendPreCallCap(String number) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && number != null && this.mCapabilityService != null && !RcsContactsUtils.isBBVersion()) {
            this.mPhoneNumber = PhoneNumberUtils.normalizeNumber(number);
            this.mCapabilityService.sendRequestContactCapabilities(this.mPhoneNumber);
        }
    }

    public void sendRcsQuestCapability(String number) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && number != null && !RcsContactsUtils.isBBVersion()) {
            this.mNumber = PhoneNumberUtils.normalizeNumber(number);
            try {
                if (RcseProfile.getRcsService() != null) {
                    HwLog.i("RcsCallLogDetailFramentHelper", "requestCapabilitiesInCsCall");
                    RcseProfile.getRcsService().requestCapabilitiesInCsCall(this.mNumber);
                }
            } catch (Exception e) {
                HwLog.d("RcsCallLogDetailFramentHelper", " requestCapabilitiesInCsCall " + e.toString());
            }
        }
    }

    private void updateCapability(Message msg) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || msg == null || this.mFragment == null || this.mFragment.getActivity() == null)) {
            Bundle bundle = msg.obj;
            String phoneNumber = "";
            Capabilities capabilities = null;
            try {
                bundle.setClassLoader(Capabilities.class.getClassLoader());
                phoneNumber = bundle.getString("phonenumber");
                capabilities = (Capabilities) bundle.getParcelable("capabilitiesclass");
                if (capabilities != null) {
                    HwLog.i("RcsCallLogDetailFramentHelper", "issupportHTTP: " + capabilities.istFtViaHttpSupported() + " isPreCallSupported: " + capabilities.isPreCallSupported() + " isOnLine: " + capabilities.isOnLine());
                }
            } catch (Exception e) {
                HwLog.d("RcsCallLogDetailFramentHelper", " updateCapability " + e.toString());
            }
            if (!phoneNumber.isEmpty() && capabilities != null) {
                if (this.isNoNamePhonenumber) {
                    showPreCallNoNameView(phoneNumber, capabilities, false, this.mNumber);
                } else {
                    showPrecallView(phoneNumber, capabilities, false, this.mPhoneNumber);
                }
            }
        }
    }

    private void showPreCallNoNameView(String phoneNumber, Capabilities cap, boolean isResponse, String dialNumber) {
        if (this.mCapabilityService != null && this.mCapabilityService.compareUri(phoneNumber, this.mNumber)) {
            isResponse = true;
        }
        if (LogConfig.HWDBG) {
            HwLog.d("RcsCallLogDetailFramentHelper", " start updateCapability ");
        }
        if (isResponse) {
            if (cap.isPreCallSupported() && cap.isOnLine()) {
                this.mFragment.getAdapter().setRcsCallActionState(1);
                if (this.precallItem != null) {
                    this.precallItem.setVisible(true);
                    this.precallItem.setEnabled(true);
                }
            } else if (cap.isPreCallSupported() && !cap.isOnLine()) {
                this.mFragment.getAdapter().setRcsCallActionState(0);
                if (this.precallItem != null) {
                    this.precallItem.setVisible(true);
                    this.precallItem.setEnabled(false);
                }
            } else if (!cap.isPreCallSupported()) {
                this.mFragment.getAdapter().setRcsCallActionState(-1);
                if (this.precallItem != null) {
                    this.precallItem.setVisible(false);
                }
            }
            this.mFragment.getAdapter().notifyDataSetChanged();
        }
    }

    private void showPrecallView(String phoneNumber, Capabilities cap, boolean isResponse, String number) {
        if (!(this.precallItem == null || this.mCapabilityService == null || !this.mCapabilityService.compareUri(phoneNumber, this.mPhoneNumber))) {
            isResponse = true;
        }
        if (LogConfig.HWDBG) {
            HwLog.d("RcsCallLogDetailFramentHelper", " start updateCapability ");
        }
        if (isResponse && this.precallItem != null) {
            if (cap.isPreCallSupported() && cap.isOnLine()) {
                this.precallItem.setVisible(true);
                this.precallItem.setEnabled(true);
            } else if (cap.isPreCallSupported() && !cap.isOnLine()) {
                this.precallItem.setVisible(true);
                this.precallItem.setEnabled(false);
            } else if (!cap.isPreCallSupported()) {
                this.precallItem.setVisible(false);
            }
        }
    }
}
