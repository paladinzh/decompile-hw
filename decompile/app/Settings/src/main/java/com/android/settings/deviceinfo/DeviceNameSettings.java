package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.settings.ItemUseStat;
import com.android.settings.Utf8ByteLengthFilter;
import com.android.settings.bluetooth.Utils;
import com.android.settings.wifi.ap.WifiApClientInfo;
import com.android.settings.wifi.ap.WifiApClientUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.nio.charset.Charset;
import java.util.List;

public class DeviceNameSettings extends SettingsDrawerActivity implements TextWatcher, OnClickListener {
    private boolean mApChangedWhenEnabled = false;
    private Button mCancleButton;
    private String mDeviceName;
    private EditText mEditText;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (DeviceNameSettings.this.mApChangedWhenEnabled) {
                        Log.e("DeviceNameSettings", "Dd not receive WIFI_AP_DISABLED after: 5000");
                        DeviceNameSettings.this.finishAndReturn();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ActionListener mListener = new ActionListener() {
        public void onSuccess() {
        }

        public void onFailure(int reason) {
            Log.e("DeviceNameSettings", "Error: set wifi p2p name error!: " + reason);
        }
    };
    private Button mOkButton;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                Log.d("DeviceNameSettings", "WIFI_AP_STATE_CHANGED_ACTION");
                DeviceNameSettings.this.handleWifiApStateChanged(intent.getIntExtra("wifi_state", 14));
            }
        }
    };
    private Toast mToast = null;
    private AlertDialog mWarningDialog;
    private WifiManager mWifiManager;

    private void handleWifiApStateChanged(int state) {
        Log.d("DeviceNameSettings", "Handle WIFI AP state = " + state);
        switch (state) {
            case 11:
                Log.d("DeviceNameSettings", "Handle WIFI_AP_STATE_DISABLED.");
                if (this.mApChangedWhenEnabled && this.mWifiManager != null) {
                    Log.d("DeviceNameSettings", "Re-Enable wifi ap.");
                    this.mWifiManager.setWifiApEnabled(null, true);
                    this.mApChangedWhenEnabled = false;
                    if (this.mHandler.hasMessages(1)) {
                        this.mHandler.removeMessages(1);
                    }
                    finishAndReturn();
                    break;
                }
            default:
                Log.w("DeviceNameSettings", "Ignore WIFI AP state change, state = " + state);
                break;
        }
        Log.d("DeviceNameSettings", "Handle WIFI AP state change finish. state = " + state);
    }

    protected void onCreate(Bundle savedInstanceState) {
        String str = null;
        super.onCreate(savedInstanceState);
        boolean isOkButtonEnabled = true;
        if (savedInstanceState != null) {
            this.mDeviceName = savedInstanceState.getString("device_name", this.mDeviceName);
            isOkButtonEnabled = savedInstanceState.getBoolean("ok_button_enabled");
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                str = intent.getStringExtra("device_name");
            }
            this.mDeviceName = str;
            if (this.mDeviceName == null) {
                this.mDeviceName = getDeviceName(this);
            } else {
                byte[] bytes = this.mDeviceName.getBytes(Charset.forName("UTF-8"));
                if (bytes.length >= 30) {
                    this.mDeviceName = new String(bytes, 0, 30, Charset.forName("UTF-8"));
                    Log.d("DeviceNameSettings", "Device name from intent is: " + this.mDeviceName + ". Length is: " + this.mDeviceName.length());
                    showTooLongToast();
                }
            }
        }
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(2130968739);
        this.mEditText = (EditText) findViewById(2131886503);
        this.mEditText.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(30, this, true)});
        this.mEditText.setText(this.mDeviceName);
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setSelection(this.mDeviceName.length());
        this.mEditText.requestFocus();
        getWindow().setSoftInputMode(16);
        this.mOkButton = (Button) findViewById(2131886504);
        this.mOkButton.setOnClickListener(this);
        this.mOkButton.setEnabled(isOkButtonEnabled);
        this.mCancleButton = (Button) findViewById(2131886370);
        this.mCancleButton.setOnClickListener(this);
        IntentFilter f = new IntentFilter();
        f.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(this.mReceiver, f);
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mReceiver);
        if (this.mWarningDialog != null && this.mWarningDialog.isShowing()) {
            this.mWarningDialog.dismiss();
        }
    }

    public void onClick(View v) {
        if (v == this.mOkButton) {
            if (hasDeviceConnectedToAp()) {
                showWarningDialog();
            } else {
                updateDeviceName(this.mDeviceName);
            }
        } else if (v == this.mCancleButton) {
            setResult(0);
            finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mEditText != null) {
            outState.putString("device_name", this.mDeviceName);
        }
        if (this.mOkButton != null) {
            outState.putBoolean("ok_button_enabled", this.mOkButton.isEnabled());
        }
    }

    public static String getDeviceName(Context context) {
        if (context == null) {
            return null;
        }
        String deviceName = Global.getString(context.getContentResolver(), "unified_device_name");
        if (deviceName == null) {
            deviceName = SystemProperties.get("ro.config.marketing_name");
        }
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = SystemProperties.get("ro.product.model", "Huawei Device");
        }
        return deviceName;
    }

    protected void updateDeviceName(String deviceName) {
        int state;
        Global.putString(getContentResolver(), "unified_device_name", deviceName);
        Global.putInt(getContentResolver(), "unified_device_name_updated", 1);
        LocalBluetoothManager localManager = Utils.getLocalBtManager(this);
        if (localManager == null) {
            Log.e("DeviceNameSettings", "get LocalBluetoothManager error! Can not update wifi ap device name.");
        } else {
            LocalBluetoothAdapter localAdapter = localManager.getBluetoothAdapter();
            if (localAdapter != null) {
                state = localAdapter.getBluetoothState();
                if (state == 12 || state == 11) {
                    localAdapter.setName(deviceName);
                }
            }
        }
        WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
        if (wifiP2pManager == null || this.mWifiManager == null) {
            storeWifiP2pName(deviceName);
        } else {
            state = this.mWifiManager.getWifiState();
            if (state == 3 || state == 2) {
                wifiP2pManager.setDeviceName(wifiP2pManager.initialize(this, getMainLooper(), null), deviceName, this.mListener);
            } else {
                storeWifiP2pName(deviceName);
            }
        }
        if (this.mWifiManager != null) {
            WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
            wifiConfig.SSID = deviceName;
            this.mWifiManager.setWifiApConfiguration(wifiConfig);
            if (isWifiApEnabled()) {
                this.mWifiManager.setWifiApEnabled(null, false);
                this.mApChangedWhenEnabled = true;
                if (this.mHandler.hasMessages(1)) {
                    this.mHandler.removeMessages(1);
                }
                this.mHandler.sendEmptyMessageDelayed(1, 5000);
                return;
            }
            finishAndReturn();
            return;
        }
        Log.e("DeviceNameSettings", "WifiManager is null, can not update wifi ap device name.");
        finishAndReturn();
    }

    private void storeWifiP2pName(String deviceName) {
        Global.putString(getContentResolver(), "wifi_p2p_device_name", deviceName);
    }

    private boolean isWifiApEnabled() {
        if (this.mWifiManager == null || !this.mWifiManager.isWifiApEnabled()) {
            return false;
        }
        return true;
    }

    private boolean hasDeviceConnectedToAp() {
        if (!isWifiApEnabled()) {
            return false;
        }
        List<WifiApClientInfo> list = WifiApClientUtils.getInstance(this).getConnectedList();
        if (list == null || list.size() <= 0) {
            return false;
        }
        return true;
    }

    private void showWarningDialog() {
        if (this.mWarningDialog != null && this.mWarningDialog.isShowing()) {
            this.mWarningDialog.dismiss();
        }
        this.mWarningDialog = new Builder(this).setTitle(2131625399).setMessage(2131628523).setPositiveButton(2131628524, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeviceNameSettings.this.updateDeviceName(DeviceNameSettings.this.mDeviceName);
            }
        }).setNegativeButton(2131625657, null).create();
        this.mWarningDialog.show();
    }

    public void showTooLongToast() {
        if (this.mToast == null) {
            this.mToast = Toast.makeText(this, 2131628349, 0);
        }
        this.mToast.show();
    }

    private void finishAndReturn() {
        setResult(-1);
        finish();
        ItemUseStat.getInstance().handleClick(this, 2, "edit_device_name");
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        if (this.mOkButton != null) {
            boolean enable = true;
            if (TextUtils.isEmpty(s.toString().trim()) || s.length() <= 0) {
                enable = false;
            }
            this.mOkButton.setEnabled(enable);
        }
        this.mDeviceName = s.toString();
    }
}
