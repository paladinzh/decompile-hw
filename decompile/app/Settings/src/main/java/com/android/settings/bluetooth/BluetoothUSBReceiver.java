package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.SystemProperties;

public final class BluetoothUSBReceiver extends BroadcastReceiver {
    private static final boolean mIsUsbAutoPairFeature = SystemProperties.getBoolean("ro.config.hw_bt_usb_autopair", false);

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && mIsUsbAutoPairFeature) {
            String action = intent.getAction();
            HwLog.i("BTUReceiver", "onReceive: action = " + intent.getAction());
            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                handleUSBDeviceAttached(context, intent);
            } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                handleUSBDeviceDetached(context, intent);
            } else if ("com.android.settings.BTU_AGREE".equals(action)) {
                handleUserAgreePair(context);
            } else if ("com.android.settings.BTU_CANCEL".equals(action)) {
                handleUserCancelPair(context);
            }
        }
    }

    private void handleUSBDeviceAttached(Context context, Intent intent) {
        UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra("device");
        if (usbDevice != null) {
            HwLog.e("BTUReceiver", "NAME:" + usbDevice.getDeviceName() + " VID:" + usbDevice.getVendorId() + " PID:" + usbDevice.getProductId());
            Bundle args = new Bundle();
            args.putParcelable("usbdevice", intent.getParcelableExtra("device"));
            args.putInt("cmdmessage", 1);
            startUSBService(context, args);
        }
    }

    private void handleUSBDeviceDetached(Context context, Intent intent) {
        Bundle args = new Bundle();
        args.putParcelable("usbdevice", intent.getParcelableExtra("device"));
        args.putInt("cmdmessage", 2);
        startUSBService(context, args);
    }

    private void handleUserAgreePair(Context context) {
        Bundle args = new Bundle();
        args.putInt("cmdmessage", 3);
        startUSBService(context, args);
    }

    private void handleUserCancelPair(Context context) {
        Bundle args = new Bundle();
        args.putInt("cmdmessage", 4);
        startUSBService(context, args);
    }

    private void startUSBService(Context context, Bundle args) {
        context.startService(new Intent(context, BluetoothUSBService.class).putExtras(args));
    }
}
