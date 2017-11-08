package com.android.mms.ui;

public class HwCustBaseConversationListFragment {
    public int getNumberTypeForServiceMessageArchival(String aRecipient, int aDefaultNumberType) {
        return aDefaultNumberType;
    }

    public boolean isServiceMessageEnabled() {
        return false;
    }
}
