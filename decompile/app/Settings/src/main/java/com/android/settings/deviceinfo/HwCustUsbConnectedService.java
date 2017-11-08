package com.android.settings.deviceinfo;

import android.content.ContentResolver;
import android.content.Context;
import android.widget.RemoteViews;

public class HwCustUsbConnectedService {
    public UsbConnectedService mUsbConnectedService = null;

    public HwCustUsbConnectedService(UsbConnectedService usbConnectedService) {
        this.mUsbConnectedService = usbConnectedService;
    }

    public boolean isSupportUsbLimit() {
        return false;
    }

    public void startSimUsbLimitActivity(Context mContext) {
    }

    public void initControlAndStartSimUsbLimitActivity(RemoteViews mRemoteView, Context mContext) {
    }

    public CharSequence getChargeOnlyTitle(Context mContext) {
        return null;
    }

    public void registerSimActiviteUSBObserver(ContentResolver contentResolver) {
    }

    public void unregisterSimActiviteUSBObserver(ContentResolver contentResolver) {
    }

    public void custRegisterUsbReceiver(Context context) {
    }

    public void custUnRegisterUsbReceiver(Context context) {
    }

    public boolean custHandleUsbRestriction(Context context) {
        return false;
    }

    public void custUsbDisconnected(Context context) {
    }

    public boolean isHideHisuiteSupport() {
        return false;
    }
}
