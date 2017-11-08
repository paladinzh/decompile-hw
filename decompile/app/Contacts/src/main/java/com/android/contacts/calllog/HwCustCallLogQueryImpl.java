package com.android.contacts.calllog;

import android.util.Log;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;

public class HwCustCallLogQueryImpl extends HwCustCallLogQuery {
    private String TAG = "HwCustCallLogQueryImpl";

    public void updateHapProjectionForEncryptCall(String[] hAP_PROJECTION, int current_column_index) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            int current_column_index2 = current_column_index + 1;
            hAP_PROJECTION[current_column_index] = "encrypt_call";
            Log.i(this.TAG, "current_column_index=" + current_column_index2);
        }
    }

    public int updateCurrentColumnIndexForEncryptCall(int current_column_index) {
        if (EncryptCallUtils.isEncryptCallEnable()) {
            return current_column_index + 1;
        }
        return current_column_index;
    }
}
