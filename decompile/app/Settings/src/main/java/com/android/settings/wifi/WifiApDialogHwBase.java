package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class WifiApDialogHwBase extends AlertDialog {
    protected WifiConfiguration config = null;
    protected EditText mPassword;
    protected int mSecurityTypeIndex = 0;
    protected EditText mSsid;
    protected View mView;
    WifiConfiguration mWifiConfig;
    protected CheckBox showPassword;

    protected WifiApDialogHwBase(Context context) {
        super(context);
    }

    protected void onStart() {
        int i;
        EditText editText = this.mPassword;
        if (this.showPassword.isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        editText.setInputType(i | 1);
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
        if (this.config != null) {
            changeConfig(this.config);
        } else {
            changeConfig(this.mWifiConfig);
        }
    }

    protected void changeConfig(WifiConfiguration mwc) {
        if (mwc != null) {
            if (this.mSsid != null) {
                this.mSsid.setText(mwc.SSID);
                if (mwc.SSID != null) {
                    this.mSsid.setSelection(mwc.SSID.length());
                }
            }
            Spinner mSecurity = (Spinner) this.mView.findViewById(2131887460);
            if (mSecurity != null) {
                mSecurity.setSelection(this.mSecurityTypeIndex);
            }
            if (this.mSecurityTypeIndex == 1 && this.mPassword != null) {
                this.mPassword.setText(mwc.preSharedKey);
            }
        }
        this.showPassword.setChecked(false);
        if (this.mPassword != null) {
            this.mPassword.setInputType(129);
        }
    }
}
