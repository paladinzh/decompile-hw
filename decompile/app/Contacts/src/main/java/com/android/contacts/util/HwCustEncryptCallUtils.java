package com.android.contacts.util;

import android.content.Context;
import android.content.Intent;

public class HwCustEncryptCallUtils {
    public void init(Context context) {
    }

    public boolean isEncryptCallEnable() {
        return false;
    }

    public boolean isEncryptCallEnable(Context context) {
        return false;
    }

    public boolean isCallCard1Encrypt() {
        return false;
    }

    public boolean isCallCard2Encrypt() {
        return false;
    }

    public void buildEncryptIntent(Intent intent) {
    }

    public int getCallDetailHistoryItemLayout(int resourceID) {
        return resourceID;
    }

    public int getCalllogMultiSelectItemLayout(int resourceID) {
        return resourceID;
    }

    public int getCallLogLisItemLayout(int resourceID) {
        return resourceID;
    }
}
