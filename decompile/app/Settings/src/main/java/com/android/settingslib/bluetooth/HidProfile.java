package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import java.util.List;

public final class HidProfile implements LocalBluetoothProfile {
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothInputDevice mService;

    private final class InputDeviceServiceListener implements ServiceListener {
        private InputDeviceServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            HwLog.d("HidProfile", "Bluetooth service connected");
            HidProfile.this.mService = (BluetoothInputDevice) proxy;
            List<BluetoothDevice> deviceList = HidProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = (BluetoothDevice) deviceList.remove(0);
                CachedBluetoothDevice device = HidProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    HwLog.w("HidProfile", "HidProfile found new device: " + nextDevice);
                    device = HidProfile.this.mDeviceManager.addDevice(HidProfile.this.mLocalAdapter, HidProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(HidProfile.this, 2);
                device.refresh();
            }
            HidProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            HwLog.d("HidProfile", "Bluetooth service disconnected");
            HidProfile.this.mIsProfileReady = false;
        }
    }

    HidProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        adapter.getProfileProxy(context, new InputDeviceServiceListener(), 4);
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.connect(device);
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        int i;
        List<BluetoothDevice> deviceList = this.mService.getConnectedDevices();
        if (deviceList.isEmpty() || !((BluetoothDevice) deviceList.get(0)).equals(device)) {
            i = 0;
        } else {
            i = this.mService.getConnectionState(device);
        }
        return i;
    }

    public boolean isPreferred(BluetoothDevice device) {
        boolean z = false;
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 0) {
            z = true;
        }
        return z;
    }

    public int getPreferred(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getPriority(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (this.mService != null) {
            if (!preferred) {
                this.mService.setPriority(device, 0);
            } else if (this.mService.getPriority(device) < 100) {
                this.mService.setPriority(device, 100);
            }
        }
    }

    public String toString() {
        return "HID";
    }

    public int getOrdinal() {
        return 3;
    }

    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_hid;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        switch (state) {
            case 0:
                return R$string.bluetooth_hid_profile_summary_use_for;
            case 2:
                return R$string.bluetooth_hid_profile_summary_connected;
            default:
                return Utils.getConnectionStateSummary(state);
        }
    }

    public int getDrawableResource(BluetoothClass btClass) {
        if (btClass == null) {
            return R$drawable.ic_bt_keyboard_hid;
        }
        return getHidClassDrawable(btClass);
    }

    public static int getHidClassDrawable(BluetoothClass btClass) {
        switch (btClass.getDeviceClass()) {
            case 1344:
            case 1472:
                return R$drawable.ic_bt_keyboard_hid;
            case 1408:
                return R$drawable.ic_bt_pointing_hid;
            case 1796:
                return R$drawable.ic_bt_watch;
            default:
                return R$drawable.ic_bt_misc_hid;
        }
    }

    protected void finalize() {
        HwLog.d("HidProfile", "finalize()");
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                HwLog.w("HidProfile", "Error cleaning up HID proxy", t);
            }
        }
    }
}
