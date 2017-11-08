package com.android.mms.transaction;

import android.content.Context;

public class HwCustSmsMessageSender {
    public String getSMSCAddress(int subID) {
        return null;
    }

    public boolean supportSendToEmail() {
        return false;
    }

    public void queueMessage(int mNumberOfDests, String[] mDests, Context mContext, String timeAddressPos, long mTimestamp, boolean requestDeliveryReport, long mThreadId, int mSubId, long groupIdTemp, String bodyAddressPos, String mMessageText, String address) {
    }

    public String getCustReplaceSmsCenterNumber(int subID) {
        return null;
    }
}
