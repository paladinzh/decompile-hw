package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface LocalBluetoothProfile {
    boolean connect(BluetoothDevice bluetoothDevice);

    boolean disconnect(BluetoothDevice bluetoothDevice);

    int getConnectionStatus(BluetoothDevice bluetoothDevice);

    boolean isAutoConnectable();

    boolean isConnectable();

    boolean isPreferred(BluetoothDevice bluetoothDevice);

    void setPreferred(BluetoothDevice bluetoothDevice, boolean z);
}
