package com.android.settings.wifi;

import android.app.Activity;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settingslib.wifi.AccessPoint;
import java.util.ArrayList;
import java.util.List;

public class WifiPlatformImp extends WifiExtAbsBase {

    private static class StaticCreateBeamUrisCallback implements CreateBeamUrisCallback {
        private String mUriString;

        public StaticCreateBeamUrisCallback(String uriString) {
            this.mUriString = uriString;
        }

        public Uri[] createBeamUris(NfcEvent event) {
            return new Uri[]{Uri.parse(this.mUriString)};
        }
    }

    private static class StaticOnCheckedChangeListener implements OnCheckedChangeListener {
        TextView mPasswordView;

        public StaticOnCheckedChangeListener(TextView passwordView) {
            this.mPasswordView = passwordView;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            int pos = this.mPasswordView.getSelectionStart();
            TextView textView = this.mPasswordView;
            if (isChecked) {
                i = 144;
            } else {
                i = 128;
            }
            textView.setInputType(i | 1);
            if (pos >= 0) {
                ((EditText) this.mPasswordView).setSelection(pos);
            }
        }
    }

    public void setSelection(View root, Bundle savedInstanceState) {
        root.findViewById(2131886149).setVisibility(8);
        if (savedInstanceState != null) {
            boolean isChecked = savedInstanceState.getBoolean("wifi_advanced_togglebox");
            if (isChecked) {
                ((CheckBox) root.findViewById(2131887463)).setChecked(isChecked);
                root.findViewById(2131887515).setVisibility(0);
                ((Spinner) root.findViewById(2131887517)).setSelection(savedInstanceState.getInt("wifi_proxy_settings"));
                ((Spinner) root.findViewById(2131887527)).setSelection(savedInstanceState.getInt("wifi_ip_settings"));
            }
        }
    }

    public int getDrawable(WifiP2pDevice device, int rssi) {
        if (rssi == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
            return 2130838267;
        }
        String[] str = device.primaryDeviceType.split("-");
        if (str[0].equals("1")) {
            return 2130838257;
        }
        if (str[0].equals("2")) {
            return 2130838259;
        }
        if (str[0].equals("3")) {
            return 2130838263;
        }
        if (str[0].equals("4")) {
            return 2130838256;
        }
        if (str[0].equals("5")) {
            return 2130838265;
        }
        if (str[0].equals("6")) {
            return 2130838262;
        }
        if (str[0].equals("7")) {
            return 2130838258;
        }
        if (str[0].equals("8")) {
            return 2130838261;
        }
        if (str[0].equals("9")) {
            return 2130838264;
        }
        if (str[0].equals("10")) {
            return 2130838260;
        }
        if (str[0].equals("11")) {
            return 2130838255;
        }
        return 2130838267;
    }

    public void setBeamPushUrisCallback(Activity activity, String uriString) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter != null) {
            mNfcAdapter.setBeamPushUrisCallback(new StaticCreateBeamUrisCallback(uriString), activity);
        }
    }

    public List<Object> buildHideList(AccessPoint accessPoint, int mode, View root) {
        List<Object> lists = new ArrayList();
        lists.add(accessPoint.getSsidStr());
        lists.add(Integer.valueOf(accessPoint.getSecurity()));
        lists.add(Integer.valueOf(accessPoint.getNetworkId()));
        lists.add(Integer.valueOf(mode));
        lists.add(root.findViewById(2131887491));
        lists.add(root.findViewById(2131887493));
        lists.add(root.findViewById(2131887504));
        return lists;
    }

    public void setPasswordView(TextView passwordView, View root) {
        int i;
        CheckBox showPassword = (CheckBox) root.findViewById(2131886368);
        if (showPassword.isChecked()) {
            i = 144;
        } else {
            i = 128;
        }
        passwordView.setInputType(i | 1);
        showPassword.setOnCheckedChangeListener(new StaticOnCheckedChangeListener(passwordView));
    }
}
