package com.android.mms.transaction;

import android.content.Context;

public class HwCustTransactionService {
    public boolean mmsOverWifiEnabled(Context context) {
        return false;
    }

    public void update() {
    }

    public boolean mmsUseWifi() {
        return false;
    }

    public void setWifiDisconnect() {
    }

    public int getWifiState() {
        return 0;
    }

    public void handleRequest(Context ctx) {
    }

    public void setWifiUsing() {
    }

    public boolean isUsingWifi(int connectivityResult) {
        return false;
    }
}
