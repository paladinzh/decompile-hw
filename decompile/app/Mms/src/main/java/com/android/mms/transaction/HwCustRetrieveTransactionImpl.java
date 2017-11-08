package com.android.mms.transaction;

import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.util.HwCustUiUtils;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.AcknowledgeInd;
import com.huawei.cspcommon.MLog;

public class HwCustRetrieveTransactionImpl extends HwCustRetrieveTransaction {
    private static final String TAG = "HwCustRetrieveTransactionImpl";

    public void sendAcknowledgeInd(AcknowledgeInd acknowledgeInd) {
        if (HwCustMmsConfigImpl.isEnableReportAllowed()) {
            try {
                MLog.d(TAG, "sendAcknowledgeInd sprint sendAcknowledgeInd reportAllowed is yes");
                acknowledgeInd.setReportAllowed(128);
            } catch (InvalidHeaderValueException e) {
                MLog.e(TAG, "sendAcknowledgeInd sprint acknowledgeInd.setReportAllowed Failed !!");
            }
        }
    }

    public boolean isLocalReceivedDate(long aTimeNow, long aServerTime) {
        return !HwCustMmsConfigImpl.isEnableLocalTime() ? HwCustUiUtils.isLocalTimeRight(aTimeNow, aServerTime) : true;
    }
}
