package com.huawei.netassistant.wifisecure;

public class WifiDetectResult {
    public static final int BIT_ARP_FAKE = 4;
    public static final int BIT_DNS_FAKE = 1;
    public static final int BIT_PHISHING_FAKE = 2;
    public static final int BIT_SECURE = 0;
    public static final int R_ARP_FAKE = 2;
    public static final int R_ARP_OK = 1;
    public static final int R_D_P_DNS_FAKE = 2;
    public static final int R_D_P_NETWORK_ERROR = 7;
    public static final int R_D_P_NO_FAKE = 1;
    public static final int R_D_P_PHISHING_FAKE = 3;
    public static final int R_ERROR = 15;
    public static final int R_INVALID = 0;
    public static final int R_NET_AVILABLE = 1;
    public static final int R_NET_NOTAVILABLE = 2;
    public static final int R_NET_NOTAVILABLE_APPROVE = 3;
    private int mArpResult = 0;
    private int mDPResult = 0;
    private int mNetResult = 0;

    public void setDnsAndPhishingResult(int nResult) {
        this.mDPResult = nResult;
    }

    public void setArpResult(int nResult) {
        this.mArpResult = nResult;
    }

    public void setNetworkStateResult(int nResult) {
        this.mNetResult = nResult;
    }

    public int getDnsAndPhishingResult() {
        return this.mDPResult;
    }

    public int getArpResult() {
        return this.mArpResult;
    }

    public int getNetworkStateResult() {
        return this.mNetResult;
    }

    public boolean isNetworkAvailable() {
        return 1 == getNetworkStateResult();
    }

    public boolean isDnsFake() {
        return 2 == getDnsAndPhishingResult();
    }

    public boolean isPhishingFake() {
        return 3 == getDnsAndPhishingResult();
    }

    public boolean isArpFake() {
        return 2 == getArpResult();
    }

    public boolean isSecure() {
        return 1 == this.mDPResult;
    }

    public boolean isDetectFinished() {
        return getDnsAndPhishingResult() != 0;
    }

    public int getUpdateCheckResult(int nLastResult) {
        int nBitDP = 0;
        switch (this.mDPResult) {
            case 1:
                break;
            case 2:
                nBitDP = 1;
                break;
            case 3:
                nBitDP = 2;
                break;
            default:
                nBitDP = nLastResult & 3;
                break;
        }
        int nBitArp = 0;
        switch (this.mArpResult) {
            case 1:
                break;
            case 2:
                nBitArp = 4;
                break;
            default:
                nBitArp = nLastResult & 4;
                break;
        }
        return nBitDP | nBitArp;
    }

    public void reset() {
        this.mDPResult = 0;
        this.mArpResult = 0;
        this.mNetResult = 0;
    }
}
