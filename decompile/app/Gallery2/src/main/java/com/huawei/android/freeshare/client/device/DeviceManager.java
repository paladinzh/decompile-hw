package com.huawei.android.freeshare.client.device;

import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.List;

public class DeviceManager {
    private List<DeviceInfo> mDevices = new ArrayList(8);
    private List<DeviceChangeListener> mListeners = new ArrayList();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addDevice(DeviceInfo device) {
        GalleryLog.d("freeshare_DeviceManger", "device mac = " + device.getMacAddress());
        boolean has = false;
        synchronized (this) {
            for (DeviceInfo d : this.mDevices) {
                if (d.equal(device)) {
                    has = true;
                    if (d.getName().equals(device.getName())) {
                        return;
                    }
                    d.setName(device.getName());
                    if (!has) {
                        this.mDevices.add(device);
                    }
                }
            }
            if (has) {
                this.mDevices.add(device);
            }
        }
    }

    public final synchronized List<DeviceInfo> getDeviceList() {
        return this.mDevices;
    }

    public synchronized void clearDevices() {
        this.mDevices.clear();
    }

    public void addDeviceChangeListener(DeviceChangeListener l) {
        this.mListeners.add(l);
    }

    public void removeDeviceChangeListener(DeviceChangeListener l) {
        this.mListeners.remove(l);
    }
}
