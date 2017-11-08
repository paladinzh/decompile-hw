package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class TestingWifiMacAddr extends SettingsPreferenceFragment {
    private AlertDialog mDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDialog = new Builder(getActivity()).setIcon(2130838231).setTitle(2131625247).setMessage(getWifiMacAddr()).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                TestingWifiMacAddr.this.finish();
            }
        }).create();
        this.mDialog.show();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
    }

    protected String getWifiMacAddr() {
        String macAddress = null;
        WifiInfo wifiInfo = ((WifiManager) getSystemService("wifi")).getConnectionInfo();
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
        }
        if (macAddress != null) {
            Log.e("wifi sWifiMacAddr", macAddress);
        }
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = getString(2131625250);
        }
        String sWifiMacAddr = macAddress.toString();
        Log.e("wifi sWifiMacAddr", sWifiMacAddr);
        return sWifiMacAddr;
    }

    protected int getMetricsCategory() {
        return 103;
    }
}
