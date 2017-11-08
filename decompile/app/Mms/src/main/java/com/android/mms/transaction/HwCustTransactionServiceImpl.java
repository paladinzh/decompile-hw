package com.android.mms.transaction;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.mms.HwCustMmsConfigImpl;

public class HwCustTransactionServiceImpl extends HwCustTransactionService {
    private static final int WIFI_STATUS_DISABLED = 100;
    private static final int WIFI_STATUS_IDEL = 101;
    private static final int WIFI_STATUS_IN_USING = 102;
    private static boolean mmsOverWifi = HwCustMmsConfigImpl.allowMmsOverWifi();
    private String TAG = "HwCustTransactionServiceImpl";
    private Context mContext;
    private int mWifiStatus = 100;

    public boolean mmsOverWifiEnabled(Context context) {
        this.mContext = context;
        if (mmsOverWifi) {
            boolean wifiEnabled = ((WifiManager) this.mContext.getSystemService("wifi")).isWifiEnabled();
            NetworkInfo nwInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
            boolean wifiConnected = (nwInfo == null || !nwInfo.isConnected()) ? false : nwInfo.getType() == 1;
            Log.v(this.TAG, "mmsOverWifiEnabled wifiMmsEnabled" + mmsOverWifi + "wifiEnabled" + wifiEnabled + "wifiConnected" + wifiConnected);
            if (!wifiEnabled) {
                wifiConnected = false;
            }
            return wifiConnected;
        }
        Log.v(this.TAG, "mmsOverWifiEnabled mms over wifi disabled");
        return false;
    }

    public void update() {
        if (mmsOverWifi && this.mWifiStatus == 102) {
            this.mWifiStatus = 101;
        }
        Log.v(this.TAG, "update mWifiStatus = " + this.mWifiStatus);
    }

    public boolean mmsUseWifi() {
        if (!mmsOverWifi || this.mWifiStatus == 100) {
            Log.v(this.TAG, "mmsUseWifi false ");
            return false;
        }
        Log.v(this.TAG, "mmsUseWifi true ");
        return true;
    }

    public void setWifiDisconnect() {
        this.mWifiStatus = 100;
        Log.v(this.TAG, "setWifiDisconnect mWifiStatus = " + this.mWifiStatus);
    }

    public int getWifiState() {
        Log.v(this.TAG, "getWifiState mWifiStatus = " + this.mWifiStatus);
        return this.mWifiStatus;
    }

    public void handleRequest(Context ctx) {
        if (mmsOverWifiEnabled(ctx) && this.mWifiStatus == 100) {
            Log.v(this.TAG, "handleRequest handle EVENT_TRANSACTION_REQUEST: mWifiStatus=" + this.mWifiStatus);
            this.mWifiStatus = 101;
            Log.v(this.TAG, "handleRequest handle EVENT_TRANSACTION_REQUEST: mWifiStatus change into WIFI_STATUS_IDEL");
        }
    }

    public void setWifiUsing() {
        this.mWifiStatus = 102;
        Log.v(this.TAG, "setWifiUsing mWifiStatus = " + this.mWifiStatus);
    }

    public boolean isUsingWifi(int connectivityResult) {
        if (mmsOverWifi && connectivityResult == 102) {
            Log.v(this.TAG, "isUsingWifi true");
            return true;
        }
        Log.v(this.TAG, "isUsingWifi false");
        return false;
    }
}
