package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settingslib.R$string;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class BluetoothPermissionActivity extends AlertActivity implements OnClickListener, OnPreferenceChangeListener {
    private CheckBox mChoiceSave;
    private BluetoothDevice mDevice;
    private Button mOkButton;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL".equals(intent.getAction()) && intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2) == BluetoothPermissionActivity.this.mRequestType) {
                if (BluetoothPermissionActivity.this.mDevice.equals((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"))) {
                    BluetoothPermissionActivity.this.dismissDialog();
                }
            }
        }
    };
    private boolean mReceiverRegistered = false;
    private int mRequestType = 0;
    private String mReturnClass = null;
    private String mReturnPackage = null;
    private View mView;
    private TextView messageView;

    private void dismissDialog() {
        dismiss();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if ("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST".equals(i.getAction())) {
            this.mDevice = (BluetoothDevice) i.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            this.mReturnPackage = i.getStringExtra("android.bluetooth.device.extra.PACKAGE_NAME");
            this.mReturnClass = i.getStringExtra("android.bluetooth.device.extra.CLASS_NAME");
            this.mRequestType = i.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2);
            HwLog.d("BluetoothPermissionActivity", "onCreate() mRequestType: " + this.mRequestType + ", mReturnClass=" + this.mReturnClass);
            if (this.mDevice == null) {
                HwLog.w("BluetoothPermissionActivity", "Null bluetooth device requested access.");
                finish();
                return;
            }
            if (this.mRequestType == 1) {
                showDialog(getString(2131624464), this.mRequestType);
            } else if (this.mRequestType == 2) {
                showDialog(getString(2131624467), this.mRequestType);
            } else if (this.mRequestType == 3) {
                showDialog(getString(2131627781), this.mRequestType);
            } else if (this.mRequestType == 4) {
                showDialog(getString(2131624473), this.mRequestType);
            } else {
                HwLog.e("BluetoothPermissionActivity", "Error: bad request type: " + this.mRequestType);
                finish();
                return;
            }
            getWindow().setGravity(80);
            registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL"));
            this.mReceiverRegistered = true;
            return;
        }
        HwLog.e("BluetoothPermissionActivity", "Error: this activity may be started only with intent ACTION_CONNECTION_ACCESS_REQUEST");
        finish();
    }

    private void showDialog(String title, int requestType) {
        AlertParams p = this.mAlertParams;
        p.mTitle = title;
        HwLog.i("BluetoothPermissionActivity", "showDialog() Request type: " + this.mRequestType + " this: " + this);
        switch (requestType) {
            case 1:
                p.mView = createConnectionDialogView();
                break;
            case 2:
                p.mView = createPhonebookDialogView();
                break;
            case 3:
                p.mView = createMapDialogView();
                break;
            case 4:
                p.mView = createSapDialogView();
                break;
        }
        if (requestType == 3) {
            p.mPositiveButtonText = getString(2131627783);
            p.mNegativeButtonText = getString(17039360);
        } else {
            p.mPositiveButtonText = getString(2131624348);
            p.mNegativeButtonText = getString(2131624349);
        }
        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;
        this.mOkButton = this.mAlert.getButton(-1);
        setupAlert();
    }

    public void onBackPressed() {
        HwLog.i("BluetoothPermissionActivity", "Back button pressed! ignoring");
    }

    private String createRemoteName() {
        String mRemoteName = null;
        if (this.mDevice != null) {
            mRemoteName = this.mDevice.getAliasName();
        }
        if (mRemoteName == null) {
            return getString(R$string.unknown);
        }
        return mRemoteName;
    }

    private View createConnectionDialogView() {
        String mRemoteName = createRemoteName();
        this.mView = getLayoutInflater().inflate(2130968648, null);
        this.messageView = (TextView) this.mView.findViewById(2131886296);
        this.messageView.setText(getString(2131624466, new Object[]{mRemoteName}));
        return this.mView;
    }

    private View createPhonebookDialogView() {
        String mRemoteName = createRemoteName();
        this.mView = getLayoutInflater().inflate(2130968648, null);
        this.mChoiceSave = (CheckBox) this.mView.findViewById(2131886297);
        this.mChoiceSave.setVisibility(0);
        this.messageView = (TextView) this.mView.findViewById(2131886296);
        this.messageView.setText(getString(2131624468, new Object[]{mRemoteName, mRemoteName}));
        return this.mView;
    }

    private View createMapDialogView() {
        String mRemoteName = createRemoteName();
        this.mView = getLayoutInflater().inflate(2130968648, null);
        this.messageView = (TextView) this.mView.findViewById(2131886296);
        this.messageView.setText(getString(2131627782, new Object[]{mRemoteName}));
        return this.mView;
    }

    private View createSapDialogView() {
        String mRemoteName = createRemoteName();
        this.mView = getLayoutInflater().inflate(2130968648, null);
        this.messageView = (TextView) this.mView.findViewById(2131886296);
        this.messageView.setText(getString(2131624474, new Object[]{mRemoteName, mRemoteName}));
        return this.mView;
    }

    private void onPositive() {
        HwLog.d("BluetoothPermissionActivity", "onPositive");
        savePermissionChoice(this.mRequestType, 1);
        sendReplyIntentToReceiver(true, true);
        finish();
    }

    private void onNegative() {
        HwLog.d("BluetoothPermissionActivity", "onNegative");
        savePermissionChoice(this.mRequestType, 2);
        boolean always = true;
        if (this.mRequestType == 3) {
            LocalBluetoothManager bluetoothManager = Utils.getLocalBtManager(this);
            if (bluetoothManager != null) {
                CachedBluetoothDeviceManager cachedDeviceManager = bluetoothManager.getCachedDeviceManager();
                CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(this.mDevice);
                if (cachedDevice == null) {
                    cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(), bluetoothManager.getProfileManager(), this.mDevice);
                }
                always = cachedDevice.checkAndIncreaseMessageRejectionCount();
            } else {
                return;
            }
        }
        sendReplyIntentToReceiver(false, always);
    }

    private void sendReplyIntentToReceiver(boolean allowed, boolean always) {
        int i;
        Intent intent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        if (!(this.mReturnPackage == null || this.mReturnClass == null)) {
            intent.setClassName(this.mReturnPackage, this.mReturnClass);
        }
        HwLog.i("BluetoothPermissionActivity", "sendReplyIntentToReceiver  mRequestType = " + this.mRequestType + ", always = " + always);
        String str = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
        if (allowed) {
            i = 1;
        } else {
            i = 2;
        }
        intent.putExtra(str, i);
        if (2 != this.mRequestType) {
            intent.putExtra("android.bluetooth.device.extra.ALWAYS_ALLOWED", always);
            HwLog.d("BluetoothPermissionActivity", "sendReplyIntentToReceiver  always allowed=" + always);
        }
        intent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
        intent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
        intent.setFlags(268435456);
        sendBroadcast(intent, "android.permission.BLUETOOTH_ADMIN");
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                onNegative();
                return;
            case -1:
                onPositive();
                return;
            default:
                return;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mReceiverRegistered) {
            unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void savePermissionChoice(int permissionType, int permissionChoice) {
        LocalBluetoothManager bluetoothManager = Utils.getLocalBtManager(this);
        if (bluetoothManager == null) {
            HwLog.w("BluetoothPermissionActivity", "bluetoothManager == null");
            return;
        }
        CachedBluetoothDeviceManager cachedDeviceManager = bluetoothManager.getCachedDeviceManager();
        CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(this.mDevice);
        if (cachedDevice == null) {
            cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(), bluetoothManager.getProfileManager(), this.mDevice);
        }
        HwLog.d("BluetoothPermissionActivity", "permissionType: " + permissionType + ", permissionChoice=" + permissionChoice);
        if (permissionType == 2) {
            if (permissionChoice == 1 && !this.mChoiceSave.isChecked()) {
                Utils.saveSprefData(this, this.mDevice.getAddress(), permissionChoice);
            }
            if (this.mChoiceSave.isChecked()) {
                cachedDevice.setPhonebookPermissionChoice(permissionChoice);
                try {
                    Editor editor = getSharedPreferences("pbap_caution_state", 0).edit();
                    editor.putInt(this.mDevice.getAddress(), 1);
                    editor.apply();
                } catch (Exception e) {
                    HwLog.e("BluetoothPermissionActivity", "Failed to update PBAP caution state.");
                    e.printStackTrace();
                }
            }
        }
    }
}
