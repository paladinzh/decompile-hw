package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbap;
import android.bluetooth.BluetoothPbap.ServiceListener;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;

public final class PbapServerProfile implements LocalBluetoothProfile {
    static final ParcelUuid[] PBAB_CLIENT_UUIDS = new ParcelUuid[]{BluetoothUuid.HSP, BluetoothUuid.Handsfree, BluetoothUuid.PBAP_PCE};
    private boolean mIsProfileReady;
    private BluetoothPbap mService;

    private final class PbapServiceListener implements ServiceListener {
        private PbapServiceListener() {
        }

        public void onServiceConnected(BluetoothPbap proxy) {
            HwLog.d("PbapServerProfile", "Bluetooth service connected");
            PbapServerProfile.this.mService = proxy;
            PbapServerProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected() {
            HwLog.d("PbapServerProfile", "Bluetooth service disconnected");
            PbapServerProfile.this.mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    PbapServerProfile(Context context) {
        BluetoothPbap pbap = new BluetoothPbap(context, new PbapServiceListener());
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        HwLog.d("PbapServerProfile", "connect() - should not get called from server");
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        boolean isDisconn = false;
        if (this.mService == null || device == null) {
            HwLog.e("PbapServerProfile", "disconnect  mService is " + this.mService + " or device is null");
            return false;
        }
        if (this.mService.isConnected(device)) {
            isDisconn = this.mService.disconnect();
        }
        HwLog.d("PbapServerProfile", "disconnect  isDisconn=" + isDisconn);
        return isDisconn;
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService != null && this.mService.isConnected(device)) {
            return 2;
        }
        return 0;
    }

    public boolean isPreferred(BluetoothDevice device) {
        return false;
    }

    public int getPreferred(BluetoothDevice device) {
        return -1;
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
    }

    public String toString() {
        return "PBAP Server";
    }

    public int getOrdinal() {
        return 6;
    }

    public int getNameResource(BluetoothDevice device) {
        return R$string.bluetooth_profile_pbap;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        return R$string.bluetooth_profile_pbap_summary;
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R$drawable.ic_bt_cellphone;
    }

    public static ParcelUuid[] getPbapClientUuids() {
        int length = PBAB_CLIENT_UUIDS.length;
        ParcelUuid[] clentUuids = new ParcelUuid[length];
        for (int i = 0; i < length; i++) {
            clentUuids[i] = PBAB_CLIENT_UUIDS[i];
        }
        return clentUuids;
    }

    protected void finalize() {
        HwLog.d("PbapServerProfile", "finalize()");
        if (this.mService != null) {
            try {
                this.mService.close();
                this.mService = null;
            } catch (Throwable t) {
                HwLog.w("PbapServerProfile", "Error cleaning up PBAP proxy", t);
            }
        }
    }
}
