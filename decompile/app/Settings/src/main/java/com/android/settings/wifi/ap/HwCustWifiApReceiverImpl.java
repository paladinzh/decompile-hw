package com.android.settings.wifi.ap;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.widget.Toast;
import java.util.List;

public class HwCustWifiApReceiverImpl extends HwCustWifiApReceiver {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final boolean IS_SHOW_MAX_AP_TOAST = SystemProperties.getBoolean("ro.config.show_max_ap_toast", false);
    private static final String TAG = "HwCustWifiApReceiverImpl";
    private String action = "";
    private Context mContext;
    private WifiManager mWifiManager;

    public void handleCustIntent(Context context, Intent intent) {
        this.mContext = context;
        this.action = intent.getAction();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (ACTION_WIFI_AP_STA_JOIN.equals(this.action) && IS_SHOW_MAX_AP_TOAST) {
            int count = 0;
            if (this.mWifiManager != null && this.mWifiManager.isWifiApEnabled()) {
                List<WifiApClientInfo> list = WifiApClientUtils.getInstance(this.mContext).getConnectedList();
                if (list != null) {
                    count = list.size();
                }
            }
            showMaxApConnectedToast(count);
        }
    }

    private void showMaxApConnectedToast(int apConnectedCount) {
        int connectedCount = apConnectedCount;
        if (apConnectedCount == Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8)) {
            Toast.makeText(this.mContext, 2131629130, 1).show();
        }
    }
}
