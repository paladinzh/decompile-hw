package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

final class A2dpSinkProfile implements LocalBluetoothProfile {
    static final ParcelUuid[] SRC_UUIDS = new ParcelUuid[]{BluetoothUuid.AudioSource, BluetoothUuid.AdvAudioDist};
    private static boolean V = true;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothA2dpSink mService;

    private final class A2dpSinkServiceListener implements ServiceListener {
        private A2dpSinkServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service connected");
            }
            A2dpSinkProfile.this.mService = (BluetoothA2dpSink) proxy;
            List<BluetoothDevice> deviceList = A2dpSinkProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = (BluetoothDevice) deviceList.remove(0);
                CachedBluetoothDevice device = A2dpSinkProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    Log.w("A2dpSinkProfile", "A2dpSinkProfile found new device: " + nextDevice);
                    device = A2dpSinkProfile.this.mDeviceManager.addDevice(A2dpSinkProfile.this.mLocalAdapter, A2dpSinkProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(A2dpSinkProfile.this, 2);
                device.refresh();
            }
            A2dpSinkProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (A2dpSinkProfile.V) {
                Log.d("A2dpSinkProfile", "Bluetooth service disconnected");
            }
            A2dpSinkProfile.this.mIsProfileReady = false;
        }
    }

    A2dpSinkProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        this.mLocalAdapter.getProfileProxy(context, new A2dpSinkServiceListener(), 11);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null) {
            return new ArrayList(0);
        }
        return this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        List<BluetoothDevice> srcs = getConnectedDevices();
        if (srcs != null) {
            for (BluetoothDevice src : srcs) {
                if (src.equals(device)) {
                    Log.d("A2dpSinkProfile", "Ignoring Connect");
                    return true;
                }
            }
            for (BluetoothDevice src2 : srcs) {
                this.mService.disconnect(src2);
            }
        }
        return this.mService.connect(device);
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 100) {
            this.mService.setPriority(device, 100);
        }
        return this.mService.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(device);
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
        return "A2DPSink";
    }

    protected void finalize() {
        if (V) {
            Log.d("A2dpSinkProfile", "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(11, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w("A2dpSinkProfile", "Error cleaning up A2DP proxy", t);
            }
        }
    }
}
