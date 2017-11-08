package com.android.contacts.hap.rcs.dialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.map.RcsAmapLocationMgr;
import com.android.contacts.hap.rcs.map.RcsGoogleLocationMgr;
import com.android.contacts.hap.rcs.map.RcsLocationMgr;
import com.android.contacts.hap.rcs.map.RcsPrecallLocationListener;
import com.android.contacts.util.HwLog;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.Capabilities;

public class RcsPreCallFragmentHelper implements RcsPrecallLocationListener {
    private CapabilityService mCapabilityService;
    private HandleLoadMapTimeout mHandleLoadMapTimeout = new HandleLoadMapTimeout();
    private boolean mIsTimeOutViewDisplay;
    private boolean mLoginStatus;
    private String mNumber = null;
    private IfMsgplusCb mRcsCallback = new Stub() {
        public void handleEvent(int wEvent, Bundle bundle) {
            HwLog.i("RcsPreCallFragmentHelper", "handleEvent");
            Message msg = RcsPreCallFragmentHelper.this.mRcseEventHandler.obtainMessage(wEvent);
            msg.obj = bundle;
            RcsPreCallFragmentHelper.this.mRcseEventHandler.sendMessage(msg);
        }
    };
    RcsLocationMgr mRcsLocationMgr;
    private RcsPreCallFragment mRcsPreCallFragment;
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            if (HwLog.HWDBG) {
                HwLog.d("RcsPreCallFragmentHelper", " handleMessage " + msg.what);
            }
            switch (status) {
                case 0:
                    RcsPreCallFragmentHelper.this.mRcsPreCallFragment.setMapAddress((String) msg.obj);
                    return;
                case 1506:
                    RcsPreCallFragmentHelper.this.updateCapability(msg);
                    return;
                case 1602:
                    RcsPreCallFragmentHelper.this.updatePictureUri(msg);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mStartLoadMap = false;
    private TextView mStatusActionView;
    private RelativeLayout mStatusLayout;
    private LoginStatusReceiver statusReceiver = null;

    public class HandleLoadMapTimeout extends Handler {
        public void handleMessage(Message msg) {
            if (RcsPreCallFragmentHelper.this.mStartLoadMap) {
                switch (msg.what) {
                    case 1:
                        RcsPreCallFragmentHelper.this.mIsTimeOutViewDisplay = true;
                        RcsPreCallFragmentHelper.this.mRcsPreCallFragment.setMapTimeOutView();
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
                RcsPreCallFragmentHelper.this.mStartLoadMap = false;
            }
        }
    }

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean z;
            Bundle extras = intent.getExtras();
            int newStatus = extras == null ? 0 : extras.getInt("new_status");
            RcsPreCallFragmentHelper rcsPreCallFragmentHelper = RcsPreCallFragmentHelper.this;
            if (newStatus == 1) {
                z = true;
            } else {
                z = false;
            }
            rcsPreCallFragmentHelper.mLoginStatus = z;
            if (RcsPreCallFragmentHelper.this.mNumber != null && 1 == newStatus) {
                RcsPreCallFragmentHelper.this.mStatusLayout.setVisibility(8);
                RcsPreCallFragmentHelper.this.sendRcsQuestCapability(RcsPreCallFragmentHelper.this.mNumber);
                RcsPreCallFragmentHelper.this.mRcsPreCallFragment.setAllEnabled(true);
            } else if (1 != newStatus) {
                RcsPreCallFragmentHelper.this.mStatusLayout.setVisibility(0);
                RcsPreCallFragmentHelper.this.mRcsPreCallFragment.setAllEnabled(false);
            }
        }
    }

    public RcsPreCallFragmentHelper(RcsPreCallFragment fragment) {
        this.mRcsPreCallFragment = fragment;
        if (RcsContactsUtils.isInChina(this.mRcsPreCallFragment.getActivity())) {
            this.mRcsLocationMgr = new RcsAmapLocationMgr(this.mRcsPreCallFragment.getActivity());
        } else {
            this.mRcsLocationMgr = new RcsGoogleLocationMgr(this.mRcsPreCallFragment.getActivity());
        }
        this.mRcsLocationMgr.setPrecallLocationListener(this);
    }

