package com.android.mms;

import android.content.Context;
import android.database.ContentObserver;
import com.android.mms.ui.HwCustComposeMessageImpl;
import com.android.mms.ui.HwCustPreferenceUtilsImpl;
import com.huawei.mms.util.HwCustPhoneServiceStateListener;
import com.huawei.sprint.chameleon.provider.ChameleonContract;

public class HwCustMMSAppImpl extends HwCustMMSApp {
    private String TAG = "HwCustMMSAppImpl";
    private ChameleonMmsAutoRetreiveObserver mChameleonMmsAutoRetreiveObserver = new ChameleonMmsAutoRetreiveObserver();
    private Context mContext;

    private class ChameleonMmsAutoRetreiveObserver extends ContentObserver {
        public ChameleonMmsAutoRetreiveObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            new HwCustPreferenceUtilsImpl(HwCustMMSAppImpl.this.mContext).checkMmsAutoRetreiveUpdate();
        }
    }

    public HwCustMMSAppImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void registerCustDbObserver() {
        if (HwCustPreferenceUtilsImpl.IS_SPRINT) {
            new HwCustPreferenceUtilsImpl(this.mContext).checkMmsAutoRetreiveUpdate();
            this.mContext.getContentResolver().registerContentObserver(ChameleonContract.CONTENT_URI_CHAMELEON, true, this.mChameleonMmsAutoRetreiveObserver);
        }
    }

    public void unRegisterCustDbObserver() {
        if (HwCustPreferenceUtilsImpl.IS_SPRINT) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mChameleonMmsAutoRetreiveObserver);
        }
    }

    public void registerPhoneServiceStateListener() {
        if (HwCustComposeMessageImpl.isVOWifiFeatureEnabled()) {
            HwCustPhoneServiceStateListener.startListeningServiceState();
        }
    }

    public void unregisterPhoneServiceStateListener() {
        if (HwCustComposeMessageImpl.isVOWifiFeatureEnabled()) {
            HwCustPhoneServiceStateListener.stopListeningServiceState();
        }
    }
}
