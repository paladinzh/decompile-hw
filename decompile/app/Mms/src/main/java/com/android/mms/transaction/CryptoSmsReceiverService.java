package com.android.mms.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.huawei.android.telephony.SmsInterceptionListenerEx;
import com.huawei.android.telephony.SmsInterceptionManagerEx;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.util.StatisticalHelper;

public class CryptoSmsReceiverService {
    public boolean mIsReceivedMsgBodyEncrypted = false;
    CryptoSmsInterceptionListener mSmsInterceptionListener;

    class CryptoSmsInterceptionListener extends SmsInterceptionListenerEx {
        CryptoSmsInterceptionListener() {
        }

        public int handleSmsDeliverAction(Bundle smsInfo) {
            int nResult = 0;
            if (smsInfo == null) {
                MLog.e("HwCustSmsReceiverServiceImpl", "handleSmsDeliverAction: Invalid RegistResponse sms info");
                return -1;
            }
            Intent intent = (Intent) smsInfo.getParcelable("HANDLE_SMS_INTENT");
            if (intent == null) {
                MLog.e("HwCustSmsReceiverServiceImpl", "handleSmsDeliverAction: fail to get RegistResponse sms intent");
                return -1;
            }
            try {
                nResult = CryptoSmsReceiverService.this.processCryptoRegistResponseSms(intent);
            } catch (Exception e) {
                MLog.e("HwCustSmsReceiverServiceImpl", "handleSmsDeliverAction: Exception", (Throwable) e);
            }
            return nResult;
        }
    }

    public void onCreate() {
        this.mSmsInterceptionListener = new CryptoSmsInterceptionListener();
        MLog.e("HwCustSmsReceiverServiceImpl", "handleSmsDeliverAction: onCreate");
        SmsInterceptionManagerEx.getInstance().registerListener(this.mSmsInterceptionListener, 10001);
    }

    public void onDestroy() {
        if (this.mSmsInterceptionListener != null) {
            SmsInterceptionManagerEx.getInstance().unregisterListener(10001);
        }
    }

    public int processCryptoRegistResponseSms(Intent intent) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        if (msgs == null || msgs.length <= 0) {
            return 0;
        }
        String bodyText;
        SmsMessage sms = msgs[0];
        int sub = MessageUtils.getSubId(sms);
        if (pduCount == 1) {
            bodyText = replaceFormFeeds(sms.getDisplayMessageBody());
        } else {
            StringBuilder body = new StringBuilder();
            for (SmsMessage sms2 : msgs) {
                if (sms2.mWrappedSmsMessage != null) {
                    body.append(sms2.getDisplayMessageBody());
                }
            }
            bodyText = replaceFormFeeds(body.toString());
        }
        return isCryptoRegistResponse(bodyText, sub) ? 1 : 0;
    }

    public static String replaceFormFeeds(String s) {
        String str = "";
        if (s != null) {
            return s.replace('\f', '\n');
        }
        return str;
    }

    public boolean isLocalEncrypted(String msg) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && CryptoMessageServiceProxy.isLocalEncrypted(msg)) {
            return true;
        }
        return false;
    }

    public boolean isNetworkEncrypted(String msg) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && CryptoMessageServiceProxy.isNetworkEncryptedConversation(msg)) {
            return true;
        }
        return false;
    }

    public String transBeforeSend(String msg, String contactNum, int subID) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !isLocalEncrypted(msg)) {
            return msg;
        }
        String tmpMsg = CryptoMessageServiceProxy.localDecrypt(msg, true);
        if (TextUtils.isEmpty(tmpMsg)) {
            return "";
        }
        StatisticalHelper.incrementReportCount(MmsApp.getApplication(), 2159);
        return CryptoMessageServiceProxy.networkEncrypt(tmpMsg, contactNum, subID);
    }

    public String transBeforeStore(String msg, int subID) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !isNetworkEncrypted(msg)) {
            return msg;
        }
        setReceivedMsgBodyEncrypted(true);
        if (AccountManager.getInstance().isCardStateActivated(subID)) {
            String tmpMsg = CryptoMessageServiceProxy.networkDecrypt(msg, subID, true);
            if (TextUtils.isEmpty(tmpMsg) || CryptoMessageServiceProxy.isLocalStoredNEMsg(tmpMsg)) {
                return tmpMsg;
            }
            return CryptoMessageServiceProxy.localEncrypt(tmpMsg, subID);
        }
        MLog.d("HwCustSmsReceiverServiceImpl", "transBeforeStore: the card is not activated");
        return CryptoMessageServiceProxy.localStoreNEMsg(msg, subID);
    }

    public boolean isCryptoRegistResponse(String msg, int subID) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !CryptoMessageServiceProxy.isNetworkEncryptoRegisterMessage(msg)) {
            return false;
        }
        CryptoMessageServiceProxy.handleEnrollMessage(msg, subID, true);
        return true;
    }

    public void setReceivedMsgBodyEncrypted(boolean encrypted) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mIsReceivedMsgBodyEncrypted = encrypted;
        }
    }

    public boolean isReceivedMsgBodyEncrypted() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            return this.mIsReceivedMsgBodyEncrypted;
        }
        return false;
    }
}
