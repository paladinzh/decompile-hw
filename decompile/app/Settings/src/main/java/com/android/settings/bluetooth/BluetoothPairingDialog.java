package com.android.settings.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.Utils;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import java.util.Locale;

public final class BluetoothPairingDialog extends AlertActivity implements OnCheckedChangeListener, OnClickListener, TextWatcher {
    private LocalBluetoothManager mBluetoothManager;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    private BluetoothDevice mDevice;
    private Button mOkButton;
    private String mPairingKey;
    private EditText mPairingView;
    private LocalBluetoothProfile mPbapClientProfile;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
                if (bondState == 12 || bondState == 10) {
                    BluetoothPairingDialog.this.dismiss();
                }
            } else if ("android.bluetooth.device.action.PAIRING_CANCEL".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device == null || device.equals(BluetoothPairingDialog.this.mDevice)) {
                    BluetoothPairingDialog.this.dismiss();
                }
            }
        }
    };
    private int mType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("BluetoothPairingDialog", "Error: getIntent() is null");
            finish();
        } else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(intent.getAction())) {
            this.mBluetoothManager = Utils.getLocalBtManager(this);
            if (this.mBluetoothManager == null) {
                Log.e("BluetoothPairingDialog", "Error: BluetoothAdapter not supported by system");
                finish();
                return;
            }
            this.mCachedDeviceManager = this.mBluetoothManager.getCachedDeviceManager();
            this.mPbapClientProfile = this.mBluetoothManager.getProfileManager().getPbapClientProfile();
            this.mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            this.mType = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
            switch (this.mType) {
                case 0:
                case 1:
                case 7:
                    createUserEntryDialog();
                    break;
                case 2:
                    if (intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
                        this.mPairingKey = String.format(Locale.US, "%06d", new Object[]{Integer.valueOf(intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE))});
                        createConfirmationDialog();
                        break;
                    }
                    Log.e("BluetoothPairingDialog", "Invalid Confirmation Passkey received, not showing any dialog");
                    return;
                case 3:
                case 6:
                    createConsentDialog();
                    break;
                case 4:
                case 5:
                    if (intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
                        if (this.mType == 4) {
                            this.mPairingKey = String.format("%06d", new Object[]{Integer.valueOf(pairingKey)});
                        } else {
                            this.mPairingKey = String.format("%04d", new Object[]{Integer.valueOf(pairingKey)});
                        }
                        createDisplayPasskeyOrPinDialog();
                        break;
                    }
                    Log.e("BluetoothPairingDialog", "Invalid Confirmation Passkey or PIN received, not showing any dialog");
                    return;
                default:
                    Log.e("BluetoothPairingDialog", "Incorrect pairing type received, not showing any dialog");
                    finish();
                    break;
            }
            getWindow().setCloseOnTouchOutside(false);
            getWindow().setGravity(Utils.isTablet() ? 17 : 80);
            registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.PAIRING_CANCEL"));
            registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
        } else {
            Log.e("BluetoothPairingDialog", "Error: this activity may be started only with intent android.bluetooth.device.action.PAIRING_REQUEST");
            finish();
        }
    }

    private void createUserEntryDialog() {
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131624810, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)});
        p.mView = createPinEntryView();
        p.mPositiveButtonText = getString(17039370);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(17039360);
        p.mNegativeButtonListener = this;
        setupAlert();
        this.mOkButton = this.mAlert.getButton(-1);
        this.mOkButton.setEnabled(false);
    }

    private View createPinEntryView() {
        int messageId;
        int maxLength;
        View view = getLayoutInflater().inflate(2130968654, null);
        TextView messageView2 = (TextView) view.findViewById(2131886310);
        CheckBox alphanumericPin = (CheckBox) view.findViewById(2131886309);
        CheckBox contactSharing = (CheckBox) view.findViewById(2131886311);
        contactSharing.setText(getString(2131624821, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)}));
        if (Utils.isWifiOnly(this)) {
            contactSharing.setVisibility(8);
        }
        if (this.mPbapClientProfile != null && this.mPbapClientProfile.isProfileReady()) {
            contactSharing.setVisibility(8);
        }
        if (this.mDevice.getPhonebookAccessPermission() == 1) {
            contactSharing.setChecked(true);
        } else if (this.mDevice.getPhonebookAccessPermission() == 2) {
            contactSharing.setChecked(false);
        } else {
            BluetoothClass bluetoothClass = this.mDevice.getBluetoothClass();
            if (bluetoothClass == null || bluetoothClass.getDeviceClass() != 1032) {
                contactSharing.setChecked(false);
                this.mDevice.setPhonebookAccessPermission(2);
            } else {
                contactSharing.setChecked(true);
                this.mDevice.setPhonebookAccessPermission(1);
            }
        }
        contactSharing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    BluetoothPairingDialog.this.mDevice.setPhonebookAccessPermission(1);
                } else {
                    BluetoothPairingDialog.this.mDevice.setPhonebookAccessPermission(2);
                }
            }
        });
        this.mPairingView = (EditText) view.findViewById(2131886308);
        this.mPairingView.addTextChangedListener(this);
        alphanumericPin.setOnCheckedChangeListener(this);
        switch (this.mType) {
            case 0:
                break;
            case 1:
                messageId = 2131624817;
                maxLength = 6;
                alphanumericPin.setVisibility(8);
                break;
            case 7:
                break;
            default:
                Log.e("BluetoothPairingDialog", "Incorrect pairing type for createPinEntryView: " + this.mType);
                return null;
        }
        messageId = 2131624816;
        maxLength = 16;
        messageView2.setText(messageId);
        this.mPairingView.setInputType(2);
        this.mPairingView.setFilters(new InputFilter[]{new LengthFilter(maxLength)});
        return view;
    }

    private View createView() {
        String messageCaption;
        View view = getLayoutInflater().inflate(2130968653, null);
        String name = this.mCachedDeviceManager.getName(this.mDevice);
        TextView messageViewCaption = (TextView) view.findViewById(2131886302);
        TextView messageViewContent = (TextView) view.findViewById(2131886303);
        TextView pairingViewCaption = (TextView) view.findViewById(2131886304);
        TextView pairingViewContent = (TextView) view.findViewById(2131886305);
        TextView messagePairing = (TextView) view.findViewById(2131886306);
        CheckBox contactSharing = (CheckBox) view.findViewById(2131886307);
        contactSharing.setText(getString(2131624821, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)}));
        if (Utils.isWifiOnly(this)) {
            contactSharing.setVisibility(8);
        }
        if (this.mPbapClientProfile != null && this.mPbapClientProfile.isProfileReady()) {
            contactSharing.setVisibility(8);
        }
        if (this.mDevice.getPhonebookAccessPermission() == 1) {
            contactSharing.setChecked(true);
        } else if (this.mDevice.getPhonebookAccessPermission() == 2) {
            contactSharing.setChecked(false);
        } else {
            BluetoothClass bluetoothClass = this.mDevice.getBluetoothClass();
            if (bluetoothClass == null || bluetoothClass.getDeviceClass() != 1032) {
                contactSharing.setChecked(false);
                this.mDevice.setPhonebookAccessPermission(2);
            } else {
                contactSharing.setChecked(true);
                this.mDevice.setPhonebookAccessPermission(1);
            }
        }
        contactSharing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    BluetoothPairingDialog.this.mDevice.setPhonebookAccessPermission(1);
                } else {
                    BluetoothPairingDialog.this.mDevice.setPhonebookAccessPermission(2);
                }
            }
        });
        CharSequence charSequence = null;
        switch (this.mType) {
            case 2:
                break;
            case 3:
            case 6:
                pairingViewCaption.setText(2131627270);
                messagePairing.setVisibility(0);
                messageCaption = getString(2131627240);
                break;
            case 4:
            case 5:
                pairingViewCaption.setText(2131627270);
                messagePairing.setVisibility(0);
                break;
            default:
                Log.e("BluetoothPairingDialog", "Incorrect pairing type received, not creating view");
                return null;
        }
        messageCaption = getString(2131627240);
        charSequence = this.mPairingKey;
        if (messageViewCaption != null) {
            messageViewCaption.setText(messageCaption);
            messageViewContent.setText(name);
        }
        if (charSequence != null) {
            pairingViewCaption.setVisibility(0);
            pairingViewContent.setVisibility(0);
            pairingViewContent.setText(charSequence);
        }
        return view;
    }

    private void createConfirmationDialog() {
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131624810, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)});
        p.mView = createView();
        p.mPositiveButtonText = getString(2131624000);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(2131624002);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    private void createConsentDialog() {
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131624810, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)});
        p.mView = createView();
        p.mPositiveButtonText = getString(2131624000);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(2131624002);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    private void createDisplayPasskeyOrPinDialog() {
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131624810, new Object[]{this.mCachedDeviceManager.getName(this.mDevice)});
        p.mView = createView();
        p.mNegativeButtonText = getString(17039360);
        p.mNegativeButtonListener = this;
        setupAlert();
        if (this.mType == 4) {
            this.mDevice.setPairingConfirmation(true);
        } else if (this.mType == 5) {
            this.mDevice.setPin(BluetoothDevice.convertPinToBytes(this.mPairingKey));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afterTextChanged(Editable s) {
        boolean z = true;
        if (this.mOkButton == null) {
            return;
        }
        if (this.mType == 7) {
            Button button = this.mOkButton;
            if (s.length() < 16) {
                z = false;
            }
            button.setEnabled(z);
            return;
        }
        button = this.mOkButton;
        if (s.length() <= 0) {
            z = false;
        }
        button.setEnabled(z);
    }

    private void onPair(String value) {
        HwLog.d("BluetoothPairingDialog", "onPair  mType=" + this.mType + ", value=" + value);
        switch (this.mType) {
            case 0:
            case 7:
                byte[] pinBytes = BluetoothDevice.convertPinToBytes(value);
                if (pinBytes != null) {
                    this.mDevice.setPin(pinBytes);
                    break;
                }
                return;
            case 1:
                this.mDevice.setPasskey(Integer.parseInt(value));
                break;
            case 2:
            case 3:
                this.mDevice.setPairingConfirmation(true);
                break;
            case 4:
            case 5:
                break;
            case 6:
                this.mDevice.setRemoteOutOfBandData();
                break;
            default:
                Log.e("BluetoothPairingDialog", "Incorrect pairing type received");
                break;
        }
    }

    private void onCancel() {
        if (this.mDevice != null) {
            this.mDevice.cancelPairingUserInput();
            handleCancelPairing();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            onCancel();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                if (this.mPairingView != null) {
                    onPair(this.mPairingView.getText().toString());
                    return;
                } else {
                    onPair(null);
                    return;
                }
            default:
                onCancel();
                return;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            this.mPairingView.setInputType(1);
        } else {
            this.mPairingView.setInputType(2);
        }
    }

    private void handleCancelPairing() {
        if (this.mType == 2) {
            int newValue = 0;
            if (Secure.getInt(getContentResolver(), "db_bluetooth_launch_pairing", 0) == 1) {
                newValue = 2;
            }
            if (!Secure.putInt(getContentResolver(), "db_bluetooth_launch_pairing", newValue)) {
                Log.e("BluetoothPairingDialog", "failed to save launch pairing status, key = db_bluetooth_launch_pairing, value = " + newValue);
            }
        }
    }
}
