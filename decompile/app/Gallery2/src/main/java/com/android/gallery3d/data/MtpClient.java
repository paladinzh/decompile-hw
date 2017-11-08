package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.mtp.MtpStorageInfo;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(12)
public class MtpClient {
    private final Context mContext;
    private final HashMap<String, MtpDevice> mDevices = new HashMap();
    private final ArrayList<String> mIgnoredDevices = new ArrayList();
    private final ArrayList<Listener> mListeners = new ArrayList();
    private final PendingIntent mPermissionIntent;
    private final ArrayList<String> mRequestPermissionDevices = new ArrayList();
    private final UsbManager mUsbManager;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra("device");
            if (usbDevice != null) {
                String deviceName = usbDevice.getDeviceName();
                synchronized (MtpClient.this.mDevices) {
                    MtpDevice mtpDevice = (MtpDevice) MtpClient.this.mDevices.get(deviceName);
                    if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                        if (mtpDevice == null) {
                            mtpDevice = MtpClient.this.openDeviceLocked(usbDevice);
                        }
                        if (mtpDevice != null) {
                            for (Listener listener : MtpClient.this.mListeners) {
                                listener.deviceAdded(mtpDevice);
                            }
                        }
                    } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                        if (mtpDevice != null) {
                            MtpClient.this.mDevices.remove(deviceName);
                            MtpClient.this.mRequestPermissionDevices.remove(deviceName);
                            MtpClient.this.mIgnoredDevices.remove(deviceName);
                            for (Listener listener2 : MtpClient.this.mListeners) {
                                listener2.deviceRemoved(mtpDevice);
                            }
                        }
                    } else if ("android.mtp.MtpClient.action.USB_PERMISSION".equals(action)) {
                        MtpClient.this.mRequestPermissionDevices.remove(deviceName);
                        boolean permission = intent.getBooleanExtra("permission", false);
                        GalleryLog.d("MtpClient", "ACTION_USB_PERMISSION: " + permission);
                        if (permission) {
                            if (mtpDevice == null) {
                                mtpDevice = MtpClient.this.openDeviceLocked(usbDevice);
                            }
                            if (mtpDevice != null) {
                                for (Listener listener22 : MtpClient.this.mListeners) {
                                    listener22.deviceAdded(mtpDevice);
                                }
                            }
                        } else {
                            MtpClient.this.mIgnoredDevices.add(deviceName);
                        }
                    }
                }
            }
        }
    };

    public interface Listener {
        void deviceAdded(MtpDevice mtpDevice);

        void deviceRemoved(MtpDevice mtpDevice);
    }

    public static boolean isCamera(UsbDevice device) {
        int count = device.getInterfaceCount();
        for (int i = 0; i < count; i++) {
            UsbInterface intf = device.getInterface(i);
            if (intf.getInterfaceClass() == 6 && intf.getInterfaceSubclass() == 1 && intf.getInterfaceProtocol() == 1) {
                return true;
            }
        }
        return false;
    }

    public MtpClient(Context context) {
        this.mContext = context;
        this.mUsbManager = (UsbManager) context.getSystemService("usb");
        this.mPermissionIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("android.mtp.MtpClient.action.USB_PERMISSION"), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        filter.addAction("android.mtp.MtpClient.action.USB_PERMISSION");
        context.registerReceiver(this.mUsbReceiver, filter);
    }

    private MtpDevice openDeviceLocked(UsbDevice usbDevice) {
        String deviceName = usbDevice.getDeviceName();
        if (!(!isCamera(usbDevice) || this.mIgnoredDevices.contains(deviceName) || this.mRequestPermissionDevices.contains(deviceName))) {
            if (this.mUsbManager.hasPermission(usbDevice)) {
                UsbDeviceConnection connection = this.mUsbManager.openDevice(usbDevice);
                if (connection != null) {
                    MtpDevice mtpDevice = new MtpDevice(usbDevice);
                    if (mtpDevice.open(connection)) {
                        this.mDevices.put(usbDevice.getDeviceName(), mtpDevice);
                        return mtpDevice;
                    }
                    this.mIgnoredDevices.add(deviceName);
                } else {
                    this.mIgnoredDevices.add(deviceName);
                }
            } else {
                this.mUsbManager.requestPermission(usbDevice, this.mPermissionIntent);
                this.mRequestPermissionDevices.add(deviceName);
            }
        }
        return null;
    }

    public void addListener(Listener listener) {
        synchronized (this.mDevices) {
            if (!this.mListeners.contains(listener)) {
                this.mListeners.add(listener);
            }
        }
    }

    public void removeListener(Listener listener) {
        synchronized (this.mDevices) {
            this.mListeners.remove(listener);
        }
    }

    public MtpDevice getDevice(String deviceName) {
        MtpDevice mtpDevice;
        synchronized (this.mDevices) {
            mtpDevice = (MtpDevice) this.mDevices.get(deviceName);
        }
        return mtpDevice;
    }

    public MtpDevice getDevice(int id) {
        MtpDevice mtpDevice;
        synchronized (this.mDevices) {
            mtpDevice = (MtpDevice) this.mDevices.get(UsbDevice.getDeviceName(id));
        }
        return mtpDevice;
    }

    public List<MtpDevice> getDeviceList() {
        List arrayList;
        synchronized (this.mDevices) {
            for (UsbDevice usbDevice : this.mUsbManager.getDeviceList().values()) {
                if (this.mDevices.get(usbDevice.getDeviceName()) == null) {
                    openDeviceLocked(usbDevice);
                }
            }
            arrayList = new ArrayList(this.mDevices.values());
        }
        return arrayList;
    }

    public List<MtpStorageInfo> getStorageList(String deviceName) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return null;
        }
        int[] storageIds = device.getStorageIds();
        if (storageIds == null) {
            return null;
        }
        ArrayList<MtpStorageInfo> storageList = new ArrayList(length);
        for (int storageInfo : storageIds) {
            MtpStorageInfo info = device.getStorageInfo(storageInfo);
            if (info == null) {
                GalleryLog.w("MtpClient", "getStorageInfo failed");
            } else {
                storageList.add(info);
            }
        }
        return storageList;
    }

    public MtpObjectInfo getObjectInfo(String deviceName, int objectHandle) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return null;
        }
        return device.getObjectInfo(objectHandle);
    }

    public List<MtpObjectInfo> getObjectList(String deviceName, int storageId, int objectHandle) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return null;
        }
        if (objectHandle == 0) {
            objectHandle = -1;
        }
        int[] handles = device.getObjectHandles(storageId, 0, objectHandle);
        if (handles == null) {
            return null;
        }
        ArrayList<MtpObjectInfo> objectList = new ArrayList(length);
        for (int objectInfo : handles) {
            MtpObjectInfo info = device.getObjectInfo(objectInfo);
            if (info == null) {
                GalleryLog.w("MtpClient", "getObjectInfo failed");
            } else {
                objectList.add(info);
            }
        }
        return objectList;
    }

    public byte[] getObject(String deviceName, int objectHandle, int objectSize) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return null;
        }
        return device.getObject(objectHandle, objectSize);
    }

    public byte[] getThumbnail(String deviceName, int objectHandle) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return null;
        }
        return device.getThumbnail(objectHandle);
    }

    public boolean importFile(String deviceName, int objectHandle, String destPath) {
        MtpDevice device = getDevice(deviceName);
        if (device == null) {
            return false;
        }
        return device.importFile(objectHandle, destPath);
    }
}
