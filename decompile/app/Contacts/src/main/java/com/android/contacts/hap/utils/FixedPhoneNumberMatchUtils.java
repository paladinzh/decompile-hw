package com.android.contacts.hap.utils;

import android.content.Context;
import com.android.contacts.ContactsUtils;
import com.huawei.numberlocation.FixedPhoneNumberCache;

public class FixedPhoneNumberMatchUtils {
    public static String parseFixedPhoneNumber(Context ctx, String number) {
        if (number == null || number.length() == 0) {
            return number;
        }
        String resultString;
        String tempNum = ContactsUtils.removeDashesAndBlanks(number);
        String result = new FixedPhoneNumberCache(ctx, tempNum).substringFixedPhoneNumber();
        if (tempNum.equals(result)) {
            resultString = number;
        } else {
            resultString = result;
        }
        return resultString;
    }
}
