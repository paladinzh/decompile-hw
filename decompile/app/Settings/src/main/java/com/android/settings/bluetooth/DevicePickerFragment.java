package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;

public final class DevicePickerFragment extends DevicePickerFragmentHwBase {
    private String mLaunchClass;
    private String mLaunchPackage;
    private boolean mNeedAuth;
    private boolean mStartScanOnResume;

    public DevicePickerFragment() {
        super(null);
    }

    void addPreferencesForActivity() {
        addPreferencesFromResource(2131230779);
        Intent intent = getActivity().getIntent();
        this.mNeedAuth = intent.getBooleanExtra("android.bluetooth.devicepicker.extra.NEED_AUTH", false);
        setFilter(intent.getIntExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 0));
        this.mLaunchPackage = intent.getStringExtra("android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE");
        this.mLaunchClass = intent.getStringExtra("android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS");
        this.mIsAutoCloseBluetooth = intent.getBooleanExtra("android.bluetoothimport.extra.DISABLEBLUETOOTH", false);
    }

    protected int getMetricsCategory() {
        return 25;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(2131627574));
        if (!((UserManager) getSystemService("user")).hasUserRestriction("no_config_bluetooth") && savedInstanceState == null) {
            z = true;
        }
        this.mStartScanOnResume = z;
    }

    public void onResume() {
        super.onResume();
        addCachedDevices();
        if (this.mStartScanOnResume) {
            this.mLocalAdapter.startScanning(true);
            this.mStartScanOnResume = false;
        }
        onScanningStateChanged(this.mLocalAdapter.isDiscovering());
    }

    boolean onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        this.mLocalAdapter.stopScanning();
        LocalBluetoothPreferences.persistSelectedDeviceInPicker(getActivity(), this.mSelectedDevice.getAddress());
        if (btPreference.getCachedDevice().getBondState() != 12 && this.mNeedAuth) {
            return super.onDevicePreferenceClick(btPreference);
        }
        sendDevicePickedIntent(this.mSelectedDevice);
        finish();
        return true;
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (bondState == 12) {
            BluetoothDevice device = cachedDevice.getDevice();
            if (device.equals(this.mSelectedDevice)) {
                sendDevicePickedIntent(device);
                finish();
            }
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        if (bluetoothState == 12) {
            this.mLocalAdapter.startScanning(false);
        }
    }

    private void sendDevicePickedIntent(BluetoothDevice device) {
        Intent intent = new Intent("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        intent.putExtra("android.bluetooth.device.extra.DEVICE", device);
        if (!(this.mLaunchPackage == null || this.mLaunchClass == null)) {
            intent.setClassName(this.mLaunchPackage, this.mLaunchClass);
        }
        getActivity().sendBroadcast(intent);
        this.mIsAutoCloseBluetooth = false;
    }
}
