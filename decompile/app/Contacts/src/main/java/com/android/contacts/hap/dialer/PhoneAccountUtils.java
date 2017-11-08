package com.android.contacts.hap.dialer;

import android.content.ComponentName;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;

public class PhoneAccountUtils {
    public static PhoneAccountHandle getAccount(String componentString, String accountId) {
        if (TextUtils.isEmpty(componentString) || TextUtils.isEmpty(accountId)) {
            return null;
        }
        return new PhoneAccountHandle(ComponentName.unflattenFromString(componentString), accountId);
    }
}
