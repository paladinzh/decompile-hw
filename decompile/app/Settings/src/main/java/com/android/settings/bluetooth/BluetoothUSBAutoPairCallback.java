package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.android.settingslib.R$string;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;

public class BluetoothUSBAutoPairCallback implements BluetoothCallback, Callback {
    private static CachedBluetoothDevice mCachedDevice = null;
    private static boolean mHasShowSuccessInfo = false;
    private static final SparseIntArray mStatusTipMap = new SparseIntArray() {
        {
            put(4, 2131628531);
            put(3, 2131628532);
        }
    };
    private BluetoothAdapter mBluetoothAdapter = null;
    private final Context mContext;
    private BluetoothUsbAutoPairUtils mUtils = null;

    private static void setCachedDevice(CachedBluetoothDevice cachedDevice) {
        mCachedDevice = cachedDevice;
    }

    private static void setHasShowSuccessInfo(boolean hasShowSuccessInfo) {
        mHasShowSuccessInfo = hasShowSuccessInfo;
    }

    public BluetoothUSBAutoPairCallback(Context context, BluetoothUsbAutoPairUtils utils) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mUtils = utils;
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == 12) {
            HwLog.i("BTUAutoPair", "onBluetoothStateChanged");
            if (this.mBluetoothAdapter != null) {
                this.mBluetoothAdapter.startDiscovery();
            }
        }
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        HwLog.i("BTUAutoPair", "onDeviceAdded");
        if (this.mBluetoothAdapter != null && isSmallWhistle(cachedDevice) && this.mBluetoothAdapter.isDiscovering() && cachedDevice.getBondState() == 10) {
            HwLog.i("BTUAutoPair", "onDeviceAdded: pair.");
            if (!cachedDevice.startPairing()) {
                Utils.showError(this.mContext, cachedDevice.getName(), R$string.bluetooth_pairing_error_message_Toast);
            }
            setCachedDevice(cachedDevice);
            mCachedDevice.registerCallback(this);
        }
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        if (isSmallWhistle(cachedDevice)) {
            HwLog.i("BTUAutoPair", "onDeviceDeleted: cachedDevice.unregisterCallback" + cachedDevice);
            cachedDevice.unregisterCallback(this);
        }
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (isSmallWhistle(cachedDevice) && 12 == bondState) {
            handlePaired(cachedDevice);
        }
    }

    public void onDeviceAttributesChanged() {
        if (!isSmallWhistle(mCachedDevice)) {
            return;
        }
        if (!isConnected(mCachedDevice)) {
            setHasShowSuccessInfo(false);
        } else if (!mHasShowSuccessInfo) {
            handleConnected(this.mContext, mCachedDevice);
            setHasShowSuccessInfo(true);
        }
    }

    private boolean isSmallWhistle(CachedBluetoothDevice cachedDevice) {
        if (cachedDevice != null) {
            return cachedDevice.getDevice().getAddress().equals(this.mUtils.getDeviceAddress());
        }
        return false;
    }

    private void handlePaired(CachedBluetoothDevice cachedDevice) {
        HwLog.i("BTUAutoPair", "handlePaired() cachedDevice = " + cachedDevice);
        cachedDevice.connect(true);
        cachedDevice.setHumanConnect(true);
    }

    private void handleConnected(Context context, CachedBluetoothDevice cachedDevice) {
        HwLog.i("BTUAutoPair", "handleConnected() mBluetoothAdapter.isDiscovering() = " + this.mBluetoothAdapter.isDiscovering());
        showToastMessage(context, 4);
        context.stopService(new Intent(context, BluetoothUSBService.class));
    }

    public void handlePairingFailure(Context context) {
        HwLog.i("BTUAutoPair", "handlePairingFailure()");
        showToastMessage(context, 3);
    }

    private void showToastMessage(Context context, int status) {
        Toast.makeText(context, mStatusTipMap.get(status), 0).show();
    }

    private boolean isConnected(CachedBluetoothDevice cachedDevice) {
        boolean z;
        boolean a2dpConnected = false;
        boolean headsetConnected = false;
        for (LocalBluetoothProfile profile : cachedDevice.getConnectableProfiles()) {
            int connectionStatus = cachedDevice.getProfileConnectionState(profile);
            if (2 == connectionStatus && profile.isProfileReady()) {
                if (profile instanceof A2dpProfile) {
                    a2dpConnected = true;
                } else if (profile instanceof HeadsetProfile) {
                    headsetConnected = true;
                }
            }
            HwLog.i("BTUAutoPair", "isConnected: profile: " + profile + " ; getConnectState: " + connectionStatus);
        }
        String str = "BTUAutoPair";
        StringBuilder append = new StringBuilder().append("isConnected: result = ");
        if (a2dpConnected) {
            z = headsetConnected;
        } else {
            z = false;
        }
        HwLog.i(str, append.append(z).toString());
        if (a2dpConnected) {
            return headsetConnected;
        }
        return false;
    }
}
