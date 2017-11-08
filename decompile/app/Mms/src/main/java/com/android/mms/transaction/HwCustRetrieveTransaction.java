package com.android.mms.transaction;

import com.google.android.mms.pdu.AcknowledgeInd;

public class HwCustRetrieveTransaction {
    public void sendAcknowledgeInd(AcknowledgeInd acknowledgeInd) {
    }

    public boolean isLocalReceivedDate(long aTimeNow, long aServerTime) {
        return true;
    }
}
