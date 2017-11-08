package com.android.contacts.hap.numbermark;

import android.telephony.PhoneNumberUtils;

public class PhoneMatch {
    private boolean mPhoneExactMatch = true;
    private String number;

    public PhoneMatch(String aNumber) {
        if (aNumber != null) {
            this.number = PhoneNumberUtils.stripSeparators(aNumber);
            if (this.number.length() >= 10) {
                this.number = this.number.substring(this.number.length() - 10);
                this.mPhoneExactMatch = false;
            } else {
                this.mPhoneExactMatch = true;
            }
        }
    }

    public String getMatchPhone() {
        return this.number;
    }
}
