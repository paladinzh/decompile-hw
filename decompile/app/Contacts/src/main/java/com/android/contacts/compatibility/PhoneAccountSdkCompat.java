package com.android.contacts.compatibility;

import android.os.Bundle;
import android.telecom.PhoneAccount;

public class PhoneAccountSdkCompat {
    public static Bundle getExtras(PhoneAccount account) {
        return account.getExtras();
    }
}
