package com.android.mms.transaction;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import java.util.ArrayList;

public class HwCustSmsSingleRecipientSender {
    private static String TAG = "HwCustSmsSingleRecipientSender";

    public HwCustSmsSingleRecipientSender(Context context) {
    }

    public ArrayList<String> hwCustDevideMessage(String messageBody) {
        Log.v(TAG, "devide sms message body as sprint requirement , and do nothing now.");
        return null;
    }

    public boolean hwCustSendSmsMessage(SmsManager smsManager, String destinationAddress, String scAddress, ArrayList<String> arrayList, ArrayList<PendingIntent> arrayList2, ArrayList<PendingIntent> arrayList3) {
        return false;
    }

    public String hwCustDestPlusCodeHandle(String number) {
        return number;
    }
}
