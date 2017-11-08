package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settingslib.R$drawable;

public abstract class DevicePickerFragmentHwBase extends DeviceListPreferenceFragment {
    private Preference mDevice;
    private boolean mFirstScanStateChange = true;
    private IntentFilter mIntentFilter;
    protected boolean mIsAutoCloseBluetooth = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DevicePickerFragmentHwBase.this.handleStateChanged(DevicePickerFragmentHwBase.this.mLocalAdapter.getBluetoothState());
        }
    };

    public DevicePickerFragmentHwBase(String restrictedKey) {
        super(null);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDevice = findPreference("bt_scan");
        if (this.mDevice != null) {
            this.mDevice.setTitle(this.mLocalAdapter.getName());
            if (getResources().getBoolean(17956956)) {
                this.mDevice.setIcon((int) R$drawable.ic_bt_cellphone);
            } else {
                this.mDevice.setIcon(2130838217);
            }
            setHasOptionsMenu(true);
        }
        this.mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
        if (savedInstanceState != null) {
            this.mFirstScanStateChange = savedInstanceState.getBoolean("key_first_scan_changed", true);
        }
    }

    public void onDetach() {
        if (this.mLocalAdapter.isEnabled() && this.mIsAutoCloseBluetooth) {
            this.mLocalAdapter.disable();
        }
        super.onDetach();
    }

    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(this.mReceiver);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int textId;
        boolean isDiscovering = this.mLocalAdapter.isDiscovering();
        if (isDiscovering) {
            textId = 2131627572;
        } else {
            textId = 2131627571;
        }
        menu.add(0, 2, 0, textId).setTitle(getString(textId)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), isDiscovering ? Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE) : Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_SEARCH))).setShowAsAction(1);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                if (!this.mLocalAdapter.isDiscovering()) {
                    if (this.mLocalAdapter.getBluetoothState() == 12) {
                        removeAllDevices();
                        this.mLocalAdapter.startScanning(true);
                        break;
                    }
                }
                this.mLocalAdapter.stopScanning();
                this.mLocalAdapter.cancelDiscovery();
                break;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onScanningStateChanged(boolean started) {
        super.onScanningStateChanged(started);
        getActivity().invalidateOptionsMenu();
        if (this.mFirstScanStateChange) {
            this.mFirstScanStateChange = false;
            return;
        }
        if (!started) {
            displayFaqPreference();
        }
    }

    void handleStateChanged(int state) {
        if (state == 13) {
            getActivity().finish();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("key_first_scan_changed", this.mFirstScanStateChange);
        super.onSaveInstanceState(outState);
    }

    void initDevicePreference(BluetoothDevicePreference preference) {
        preference.setRefreshSummary(false);
        if (preference.getCachedDevice().getBondState() == 12) {
            preference.setWidgetLayoutResource(2130968998);
            preference.setSummary(2131628285);
        }
    }
}
