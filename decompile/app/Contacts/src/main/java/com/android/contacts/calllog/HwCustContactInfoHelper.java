package com.android.contacts.calllog;

import android.net.Uri;
import android.telephony.PhoneNumberUtils;

public class HwCustContactInfoHelper {
    public String getNormalizedNumber(String contactNumber) {
        return Uri.encode(PhoneNumberUtils.normalizeNumber(contactNumber), "#");
    }
}
