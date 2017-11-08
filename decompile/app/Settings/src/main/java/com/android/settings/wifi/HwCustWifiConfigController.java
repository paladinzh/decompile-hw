package com.android.settings.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settingslib.wifi.AccessPoint;

public class HwCustWifiConfigController {
    public static final String EAP_METHOD_SIM = "SIM";
    public WifiConfigController mWifiConfigController;

    public HwCustWifiConfigController(WifiConfigController wifiConfigController) {
        this.mWifiConfigController = wifiConfigController;
    }

    public boolean getConfig(int mAccessPointSecurity, WifiConfiguration config, View mView) {
        return false;
    }

    public void showWapiSecurityFields(int mAccessPointSecurity, View mView) {
    }

    public boolean isPasswordInvalid(int mAccessPointSecurity, TextView mPasswordView) {
        return false;
    }

    public int getEapMethodDefault(Spinner spinner, Context context) {
        int selectedItemPosition = spinner.getSelectedItemPosition();
        if (-1 == selectedItemPosition) {
            return 0;
        }
        return selectedItemPosition;
    }

    public boolean checkEapSimDefault(Context context) {
        return false;
    }

    public int setSpinerSelection(Spinner spinner, String name) {
        return 0;
    }

    public boolean isNotEditWifi(AccessPoint mSelectedAccessPoint, Context context) {
        return false;
    }

    public void notAllowModifyWifi(View view, AccessPoint mAccessPoint) {
    }
}
