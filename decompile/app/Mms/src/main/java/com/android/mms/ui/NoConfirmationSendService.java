package com.android.mms.ui;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.SmsMessageSender;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsNoConfirmationSendService;
import com.huawei.cspcommon.MLog;

public class NoConfirmationSendService extends IntentService {
    private RcsNoConfirmationSendService mCust;

    public NoConfirmationSendService() {
        super(NoConfirmationSendService.class.getName());
        setIntentRedelivery(true);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mCust == null) {
            this.mCust = new RcsNoConfirmationSendService();
        }
    }

    protected void onHandleIntent(Intent intent) {
        ComposeMessageFragment.log("NoConfirmationSendService onHandleIntent");
        if (MmsConfig.isSmsEnabled(this)) {
            String action = intent.getAction();
            if ("android.intent.action.RESPOND_VIA_MESSAGE".equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    ComposeMessageFragment.log("Called to send SMS but no extras");
                    return;
                }
                String message = extras.getString("android.intent.extra.TEXT");
                int subscription = 0;
                if (MessageUtils.isMultiSimEnabled()) {
                    if (MessageUtils.isCTCdmaCardInGsmMode()) {
                        subscription = 0;
                    } else {
                        subscription = MessageUtils.getSimIdFromIntent(intent, 0);
                    }
                }
                String recipients = Conversation.getRecipients(intent.getData());
                if (TextUtils.isEmpty(recipients)) {
                    ComposeMessageFragment.log("Recipient(s) cannot be empty");
                    return;
                }
                if (extras.getBoolean("showUI", false)) {
                    intent.setClassName(this, "com.android.mms.ui.ComposeMessageActivityNoLockScreen");
                    intent.addFlags(268435456);
                    startActivity(intent);
                } else if (TextUtils.isEmpty(message)) {
                    ComposeMessageFragment.log("Message cannot be empty");
                    return;
                } else {
                    String[] dests = TextUtils.split(recipients, ";");
                    if (this.mCust == null || !this.mCust.isCanSendIm(dests)) {
                        SmsMessageSender smsMessageSender;
                        if (MessageUtils.isMultiSimEnabled()) {
                            smsMessageSender = new SmsMessageSender(this, dests, message, 0, subscription);
                        } else {
                            SmsMessageSender smsMessageSender2 = new SmsMessageSender(this, dests, message, 0, MessageUtils.getPreferredSmsSubscription());
                        }
                        try {
                            smsMessageSender.sendMessage(0);
                        } catch (Throwable e) {
                            MLog.e("Mms/NoConfirmationSendService", "Failed to send SMS message, threadId=" + 0, e);
                        }
                    } else {
                        this.mCust.sendImFirst(message, dests[0], getApplicationContext());
                        return;
                    }
                }
                return;
            }
            ComposeMessageFragment.log("NoConfirmationSendService onHandleIntent wrong action: " + action);
            return;
        }
        ComposeMessageFragment.log("NoConfirmationSendService is not the default sms app");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
