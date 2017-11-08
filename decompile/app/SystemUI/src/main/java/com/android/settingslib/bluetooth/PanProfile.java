package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import java.util.HashMap;
import java.util.List;

public final class PanProfile implements LocalBluetoothProfile {
    private final HashMap<BluetoothDevice, Integer> mDeviceRoleMap = new HashMap();
    private boolean mIsProfileReady;
    private BluetoothPan mService;

    private final class PanServiceListener implements ServiceListener {
        private PanServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            HwLog.d("PanProfile", "Bluetooth service connected");
            PanProfile.this.mService = (BluetoothPan) proxy;
            PanProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            HwLog.d("PanProfile", "Bluetooth service disconnected");
            PanProfile.this.mIsProfileReady = false;
        }
    }

    PanProfile(Context context) {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, new PanServiceListener(), 5);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            HwLog.e("PanProfile", "connect() - mService=" + this.mService);
            return false;
        }
        List<BluetoothDevice> sinks = this.mService.getConnectedDevices();
        if (sinks != null) {
            HwLog.d("PanProfile", "connect() - connected size=" + sinks.size() + ", disconnect all device and connect");
            for (BluetoothDevice sink : sinks) {
                this.mService.disconnect(sink);
            }
        }
        boolean isConn = this.mService.connect(device);
        HwLog.d("PanProfile", "connect() - isConn=" + isConn);
        return isConn;
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            HwLog.e("PanProfile", "disconnect() - mService=" + this.mService);
            return false;
        }
        boolean isDisconn = this.mService.disconnect(device);
        HwLog.d("PanProfile", "disconnect() - isDisconn=" + isDisconn);
        return isDisconn;
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(device);
    }

    public boolean isPreferred(BluetoothDevice device) {
        return true;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return "PAN";
    }

    void setLocalRole(BluetoothDevice device, int role) {
        this.mDeviceRoleMap.put(device, Integer.valueOf(role));
    }

    boolean isLocalRoleNap(BluetoothDevice device) {
        if (!this.mDeviceRoleMap.containsKey(device)) {
            return false;
        }
        return ((Integer) this.mDeviceRoleMap.get(device)).intValue() == 1;
    }

    protected void finalize() {
        HwLog.d("PanProfile", "finalize()");
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(5, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                HwLog.w("PanProfile", "Error cleaning up PAN proxy", t);
            }
        }
    }
}
