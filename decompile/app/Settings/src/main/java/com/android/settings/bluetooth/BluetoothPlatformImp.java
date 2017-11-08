package com.android.settings.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.settings.MLog;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.huawei.cust.HwCustUtils;

public class BluetoothPlatformImp extends BluetoothExtAbsBase {
    public HwCustBluetoothPlatformImp mHwCustBluetoothPlatformImp = ((HwCustBluetoothPlatformImp) HwCustUtils.createObj(HwCustBluetoothPlatformImp.class, new Object[0]));

    static class BluetoothBeamUrisCallback implements CreateBeamUrisCallback {
        BluetoothBeamUrisCallback() {
        }

        public Uri[] createBeamUris(NfcEvent event) {
            return new Uri[]{Uri.parse("content://huawei/btconnect/1")};
        }
    }

    public void setBeamPushUrisCallback(Activity activity) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter != null) {
            nfcAdapter.setBeamPushUrisCallback(new BluetoothBeamUrisCallback(), activity);
        } else {
            MLog.e("BluetoothPlatformImp", "in setBeamPushUrisCallback, nfcAdapter is null!");
        }
    }

    public void handleAdapterStateChange(LocalBluetoothAdapter adapter, int state, Context context) {
        if (context == null || adapter == null) {
            MLog.e("BluetoothPlatformImp", "in handleAdapterStateChange, params is error!");
            return;
        }
        if (12 == state) {
            if (this.mHwCustBluetoothPlatformImp == null || this.mHwCustBluetoothPlatformImp.getBluetoothDiscoverable()) {
                makeDeviceDiscoverable(adapter, PreferenceManager.getDefaultSharedPreferences(context).getInt("bt_discoverable_timeout_number", 120), context);
            } else {
                MLog.i("BluetoothPlatformImp", "close bluetooth discovery");
            }
        }
    }

    private boolean makeDeviceDiscoverable(LocalBluetoothAdapter adapter, int timeout, Context context) {
        boolean discoverable = adapter.setScanMode(23, timeout);
        Log.i("BluetoothPlatformImp", "makeDeviceDiscoverable isDiscoverable = " + discoverable + " DiscoverableTime = " + timeout);
        if (!discoverable) {
            return false;
        }
        long endTime = System.currentTimeMillis() + (((long) timeout) * 1000);
        LocalBluetoothPreferences.persistDiscoverableEndTimestamp(context, endTime);
        if (timeout > 0) {
            BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(context, endTime);
        }
        return true;
    }

    public int getDiscoverableTimeOut(Intent intent, Context context) {
        if (intent == null) {
            return 120;
        }
        int timeout;
        if (intent.hasExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION")) {
            timeout = intent.getIntExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 120);
        } else {
            timeout = PreferenceManager.getDefaultSharedPreferences(context).getInt("bt_discoverable_timeout_number", 120);
        }
        return timeout;
    }
}
