package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.huawei.android.provider.SettingsEx.Systemex;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SetWifiApSettingsReceiver extends BroadcastReceiver {
    private static int isSetSsidAndPassword = 0;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        isSetSsidAndPassword = Systemex.getInt(context.getContentResolver(), "hw_devicenameandimei_ssid", 0);
        boolean mCustomEapSim = Systemex.getInt(context.getContentResolver(), "hw_custom_eapsim", 0) == 1;
        CharSequence simState = intent.getCharSequenceExtra("ss");
        if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
            if (mCustomEapSim && isSimAbsent(simState)) {
                Log.i("SetWifiApSettingsReceiver", "SimCard is Absent");
                disconnetEapSim(context);
            }
        } else if ("android.intent.action.BOOT_COMPLETED".equals(action) && isSetSsidAndPassword != 0) {
            setCustDefaultAp(context);
        }
    }

    private void setCustDefaultAp(Context context) {
        String custFullApName = Systemex.getString(context.getContentResolver(), "hw_full_ssid_name");
        if (!TextUtils.isEmpty(custFullApName)) {
            int randLength = Systemex.getInt(context.getContentResolver(), "hw_ssid_rand_length", 4);
            if (!Utils.isMultiUserExist(context) || Utils.isOwner(context)) {
                String ssid = custFullApName.replace("#mn", Build.MODEL).replace("#pn", Build.PRODUCT).replace("#rn", UUID.randomUUID().toString().substring(0, randLength).toUpperCase(Locale.US));
                String marketing_name = SystemProperties.get("ro.config.marketing_name");
                if (TextUtils.isEmpty(marketing_name)) {
                    ssid = ssid.replace("#mk", Build.PRODUCT);
                } else {
                    ssid = ssid.replace("#mk", marketing_name);
                }
                WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
                if (wifiManager != null) {
                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    wifiConfig.SSID = ssid;
                    wifiConfig.allowedKeyManagement.set(4);
                    wifiConfig.preSharedKey = UUID.randomUUID().toString().substring(0, 8);
                    wifiManager.setWifiApConfiguration(wifiConfig);
                    Systemex.putInt(context.getContentResolver(), "hw_devicenameandimei_ssid", 0);
                }
            }
        }
    }

    private boolean isSimAbsent(CharSequence cs) {
        return "ABSENT".equals(cs);
    }

    private void disconnetEapSim(Context context) {
        String msg = context.getString(2131629261);
        WifiManager mWifiManager = (WifiManager) context.getSystemService("wifi");
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        WifiInfo winfo = mWifiManager.getConnectionInfo();
        if (winfo == null) {
            Log.i("SetWifiApSettingsReceiver", "is not connected");
            return;
        }
        if (isEapSimWifi(configs, winfo)) {
            Toast.makeText(context, msg, 1).show();
            mWifiManager.setWifiEnabled(false);
            mWifiManager.setWifiEnabled(true);
        }
    }

    private boolean isEapSimWifi(List<WifiConfiguration> configs, WifiInfo winfo) {
        if (configs == null) {
            return false;
        }
        for (WifiConfiguration mConfig : configs) {
            if (mConfig.networkId != -1 && winfo.getNetworkId() == mConfig.networkId) {
                if (mConfig.isEnterprise() && mConfig.enterpriseConfig.getEapMethod() == 4) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
