package com.android.contacts.hap;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class HwCustCommonUtilMethods {
    public boolean isEnableSimAddPlus(boolean charAdded, boolean isDialable, boolean isCLIR) {
        return charAdded;
    }

    public boolean isHebrewLanForDialpad() {
        return false;
    }

    public int queryLastCallNumberFromCust(String number, Context context) {
        return CommonUtilMethods.queryCallNumberLastSlot(number, context);
    }

    public boolean checkAndInitCall(Context aContext, Intent aIntent) {
        return false;
    }

    public void setNameViewDirection(TextView view) {
    }
}
