package com.android.mms.transaction;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;

public class MessageStatusService extends IntentService {
    private static final String[] ID_PROJECTION = new String[]{"_id"};
    private static final Uri STATUS_URI = Uri.parse("content://sms/status");

    public MessageStatusService() {
        super(MessageStatusService.class.getName());
        setIntentRedelivery(true);
    }

    protected void onHandleIntent(Intent intent) {
        if (MmsConfig.isSmsEnabled(this)) {
            Uri messageUri = intent.getData();
            byte[] pdu = intent.getByteArrayExtra("pdu");
            if (pdu == null) {
                error("pdu is null, error intent");
                return;
            }
            SmsMessage message = updateMessageStatus(this, messageUri, pdu, intent.getStringExtra("format"));
            if (message != null && message.getStatus() < 32) {
                MessagingNotification.blockingUpdateStatusMessage(getApplicationContext(), message.isStatusReportMessage(), messageUri);
            }
            return;
        }
        MLog.d("MessageStatusReceiver", "MessageStatusService: is not the default sms app");
    }

    private SmsMessage updateMessageStatus(Context context, Uri messageUri, byte[] pdu, String format) {
        SmsMessage message = SmsMessage.createFromPdu(pdu, format);
        if (message == null) {
            return null;
        }
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), messageUri, ID_PROJECTION, null, null, null);
        if (cursor == null) {
            return message;
        }
        try {
            if (cursor.moveToFirst()) {
                Uri updateUri = ContentUris.withAppendedId(STATUS_URI, (long) cursor.getInt(0));
                int status = message.getStatus();
                boolean isStatusReport = message.isStatusReportMessage();
                if (2 == MmsApp.getDefaultTelephonyManager().getPhoneType()) {
                    status >>= 16;
                    int errClass = status >> 8;
                    int errCause = status & 255;
                    MLog.d("updateMessageStatus", "CDMA sms status errClass = " + errClass + ", " + "errCause = " + errCause);
                    if (errClass == 0) {
                        if (3 == errCause) {
                            status = 64;
                        } else {
                            status = 0;
                        }
                    } else if (2 == errClass) {
                        status = 64;
                    } else if (3 == errClass) {
                        status = 64;
                    } else {
                        MLog.e("updateMessageStatus", "Unkonw status: " + status);
                        cursor.close();
                        return message;
                    }
                }
                ContentValues contentValues = new ContentValues(2);
                if (MLog.isLoggable(HwCustUpdateUserBehaviorImpl.MMS, 3)) {
                    log("updateMessageStatus: msgUrl=" + messageUri + ", status=" + status + ", isStatusReport=" + isStatusReport);
                }
                contentValues.put("status", Integer.valueOf(status));
                long time = System.currentTimeMillis();
                contentValues.put("date_sent", Long.valueOf(time));
                log("mms update message status time: " + time);
                SqliteWrapper.update(context, context.getContentResolver(), updateUri, contentValues, null, null);
            } else {
                error("Can't find message for status update ");
            }
            cursor.close();
            return message;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private void error(String message) {
        MLog.e("MessageStatusReceiver", "[MessageStatusReceiver] " + message);
    }

    private void log(String message) {
        MLog.d("MessageStatusReceiver", "[MessageStatusReceiver] " + message);
    }
}
