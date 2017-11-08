package com.android.server.location;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import huawei.android.debug.HwDBGSwitchController;

public class HwGnssCommParam {
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    private static final int CONNECTED = 1;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int DISABLE = 0;
    private static final int DISCONNECTED = 0;
    private static final int ENABLE = 1;
    private static final String ORIENTATION_LANDSCAPE = "ORIENTATION_LANDSCAPE";
    private static final String ORIENTATION_PORTRAIT = "ORIENTATION_PORTRAIT";
    private static final String ORIENTATION_UNKNOWN = "ORIENTATION_UNKNOWN";
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_DISCONNECTING = 3;
    private static final String TAG = "HwGnssLog_CommParam";
    public static final String USB_CONNECTED = "connected";
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(HwGnssCommParam.ACTION_USB_STATE)) {
                if (intent.getBooleanExtra(HwGnssCommParam.USB_CONNECTED, false)) {
                    HwGnssCommParam.this.mUsbConnectState = 1;
                } else {
                    HwGnssCommParam.this.mUsbConnectState = 0;
                }
            }
        }
    };
    private Context mContext;
    private int mUsbConnectState = 0;

    public HwGnssCommParam(Context context) {
        this.mContext = context;
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_STATE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public String getScreenOrientation() {
        int ori = this.mContext.getResources().getConfiguration().orientation;
        String oriRes = ORIENTATION_UNKNOWN;
        if (ori == 2) {
            oriRes = ORIENTATION_LANDSCAPE;
        } else if (ori == 1) {
            oriRes = ORIENTATION_PORTRAIT;
        }
        if (DEBUG) {
            Log.d(TAG, "screen orientation is : " + oriRes);
        }
        return oriRes;
    }

    public int getBtSwitchState() {
        int btSwitch = 0;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.d(TAG, "bluetooth adapter is not avaibable!");
        } else if (btAdapter.isEnabled()) {
            btSwitch = 1;
        }
        if (DEBUG) {
            Log.d(TAG, "bt switch state is : " + btSwitch);
        }
        return btSwitch;
    }

    public int getBtConnectionState() {
        int btConnectState = 0;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.d(TAG, "bluetooth adapter is not avaibable!");
        } else if (btAdapter.getConnectionState() == 2) {
            btConnectState = 1;
        }
        if (DEBUG) {
            Log.d(TAG, "bt connection state is : " + btConnectState);
        }
        return btConnectState;
    }

    public int getNfcSwitchState() {
        int nfcSwitch = 0;
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter == null) {
            Log.d(TAG, "nfc adapter is not avaibable!");
        } else if (nfcAdapter.isEnabled()) {
            nfcSwitch = 1;
        }
        if (DEBUG) {
            Log.d(TAG, "NFC switch state is : " + nfcSwitch);
        }
        return nfcSwitch;
    }

    public int getUsbConnectState() {
        return this.mUsbConnectState;
    }
}
