package com.android.mms.ui;

import com.android.mms.HwCustMmsConfigImpl;

public class HwCustEditTextWithSmileyImpl extends HwCustEditTextWithSmiley {
    public boolean isDisableSmileyInputConnection() {
        return HwCustMmsConfigImpl.isDisableSmileyInputConnection();
    }
}
