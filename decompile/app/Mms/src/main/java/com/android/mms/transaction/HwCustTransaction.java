package com.android.mms.transaction;

import android.content.Context;
import java.io.IOException;

public class HwCustTransaction {
    public boolean useWifi(Context context) {
        return false;
    }

    public byte[] getPduInWifi(String url, TransactionSettings mTransactionSettings) throws IOException {
        return new byte[0];
    }

    public byte[] setPduInWifi(long token, byte[] pdu, String mmscUrl, TransactionSettings mTransactionSettings) throws IOException {
        return new byte[0];
    }

    public void ensureRouteToHostInWifi(String url) {
    }
}
