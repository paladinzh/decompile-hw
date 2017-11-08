package com.android.contacts.calllog;

import android.net.Uri;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustContactInfoHelperImpl extends HwCustContactInfoHelper {
    private static final int DEFAULT_ELEVEN_NUMBER_MATCH = 11;
    private boolean mNumMatchProp = SystemProperties.getBoolean("ro.config.hw_calllog_numMatch", false);

    public String getNormalizedNumber(String contactNumber) {
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(contactNumber);
        if (SystemProperties.getBoolean("ro.config.hw_ContactStarMatch", false) && contactNumber != null && contactNumber.startsWith("*")) {
            return "*" + normalizedNumber;
        }
        if (specialNumberSearchRequied() && isSpecialNumber(contactNumber)) {
            return contactNumber;
        }
        if (SystemProperties.getBoolean("ro.config.hw_ContactVmMatch", false) && (PhoneNumberUtils.isVoiceMailNumber(contactNumber) || (contactNumber != null && contactNumber.startsWith("+") && contactNumber.length() <= 7))) {
            return normalizedNumber;
        }
        if (!this.mNumMatchProp) {
            return Uri.encode(normalizedNumber, "#");
        }
        int numMatch = SystemProperties.getInt("ro.config.hwft_MatchNum", 0);
        if (numMatch == 0) {
            numMatch = SystemProperties.getInt("gsm.hw.matchnum", 0);
        }
        if (11 == numMatch) {
            return normalizedNumber;
        }
        return Uri.encode(normalizedNumber, "#");
    }

    private boolean isSpecialNumber(String contactNumber) {
        return (TextUtils.isEmpty(contactNumber) || contactNumber.length() <= 0) ? false : isSpecialChar(contactNumber.charAt(0));
    }

    private boolean isSpecialChar(char ch) {
        return ch == '*' || ch == '#';
    }

    public boolean specialNumberSearchRequied() {
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }
}
