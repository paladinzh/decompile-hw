package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CachedBluetoothDeviceManager {
    private final LocalBluetoothManager mBtManager;
    private final List<CachedBluetoothDevice> mCachedDevices = new ArrayList();
    private Context mContext;

    CachedBluetoothDeviceManager(Context context, LocalBluetoothManager localBtManager) {
        this.mContext = context;
        this.mBtManager = localBtManager;
    }

    public synchronized Collection<CachedBluetoothDevice> getCachedDevicesCopy() {
        return new ArrayList(this.mCachedDevices);
    }

    public synchronized boolean onDeviceDisappeared(CachedBluetoothDevice cachedDevice) {
        if (cachedDevice == null) {
            return false;
        }
        cachedDevice.setVisible(false);
        return BluetoothExtUtils.checkForDeviceRemoval(this.mCachedDevices, cachedDevice);
    }

    public void onDeviceNameUpdated(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.refreshName();
        }
    }

    public CachedBluetoothDevice findDevice(BluetoothDevice device) {
        for (CachedBluetoothDevice cachedDevice : this.mCachedDevices) {
            if (cachedDevice.getDevice().equals(device)) {
                return cachedDevice;
            }
        }
        return null;
    }

    public CachedBluetoothDevice addDevice(LocalBluetoothAdapter adapter, LocalBluetoothProfileManager profileManager, BluetoothDevice device) {
        CachedBluetoothDevice newDevice = new CachedBluetoothDevice(this.mContext, adapter, profileManager, device);
        synchronized (this.mCachedDevices) {
            this.mCachedDevices.add(newDevice);
            this.mBtManager.getEventManager().dispatchDeviceAdded(newDevice);
        }
        return newDevice;
    }

    public String getName(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            return cachedDevice.getName();
        }
        String name = device.getAliasName();
        if (name != null) {
            return name;
        }
        return device.getAddress();
    }

    public synchronized void clearNonBondedDevices() {
        for (int i = this.mCachedDevices.size() - 1; i >= 0; i--) {
            if (((CachedBluetoothDevice) this.mCachedDevices.get(i)).getBondState() != 12) {
                this.mCachedDevices.remove(i);
            }
        }
    }

    public synchronized void onScanningStateChanged(boolean started) {
        if (started) {
            for (int i = this.mCachedDevices.size() - 1; i >= 0; i--) {
                ((CachedBluetoothDevice) this.mCachedDevices.get(i)).setVisible(false);
            }
            BluetoothExtUtils.broadcastAllDevicesRemoved(this.mCachedDevices, this.mContext);
        }
    }

    public synchronized void onBtClassChanged(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.refreshBtClass();
        }
    }

    public synchronized void onUuidChanged(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.onUuidChanged();
        }
    }

    public synchronized void onBluetoothStateChanged(int bluetoothState) {
        int i;
        if (bluetoothState == 13) {
            for (i = this.mCachedDevices.size() - 1; i >= 0; i--) {
                CachedBluetoothDevice cachedDevice = (CachedBluetoothDevice) this.mCachedDevices.get(i);
                if (cachedDevice.getBondState() != 12) {
                    cachedDevice.setVisible(false);
                    this.mCachedDevices.remove(i);
                } else {
                    cachedDevice.clearProfileConnectionState();
                }
            }
        } else if (bluetoothState == 10) {
            for (i = this.mCachedDevices.size() - 1; i >= 0; i--) {
                ((CachedBluetoothDevice) this.mCachedDevices.get(i)).clearProfileConnectionState();
            }
        }
    }
}
