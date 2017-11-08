package com.android.contacts.model.dataitem;

import android.content.ContentValues;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class PhoneDataItem extends DataItem {
    PhoneDataItem(ContentValues values) {
        super(values);
    }

    public String getNumber() {
        return getContentValues().getAsString("data1");
    }

    public String getNormalizedNumber() {
        return getContentValues().getAsString("data4");
    }

    public String getFormattedPhoneNumber() {
        return getContentValues().getAsString("formattedPhoneNumber");
    }

    public void computeFormattedPhoneNumber(String defaultCountryIso) {
        String phoneNumber = getNumber();
        if (phoneNumber != null) {
            String formattedPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, getNormalizedNumber(), defaultCountryIso);
            if (TextUtils.isEmpty(formattedPhoneNumber) || formattedPhoneNumber.contains("null")) {
                getContentValues().put("formattedPhoneNumber", phoneNumber);
            } else {
                getContentValues().put("formattedPhoneNumber", formattedPhoneNumber);
            }
        }
    }
}
