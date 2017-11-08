package com.android.mms.data;

import com.android.mms.HwCustMmsConfigImpl;

public class HwCustContactImpl extends HwCustContact {
    public boolean isPoundCharValid() {
        return HwCustMmsConfigImpl.isPoundCharValid();
    }

    public String setMatchNumber(String normalizedNumber, String number) {
        return number;
    }
}
