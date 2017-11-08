package com.android.settings.wifi.ap;

import android.content.Context;

public class WifiApClientNavigation implements WifiConfirmDialogListener {
    private Context mContext;
    private WifiApClientInfo mInfo;
    private WifiApClientListener mListener;

    interface WifiApClientListener {
        void onDeviceDisconnected();

        void onDeviceRemoved();
    }

    public WifiApClientNavigation(Context context, WifiApClientListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void confirmToRemoveAllowedDevice(WifiApClientInfo info) {
        this.mInfo = info;
        new WifiConfirmDialog(this.mContext, null, loadDialogMessage(2131627428)).showDialog(this, 1);
    }

    public void confirmToDisconnectDevice(WifiApClientInfo info) {
        this.mInfo = info;
        new WifiConfirmDialog(this.mContext, loadDialogMessage(2131627430), 2131627429).showDialog(this, 2);
    }

    public void onCancel(int dialogId) {
    }

    public void onOk(int dialogId) {
        if (dialogId == 1) {
            removeDevice();
        } else if (dialogId == 2) {
            disconnectDevice();
        }
    }

    private void removeDevice() {
        WifiApClientUtils.getInstance(this.mContext).removeAllowedDevice(this.mContext, this.mInfo);
        if (this.mListener != null) {
            this.mListener.onDeviceRemoved();
        }
    }

    private String loadDialogMessage(int resMsgId) {
        if (this.mInfo == null) {
            return "";
        }
        CharSequence device = this.mInfo.getDeviceName() != null ? this.mInfo.getDeviceName() : this.mInfo.getMAC();
        return String.format(this.mContext.getString(resMsgId), new Object[]{device});
    }

    private void disconnectDevice() {
        WifiApClientUtils.getInstance(this.mContext).disconnectDevice(this.mInfo);
        if (this.mListener != null) {
            this.mListener.onDeviceDisconnected();
        }
    }
}
