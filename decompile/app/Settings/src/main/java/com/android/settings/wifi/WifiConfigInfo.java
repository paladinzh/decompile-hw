package com.android.settings.wifi;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import java.util.List;

public class WifiConfigInfo extends Activity {
    private TextView mConfigList;
    private WifiManager mWifiManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        setContentView(2130969269);
        this.mConfigList = (TextView) findViewById(2131887480);
    }

    protected void onResume() {
        super.onResume();
        if (this.mWifiManager.isWifiEnabled()) {
            List<WifiConfiguration> wifiConfigs = this.mWifiManager.getConfiguredNetworks();
            StringBuffer configList = new StringBuffer();
            if (wifiConfigs != null) {
                for (int i = wifiConfigs.size() - 1; i >= 0; i--) {
                    configList.append(wifiConfigs.get(i));
                }
            }
            this.mConfigList.setText(configList);
            return;
        }
        this.mConfigList.setText(2131625087);
    }
}
