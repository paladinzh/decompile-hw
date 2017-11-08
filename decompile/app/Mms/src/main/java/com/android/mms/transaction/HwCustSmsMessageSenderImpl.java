package com.android.mms.transaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.ui.HwCustMessageUtilsImpl;
import com.android.mms.ui.MessageUtils;

public class HwCustSmsMessageSenderImpl extends HwCustSmsMessageSender {
    private static final String TAG = "HwCustSmsMessageSenderImpl";

    public String getCustSMSCAddress() {
        return HwCustMmsConfigImpl.getCustSMSCAddress();
    }

    public String getSMSCAddress(int subID) {
        if (HwCustMmsConfigImpl.getEnableShowSmscNotEdit() && getCustSMSCAddress() != null) {
            return getCustSMSCAddress();
        }
        if (MmsConfig.getSMSCAddress() != null) {
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
            String smscAddress = getCenterAddressforPreferences(sh, subID);
            if (smscAddress == null) {
                initCenterAddress();
                smscAddress = getCenterAddressforPreferences(sh, subID);
            }
            if (TextUtils.isEmpty(smscAddress)) {
                smscAddress = MmsConfig.getSMSCAddress();
                if (!TextUtils.isEmpty(smscAddress)) {
                    refreshCenterAddress(subID, smscAddress);
                }
                return smscAddress;
            }
        }
        return null;
    }

    public String getCenterAddressforPreferences(SharedPreferences sharedPreferences, int subID) {
        if (MessageUtils.isMultiSimEnabled()) {
            return sharedPreferences.getString("sim_center_address_" + subID, null);
        }
        return sharedPreferences.getString("sim_center_address_0", null);
    }

    public void initCenterAddress() {
        String smscAddress1;
        String smsAddressBySubID;
        if (MessageUtils.isMultiSimEnabled()) {
            smscAddress1 = MessageUtils.getSmsAddressBySubID(0);
            smsAddressBySubID = MessageUtils.getSmsAddressBySubID(1);
        } else {
            smscAddress1 = MessageUtils.getSmsAddressBySubID(0);
            smsAddressBySubID = null;
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication()).edit();
        editor.putString("sim_center_address_0", smscAddress1);
        editor.putString("sim_center_address_1", smsAddressBySubID);
        editor.commit();
    }

    public boolean supportSendToEmail() {
        return HwCustMmsConfigImpl.allowSendSmsToEmail();
    }

    public void queueMessage(int mNumberOfDests, String[] mDests, Context mContext, String timeAddressPos, long mTimestamp, boolean requestDeliveryReport, long mThreadId, int mSubId, long groupIdTemp, String bodyAddressPos, String mMessageText, String address) {
        String uriString = "content://sms/queued_with_group_id";
        StringBuffer msgBuffer;
        if (mNumberOfDests > 1) {
            StringBuffer stringBuf = new StringBuffer();
            for (int i = 0; i < mNumberOfDests; i++) {
                if (Contact.isEmailAddress(mDests[i])) {
                    msgBuffer = new StringBuffer();
                    msgBuffer.append(mDests[i]);
                    msgBuffer.append(" ");
                    msgBuffer.append(mMessageText);
                    Context context = mContext;
                    MessageUtils.smsAddMessageToUri(context, Uri.parse(uriString), mDests[i], msgBuffer.toString(), null, Long.valueOf(mTimestamp), true, requestDeliveryReport, mThreadId, mSubId, groupIdTemp, bodyAddressPos, timeAddressPos);
                } else {
                    stringBuf.append(mDests[i]).append(",");
                }
            }
            uriString = "content://sms/bulk_queued";
            address = stringBuf.toString();
        } else if (Contact.isEmailAddress(address)) {
            msgBuffer = new StringBuffer();
            msgBuffer.append(address);
            msgBuffer.append(" ");
            msgBuffer.append(mMessageText);
            mMessageText = msgBuffer.toString();
        }
        MessageUtils.smsAddMessageToUri(mContext, Uri.parse(uriString), address, mMessageText, null, Long.valueOf(mTimestamp), true, requestDeliveryReport, mThreadId, mSubId, groupIdTemp, bodyAddressPos, timeAddressPos);
    }

    public String getCustReplaceSmsCenterNumber(int subID) {
        if (HwCustMmsConfigImpl.getCustReplaceSMSCAddressByCard() != null) {
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
            String smscAddress = getCenterAddressforPreferences(sh, subID);
            if (smscAddress == null) {
                initCenterAddress();
                smscAddress = getCenterAddressforPreferences(sh, subID);
            }
            if (TextUtils.isEmpty(smscAddress)) {
                smscAddress = HwCustMessageUtilsImpl.getCustReplaceSmsCenterNumber(subID);
                if (!TextUtils.isEmpty(smscAddress)) {
                    refreshCenterAddress(subID, smscAddress);
                }
                return smscAddress;
            }
        }
        return null;
    }

    private void refreshCenterAddress(int subId, String smscNumber) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication()).edit();
        if (MessageUtils.isMultiSimEnabled()) {
            editor.putString("sim_center_address_" + subId, smscNumber);
            if (1 == subId) {
                editor.putString("pref_key_simuim2_message_center", smscNumber);
            } else {
                editor.putString("pref_key_simuim1_message_center", smscNumber);
            }
        } else {
            editor.putString("sim_center_address_0", smscNumber);
            editor.putString("sms_center_number", smscNumber);
        }
        editor.commit();
    }
}
