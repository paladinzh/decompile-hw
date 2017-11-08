package com.android.settings.wifi.bridge;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;
import com.huawei.android.net.wifi.p2p.WifiP2pManagerCommonEx;

public class WifiBridgeSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private boolean isRecreateWifibridge = false;
    private Channel mChannel;
    private ConnectivityManager mConnectManager;
    private Preference mCreateNetworkBridgeConfig;
    private ActionListener mGroupRemoveActionListener = new ActionListener() {
        public void onSuccess() {
            MLog.i("WifiBridgeSettings", "Stop wifi bridge success.");
        }

        public void onFailure(int arg0) {
            WifiBridgeSettings.this.mWifiBridgeSwitch.setEnabled(true);
            WifiBridgeSettings.this.mWifiBridgeSwitch.setChecked(true);
            MLog.i("WifiBridgeSettings", "Stop wifi bridge failed and the status is : " + arg0);
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 127:
                    WifiBridgeSettings.this.updateWifiBridgeSwitchStatus();
                    return;
                case 128:
                    if (WifiBridgeSettings.this.mChannel != null && WifiBridgeSettings.this.mWifiBridgeCreateListener != null) {
                        WifiP2pManagerCommonEx.createGroupWifiRepeater(WifiBridgeSettings.this.mChannel, WifiBridgeSettings.this.mWifiConfig, WifiBridgeSettings.this.mWifiBridgeCreateListener);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsConnectedToWifi = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (info != null && WifiBridgeSettings.this.mWifiManager.isWifiEnabled() && DetailedState.CONNECTED.equals(info.getDetailedState())) {
                    WifiInfo wifiInfo = WifiBridgeSettings.this.mWifiManager.getConnectionInfo();
                    if (wifiInfo != null && wifiInfo.getWifiSsid() != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        WifiBridgeSettings.this.mIsConnectedToWifi = true;
                        WifiBridgeSettings.this.mCreateNetworkBridgeConfig.setEnabled(true);
                        WifiBridgeSettings.this.updateWifiBridgeSwitchStatus();
                        WifiBridgeSettings.this.mWifiBridgeSwitch.setSummary(2131627248);
                    } else {
                        return;
                    }
                }
                WifiBridgeSettings.this.mIsConnectedToWifi = false;
                WifiBridgeSettings.this.mWifiBridgeSwitch.setChecked(false);
                WifiBridgeSettings.this.mWifiBridgeSwitch.setEnabled(false);
                WifiBridgeSettings.this.mCreateNetworkBridgeConfig.setEnabled(false);
                WifiBridgeSettings.this.mWifiBridgeSwitch.setSummary(2131627247);
            }
        }
    };
    private final WifiBridgeStatusObserver mSettingsObserver = new WifiBridgeStatusObserver();
    private Preference mUsehelp;
    private ActionListener mWifiBridgeCreateListener = new ActionListener() {
        public void onSuccess() {
            MLog.i("WifiBridgeSettings", "Start wifi bridge success.");
        }

        public void onFailure(int arg0) {
            WifiBridgeSettings.this.mWifiBridgeSwitch.setEnabled(true);
            WifiBridgeSettings.this.mWifiBridgeSwitch.setChecked(false);
            MLog.i("WifiBridgeSettings", "Start wifi bridge and the status is : " + arg0);
        }
    };
    private int mWifiBridgeStatus = 0;
    private CustomSwitchPreference mWifiBridgeSwitch;
    private WifiConfiguration mWifiConfig;
    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;

    private final class WifiBridgeStatusObserver extends ContentObserver {
        private final Uri WIFI_BRIDGE_STATUS_URI = Global.getUriFor("wifi_repeater_on");

        public WifiBridgeStatusObserver() {
            super(WifiBridgeSettings.this.mHandler);
        }

        public void register(boolean register) {
            ContentResolver cr = WifiBridgeSettings.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.WIFI_BRIDGE_STATUS_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.WIFI_BRIDGE_STATUS_URI.equals(uri)) {
                WifiBridgeSettings.this.mHandler.sendMessage(WifiBridgeSettings.this.mHandler.obtainMessage(127));
            }
        }
    }

    private void setUseHelpInfo() {
        CharSequence message = String.format(getResources().getString(2131628856, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(4)}), new Object[0]);
        if (this.mUsehelp != null) {
            this.mUsehelp.setSummary(message);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230937);
        this.mWifiBridgeSwitch = (CustomSwitchPreference) findPreference("wifi_bridge_switch");
        this.mUsehelp = findPreference("wifi_bridge_use_help");
        setUseHelpInfo();
        this.mCreateNetworkBridgeConfig = findPreference("config_wifi_bridge");
        initWifiBridge();
    }

    public void onResume() {
        super.onResume();
        this.mSettingsObserver.register(true);
        this.mWifiBridgeSwitch.setOnPreferenceChangeListener(this);
        getActivity().registerReceiver(this.mReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
        getActivity().unregisterReceiver(this.mReceiver);
        this.mWifiBridgeSwitch.setOnPreferenceChangeListener(null);
        ItemUseStat.getInstance().cacheData(getContext());
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mCreateNetworkBridgeConfig) {
            MLog.i("WifiBridgeSettings", "Start wifi bridge config and jump to WifiBridgeDialogActivity");
            startWifiBridgeConfig();
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference != this.mWifiBridgeSwitch) {
            return false;
        }
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getContext(), preference, value);
        boolean enable = ((Boolean) value).booleanValue();
        MLog.d("WifiBridgeSettings", "Wifi bridge switch should checked: " + enable);
        this.mWifiBridgeSwitch.setEnabled(false);
        if (enable) {
            MLog.d("WifiBridgeSettings", "Start wifi bridge");
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mWifiConfig.apChannel = convertFrequencyToChannelNumber(wifiInfo.getFrequency());
                this.mWifiConfig.apBand = convertFrequencyToBand(wifiInfo.getFrequency());
            }
            WifiP2pManagerCommonEx.createGroupWifiRepeater(this.mChannel, this.mWifiConfig, this.mWifiBridgeCreateListener);
        } else {
            MLog.d("WifiBridgeSettings", "Stop wifi bridge");
            this.mWifiP2pManager.removeGroup(this.mChannel, this.mGroupRemoveActionListener);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 11 && resultCode == -1) {
            this.mWifiConfig = (WifiConfiguration) intent.getParcelableExtra("wifi_bridge_config");
            this.mWifiBridgeStatus = Global.getInt(getContentResolver(), "wifi_repeater_on", 0);
            if (this.mWifiConfig != null) {
                MLog.i("WifiBridgeSettings", "Jump from WifiBridgeDialogActivity to WifiBridgeSettings: mWifiBridgeStatus" + this.mWifiBridgeStatus);
                if (this.mWifiBridgeStatus == 1) {
                    this.isRecreateWifibridge = true;
                    MLog.i("WifiBridgeSettings", "Restart wifi bridge");
                    this.mWifiBridgeSwitch.setEnabled(false);
                    this.mWifiP2pManager.removeGroup(this.mChannel, this.mGroupRemoveActionListener);
                }
                WifiP2pManagerCommonEx.setWifiRepeaterConfiguration(this.mWifiConfig);
            }
        }
    }

    private void initWifiBridge() {
        this.mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService("wifi");
        this.mWifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
        this.mConnectManager = (ConnectivityManager) getSystemService("connectivity");
        this.mWifiConfig = WifiP2pManagerCommonEx.getWifiRepeaterConfiguration();
        if (this.mWifiP2pManager != null) {
            this.mChannel = this.mWifiP2pManager.initialize(getContext(), getActivity().getMainLooper(), null);
            if (this.mChannel == null) {
                this.mWifiP2pManager = null;
            }
        } else {
            MLog.e("WifiBridgeSettings", "WifiP2pManager is null !");
            finish();
        }
        checkWifiBridgeUsable();
    }

    private int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < 5170 || frequency > 5825) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    private int convertFrequencyToBand(int frequency) {
        if ((frequency < 2412 || frequency > 2484) && frequency >= 5170 && frequency <= 5825) {
            return 1;
        }
        return 0;
    }

    private void checkWifiBridgeUsable() {
        if (isConnectedToWifi()) {
            this.mCreateNetworkBridgeConfig.setEnabled(true);
            updateWifiBridgeSwitchStatus();
            return;
        }
        this.mWifiBridgeSwitch.setEnabled(false);
        this.mCreateNetworkBridgeConfig.setEnabled(false);
    }

    private boolean isConnectedToWifi() {
        if (this.mConnectManager != null) {
            return this.mConnectManager.getNetworkInfo(1).isConnected();
        }
        return false;
    }

    private void startWifiBridgeConfig() {
        Intent intent = new Intent(getContext(), WifiBridgeDialogActivity.class);
        intent.putExtra("wifi_bridge_config", this.mWifiConfig);
        startActivityForResult(intent, 11);
    }

    private void updateWifiBridgeSwitchStatus() {
        this.mWifiBridgeStatus = Global.getInt(getContentResolver(), "wifi_repeater_on", 0);
        Log.i("WifiBridgeSettings", "mWifiBridgeStatus = " + this.mWifiBridgeStatus);
        switch (this.mWifiBridgeStatus) {
            case 0:
                if (this.isRecreateWifibridge) {
                    this.isRecreateWifibridge = false;
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(128));
                }
                if (this.mIsConnectedToWifi || isConnectedToWifi()) {
                    this.mWifiBridgeSwitch.setEnabled(true);
                }
                this.mWifiBridgeSwitch.setChecked(false);
                return;
            case 1:
                this.mWifiBridgeSwitch.setEnabled(true);
                this.mWifiBridgeSwitch.setChecked(true);
                return;
            case 2:
            case 3:
                this.mWifiBridgeSwitch.setEnabled(false);
                return;
            default:
                return;
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
