package com.huawei.mms.util;

import android.telephony.PhoneNumberUtils;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;

public class HwRecipientUtils {
    public static boolean isValidAddress(String number, boolean isMms) {
        boolean z = true;
        if (MessageUtils.isEmptyPhoneNumber(number) || MmsConfig.filteNumberByLocal(number).length() < MmsConfig.getEnableSendMmsNumMinLength()) {
            return false;
        }
        if (isMms) {
            return MessageUtils.isValidMmsAddress(number);
        }
        if (MmsConfig.isUseGgSmsAddressCheck()) {
            if (!PhoneNumberUtils.isWellFormedSmsAddress(number)) {
                z = Contact.isEmailAddress(number);
            }
            return z;
        }
        if (!isWellFormedSmsAddress(number)) {
            z = Contact.isEmailAddress(number);
        }
        return z;
    }

    private static boolean isWellFormedSmsAddress(String address) {
        address = MmsConfig.filteNumberByLocal(address);
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!PhoneNumberUtils.isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isComplexInvalidRecipient(String number) {
        if (number.contains("<") && number.contains(">")) {
            number = number.substring(number.indexOf("<") + 1, number.indexOf(">"));
        }
        number = number.trim();
        int len = number.length();
        int i = 0;
        while (i < len) {
            char c = number.charAt(i);
            if (c < '0' || c > '9') {
                if (MmsConfig.isStrictAddrCharSet()) {
                    if (!(c == ' ' || c == '-')) {
                        if (c == '+' && i == 0) {
                        }
                    }
                } else if (!(c == ' ' || c == '-' || c == '(' || c == ')' || c == '.' || c == '+' || c == '#' || c == '*')) {
                }
                return true;
            }
            i++;
        }
        return false;
    }

    public static boolean isInvalidRecipient(String number, boolean isMms) {
        if (isValidAddress(number, isMms) || (MmsConfig.getEmailGateway() != null && MessageUtils.isAlias(number))) {
            return false;
        }
        return true;
    }
}