    private void registerRCSReceiver(Context context) {
        if (this.statusReceiver == null) {
            this.statusReceiver = new LoginStatusReceiver();
        }
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.loginstatus");
        if (context != null) {
            HwLog.i("RcsPreCallFragmentHelper", "register");
            context.registerReceiver(this.statusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    public void handleCustomizationsOnCreate() {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRcsPreCallFragment != null) {
            registerRCSReceiver(this.mRcsPreCallFragment.getActivity());
            if (this.mCapabilityService == null) {
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            if (this.mCapabilityService != null) {
                this.mCapabilityService.setRcsCallBack(Integer.valueOf(1506), this.mRcsCallback);
                this.mCapabilityService.checkRcsServiceBind();
                this.mLoginStatus = this.mCapabilityService.getLoginState();
            }
        }
    }

    public void setRcsStatusView(RelativeLayout exceptionView, TextView messageView, TextView actionView) {
        this.mStatusLayout = exceptionView;
        this.mStatusActionView = actionView;
        this.mStatusActionView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                try {
                    Intent lIntent = new Intent();
                    lIntent.setClassName("com.huawei.rcsserviceapplication", "com.huawei.rcs.configuration.GuideActivity");
                    RcsPreCallFragmentHelper.this.mRcsPreCallFragment.getActivity().startActivity(lIntent);
                } catch (Exception e) {
                    HwLog.e("RcsPreCallFragmentHelper", "start rcs setting fail!");
                }
            }
        });
    }

    public boolean isRcsLoginSuccess() {
        return this.mLoginStatus;
    }

