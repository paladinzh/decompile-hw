package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.android.mms.ui.HwCustGeneralPreferenceFragmentImpl;
import com.android.mms.ui.HwPreference;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustMccMncConfigImpl extends HwCustMccMncConfig {
    private static final int AUTO_RETRIEVAL_DEFAULT = -1;
    private static final String AUTO_RETRIEVAL_FLAG = "auto_retrieval_falg";
    private static final int AUTO_RETRIEVAL_MATCHED = 1;
    public static final String BRAZIL_MMS_DELIVERY_REPORTS_FLAG = "brazil_mms_delivery_report_flag";
    private static final String CARD_READ_FLAG = "mms_card_read_flag_";
    private static final String TAG = "HwCustMccMncConfigImpl";
    private Context mContext;

    public HwCustMccMncConfigImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    private boolean isEnableBrazilMMSDeliveryReports() {
        if (HwCustMmsConfigImpl.getCustMccForBrazilMMSDeliveryReports() != null) {
            return true;
        }
        return false;
    }

    private boolean isCurrentMccEqualsCustMcc(String currentMccmnc, String custMcc) {
        String[] custPlmns = custMcc.split(";");
        int j = 0;
        while (j < custPlmns.length) {
            if (currentMccmnc.substring(0, 3).equals(custPlmns[j]) || currentMccmnc.equals(custPlmns[j])) {
                return true;
            }
            j++;
        }
        return false;
    }

    public void setMMSDeliveryReportsForBrazilCard() {
        if (isEnableBrazilMMSDeliveryReports()) {
            String brazilFlag = Systemex.getString(this.mContext.getContentResolver(), BRAZIL_MMS_DELIVERY_REPORTS_FLAG);
            if (TextUtils.isEmpty(brazilFlag) || !"true".equals(brazilFlag)) {
                int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                String custPlmnsString = HwCustMmsConfigImpl.getCustMccForBrazilMMSDeliveryReports();
                if (phoneCount <= 0 || TextUtils.isEmpty(custPlmnsString)) {
                    Log.d(TAG, "setMMSDeliveryReportsForBrazilCard called>>> phoneCount=" + phoneCount + ",custPlmnsString=" + custPlmnsString);
                    return;
                }
                for (int i = 0; i < phoneCount; i++) {
                    if (5 == TelephonyManager.getDefault().getSimState(i)) {
                        String simMccMnc = TelephonyManager.getDefault().getSimOperator(i);
                        if (MccMncConfig.isValideOperator(simMccMnc)) {
                            String isCardRead = Systemex.getString(this.mContext.getContentResolver(), CARD_READ_FLAG + i);
                            if (!TextUtils.isEmpty(isCardRead) && "true".equals(isCardRead)) {
                                Log.d(TAG, "setMMSDeliveryReportsForBrazilCard called>>>the mms_card_read_flag_" + i + "=[" + isCardRead + "],continue!");
                            } else if (isCurrentMccEqualsCustMcc(simMccMnc, custPlmnsString)) {
                                setMMSDeliveryReportsState();
                                Systemex.putString(this.mContext.getContentResolver(), BRAZIL_MMS_DELIVERY_REPORTS_FLAG, "true");
                                return;
                            } else {
                                Systemex.putString(this.mContext.getContentResolver(), CARD_READ_FLAG + i, "true");
                            }
                        } else {
                            Log.d(TAG, "setMMSDeliveryReportsForBrazilCard called>>>card[" + i + "] mccmnc=[" + simMccMnc + "],continue!");
                        }
                    }
                }
                return;
            }
            Log.d(TAG, "setMMSDeliveryReportsForBrazilCard called>>>the switcher of mms delivery reports is already set to true,return");
            return;
        }
        Log.d(TAG, "setMMSDeliveryReportsForBrazilCard called>>>this function are close,return!");
    }

    private void setMMSDeliveryReportsState() {
        int deliveryReportState = MmsConfig.getDefaultDeliveryReportState();
        if (deliveryReportState < 2) {
            boolean smsReportMode = false;
            if (1 == deliveryReportState) {
                smsReportMode = true;
            }
            if (smsReportMode) {
                deliveryReportState = 3;
            } else {
                deliveryReportState = 2;
            }
            new HwPreference(this.mContext).setState(deliveryReportState);
            Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            edit.putInt("pref_key_delivery_reports", deliveryReportState);
            edit.putBoolean("pref_key_mms_delivery_reports", true);
            edit.putBoolean("pref_key_sms_delivery_reports", smsReportMode);
            edit.putBoolean("pref_key_sms_delivery_reports_sub0", smsReportMode);
            edit.putBoolean("pref_key_sms_delivery_reports_sub1", smsReportMode).commit();
            Systemex.putInt(this.mContext.getContentResolver(), HwCustGeneralPreferenceFragmentImpl.CUST_DELIVERY_REPORT_KEY, deliveryReportState);
        }
    }

    public void setDefaultAutoRetrievalMmsForOperator() {
        if (System.getInt(this.mContext.getContentResolver(), AUTO_RETRIEVAL_FLAG, -1) != 1 && !TextUtils.isEmpty(HwCustMmsConfigImpl.getCustomMccMncMmsRetrive())) {
            if (MccMncConfig.isValideOperator(MccMncConfig.getDefault().getOperator())) {
                System.putInt(this.mContext.getContentResolver(), AUTO_RETRIEVAL_FLAG, 1);
            }
            MmsConfig.setAutoReceivePrefState();
        }
    }

    public String getCustForbidMmsList() {
        return HwCustMmsConfigImpl.getAdaptForbidMmsList();
    }

    public boolean isExistInForbidMmsList(String mccmnc) {
        String forbidmms = HwCustMmsConfigImpl.getAdaptForbidMmsList();
        if (TextUtils.isEmpty(forbidmms)) {
            Log.d(TAG, "no cust for forbid mms feature");
            return false;
        }
        for (String str : forbidmms.split(";")) {
            if (mccmnc.equals(str)) {
                Log.d(TAG, "mccmnc " + mccmnc + " forbid mms feature");
                return true;
            }
        }
        Log.d(TAG, "mccmnc " + mccmnc + " have no forbid mms feature");
        return false;
    }

    public void setAdaptForbidMmsConfigs(Context context, boolean removeMms, boolean isShowEmailPop, int mmsEnabled, String isShare) {
        Log.d(TAG, "setAdaptForbidMmsConfigs");
        HwCustMmsConfigImpl.setRemoveMms(removeMms);
        MmsConfig.setShowCheckEmailPoup(isShowEmailPop);
        MmsConfig.setMmsEnabled(mmsEnabled);
        System.putString(context.getContentResolver(), "hw_share_app_no_mms", isShare);
    }

    public void initForbidMmsFeature(Context context, String mccmnc) {
        if (isExistInForbidMmsList(mccmnc)) {
            setAdaptForbidMmsConfigs(context, true, false, 0, "true");
            MmsConfig.setMultipartSmsEnabled(true);
            return;
        }
        setAdaptForbidMmsConfigs(context, false, true, 1, "false");
    }
}
