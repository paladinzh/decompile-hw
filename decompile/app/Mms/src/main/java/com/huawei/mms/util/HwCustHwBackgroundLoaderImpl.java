package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;
import com.huawei.cspcommon.MLog;
import java.util.HashMap;

public class HwCustHwBackgroundLoaderImpl extends HwCustHwBackgroundLoader {
    private static final String BROADCAST_PERMISSION_CHANGE_FLAG = "huawei.permmisons.mms.CHANGE_FOLLOW_FLAG";
    private static final String CHANGE_FLAG_ACTION = "android.action.CHANGE_MMS_FOLLOW_FLAG";
    private static final String TAG = "HwCustHwBackgroundLoaderImpl";
    private ChangeFollowFlagReceiver mChangeFollowFlagReceiver = new ChangeFollowFlagReceiver();

    public class ChangeFollowFlagReceiver extends BroadcastReceiver {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && HwCustHwBackgroundLoaderImpl.CHANGE_FLAG_ACTION.equals(action) && intent.hasExtra("flag")) {
                    boolean flag = intent.getBooleanExtra("flag", true);
                    int subId = intent.getIntExtra("subId", 0);
                    MLog.d(HwCustHwBackgroundLoaderImpl.TAG, "ChangeFollowFlagReceiver flag = " + flag + " subId = " + subId);
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    if (MessageUtils.isMultiSimEnabled()) {
                        MLog.d(HwCustHwBackgroundLoaderImpl.TAG, "dual sim change follow flag");
                        if (subId == 1) {
                            sp.edit().putBoolean("pref_mms_is_follow_notification_sub1", flag).commit();
                        } else {
                            sp.edit().putBoolean("pref_mms_is_follow_notification_sub0", flag).commit();
                        }
                    } else {
                        sp.edit().putBoolean("pref_mms_is_follow_notification", flag).commit();
                    }
                }
            }
        }
    }

    public String getMmsParams() {
        return HwCustMmsConfigImpl.getMmsParams();
    }

    public void refreshParameterSettings() {
        if (!MessageUtils.isMultiSimEnabled()) {
            String currentMccMnc = MmsApp.getDefaultTelephonyManager().getSimOperator();
            if (MccMncConfig.isValideOperator(currentMccMnc)) {
                String mmsParams = HwCustMmsConfigImpl.getMmsParams();
                if (!TextUtils.isEmpty(mmsParams)) {
                    for (String str : mmsParams.split(";")) {
                        String[] mccmncSet = str.split(",");
                        if (mccmncSet.length == 3 && currentMccMnc.equals(mccmncSet[0])) {
                            int smsToMmsTextThreshold = Integer.parseInt(mccmncSet[1]);
                            int maxMessageSize = Integer.parseInt(mccmncSet[2]);
                            if (smsToMmsTextThreshold == -1) {
                                MmsConfig.setMultipartSmsEnabled(true);
                            } else {
                                MmsConfig.setMultipartSmsEnabled(false);
                            }
                            MLog.d(TAG, "refresh smsToMmsTextThreshold = " + smsToMmsTextThreshold + "  maxMessageSize = " + maxMessageSize);
                            MmsConfig.setSmsToMmsTextThreshold(smsToMmsTextThreshold);
                            MmsConfig.setMaxMessageSize(maxMessageSize);
                        } else {
                            MLog.d(TAG, "getMmsParams format illegal, plz check");
                        }
                    }
                }
            }
        }
    }

    public void refreshRecipientLimitSettings(Intent intent) {
        String custRecipientLimitStr = HwCustMmsConfigImpl.getCustRecipientLimitBySimCard();
        if (!TextUtils.isEmpty(custRecipientLimitStr)) {
            int slotId = IIccPhoneBookManagerEx.getDefault().getSoltIdInSimStateChangeIntent(intent);
            String currentPlmns = TelephonyManager.getDefault().getSimOperator(slotId);
            if (!MccMncConfig.isValideOperator(currentPlmns)) {
                return;
            }
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || slotId != 1 || !isSIMCardPresent(0)) {
                String recipientLimitStr = (String) parseCustRecipientLimitStr(custRecipientLimitStr).get(currentPlmns);
                int recipientLimit = 1000;
                if (!TextUtils.isEmpty(recipientLimitStr)) {
                    recipientLimit = Integer.parseInt(recipientLimitStr);
                }
                MmsConfig.setRecipientLimit(recipientLimit);
            }
        }
    }

    private boolean isSIMCardPresent(int slotId) {
        int simState = MessageUtils.getSimState(slotId);
        if (simState == 2 || simState == 3 || simState == 4 || simState == 5) {
            return true;
        }
        return false;
    }

    private HashMap<String, String> parseCustRecipientLimitStr(String custRecipientLimitStr) {
        HashMap<String, String> recipientLimitMap = new HashMap();
        String[] units = custRecipientLimitStr.split(";");
        if (units.length > 0) {
            for (int i = 0; i < units.length; i++) {
                if (!TextUtils.isEmpty(units[i])) {
                    String[] recipientLimitUnit = units[i].split(":");
                    if (recipientLimitUnit.length == 2) {
                        recipientLimitMap.put(recipientLimitUnit[0], recipientLimitUnit[1]);
                    }
                }
            }
        }
        return recipientLimitMap;
    }

    public void registerChangeFollowFlagReceiver(Context context) {
        MLog.d(TAG, "registerChangeFollowFlagReceiver");
        IntentFilter intentFilter = new IntentFilter(CHANGE_FLAG_ACTION);
        if (this.mChangeFollowFlagReceiver != null) {
            context.registerReceiver(this.mChangeFollowFlagReceiver, intentFilter, "huawei.permmisons.mms.CHANGE_FOLLOW_FLAG", null);
        }
    }

    public void unRegisterChangeFollowFlagReceiver(Context context) {
        MLog.d(TAG, "unRegisterChangeFollowFlagReceiver");
        if (this.mChangeFollowFlagReceiver != null) {
            context.unregisterReceiver(this.mChangeFollowFlagReceiver);
        }
    }

    public void refreshDeliveryReportsSettings(Context context) {
        String custConfig = HwCustMmsConfigImpl.getCustConfigForDeliveryReports();
        if (!TextUtils.isEmpty(custConfig) && System.getInt(context.getContentResolver(), "enable_delivery_reports", 0) <= 0) {
            String currentMccmnc = getTargetPlmn();
            if (MccMncConfig.isValideOperator(currentMccmnc)) {
                int deliveryReportsStatus = getCustDeliveryConfig(currentMccmnc, custConfig);
                if (deliveryReportsStatus > 0) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("pref_key_delivery_reports", deliveryReportsStatus).apply();
                    System.putInt(context.getContentResolver(), "enable_delivery_reports", deliveryReportsStatus);
                }
            }
        }
    }

    public void refreshReadReportsSettings(Context context) {
        String custConfig = HwCustMmsConfigImpl.getCustConfigForMmsReadReports();
        if (!TextUtils.isEmpty(custConfig) && TextUtils.isEmpty(System.getString(context.getContentResolver(), "enable_mms_read_reports"))) {
            String currentMccmnc = getTargetPlmn();
            if (MccMncConfig.isValideOperator(currentMccmnc) && isTargetCard(currentMccmnc, custConfig)) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_mms_read_reports", true).apply();
                System.putString(context.getContentResolver(), "enable_mms_read_reports", "true");
            }
        }
    }

    public void refreshReplyReadReportsSettings(Context context) {
        String custConfig = HwCustMmsConfigImpl.getCustConfigForMmsReplyReadReports();
        if (!TextUtils.isEmpty(custConfig) && TextUtils.isEmpty(System.getString(context.getContentResolver(), "enable_mms_reply_read_reports"))) {
            String currentMccmnc = getTargetPlmn();
            if (MccMncConfig.isValideOperator(currentMccmnc) && isTargetCard(currentMccmnc, custConfig)) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_key_mms_auto_reply_read_reports", true).apply();
                System.putString(context.getContentResolver(), "enable_mms_reply_read_reports", "true");
            }
        }
    }

    private String getTargetPlmn() {
        if (MmsApp.getDefaultTelephonyManager().isMultiSimEnabled()) {
            return MmsApp.getDefaultTelephonyManager().getSimOperator(SubscriptionManager.getDefaultSubscriptionId());
        }
        return MmsApp.getDefaultTelephonyManager().getSimOperator();
    }

    private boolean isTargetCard(String mccmnc, String custConfig) {
        try {
            String[] mccMncList = custConfig.split(",");
            int i = 0;
            while (i < mccMncList.length) {
                if (mccmnc.startsWith(mccMncList[i]) || mccmnc.equals(mccMncList[i])) {
                    return true;
                }
                i++;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getCustDeliveryConfig(String mccmnc, String custConfig) {
        try {
            String[] keyVals = custConfig.split(";");
            for (int i = 0; i < keyVals.length; i++) {
                String keyStr = keyVals[i].split(":")[0];
                String valStr = keyVals[i].split(":")[1];
                if (!TextUtils.isEmpty(keyStr) && !TextUtils.isEmpty(valStr) && (mccmnc.startsWith(keyStr) || mccmnc.equals(keyStr))) {
                    return Integer.parseInt(valStr);
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
