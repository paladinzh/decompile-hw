package com.android.contacts;

public class HwCustPhoneCallDetailsImpl extends HwCustPhoneCallDetails {
    private boolean mIsEncryptCall;

    public void setEncryptCall(boolean isEncryptCall) {
        this.mIsEncryptCall = isEncryptCall;
    }

    public boolean isEncryptCall() {
        return this.mIsEncryptCall;
    }
}
