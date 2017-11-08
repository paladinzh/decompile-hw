package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;

public class HwCustLocalBluetoothProfileManagerImpl extends HwCustLocalBluetoothProfileManager {
    private static final String LOG_TAG = "HwcustLocalBluetoothProfileManagerImpl";
    private String connect_currentState = "";
    private String devicename = null;
    private String disconnect_currentState = "";
    private boolean isConnected = false;
    private Context mContext;
    private CachedBluetoothDeviceManager mDeviceManager;

    public HwCustLocalBluetoothProfileManagerImpl(Context context, CachedBluetoothDeviceManager deviceManager) {
        super(context, deviceManager);
        this.mContext = context;
        this.mDeviceManager = deviceManager;
    }

    public void handleCustIntent(Intent intent, BluetoothDevice device) {
        int newState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
        int oldState = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
        Log.d(LOG_TAG, "newState:" + newState + ", oldState:" + oldState);
        CachedBluetoothDevice cachedDevice = this.mDeviceManager.findDevice(device);
        if (cachedDevice != null) {
            this.devicename = cachedDevice.getName();
            this.isConnected = cachedDevice.isConnected();
            Log.d(LOG_TAG, "isConnected:[" + this.isConnected + "]");
            this.connect_currentState = this.mContext.getResources().getString(2131629053);
            this.connect_currentState = String.format(this.connect_currentState, new Object[]{this.devicename});
            this.disconnect_currentState = this.mContext.getResources().getString(2131629052);
            this.disconnect_currentState = String.format(this.disconnect_currentState, new Object[]{this.devicename});
            if (SystemProperties.getBoolean("ro.config.earphone_hint", false)) {
                if (newState == 2 && oldState != newState && this.isConnected) {
                    Toast.makeText(this.mContext, this.connect_currentState, 0).show();
                }
                if (!(newState != 0 || oldState == newState || this.isConnected)) {
                    Toast.makeText(this.mContext, this.disconnect_currentState, 0).show();
                }
            }
        }
    }
}
