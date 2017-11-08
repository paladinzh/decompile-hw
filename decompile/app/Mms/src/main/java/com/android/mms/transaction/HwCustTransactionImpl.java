package com.android.mms.transaction;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.mms.HwCustMmsConfigImpl;
import java.io.IOException;

public class HwCustTransactionImpl extends HwCustTransaction {
    private static final String TAG = "HwCustTransactionImpl";
    private Context mContext;

    public boolean useWifi(Context context) {
        this.mContext = context;
        if (HwCustMmsConfigImpl.allowMmsOverWifi() && getWifiInfo(this.mContext)) {
            Log.v("transaction", "useWifi true");
            return true;
        }
        Log.v("transaction", "useWifi false");
        return false;
    }

    private boolean getWifiInfo(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService("connectivity");
        boolean wifiEnabled = ((WifiManager) context.getSystemService("wifi")).isWifiEnabled();
        NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
        boolean wifiConnected = (nwInfo == null || !nwInfo.isConnected()) ? false : nwInfo.getType() == 1;
        Log.v("transaction", "wifiMmsEnabledwifiEnabled" + wifiEnabled + "wifiConnected" + wifiConnected);
        if (wifiEnabled && wifiConnected) {
            Log.v("transaction", "getWifiInfo isAvailable");
            return true;
        }
        Log.v("transaction", "getWifiInfo not isAvailable");
        return false;
    }

    public byte[] getPduInWifi(String url, TransactionSettings mTransactionSettings) throws IOException {
        Log.v("transaction", "get pdu through wifi");
        return HttpUtils.httpConnection(this.mContext, -1, url, null, 2, false, mTransactionSettings.getProxyAddress(), mTransactionSettings.getProxyPort());
    }

    public byte[] setPduInWifi(long token, byte[] pdu, String mmscUrl, TransactionSettings mTransactionSettings) throws IOException {
        Log.v("transaction", "send pdu through wifi");
        return HttpUtils.httpConnection(this.mContext, token, mmscUrl, pdu, 1, false, mTransactionSettings.getProxyAddress(), mTransactionSettings.getProxyPort());
    }

    public void ensureRouteToHostInWifi(String url) {
        Log.v("transaction", "ensure wifi connect " + Uri.parse(url));
    }
}
