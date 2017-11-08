package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.constant.Constant;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.HwCustMessagingNotification;
import com.android.mms.transaction.HwCustMessagingNotificationImpl;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwBaseActivity;
import java.util.ArrayList;

public class ClassZeroActivity extends HwBaseActivity {
    private static final String[] REPLACE_PROJECTION = new String[]{"_id", "address", "protocol"};
    private final OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            if (MmsConfig.getEnableChangeClassZeroMessageShow()) {
                MessagingNotification.cancelNotification(ClassZeroActivity.this, HwCustMessagingNotificationImpl.NOTIFICATION_CLASS_ZERO_ID);
            }
            ClassZeroActivity.this.processNextMessage();
        }
    };
    private AlertDialog mDialog = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ClassZeroActivity.this.mRead = false;
                ClassZeroActivity.this.mDialog.dismiss();
                ClassZeroActivity.this.saveMessage();
                ClassZeroActivity.this.processNextMessage();
            }
        }
    };
    private HwCustMessagingNotification mHwCustMessagingNotification = null;
    private ArrayList<SmsMessage[]> mMessageQueue = null;
    private SmsMessage[] mMessages = null;
    private boolean mRead = false;
    private final OnClickListener mSaveListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            ClassZeroActivity.this.mRead = true;
            ClassZeroActivity.this.saveMessage();
            dialog.dismiss();
            if (MmsConfig.getEnableChangeClassZeroMessageShow()) {
                MessagingNotification.cancelNotification(ClassZeroActivity.this, HwCustMessagingNotificationImpl.NOTIFICATION_CLASS_ZERO_ID);
            }
            ClassZeroActivity.this.processNextMessage();
        }
    };
    private long mTimerSet = 0;

    private boolean queueMsgFromIntent(Intent msgIntent) {
        SmsMessage[] rawMessage = Intents.getMessagesFromIntent(msgIntent);
        if (TextUtils.isEmpty(getMessageBody(rawMessage))) {
            if (this.mMessageQueue.size() == 0) {
                finish();
            }
            MLog.i("display_00", "Zero message is empty in queueMsgFromIntent(),return false.");
            return false;
        }
        this.mMessageQueue.add(rawMessage);
        return true;
    }

    private void processNextMessage() {
        if (this.mMessageQueue.size() != 0) {
            this.mMessageQueue.remove(0);
        }
        if (this.mMessageQueue.size() == 0) {
            finish();
        } else {
            displayZeroMessage((SmsMessage[]) this.mMessageQueue.get(0));
        }
    }

    private void saveMessage() {
        Uri messageUri;
        if (this.mMessages[0].isReplace()) {
            messageUri = replaceMessage(this.mMessages);
        } else {
            messageUri = storeMessage(this.mMessages);
        }
        if (!this.mRead && messageUri != null) {
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, -1, false);
        }
    }

    protected void onNewIntent(Intent msgIntent) {
        super.onNewIntent(msgIntent);
        queueMsgFromIntent(msgIntent);
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (isCall() && inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(524288);
        }
        this.mHwCustMessagingNotification = (HwCustMessagingNotification) HwCustUtils.createObj(HwCustMessagingNotification.class, new Object[0]);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawableResource(R.drawable.class_zero_background);
        if (getIntent() == null) {
            MLog.w("display_00", "getIntent() is null in onCreate(),return it.");
            finish();
            return;
        }
        if (this.mMessageQueue == null) {
            this.mMessageQueue = new ArrayList();
        }
        if (queueMsgFromIntent(getIntent())) {
            if (this.mMessageQueue.size() == 1) {
                displayZeroMessage((SmsMessage[]) this.mMessageQueue.get(0));
            }
            if (icicle != null) {
                this.mTimerSet = icicle.getLong("timer_fire", this.mTimerSet);
            }
        }
    }

    private boolean isCall() {
        int callState = 0;
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        if (tm != null) {
            callState = tm.getCallState();
        }
        if (callState != 0) {
            return true;
        }
        return false;
    }

    private boolean inKeyguardRestrictedInputMode() {
        KeyguardManager km = (KeyguardManager) getSystemService("keyguard");
        if (km != null) {
            return km.inKeyguardRestrictedInputMode();
        }
        return false;
    }

    private void displayZeroMessage(SmsMessage[] rawMessage) {
        String message = getMessageBody(rawMessage);
        this.mMessages = rawMessage;
        this.mDialog = new Builder(this).setMessage(message).setPositiveButton(R.string.save, this.mSaveListener).setNegativeButton(17039360, this.mCancelListener).setCancelable(false).show();
        this.mTimerSet = Constant.FIVE_MINUTES + SystemClock.uptimeMillis();
    }

    protected void onStart() {
        super.onStart();
        if (this.mTimerSet <= SystemClock.uptimeMillis()) {
            this.mHandler.sendEmptyMessage(1);
        } else {
            this.mHandler.sendEmptyMessageAtTime(1, this.mTimerSet);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timer_fire", this.mTimerSet);
    }

    private String getDisplayMessageBody(SmsMessage[] smsMessages) {
        StringBuffer message = new StringBuffer();
        for (SmsMessage displayMessageBody : smsMessages) {
            message.append(displayMessageBody.getDisplayMessageBody());
        }
        return message.toString();
    }

    private String getMessageBody(SmsMessage[] smsMessages) {
        StringBuffer message = new StringBuffer();
        for (SmsMessage messageBody : smsMessages) {
            message.append(messageBody.getMessageBody());
        }
        return message.toString();
    }

    protected void onStop() {
        super.onStop();
        this.mHandler.removeMessages(1);
    }

    protected void onDestroy() {
        if (MmsConfig.getEnableChangeClassZeroMessageShow()) {
            MessagingNotification.cancelNotification(getApplicationContext(), HwCustMessagingNotificationImpl.NOTIFICATION_CLASS_ZERO_ID);
        }
        super.onDestroy();
    }

    private ContentValues extractContentValues(SmsMessage sms) {
        int i;
        int i2 = 1;
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("read", Integer.valueOf(this.mRead ? 1 : 0));
        String str = "seen";
        if (this.mRead) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        if (sms.getPseudoSubject().length() > 0) {
            values.put("subject", sms.getPseudoSubject());
        }
        String str2 = "reply_path_present";
        if (!sms.isReplyPathPresent()) {
            i2 = 0;
        }
        values.put(str2, Integer.valueOf(i2));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private Uri replaceMessage(SmsMessage[] sms) {
        ContentValues values = extractContentValues(sms[0]);
        values.put("body", getMessageBody(sms));
        ContentResolver resolver = getContentResolver();
        String originatingAddress = sms[0].getOriginatingAddress();
        int protocolIdentifier = sms[0].getProtocolIdentifier();
        String[] selectionArgs = new String[]{originatingAddress, Integer.toString(protocolIdentifier)};
        Cursor cursor = SqliteWrapper.query(this, resolver, Inbox.CONTENT_URI, REPLACE_PROJECTION, "address = ? AND protocol = ?", selectionArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    Uri messageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, cursor.getLong(0));
                    SqliteWrapper.update(this, resolver, messageUri, values, null, null);
                    return messageUri;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return storeMessage(sms);
    }

    private Uri storeMessage(SmsMessage[] sms) {
        ContentValues values = extractContentValues(sms[0]);
        Long threadId = values.getAsLong("thread_id");
        if (threadId == null) {
            threadId = Long.valueOf(0);
        }
        String address = values.getAsString("address");
        boolean isServerAddr = MessageUtils.isServerAddress(address);
        if (TextUtils.isEmpty(address)) {
            address = getString(R.string.unknown_sender);
            values.put("address", address);
        } else {
            Contact cacheContact = Contact.get(address, true);
            if (cacheContact != null) {
                address = cacheContact.getNumber();
            }
        }
        if ((threadId == null || threadId.longValue() == 0) && address != null) {
            values.put("thread_id", Long.valueOf(Conversation.getOrCreateThreadId(this, address, isServerAddr)));
        }
        values.put("body", getDisplayMessageBody(sms));
        values.put("sub_id", Integer.valueOf(MessageUtils.getSubId(sms[0])));
        values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(MessageUtils.getSubId(sms[0]))));
        return SqliteWrapper.insert(this, getContentResolver(), Inbox.CONTENT_URI, values);
    }
}
