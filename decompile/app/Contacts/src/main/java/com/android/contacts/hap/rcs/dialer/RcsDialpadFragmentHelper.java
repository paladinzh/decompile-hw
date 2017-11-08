package com.android.contacts.hap.rcs.dialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.Capabilities;

public class RcsDialpadFragmentHelper {
    private CapabilityService mCapabilityService;
    private DialpadFragment mFragment;
    private boolean mIsLandscape = false;
    private View mLineHorizontalTopTableRcs;
    private boolean mLoginStatus = false;
    private String mNumber = null;
    private RcsCLIRBroadCastHelper mRcsCLIRBroadCastHelper;
    private View mRcsCallButton;
    private ImageView mRcsCallImageButton;
    private IfMsgplusCb mRcsCallback = new Stub() {
        public void handleEvent(int wEvent, Bundle bundle) {
            HwLog.i("RcsDialpadFragmentHelper", "handleEvent");
            Message msg = RcsDialpadFragmentHelper.this.mRcseEventHandler.obtainMessage(wEvent);
            msg.obj = bundle;
            RcsDialpadFragmentHelper.this.mRcseEventHandler.sendMessage(msg);
        }
    };
    private TextView mRcsTextView;
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            HwLog.i("RcsDialpadFragmentHelper", " handleMessage " + msg.what);
            switch (status) {
                case 1:
                    String lNumber = PhoneNumberUtils.normalizeNumber(RcsDialpadFragmentHelper.this.mNumber);
                    try {
                        if (RcseProfile.getRcsService() != null) {
                            HwLog.i("RcsDialpadFragmentHelper", "requestCapabilitiesInCsCall");
                            RcseProfile.getRcsService().requestCapabilitiesInCsCall(lNumber);
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        HwLog.i("RcsDialpadFragmentHelper", " requestCapabilitiesInCsCall " + e.toString());
                        return;
                    }
                case 1505:
                    HwLog.i("RcsDialpadFragmentHelper", "update view");
                    RcsDialpadFragmentHelper.this.updateCapability(msg);
                    return;
                default:
                    return;
            }
        }
    };
    private View mTableRowRcs;
    private View mTableRowRcsView;
    private int saveRotateRcsViewState = 0;
    private LoginStatusReceiver statusReceiver = null;

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            Bundle extras = intent.getExtras();
            int newStatus = extras == null ? 0 : extras.getInt("new_status");
            RcsDialpadFragmentHelper rcsDialpadFragmentHelper = RcsDialpadFragmentHelper.this;
            if (newStatus == 1) {
                z = true;
            }
            rcsDialpadFragmentHelper.mLoginStatus = z;
            HwLog.i("RcsDialpadFragmentHelper", "newStatus = " + newStatus);
            if (RcsDialpadFragmentHelper.this.mNumber != null && 1 == newStatus) {
                if (RcsDialpadFragmentHelper.this.mCapabilityService != null) {
                    RcsDialpadFragmentHelper.this.mCapabilityService.checkRcsServiceBind();
                }
                RcsDialpadFragmentHelper.this.sendRcsQuestCapability(PhoneNumberUtils.normalizeNumber(RcsDialpadFragmentHelper.this.mNumber));
                if (HwLog.HWDBG) {
                    HwLog.d("RcsDialpadFragmentHelper", "login status changed,sendRcsQuestCapability,login status =" + newStatus);
                }
            }
            if (!(RcsDialpadFragmentHelper.this.mNumber == null || 1 == newStatus || RcsDialpadFragmentHelper.this.mFragment == null || RcsDialpadFragmentHelper.this.mFragment.getActivity() == null || RcsDialpadFragmentHelper.this.mFragment.getDialerEditText().getVisibility() != 0)) {
                RcsDialpadFragmentHelper.this.updateNotLoginRcsView();
            }
            if (RcsDialpadFragmentHelper.this.mFragment == null && HwLog.HWDBG) {
                HwLog.d("RcsDialpadFragmentHelper", "mFragment = null");
            }
        }
    }

    public void handleCustomizationsOnCreate(DialpadFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && fragment != null) {
            this.mFragment = fragment;
            registerRCSReceiver(fragment.getActivity());
            if (this.mCapabilityService == null) {
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            if (this.mCapabilityService != null) {
                this.mCapabilityService.setRcsCallBack(Integer.valueOf(1505), this.mRcsCallback);
                this.mCapabilityService.checkRcsServiceBind();
                this.mLoginStatus = this.mCapabilityService.getLoginState();
                HwLog.i("RcsDialpadFragmentHelper", "onCreate status = " + this.mLoginStatus);
            }
            this.mRcsCLIRBroadCastHelper = RcsCLIRBroadCastHelper.getInstance(this.mFragment.getActivity());
        }
    }

    public void handleCustomizationsOnDestroy(Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.statusReceiver != null && context != null && this.mCapabilityService != null) {
            context.unregisterReceiver(this.statusReceiver);
            HwLog.i("RcsDialpadFragmentHelper", "remove callback");
            this.mCapabilityService.removeRcsCallBack(Integer.valueOf(1505), this.mRcsCallback);
        }
    }

    private void registerRCSReceiver(Context context) {
        if (this.statusReceiver == null) {
            this.statusReceiver = new LoginStatusReceiver();
        }
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.loginstatus");
        if (context != null) {
            HwLog.i("RcsDialpadFragmentHelper", "register");
            context.registerReceiver(this.statusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    public void sendRcsQuestCapability(String number) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && number != null && this.mCapabilityService != null) {
            if (RcsContactsUtils.isBBVersion()) {
                hideRcsCallButton();
                return;
            }
            this.mNumber = number;
            if (!(this.mTableRowRcs == null || this.mFragment == null)) {
                this.mTableRowRcs.setVisibility(8);
                this.mFragment.setTableRow0Visible(this.mFragment.getNewContactVisibility() & 8);
                this.mLineHorizontalTopTableRcs.setVisibility(this.mFragment.getNewContactVisibility() & 8);
                this.mFragment.changeDialerDigitsHeight();
            }
            if (this.mRcsCallButton != null && this.mIsLandscape) {
                this.mRcsCallImageButton.setVisibility(4);
                setEnabledAll(this.mRcsCallButton, false);
            }
            if (number.length() >= 7 && number.length() <= 20) {
                if (this.mRcseEventHandler.hasMessages(1)) {
                    this.mRcseEventHandler.removeMessages(1);
                }
                this.saveRotateRcsViewState = 0;
                Message msg = new Message();
                msg.what = 1;
                this.mRcseEventHandler.sendMessageDelayed(msg, 1000);
            }
        }
    }

    public void setRcsCallView(View view) {
        if (view != null) {
            this.mRcsCallButton = view;
            this.mRcsCallImageButton = (ImageView) this.mRcsCallButton.findViewById(R.id.menu_item_image);
            this.mRcsCallImageButton.setVisibility(4);
            if (this.mIsLandscape) {
                this.mRcsCallButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        RcsDialpadFragmentHelper.this.startPreCallActivity();
                    }
                });
            }
        }
    }

    private void startPreCallActivity() {
        if (this.mRcsCLIRBroadCastHelper.isCLIROpen()) {
            this.mRcsCLIRBroadCastHelper.showDialog(this.mFragment.getActivity());
            return;
        }
        RcsContactsUtils.startPreCallActivity(this.mFragment.getActivity(), this.mFragment.getDialerEditText().getText().toString());
        this.mFragment.clearDigitsText();
    }

    public void setTableRowRcs(View tableRowRcs, View lineHorizontalTopTableRcs) {
        if (tableRowRcs != null && lineHorizontalTopTableRcs != null) {
            this.mTableRowRcs = tableRowRcs;
            this.mLineHorizontalTopTableRcs = lineHorizontalTopTableRcs;
            this.mTableRowRcsView = this.mTableRowRcs.findViewById(R.id.rcs_call_view);
            this.mRcsTextView = (TextView) this.mTableRowRcs.findViewById(R.id.rcs_call_text);
            this.mTableRowRcs.setVisibility(8);
            this.mLineHorizontalTopTableRcs.setVisibility(this.mFragment.getNewContactVisibility() & 8);
            this.mFragment.setTableRow0Visible(this.mFragment.getNewContactVisibility() & 8);
            this.mTableRowRcsView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RcsDialpadFragmentHelper.this.startPreCallActivity();
                }
            });
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateCapability(Message msg) {
        if (!((this.mRcsCallButton == null && this.mIsLandscape) || this.mTableRowRcs == null || !EmuiFeatureManager.isRcsFeatureEnable() || msg == null || this.mFragment == null || this.mFragment.getActivity() == null)) {
            Bundle bundle = msg.obj;
            String phoneNumber = "";
            Capabilities cap = null;
            try {
                bundle.setClassLoader(Capabilities.class.getClassLoader());
                phoneNumber = bundle.getString("phonenumber");
                cap = (Capabilities) bundle.getParcelable("capabilitiesclass");
            } catch (Exception e) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsDialpadFragmentHelper", " updateCapability " + e.toString());
                }
            }
            if (!phoneNumber.isEmpty() && cap != null) {
                HwLog.i("RcsDialpadFragmentHelper", "issupportHTTP: " + cap.istFtViaHttpSupported());
                HwLog.i("RcsDialpadFragmentHelper", "isPreCallSupported: " + cap.isPreCallSupported());
                HwLog.i("RcsDialpadFragmentHelper", "isOnLine: " + cap.isOnLine());
                boolean isResponse = false;
                String number = "";
                number = PhoneNumberUtils.normalizeNumber(this.mNumber);
                if (this.mCapabilityService != null && this.mCapabilityService.compareUri(phoneNumber, number)) {
                    isResponse = true;
                }
                HwLog.i("RcsDialpadFragmentHelper", " start updateCapability ");
                if (isResponse) {
                    updateCapabilityWithResponse(cap);
                    setRotateRcsViewState(cap);
                }
            }
        }
    }

    private void setRotateRcsViewState(Capabilities cap) {
        this.saveRotateRcsViewState = 0;
        if (cap.isPreCallSupported() && cap.isOnLine()) {
            this.saveRotateRcsViewState = 1;
        } else if (cap.isPreCallSupported() && !cap.isOnLine()) {
            this.saveRotateRcsViewState = 2;
        } else if (!cap.isPreCallSupported()) {
            this.saveRotateRcsViewState = 3;
        }
    }

    public int getSaveRotateRcsViewState() {
        return this.saveRotateRcsViewState;
    }

    public void setRotateRcsView(int state, String number) {
        this.saveRotateRcsViewState = state;
        this.mNumber = number;
        Capabilities cap = new Capabilities();
        switch (state) {
            case 0:
                sendRcsQuestCapability(number);
                return;
            case 1:
                cap.setIsSuptPreCall(true);
                cap.setIsOnLine(true);
                updateCapabilityWithResponse(cap);
                return;
            case 2:
                cap.setIsSuptPreCall(true);
                cap.setIsOnLine(false);
                updateCapabilityWithResponse(cap);
                return;
            case 3:
                cap.setIsSuptPreCall(false);
                updateCapabilityWithResponse(cap);
                return;
            default:
                return;
        }
    }

    private void updateCapabilityWithResponse(Capabilities cap) {
        if (RcsContactsUtils.isBBVersion()) {
            hideRcsCallButton();
            return;
        }
        if (this.mIsLandscape) {
            this.mRcsCallImageButton.setImageResource(R.drawable.pre_call_mark_icon);
            if (cap.isPreCallSupported() && cap.isOnLine()) {
                this.mRcsCallImageButton.setVisibility(0);
                setEnabledAll(this.mRcsCallButton, true);
            } else if (cap.isPreCallSupported() && !cap.isOnLine()) {
                this.mRcsCallImageButton.setVisibility(0);
                setEnabledAll(this.mRcsCallButton, false);
            } else if (!cap.isPreCallSupported()) {
                this.mRcsCallImageButton.setVisibility(4);
                setEnabledAll(this.mRcsCallButton, false);
            }
        } else {
            if (cap.isPreCallSupported() && cap.isOnLine()) {
                this.mTableRowRcs.setVisibility(0);
                HwLog.i("RcsDialpadFragmentHelper", "updateCapability mTableRowRcs visible and enable");
                setEnabledAll(this.mTableRowRcs, true);
                this.mRcsTextView.setTextColor(this.mFragment.getActivity().getResources().getColor(R.color.dialpad_huawei_text_color));
                this.mLineHorizontalTopTableRcs.setVisibility(0);
                this.mFragment.setTableRow0Visible(0);
            } else if (cap.isPreCallSupported() && !cap.isOnLine()) {
                this.mTableRowRcs.setVisibility(0);
                this.mFragment.setTableRow0Visible(0);
                HwLog.i("RcsDialpadFragmentHelper", "updateCapability mTableRowRcs visible and disable");
                setEnabledAll(this.mTableRowRcs, false);
                this.mRcsTextView.setTextColor(this.mFragment.getActivity().getResources().getColor(R.color.rcs_text_color_grey));
                this.mLineHorizontalTopTableRcs.setVisibility(0);
            } else if (!cap.isPreCallSupported()) {
                HwLog.i("RcsDialpadFragmentHelper", "updateCapability mTableRowRcs gone");
                this.mTableRowRcs.setVisibility(8);
                this.mFragment.setTableRow0Visible(this.mFragment.getNewContactVisibility() & 8);
                this.mLineHorizontalTopTableRcs.setVisibility(this.mFragment.getNewContactVisibility() & 8);
            }
            this.mFragment.changeDialerDigitsHeight();
        }
    }

    public void setRcsQuestNumber(String number) {
        this.mNumber = number;
    }

    public void updateNotLoginRcsView() {
        if (RcsContactsUtils.isBBVersion()) {
            hideRcsCallButton();
            return;
        }
        if (this.mIsLandscape) {
            this.mRcsCallImageButton.setImageResource(R.drawable.pre_call_mark_icon);
            this.mRcsCallImageButton.setVisibility(0);
            setEnabledAll(this.mRcsCallButton, false);
        } else {
            this.mTableRowRcs.setVisibility(0);
            setEnabledAll(this.mTableRowRcs, false);
            this.mRcsTextView.setTextColor(this.mFragment.getActivity().getResources().getColor(R.color.rcs_text_color_grey));
            this.mLineHorizontalTopTableRcs.setVisibility(0);
            this.mFragment.setTableRow0Visible(0);
            this.mFragment.changeDialerDigitsHeight();
        }
    }

    public void setLandscapeState(boolean isLandscape) {
        this.mIsLandscape = isLandscape;
    }

    public static void setEnabledAll(View v, boolean enabled) {
        v.setEnabled(enabled);
        v.setFocusable(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                setEnabledAll(vg.getChildAt(i), enabled);
            }
        }
    }

    public void hideRcsCallButton() {
        this.mNumber = "";
        if (this.mIsLandscape) {
            this.mRcsCallImageButton.setVisibility(4);
            setEnabledAll(this.mRcsCallButton, false);
            return;
        }
        this.mTableRowRcs.setVisibility(8);
        this.mFragment.setTableRow0Visible(this.mFragment.getNewContactVisibility() & 8);
        this.mLineHorizontalTopTableRcs.setVisibility(this.mFragment.getNewContactVisibility() & 8);
    }
}
