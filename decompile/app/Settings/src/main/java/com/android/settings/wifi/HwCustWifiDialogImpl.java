package com.android.settings.wifi;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.Button;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustWifiDialogImpl extends HwCustWifiDialog {
    private static final String ATTWIFI = "attwifi";
    private static final int AUTO_CONNECT_SWITCH_OPEN = 1;
    private static final int AUTO_CONNECT_SWITCH_VALUE = SystemProperties.getInt("ro.config.auto_connect_attwifi", 0);
    private static final int DISABLE = 0;
    private static final String WIFI_HOTSPOT_REDEFINDED = "Z736563757265";
    public Context mContext;
    public WifiDialog mWifiDialog;

    public HwCustWifiDialogImpl(WifiDialog wifiDialog) {
        super(wifiDialog);
        this.mWifiDialog = wifiDialog;
    }

    public HwCustWifiDialogImpl(WifiDialog wifiDialog, Context context) {
        super(wifiDialog, context);
        this.mWifiDialog = wifiDialog;
        this.mContext = context;
    }

    public void custDialogButton(AccessPoint accessPoint) {
        if (AUTO_CONNECT_SWITCH_VALUE == 1 && accessPoint != null && this.mWifiDialog.getForgetButton() != null) {
            String accessPoin_ssid = accessPoint.getSsidStr();
            if (ATTWIFI.equals(accessPoin_ssid) || WIFI_HOTSPOT_REDEFINDED.equals(accessPoin_ssid)) {
                this.mWifiDialog.getForgetButton().setVisibility(8);
            }
        }
    }

    public void setForgetButtonFales(Button mButton, AccessPoint mAccessPoint) {
        if (mAccessPoint != null && mButton != null && this.mContext != null) {
            String custConf = Systemex.getString(this.mContext.getContentResolver(), "hw_eapsim_default");
            if (custConf != null) {
                for (String eapName : custConf.split(",")) {
                    if (eapName != null && mAccessPoint.getSecurity() == 3 && eapName.equals(mAccessPoint.getSsidStr())) {
                        mButton.setEnabled(false);
                    }
                }
            }
        }
    }
}