    public void handleCustomizationsOnDestroy(Context context) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || this.statusReceiver == null || context == null || this.mCapabilityService == null)) {
            context.unregisterReceiver(this.statusReceiver);
            HwLog.i("RcsPreCallFragmentHelper", "remove callback");
            this.mCapabilityService.removeRcsCallBack(Integer.valueOf(1506), this.mRcsCallback);
        }
        if (this.mHandleLoadMapTimeout.hasMessages(1)) {
            this.mHandleLoadMapTimeout.removeMessages(1);
        }
        this.mRcseEventHandler.removeCallbacksAndMessages(null);
        this.mRcsLocationMgr.locationOnDestroy();
        this.mRcsLocationMgr.setPrecallLocationListener(null);
    }

    public void sendRcsQuestCapability(String number) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && number != null) {
            this.mNumber = number;
            String lNumber = PhoneNumberUtils.normalizeNumber(number);
            try {
                if (RcseProfile.getRcsService() != null) {
                    HwLog.i("RcsPreCallFragmentHelper", "requestCapabilitiesInCsCall");
                    RcseProfile.getRcsService().requestCapabilitiesInCsCall(lNumber);
                }
            } catch (Exception e) {
                HwLog.d("RcsPreCallFragmentHelper", " requestCapabilitiesInCsCall " + e.toString());
            }
        }
    }

    private void updateCapability(Message msg) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || msg == null || this.mRcsPreCallFragment == null || this.mRcsPreCallFragment.getActivity() == null)) {
            Bundle bundle = msg.obj;
            String phoneNumber = "";
            Capabilities capabilities = null;
            try {
                bundle.setClassLoader(Capabilities.class.getClassLoader());
                phoneNumber = bundle.getString("phonenumber");
                capabilities = (Capabilities) bundle.getParcelable("capabilitiesclass");
                if (capabilities != null) {
                    HwLog.i("RcsPreCallFragmentHelper", "issupportHTTP: " + capabilities.istFtViaHttpSupported() + " isPreCallSupported: " + capabilities.isPreCallSupported() + " isOnLine: " + capabilities.isOnLine());
                }
            } catch (Exception e) {
                HwLog.e("RcsPreCallFragmentHelper", " updateCapability " + e.toString());
            }
            if (!phoneNumber.isEmpty() && r2 != null) {
                boolean isResponse = false;
                String number = "";
                number = PhoneNumberUtils.normalizeNumber(this.mNumber);
                if (this.mCapabilityService != null && this.mCapabilityService.compareUri(phoneNumber, number)) {
                    isResponse = true;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("RcsPreCallFragmentHelper", " start updateCapability ");
                }
                if (!isResponse || r2.isOnLine()) {
                }
            }
        }
    }

    private void updatePictureUri(Message msg) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || msg == null || this.mRcsPreCallFragment == null || this.mRcsPreCallFragment.getActivity() == null)) {
            Bundle bundle = msg.obj;
            String phoneNumber = "";
            String Url = "";
            try {
                bundle.setClassLoader(Capabilities.class.getClassLoader());
                phoneNumber = bundle.getString("peerNumber");
                HwLog.i("RcsPreCallFragmentHelper", "Url = " + bundle.getString("imageInfo"));
            } catch (Exception e) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsPreCallFragmentHelper", " updatePictureUrl " + e.toString());
                }
            }
            if (!phoneNumber.isEmpty()) {
                boolean isResponse = false;
                String number = "";
                number = PhoneNumberUtils.normalizeNumber(this.mNumber);
                if (this.mCapabilityService != null && this.mCapabilityService.compareUri(phoneNumber, number)) {
                    isResponse = true;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("RcsPreCallFragmentHelper", " start updatePictureUrl ");
                }
                if (isResponse) {
                    this.mRcsPreCallFragment.setPictureUrlAndTime(System.currentTimeMillis());
                }
            }
        }
    }

    public void preCallCreate(String number) {
        if (RcseProfile.getRcsService() != null) {
            try {
                HwLog.i("RcsPreCallFragmentHelper", "create session");
                RcseProfile.getRcsService().preCallCreate(number);
            } catch (Exception e) {
                HwLog.e("RcsPreCallFragmentHelper", "failed to create session");
            }
        }
    }

    public static void preCallSendImage(String imageFile, String number) {
        if (RcseProfile.getRcsService() != null) {
            try {
                HwLog.i("RcsPreCallFragmentHelper", "preCallSendImage");
                RcseProfile.getRcsService().preCallSendImageByNumber(number, imageFile);
            } catch (Exception e) {
                HwLog.e("RcsPreCallFragmentHelper", "failed to send image");
            }
        }
    }

    public static void preCallSendCompserInfo(Bundle bundle, String number) {
        if (RcseProfile.getRcsService() != null) {
            try {
                HwLog.i("RcsPreCallFragmentHelper", "preCallSendCompserInfo");
                RcseProfile.getRcsService().preCallSendCompserInfoByNumber(number, bundle);
            } catch (Exception e) {
                HwLog.e("RcsPreCallFragmentHelper", "failed to send composer info");
            }
        }
    }

    public static Bundle createComposerInfoBundle(String title, int isImportant, double latitude, double longitude) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putInt("isImportant", isImportant);
        if (!(latitude == 360.0d || longitude == 360.0d)) {
            bundle.putString("latitude", String.valueOf(latitude));
            bundle.putString("longitude", String.valueOf(longitude));
        }
        return bundle;
    }

    public void registerRcsCallBack() {
        if (RcseProfile.getRcsService() != null) {
            try {
                RcseProfile.registerRcsCallBack(Integer.valueOf(1602), this.mRcsCallback);
            } catch (Exception e) {
                Log.e("RcsPreCallFragmentHelper", "failed to register callback");
            }
        }
    }

    public void setMapdisplayView(RelativeLayout mapDisplayView, Bundle bundle) {
        HwLog.i("RcsPreCallFragmentHelper", "setMapView");
        this.mRcsLocationMgr.setMapdisplayView(mapDisplayView, bundle);
    }

    public void setStartLoadMap(boolean startLoadMap) {
        this.mStartLoadMap = startLoadMap;
    }

    public void startLoadMap(double lan, double lon) {
        HwLog.i("RcsPreCallFragmentHelper", "startLoadMap");
        this.mStartLoadMap = true;
        this.mIsTimeOutViewDisplay = false;
        if (this.mHandleLoadMapTimeout.hasMessages(1)) {
            this.mHandleLoadMapTimeout.removeMessages(1);
        }
        this.mHandleLoadMapTimeout.sendEmptyMessageDelayed(1, 20000);
        this.mRcsLocationMgr.startLoadMap(lan, lon);
    }

    public void displayMap(double lat, double lon) {
        this.mRcsLocationMgr.displayMap(lat, lon, true);
    }

    public void onLocationLatLngResult(double lat, double lon) {
        HwLog.i("RcsPreCallFragmentHelper", "onLocationLatLngResult");
        if (!this.mIsTimeOutViewDisplay) {
            this.mRcsPreCallFragment.setLanLng(lat, lon);
        }
    }

    public void onLocationAddressResult(String address) {
        HwLog.i("RcsPreCallFragmentHelper", "onLocationAddressResult");
        if (!this.mIsTimeOutViewDisplay) {
            this.mRcseEventHandler.sendMessage(Message.obtain(this.mRcseEventHandler, 0, address));
        }
    }

    public void onLocatinonSnapShot(Bitmap mapBitMap) {
        HwLog.i("RcsPreCallFragmentHelper", "onLocatinonSnapShot");
        if (this.mIsTimeOutViewDisplay) {
            if (mapBitMap != null) {
                mapBitMap.recycle();
            }
            return;
        }
        this.mRcsPreCallFragment.snapshotMap(mapBitMap);
    }
}
