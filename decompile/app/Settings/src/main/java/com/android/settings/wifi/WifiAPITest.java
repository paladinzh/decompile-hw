package com.android.settings.wifi;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import com.android.settings.SettingsPreferenceFragment;

public class WifiAPITest extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private Preference mWifiDisableNetwork;
    private AlertDialog mWifiDisableNetworkDialog;
    private Preference mWifiDisconnect;
    private Preference mWifiEnableNetwork;
    private AlertDialog mWifiEnableNetworkDialog;
    private WifiManager mWifiManager;
    private int netid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(2130969267);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        this.mWifiDisconnect = preferenceScreen.findPreference("disconnect");
        this.mWifiDisconnect.setOnPreferenceClickListener(this);
        this.mWifiDisableNetwork = preferenceScreen.findPreference("disable_network");
        this.mWifiDisableNetwork.setOnPreferenceClickListener(this);
        this.mWifiEnableNetwork = preferenceScreen.findPreference("enable_network");
        this.mWifiEnableNetwork.setOnPreferenceClickListener(this);
    }

    protected int getMetricsCategory() {
        return 89;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        return false;
    }

    public boolean onPreferenceClick(Preference pref) {
        if (pref == this.mWifiDisconnect) {
            this.mWifiManager.disconnect();
        } else if (pref == this.mWifiDisableNetwork) {
            alert = new Builder(getContext());
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            input = new EditText(getPrefContext());
            input.setKeyListener(DigitsKeyListener.getInstance());
            input.setFilters(new InputFilter[]{new LengthFilter(9)});
            alert.setView(input);
            alert.setPositiveButton("Ok", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        WifiAPITest.this.netid = Integer.parseInt(input.getText().toString());
                        WifiAPITest.this.mWifiManager.disableNetwork(WifiAPITest.this.netid);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            this.mWifiDisableNetworkDialog = alert.show();
        } else if (pref == this.mWifiEnableNetwork) {
            alert = new Builder(getContext());
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            input = new EditText(getPrefContext());
            alert.setView(input);
            alert.setPositiveButton("Ok", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        WifiAPITest.this.netid = Integer.parseInt(input.getText().toString());
                        WifiAPITest.this.mWifiManager.enableNetwork(WifiAPITest.this.netid, false);
                    } catch (NumberFormatException e) {
                    }
                }
            });
            alert.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            this.mWifiEnableNetworkDialog = alert.show();
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWifiDisableNetworkDialog != null && this.mWifiDisableNetworkDialog.isShowing()) {
            this.mWifiDisableNetworkDialog.dismiss();
        }
        if (this.mWifiEnableNetworkDialog != null && this.mWifiEnableNetworkDialog.isShowing()) {
            this.mWifiEnableNetworkDialog.dismiss();
        }
    }
}
