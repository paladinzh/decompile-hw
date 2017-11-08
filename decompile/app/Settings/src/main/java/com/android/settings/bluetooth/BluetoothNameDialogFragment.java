package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.Utf8ByteLengthFilter;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public final class BluetoothNameDialogFragment extends DialogFragment implements TextWatcher {
    private AlertDialog mAlertDialog;
    private boolean mDeviceNameEdited;
    private boolean mDeviceNameUpdated;
    EditText mDeviceNameView;
    final LocalBluetoothAdapter mLocalAdapter;
    private Button mOkButton;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED")) {
                BluetoothNameDialogFragment.this.updateDeviceName();
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED") && intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE) == 12) {
                BluetoothNameDialogFragment.this.updateDeviceName();
            }
        }
    };

    public BluetoothNameDialogFragment() {
        LocalBluetoothManager localManager = Utils.getLocalBtManager(getActivity());
        if (localManager == null) {
            this.mLocalAdapter = null;
            MLog.e("BluetoothNameDialogFragment", "Error: get LocalBluetoothManager error!");
            return;
        }
        this.mLocalAdapter = localManager.getBluetoothAdapter();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (this.mLocalAdapter == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        String deviceName = this.mLocalAdapter.getName();
        if (savedInstanceState != null) {
            deviceName = savedInstanceState.getString("device_name", deviceName);
            this.mDeviceNameEdited = savedInstanceState.getBoolean("device_name_edited", false);
        }
        this.mAlertDialog = new Builder(getActivity()).setTitle(2131627349).setView(createDialogView(deviceName)).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothNameDialogFragment.this.setDeviceName(BluetoothNameDialogFragment.this.mDeviceNameView.getText().toString());
                ItemUseStat.getInstance().handleClick(BluetoothNameDialogFragment.this.getActivity(), 2, "edit_bt_name");
            }
        }).setNegativeButton(2131625657, null).create();
        this.mAlertDialog.getWindow().setSoftInputMode(5);
        return this.mAlertDialog;
    }

    private void setDeviceName(String deviceName) {
        Log.d("BluetoothNameDialogFragment", "Setting device name to " + deviceName);
        this.mLocalAdapter.setName(deviceName);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mDeviceNameView != null) {
            outState.putString("device_name", this.mDeviceNameView.getText().toString());
        }
        outState.putBoolean("device_name_edited", this.mDeviceNameEdited);
    }

    private View createDialogView(String deviceName) {
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(2130968742, null);
        this.mDeviceNameView = (EditText) view.findViewById(2131886503);
        this.mDeviceNameView.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(247)});
        this.mDeviceNameView.setText(deviceName);
        if (deviceName != null) {
            this.mDeviceNameView.setSelection(deviceName.length());
        }
        this.mDeviceNameView.addTextChangedListener(this);
        return view;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAlertDialog = null;
        this.mDeviceNameView = null;
        this.mOkButton = null;
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        if (this.mLocalAdapter == null) {
            dismiss();
        }
        if (this.mOkButton == null && this.mAlertDialog != null) {
            this.mOkButton = this.mAlertDialog.getButton(-1);
            String deviceNameViewStr = "";
            if (this.mDeviceNameView != null) {
                deviceNameViewStr = this.mDeviceNameView.getText().toString();
            }
            Button button = this.mOkButton;
            if (this.mDeviceNameEdited && deviceNameViewStr.length() != 0) {
                z = true;
            }
            button.setEnabled(z);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    void updateDeviceName() {
        if (this.mLocalAdapter != null && this.mLocalAdapter.isEnabled()) {
            this.mDeviceNameUpdated = true;
            this.mDeviceNameEdited = false;
            this.mDeviceNameView.setText(this.mLocalAdapter.getName());
            if (this.mLocalAdapter.getName() != null) {
                this.mDeviceNameView.setSelection(this.mLocalAdapter.getName().length());
            }
        }
    }

    public void afterTextChanged(Editable s) {
        boolean z = true;
        if (this.mDeviceNameUpdated) {
            this.mDeviceNameUpdated = false;
            this.mOkButton.setEnabled(false);
            return;
        }
        this.mDeviceNameEdited = true;
        if (this.mOkButton != null) {
            Button button = this.mOkButton;
            if (s.toString().trim().length() == 0) {
                z = false;
            }
            button.setEnabled(z);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void show(FragmentManager manager, String tag) {
        Fragment f = manager.findFragmentByTag(tag);
        if (f == null || !f.isAdded()) {
            super.show(manager, tag);
        }
    }
}
