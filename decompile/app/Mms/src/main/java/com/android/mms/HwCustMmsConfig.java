package com.android.mms;

public class HwCustMmsConfig {
    public int getMccMncMmsExpiry(int lMmsExpiry) {
        return lMmsExpiry;
    }

    public boolean getCustDefaultMMSDeliveryReports(boolean defaultMMSDeliveryReports) {
        return defaultMMSDeliveryReports;
    }

    public boolean custEnableForwardMessageFrom(boolean defaultValue) {
        return defaultValue;
    }

    public boolean isNotifyMsgtypeChangeEnable(boolean defaultValue) {
        return defaultValue;
    }

    public int getCustRecipientLimit(boolean isMms, int defaultValue) {
        return defaultValue;
    }

    public boolean isRefreshRxNumByMccMnc(boolean defaultValue) {
        return defaultValue;
    }

    public int getMccMncMmsRetrive(int lDefaultAutoRetrievalMms) {
        return lDefaultAutoRetrievalMms;
    }

    public boolean isSupportSubjectForSimpleUI() {
        return false;
    }

    public int getCustConfigForDeliveryReports(int defaultDeliveryReports) {
        return defaultDeliveryReports;
    }

    public boolean getCustConfigForMmsReadReports(boolean defaultMmsReadReports) {
        return defaultMmsReadReports;
    }

    public boolean getCustConfigForMmsReplyReadReports(boolean defaultMmsReplyReadReports) {
        return defaultMmsReplyReadReports;
    }
}
