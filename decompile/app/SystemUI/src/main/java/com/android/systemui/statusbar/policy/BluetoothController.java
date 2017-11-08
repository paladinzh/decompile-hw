package com.android.systemui.statusbar.policy;

import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import java.util.Collection;

public interface BluetoothController {

    public interface Callback {
        void onBluetoothDevicesChanged();

        void onBluetoothStateChange(boolean z);
    }

    void addStateChangedCallback(Callback callback);

    boolean canConfigBluetooth();

    void connect(CachedBluetoothDevice cachedBluetoothDevice);

    void disconnect(CachedBluetoothDevice cachedBluetoothDevice);

    Collection<CachedBluetoothDevice> getDevices();

    String getLastDeviceName();

    int getSuggestBluetoothIcon(int i);

    boolean isBluetoothConnected();

    boolean isBluetoothConnecting();

    boolean isBluetoothEnabled();

    boolean isBluetoothSupported();

    boolean isBluetoothTransfering();

    void removeStateChangedCallback(Callback callback);

    void setBluetoothBatteryEnable(boolean z);

    void setBluetoothEnabled(boolean z);
}
