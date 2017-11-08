package com.android.mms.transaction;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsMessage;

public class HwCustSmsReceiverService {
    public void setHwCustSmsReceiverService(Context context) {
    }

    public void handleMessageReceived(String action, Intent aIntent, Handler handler) {
    }

    public boolean isDiscardSms(String messageBody) {
        return false;
    }

    public void handleMessageFailedToSend(Context context, Uri uri, int error, Handler mToastHandler) {
    }

    public Uri hwCustStoreMessage(Context context, ContentResolver resolver, Uri uri, ContentValues cv) {
        return null;
    }

    public boolean isNeedShowMultiMessage(Uri insertedUri) {
        return false;
    }

    public boolean isDiscardSMSFrom3311(SmsMessage message) {
        return false;
    }

    public long getReceivedTime(long aTimeNow, long aServerTime) {
        return aTimeNow;
    }
}
