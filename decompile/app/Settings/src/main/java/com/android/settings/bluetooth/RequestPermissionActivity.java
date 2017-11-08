package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class RequestPermissionActivity extends Activity implements OnClickListener {
    private AlertDialog mDialog;
    private boolean mEnableOnly;
    private LocalBluetoothAdapter mLocalAdapter;
    private boolean mNeededToEnableBluetooth;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && RequestPermissionActivity.this.mNeededToEnableBluetooth && "android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction()) && intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE) == 12 && RequestPermissionActivity.this.mUserConfirmed) {
                RequestPermissionActivity.this.proceedAndFinish();
            }
        }
    };
    private int mTimeout = 120;
    private boolean mUserConfirmed;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (parseIntent()) {
            finish();
            return;
        }
        getWindow().setBackgroundDrawableResource(17170445);
        int btState = this.mLocalAdapter.getState();
        switch (btState) {
            case 10:
            case 11:
            case 13:
                registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                Intent intent = new Intent();
                intent.setClass(this, RequestPermissionHelperActivity.class);
                if (this.mEnableOnly) {
                    intent.setAction("com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON");
                } else {
                    intent.setAction("com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE");
                    intent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", this.mTimeout);
                }
                startActivityForResult(intent, 1);
                this.mNeededToEnableBluetooth = true;
                break;
            case 12:
                proceedAndFinish();
                break;
            default:
                Log.e("RequestPermissionActivity", "Unknown adapter state: " + btState);
                break;
        }
    }

    private void createDialog() {
        Builder builder = new Builder(this);
        if (this.mNeededToEnableBluetooth) {
            builder.setMessage(getString(2131624461));
            builder.setCancelable(true);
        } else {
            if (this.mTimeout == 0) {
                builder.setMessage(getString(2131624455));
            } else {
                builder.setMessage(getResources().getQuantityString(2131689506, this.mTimeout, new Object[]{Integer.valueOf(this.mTimeout)}));
            }
            builder.setPositiveButton(getString(2131624351), this);
            builder.setNegativeButton(getString(2131624352), this);
        }
        this.mDialog = builder.create();
        this.mDialog.show();
        if (getResources().getBoolean(2131492875)) {
            onClick(null, -1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            Log.e("RequestPermissionActivity", "Unexpected onActivityResult " + requestCode + ' ' + resultCode);
            setResult(0);
            finish();
        } else if (resultCode != -1000) {
            setResult(resultCode);
            finish();
        } else {
            this.mUserConfirmed = true;
            if (this.mLocalAdapter.getBluetoothState() == 12) {
                proceedAndFinish();
            } else {
                createDialog();
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                setResult(0);
                finish();
                return;
            case -1:
                proceedAndFinish();
                return;
            default:
                return;
        }
    }

    private void proceedAndFinish() {
        int returnCode;
        if (this.mEnableOnly) {
            returnCode = -1;
        } else if (this.mLocalAdapter.setScanMode(23, this.mTimeout)) {
            long endTime = System.currentTimeMillis() + (((long) this.mTimeout) * 1000);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this, endTime);
            if (this.mTimeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this, endTime);
            }
            returnCode = this.mTimeout;
            if (returnCode < 1) {
                returnCode = 1;
            }
        } else {
            returnCode = 0;
        }
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        setResult(returnCode);
        finish();
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent != null && "android.bluetooth.adapter.action.REQUEST_ENABLE".equals(intent.getAction())) {
            this.mEnableOnly = true;
        } else if (intent == null || !"android.bluetooth.adapter.action.REQUEST_DISCOVERABLE".equals(intent.getAction())) {
            Log.e("RequestPermissionActivity", "Error: this activity may be started only with intent android.bluetooth.adapter.action.REQUEST_ENABLE or android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            setResult(0);
            return true;
        } else {
            this.mTimeout = BluetoothExtUtils.getDiscoverableTimeOut(intent, this);
            Log.d("RequestPermissionActivity", "Setting Bluetooth Discoverable Timeout = " + this.mTimeout);
            if (this.mTimeout < 0 || this.mTimeout > 3600) {
                this.mTimeout = 120;
            }
        }
        LocalBluetoothManager manager = Utils.getLocalBtManager(this);
        if (manager == null) {
            Log.e("RequestPermissionActivity", "Error: there's a problem starting Bluetooth");
            setResult(0);
            return true;
        }
        this.mLocalAdapter = manager.getBluetoothAdapter();
        return false;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mNeededToEnableBluetooth) {
            unregisterReceiver(this.mReceiver);
        }
    }

    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }
}
