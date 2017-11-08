package com.android.mms.transaction;

import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.util.HwCustUiUtils;

public class HwCustNotificationTransactionImpl extends HwCustNotificationTransaction {
    private static final String TAG = "HwCustNotificationTransactionImpl";

    public boolean isLocalReceivedDate(long aTimeNow, long aServerTime) {
        return !HwCustMmsConfigImpl.isEnableLocalTime() ? HwCustUiUtils.isLocalTimeRight(aTimeNow, aServerTime) : true;
    }
}
