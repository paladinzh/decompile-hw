package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;

final class OppProfile implements LocalBluetoothProfile {
    OppProfile() {
    }

    public boolean isConnectable() {
        return false;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        return false;
    }

    public int getConnectionStatus(BluetoothDevice device) {
        return 0;
    }

    public boolean isPreferred(BluetoothDevice device) {
        return false;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return "OPP";
    }
}
