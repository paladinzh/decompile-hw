package com.android.contacts.calllog;

import android.database.Cursor;
import com.android.contacts.HwCustPhoneCallDetails;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;

public class HwCustCallLogDetailHelperImpl extends HwCustCallLogDetailHelper {
    public void updateEncryptCall(Cursor callCursor, PhoneCallDetails lTempPhoneDetails) {
        boolean z = true;
        if (EncryptCallUtils.isEncryptCallEnable()) {
            int encryptCallIndex = callCursor.getColumnIndex("encrypt_call");
            if (encryptCallIndex >= 0) {
                int encryptCall = callCursor.getInt(encryptCallIndex);
                if (lTempPhoneDetails.getHwCust() != null) {
                    HwCustPhoneCallDetails hwCust = lTempPhoneDetails.getHwCust();
                    if (encryptCall != 1) {
                        z = false;
                    }
                    hwCust.setEncryptCall(z);
                }
            }
        }
    }

    public String[] updateCallLogProjectForEncrypt(String[] hAP_OR_BASE_CALL_LOG_PROJECTION) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
            return hAP_OR_BASE_CALL_LOG_PROJECTION;
        }
        String[] temp = new String[(hAP_OR_BASE_CALL_LOG_PROJECTION.length + 1)];
        System.arraycopy(hAP_OR_BASE_CALL_LOG_PROJECTION, 0, temp, 0, hAP_OR_BASE_CALL_LOG_PROJECTION.length);
        temp[hAP_OR_BASE_CALL_LOG_PROJECTION.length] = "encrypt_call";
        return temp;
    }
}
