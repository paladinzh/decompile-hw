package com.android.mms.transaction;

public class HwCustMMSProxyDetails {
    public String mProxyAddress;
    public int mProxyPort = -1;
    public String mServiceCenter;

    public HwCustMMSProxyDetails(String aServiceCenter, String aProxyAddress, int aProxyPort) {
        this.mServiceCenter = aServiceCenter;
        this.mProxyAddress = aProxyAddress;
        this.mProxyPort = aProxyPort;
    }
}
