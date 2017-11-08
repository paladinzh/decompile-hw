package com.android.mms.transaction;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;
import java.util.ArrayList;

public class SmsSingleRecipientSender extends SmsMessageSender {
    private String mDest;
    private HwCustSmsSingleRecipientSender mHwCustSmsSingleRecipientSender = null;
    private final boolean mRequestDeliveryReport;
    private Uri mUri;

    public SmsSingleRecipientSender(Context context, String dest, String msgText, long threadId, boolean requestDeliveryReport, Uri uri, int subId) {
        super(context, null, msgText, threadId, subId);
        this.mHwCustSmsSingleRecipientSender = (HwCustSmsSingleRecipientSender) HwCustUtils.createObj(HwCustSmsSingleRecipientSender.class, new Object[]{context});
        this.mRequestDeliveryReport = requestDeliveryReport;
        this.mDest = MessageUtils.getAdjustedSpecialNumber(dest, subId);
        this.mUri = uri;
    }

    public boolean sendMessage(long token) throws MmsException {
        MLog.v("Mms_TXS_SRSender", "sendMessage token: " + token);
        if (this.mMessageText == null) {
            throw new MmsException("Null message body or have multiple destinations.");
        }
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList arrayList = null;
        if (MmsConfig.getEmailGateway() == null || !(Contact.isEmailAddress(this.mDest) || MessageUtils.isAlias(this.mDest))) {
            if (this.mHwCustSmsSingleRecipientSender != null) {
                arrayList = this.mHwCustSmsSingleRecipientSender.hwCustDevideMessage(this.mMessageText);
            }
            if (arrayList == null) {
                arrayList = smsManager.divideMessage(this.mMessageText);
            }
            this.mDest = PhoneNumberUtils.stripSeparators(this.mDest);
            this.mDest = Conversation.verifySingleRecipient(this.mContext, this.mThreadId, this.mDest);
            if (this.mHwCustSmsSingleRecipientSender != null) {
                this.mDest = this.mHwCustSmsSingleRecipientSender.hwCustDestPlusCodeHandle(this.mDest);
            }
        } else {
            String msgText = this.mDest + " " + this.mMessageText;
            this.mDest = MmsConfig.getEmailGateway();
            if (this.mHwCustSmsSingleRecipientSender != null) {
                arrayList = this.mHwCustSmsSingleRecipientSender.hwCustDevideMessage(msgText);
            }
            if (arrayList == null) {
                arrayList = smsManager.divideMessage(msgText);
            }
        }
        int messageCount = arrayList.size();
        if (messageCount == 0) {
            throw new MmsException("SmsMessageSender.sendMessage: divideMessage returned empty messages. Original message is \"" + this.mMessageText + "\"");
        } else if (Sms.moveMessageToFolder(this.mContext, this.mUri, 4, 0)) {
            MLog.v("Mms_TXS_SRSender", "sendMessage requestDeliveryReport : " + this.mRequestDeliveryReport);
            ArrayList<PendingIntent> deliveryIntents = new ArrayList(messageCount);
            ArrayList<PendingIntent> sentIntents = new ArrayList(messageCount);
            int i = 0;
            while (i < messageCount) {
                if (this.mRequestDeliveryReport && i == messageCount - 1) {
                    deliveryIntents.add(PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED", this.mUri, this.mContext, MessageStatusReceiver.class), 0));
                } else {
                    deliveryIntents.add(null);
                }
                Intent intent = new Intent("com.android.mms.transaction.MESSAGE_SENT", this.mUri, this.mContext, SmsReceiver.class);
                int requestCode = 0;
                int flag = 0;
                if (i == messageCount - 1) {
                    requestCode = 1;
                    intent.putExtra("SendNextMsg", true);
                    flag = 134217728;
                }
                sentIntents.add(PendingIntent.getBroadcast(this.mContext, requestCode, intent, flag));
                i++;
            }
            try {
                if (MessageUtils.isMultiSimEnabled()) {
                    MessageUtils.sendMultipartTextMessage(this.mDest, this.mServiceCenter, arrayList, sentIntents, deliveryIntents, this.mSubId);
                } else if (this.mHwCustSmsSingleRecipientSender == null || !this.mHwCustSmsSingleRecipientSender.hwCustSendSmsMessage(smsManager, this.mDest, this.mServiceCenter, arrayList, sentIntents, deliveryIntents)) {
                    smsManager.sendMultipartTextMessage(this.mDest, this.mServiceCenter, arrayList, sentIntents, deliveryIntents);
                }
                if (MLog.isLoggable("Mms_TXN", 2)) {
                    log("sendMessage: threadId=" + this.mThreadId + ", msgs.count=" + messageCount);
                }
                return false;
            } catch (Exception ex) {
                MLog.e("Mms_TXS_SRSender", "SmsMessageSender.sendMessage: has exception ", (Throwable) ex);
                throw new MmsException("SmsMessageSender.sendMessage: caught " + ex + " from MSimSmsManager.sendTextMessage()");
            }
        } else {
            throw new MmsException("SmsMessageSender.sendMessage: couldn't move message to outbox: " + this.mUri);
        }
    }

    private void log(String msg) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "[SmsSingleRecipientSender] " + msg);
    }
}
