package com.android.settings.wifi.p2p;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.settings.FaqTextPreference;
import com.android.settings.MLog;
import com.android.settings.ProgressCategory;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.R$string;

public class WifiP2pSettingsHwBase extends SettingsPreferenceFragment implements TextWatcher {
    protected AlertDialog mAlertDialog;
    protected OnShowListener mAlertDlgOnShowListener = new OnShowListener() {
        public void onShow(DialogInterface dialog) {
            boolean z = false;
            if (WifiP2pSettingsHwBase.this.mAlertDialog != null) {
                String deviceNameText = "";
                if (WifiP2pSettingsHwBase.this.mDeviceNameText != null) {
                    deviceNameText = WifiP2pSettingsHwBase.this.mDeviceNameText.getText().toString();
                }
                WifiP2pSettingsHwBase.this.mOkButton = WifiP2pSettingsHwBase.this.mAlertDialog.getButton(-1);
                if (WifiP2pSettingsHwBase.this.mOkButton != null) {
                    Button button = WifiP2pSettingsHwBase.this.mOkButton;
                    if (deviceNameText.length() != 0) {
                        z = true;
                    }
                    button.setEnabled(z);
                }
            }
        }
    };
    protected EditText mDeviceNameText;
    private FaqTextPreference mNotFindoutAvailDevices;
    protected Button mOkButton;
    protected WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
    protected ProgressCategory mPeersGroup;
    protected int mScreenType = 0;
    protected boolean mStopScanning = false;
    protected WifiP2pDevice mThisDevice;
    protected WifiManager mWifiManager;
    protected WifiP2pManager mWifiP2pManager;
    protected boolean mWifiP2pSearching;
    protected PreferenceCategory myDeviceCatotgory;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Object action = null;
        Intent i = getActivity().getIntent();
        if (i != null) {
            action = i.getAction();
        }
        if ("android.settings.WIFI_DIRECT_SETTINGS".equals(action)) {
            this.mScreenType = 1;
            if (this.mWifiManager != null && !this.mWifiManager.isWifiEnabled()) {
                MLog.d("WifiP2pSettingsHwBase", "open Wi-Fi automatically to support Wi-Fi Direct");
                this.mWifiManager.setWifiEnabled(true);
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    protected void stopSearch() {
        if (this.mWifiP2pManager != null && this.mWifiP2pSearching) {
            displayCausesOfUndiscovered(this.mPeersGroup);
            this.mStopScanning = true;
            updateSearchMenu(false);
        }
    }

    protected void updateSearchMenu(boolean searching) {
    }

    protected void buildDeviceCategory(PreferenceScreen root) {
        this.myDeviceCatotgory = new PreferenceCategory(getActivity());
        this.myDeviceCatotgory.setLayoutResource(2130968916);
        this.myDeviceCatotgory.setTitle(2131627447);
        root.addPreference(this.myDeviceCatotgory);
    }

    public void afterTextChanged(Editable s) {
        boolean z = false;
        if (this.mOkButton != null) {
            Button button = this.mOkButton;
            if (s.length() != 0) {
                z = true;
            }
            button.setEnabled(z);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    protected void displayCausesOfUndiscovered(ProgressCategory progressCategory) {
        if (progressCategory != null) {
            if (progressCategory.getPreferenceCount() == 0 || (1 == progressCategory.getPreferenceCount() && "no_device_found".equals(progressCategory.getPreference(0).getKey()))) {
                if (this.mNotFindoutAvailDevices == null) {
                    this.mNotFindoutAvailDevices = new FaqTextPreference(getActivity(), 2130968997, 2);
                    this.mNotFindoutAvailDevices.setKey("faq_no_device_found");
                }
                progressCategory.addPreference(this.mNotFindoutAvailDevices);
            } else {
                removePreference("empty");
                Preference emptyHeightPrefernce = new Preference(getActivity());
                emptyHeightPrefernce.setKey("empty");
                emptyHeightPrefernce.setOrder(1000);
                emptyHeightPrefernce.setLayoutResource(2130968938);
                emptyHeightPrefernce.setSelectable(false);
                getPreferenceScreen().addPreference(emptyHeightPrefernce);
            }
        }
    }

    protected AlertDialog showConnectFailDialog(Context context) {
        if (context == null) {
            return null;
        }
        View layoutView = LayoutInflater.from(context).inflate(2130968741, null);
        TextView causesView = (TextView) layoutView.findViewById(2131886507);
        causesView.setText(2131628042);
        causesView.setTextColor(context.getResources().getColor(2131427330));
        TextView knowMoreView = (TextView) layoutView.findViewById(2131886508);
        if (Utils.hasPackageInfo(context.getPackageManager(), "com.huawei.phoneservice")) {
            knowMoreView.setText(R$string.know_more);
            knowMoreView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent("android.intent.action.FAQ_HELP");
                    intent.putExtra("faq_device_type", 2);
                    WifiP2pSettingsHwBase.this.getActivity().startActivity(intent);
                }
            });
        } else {
            knowMoreView.setVisibility(8);
        }
        return new Builder(context).setView(layoutView).setTitle(context.getString(2131628045)).setPositiveButton(context.getString(2131625656), null).create();
    }
}
