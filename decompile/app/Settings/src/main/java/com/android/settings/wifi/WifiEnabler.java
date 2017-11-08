package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;
import com.android.settings.RadarReporter;
import com.android.settings.search.Index;
import com.android.settingslib.WirelessUtils;
import com.huawei.cust.HwCustUtils;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiEnabler extends WifiEnablerHwBase implements OnPreferenceChangeListener {
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    private boolean mEnabling = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Index.getInstance(WifiEnabler.this.mContext).updateFromClassNameResource(WifiSettings.class.getName(), true, msg.getData().getBoolean("is_wifi_on"));
                    return;
                case 1:
                    if (WifiEnabler.this.mEnabling && WifiEnabler.this.mWifiManager != null) {
                        HashMap<Short, Object> map = new HashMap();
                        map.put(Short.valueOf((short) 0), Integer.valueOf(WifiEnabler.this.mWifiManager.getWifiState()));
                        RadarReporter.reportRadar(907018002, map);
                        int reason = 2;
                        if (WifiEnabler.this.mWifiManager.isWifiEnabled() != WifiEnabler.this.mSwitch.isChecked()) {
                            if (WifiEnabler.this.mWifiManager.isWifiEnabled()) {
                                reason = 0;
                            } else {
                                reason = 1;
                            }
                        }
                        Intent intent = new Intent("com.huawei.chr.wifi.action.SETTING_AND_WIFISERVICE_STATE_DIFFERENT");
                        intent.putExtra("wifi_open_close_failed_state", reason);
                        WifiEnabler.this.mContext.sendBroadcast(intent);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private HwCustWifiEnabler mHwCustWifiEnabler;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("WifiEnabler", "action=" + action);
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                WifiEnabler.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                if (!WifiEnabler.this.mConnected.get()) {
                    WifiEnabler.this.handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra("newState")));
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (info != null) {
                    WifiEnabler.this.mConnected.set(info.isConnected());
                    WifiEnabler.this.handleStateChanged(info.getDetailedState());
                }
            } else if (HwCustWifiEnabler.WIFI_STATE_DISABLE_HISI_ACTION.equals(action) && WifiEnabler.this.mHwCustWifiEnabler != null && !WifiEnabler.this.mHwCustWifiEnabler.isSupportStaP2pCoexist()) {
                WifiEnabler.this.setSwitchChecked(false);
                WifiEnabler.this.setSwitchEnabled(WifiEnabler.this.mWifiExt.getSwitchState());
                WifiEnabler.this.updateStatusText(false);
            }
        }
    };
    private boolean mStateMachineEvent;

    public WifiEnabler(Context context, SwitchPreference switch_) {
        super(context);
        this.mContext = context;
        this.mSwitch = switch_;
        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mHwCustWifiEnabler = (HwCustWifiEnabler) HwCustUtils.createObj(HwCustWifiEnabler.class, new Object[]{this});
        if (this.mHwCustWifiEnabler != null) {
            this.mHwCustWifiEnabler.addAction(this.mIntentFilter);
        }
    }

    public void resume() {
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        this.mWifiExt.registerAirplaneModeObserver(this.mSwitch);
        if (this.mSwitch != null) {
            this.mSwitch.setOnPreferenceChangeListener(this);
            this.mWifiExt.initSwitchState(this.mSwitch);
        }
        updateTextByWifiStatus();
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mReceiver);
        if (this.mSwitch != null) {
            this.mSwitch.setOnPreferenceChangeListener(null);
        }
        this.mWifiExt.unRegisterAirplaneObserver();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mSwitch) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(this.mContext, preference, newValue);
            if (this.mStateMachineEvent) {
                return false;
            }
            boolean isChecked = ((Boolean) newValue).booleanValue();
            if (isChecked) {
                System.putInt(this.mContext.getContentResolver(), "wlan_switch_on", 1);
            }
            if (isChecked == this.mSwitch.isChecked()) {
                return true;
            }
            if (!isChecked || WirelessUtils.isRadioAllowed(this.mContext, "wifi")) {
                int wifiApState = this.mWifiManager.getWifiApState();
                if (isChecked && (wifiApState == 12 || wifiApState == 13)) {
                    WifiExtUtils.setWifiApEnabled(this.mContext, this.mWifiManager, null, false);
                }
                this.mSwitch.setEnabled(false);
                MetricsLogger.action(this.mContext, isChecked ? 139 : 138);
                if (!this.mWifiManager.setWifiEnabled(isChecked)) {
                    this.mSwitch.setEnabled(true);
                    Toast.makeText(this.mContext, 2131627277, 0).show();
                } else if (isChecked) {
                    startCount();
                }
                Log.d("WifiEnabler", "mSwitch state=" + isChecked);
                Log.d("WifiEnabler", "mSwitch isEnabled=" + this.mSwitch.isEnabled());
            } else {
                Toast.makeText(this.mContext, 2131627278, 0).show();
                setSwitchChecked(false);
                return true;
            }
        }
        return true;
    }

    private void handleWifiStateChanged(int state) {
        Log.d("WifiEnabler", "state=" + state);
        switch (state) {
            case 0:
                setSwitchEnabled(false);
                updateStatusText(false);
                stopCount();
                return;
            case 1:
                setSwitchChecked(false);
                setSwitchEnabled(this.mWifiExt.getSwitchState());
                updateStatusText(false);
                updateSearchIndex(false);
                stopCount();
                return;
            case 2:
                setSwitchEnabled(false);
                updateStatusText(false);
                return;
            case 3:
                setSwitchChecked(true);
                setSwitchEnabled(this.mWifiExt.getSwitchState());
                updateStatusText(true);
                updateSearchIndex(true);
                stopCount();
                return;
            default:
                setSwitchChecked(false);
                setSwitchEnabled(this.mWifiExt.getSwitchState());
                updateStatusText(false);
                updateSearchIndex(false);
                return;
        }
    }

    private void updateSearchIndex(boolean isWiFiOn) {
        this.mHandler.removeMessages(0);
        Message msg = new Message();
        msg.what = 0;
        msg.getData().putBoolean("is_wifi_on", isWiFiOn);
        this.mHandler.sendMessage(msg);
    }

    private void setSwitchChecked(boolean checked) {
        if (this.mSwitch != null && checked != this.mSwitch.isChecked()) {
            this.mStateMachineEvent = true;
            this.mSwitch.setOnPreferenceChangeListener(null);
            this.mSwitch.setChecked(checked);
            this.mSwitch.setOnPreferenceChangeListener(this);
            this.mStateMachineEvent = false;
        }
    }

    private void startCount() {
        this.mEnabling = true;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), 10000);
    }

    private void stopCount() {
        this.mEnabling = false;
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
    }
}
