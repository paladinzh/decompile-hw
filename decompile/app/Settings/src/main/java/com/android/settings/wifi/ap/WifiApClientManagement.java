package com.android.settings.wifi.ap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class WifiApClientManagement extends SettingsPreferenceFragment implements OnAddDeviceListener, OnEditDevicesListener, OnViewDeviceListener, WifiApClientListener {
    private static final String[] ALLOWED_DEVICE_ENTRIES = new String[]{"wifi_ap_all_device", "wifi_ap_allowed_device"};
    private WifiApAddDevicePreference mAddDevicePref;
    private PreferenceCategory mAllowedDevicesPref;
    private PreferenceCategory mConnectedUsers;
    private HwCustWifiApClientManagement mCust;
    private ListPreference mDevicesAllowStatus;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Activity activity = WifiApClientManagement.this.getActivity();
            if (activity != null && !activity.isFinishing() && msg.what == 0) {
                WifiApClientManagement.this.updateConnectedDevices();
                WifiApClientManagement.this.updateAllowedDevices();
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.settings.wifi.action.connected_devices_changed".equals(intent.getAction())) {
                MLog.d("WifiApClientManagement", "ACTION_CONNECTED_DEVICES_CHANGED");
                WifiApClientManagement.this.updateConnectedDevices();
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                MLog.d("WifiApClientManagement", "WIFI_AP_STATE_CHANGED_ACTION");
                if (intent.getIntExtra("wifi_state", 14) == 11) {
                    WifiApClientManagement.this.updateConnectedDevices();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230935);
        PreferenceScreen screen = getPreferenceScreen();
        this.mCust = (HwCustWifiApClientManagement) HwCustUtils.createObj(HwCustWifiApClientManagement.class, new Object[]{this});
        this.mAllowedDevicesPref = (PreferenceCategory) screen.findPreference("current_allowed_device");
        this.mAllowedDevicesPref.setTitle((CharSequence) getResources().getQuantityString(2131689523, 8, new Object[]{Integer.valueOf(8)}));
        this.mAddDevicePref = (WifiApAddDevicePreference) screen.findPreference("add_allowed_device");
        this.mAddDevicePref.setListener(this);
        this.mAddDevicePref.setOrder(9);
        this.mConnectedUsers = (PreferenceCategory) screen.findPreference("current_connected_device");
        initDevicesAllowStatus();
        if (!WifiApClientUtils.getInstance(getActivity()).isSupportConnectManager()) {
            MLog.w("WifiApClientManagement", "add to allow is not supported");
            screen.removePreference(this.mAllowedDevicesPref);
            screen.removePreference(this.mDevicesAllowStatus);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDevicesAllowStatus() {
        this.mDevicesAllowStatus = (ListPreference) getPreferenceScreen().findPreference("devices_allow_status");
        if (this.mDevicesAllowStatus != null) {
            CharSequence[] entryValues = new CharSequence[]{String.valueOf(0), String.valueOf(1)};
            this.mDevicesAllowStatus.setEntries(new CharSequence[]{getResources().getString(2131627737), getResources().getString(2131627738)});
            this.mDevicesAllowStatus.setEntryValues(entryValues);
            this.mDevicesAllowStatus.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        System.putInt(WifiApClientManagement.this.getContentResolver(), "AllowConnectDevices", Integer.parseInt(newValue.toString()));
                        ItemUseStat.getInstance().handleClickListPreference(WifiApClientManagement.this.getActivity().getApplicationContext(), WifiApClientManagement.this.mDevicesAllowStatus, WifiApClientManagement.ALLOWED_DEVICE_ENTRIES, newValue.toString());
                        WifiApClientManagement.this.updateDevicesAllowStatus();
                        return true;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        MLog.e("WifiApClientManagement", "save devices allow status change error!");
                        return false;
                    }
                }
            });
            if (this.mCust != null) {
                this.mCust.enableDevicesAllowStatus();
            }
        }
    }

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.android.settings.wifi.action.connected_devices_changed");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
        updateDevicesAllowStatus();
        updateAllowedDevices();
        updateConnectedDevices();
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        WifiApClientUtils.getInstance(getActivity()).persistAllowedListIfChanged(getActivity());
    }

    public void onEditAllowedDevice(WifiApClientInfo info) {
        WifiApClientUtils.getInstance(getActivity()).editAllowedDevice(getActivity(), info);
        updateAllowedDevices();
        updateConnectedDevices();
    }

    public void onRemoveAllowedDevice(WifiApClientInfo info) {
        new WifiApClientNavigation(getActivity(), this).confirmToRemoveAllowedDevice(info);
    }

    public void onAddAllowedDevice(WifiApClientInfo info) {
        WifiApClientUtils.getInstance(getActivity()).addAllowedDevice(getActivity(), info);
        updateAllowedDevices();
        updateConnectedDevices();
    }

    public void onDeviceDisconnected() {
        updateConnectedDevices();
    }

    public void onDisconnectDevice(WifiApClientInfo info) {
        new WifiApClientNavigation(getActivity(), this).confirmToDisconnectDevice(info);
    }

    public void onDeviceRemoved() {
        updateAllowedDevices();
        updateConnectedDevices();
    }

    private void updateDevicesAllowStatus() {
        boolean z = false;
        if (this.mDevicesAllowStatus != null) {
            int value = System.getInt(getContentResolver(), "AllowConnectDevices", 0);
            WifiApClientUtils instance = WifiApClientUtils.getInstance(getActivity());
            Context activity = getActivity();
            if (value == 0) {
                z = true;
            }
            instance.allowAllDevices(activity, z);
            this.mDevicesAllowStatus.setValue(String.valueOf(value));
            this.mDevicesAllowStatus.setSummary(this.mDevicesAllowStatus.getEntries()[value]);
            if (value == 0 || !WifiApClientUtils.getInstance(getActivity()).isSupportConnectManager()) {
                getPreferenceScreen().removePreference(this.mAllowedDevicesPref);
            } else {
                getPreferenceScreen().addPreference(this.mAllowedDevicesPref);
            }
        }
    }

    private void updateAllowedDevices() {
        if (this.mAllowedDevicesPref != null && this.mAddDevicePref != null) {
            this.mAllowedDevicesPref.removeAll();
            WifiApClientUtils apClientUtils = WifiApClientUtils.getInstance(getActivity());
            List<WifiApClientInfo> allowedList = apClientUtils.getAllowedList();
            List<WifiApClientInfo> connectlist = apClientUtils.getConnectedList();
            for (int i = 0; i < allowedList.size(); i++) {
                WifiApClientInfo info = (WifiApClientInfo) allowedList.get(i);
                if (TextUtils.isEmpty(info.getDeviceName()) && connectlist != null) {
                    for (WifiApClientInfo tmpInfo : connectlist) {
                        if (tmpInfo.isMACEquals(info) && !TextUtils.isEmpty(tmpInfo.getDeviceName())) {
                            info.setDeviceName(tmpInfo.getDeviceName());
                            apClientUtils.addAllowedDevice(getActivity(), info);
                        }
                    }
                }
                WifiApEditDevicePreference pref = new WifiApEditDevicePreference(getActivity(), info, this);
                pref.setOrder(i);
                this.mAllowedDevicesPref.addPreference(pref);
            }
            this.mAllowedDevicesPref.addPreference(this.mAddDevicePref);
            this.mAddDevicePref.setEnabled(WifiApClientUtils.getInstance(getActivity()).canAddDevice());
        }
    }

    private void updateConnectedDevices() {
        if (this.mConnectedUsers != null) {
            this.mConnectedUsers.removeAll();
            List<WifiApClientInfo> connectlist = WifiApClientUtils.getInstance(getActivity()).getConnectedList();
            if (connectlist != null && connectlist.size() > 0) {
                for (WifiApClientInfo info : connectlist) {
                    this.mConnectedUsers.addPreference(new WifiApConnectedDevicePreference(getActivity(), info, this));
                    if (isNeedRefreshAgain(info)) {
                        this.mHandler.removeMessages(0);
                        this.mHandler.sendEmptyMessageDelayed(0, 5000);
                    }
                }
            }
        }
    }

    private boolean isNeedRefreshAgain(WifiApClientInfo info) {
        if (info.getDeviceName() != null && !info.getDeviceName().toString().equals("") && info.getIP() != null && !info.getIP().toString().equals("")) {
            return false;
        }
        if (SystemClock.elapsedRealtime() - info.getConnectedTime() <= 15000) {
            return true;
        }
        MLog.w("WifiApClientManagement", "We can't get client device name and ip after the limited time!");
        return false;
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
