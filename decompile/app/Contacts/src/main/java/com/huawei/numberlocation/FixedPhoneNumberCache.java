package com.huawei.numberlocation;

import android.content.Context;

public class FixedPhoneNumberCache extends FixedPhoneNumber {
    String number;

    public FixedPhoneNumberCache(Context ctx, String numberString) {
        super(ctx, numberString);
        this.number = numberString;
    }

    public String substringFixedPhoneNumber() {
        if (!parseFixedPhoneNumber()) {
            return this.number;
        }
        String resultString = this.number;
        if (!(getParseResult() == null || this.areaCodeString == null)) {
            resultString = this.number.substring(this.areaCodeString.length());
        }
        return resultString;
    }
}
