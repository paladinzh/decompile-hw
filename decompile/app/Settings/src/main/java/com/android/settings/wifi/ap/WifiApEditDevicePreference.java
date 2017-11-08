package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.settings.CustomDialogPreference;

public class WifiApEditDevicePreference extends CustomDialogPreference {
    private Context mContext;
    private AlertDialog mEditDialog;
    private EditText mEditMac;
    private TextWatcher mEditMacTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            boolean enableButton = WifiApClientUtils.isMacValid(s) && WifiApEditDevicePreference.this.mEditName.getText().length() > 0;
            WifiApEditDevicePreference.this.enableButton(-1, enableButton);
        }
    };
    private EditText mEditName;
    private TextWatcher mEditNameTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            boolean enableButton = WifiApClientUtils.isMacValid(WifiApEditDevicePreference.this.mEditMac.getText()) && s.length() > 0;
            WifiApEditDevicePreference.this.enableButton(-1, enableButton);
        }
    };
    private WifiApClientInfo mInfo;
    private OnEditDevicesListener mListener;

    interface OnEditDevicesListener {
        void onEditAllowedDevice(WifiApClientInfo wifiApClientInfo);

        void onRemoveAllowedDevice(WifiApClientInfo wifiApClientInfo);
    }

    public WifiApEditDevicePreference(Context context, WifiApClientInfo info, OnEditDevicesListener listener) {
        super(context, null);
        this.mInfo = new WifiApClientInfo(info);
        setListener(listener);
        this.mContext = context;
        setLayout();
        setDeviceName();
        setSummaryMac();
        setWidgetLayoutResource(2130968998);
        setIcon(2130838260);
    }

    protected void setLayout() {
        setLayoutResource(2130969262);
    }

    private void setDeviceName() {
        setTitle(loadDeviceName());
    }

    private void setListener(OnEditDevicesListener listener) {
        this.mListener = listener;
    }

    public CharSequence loadDeviceName() {
        CharSequence deviceName = this.mInfo.getDeviceName();
        if (deviceName == null || "".equals(deviceName)) {
            return getContext().getString(2131627426);
        }
        return deviceName;
    }

    private void setSummaryMac() {
        setSummary((CharSequence) String.format(getContext().getString(2131627420), new Object[]{this.mInfo.getMAC()}));
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        builder.setItems(new CharSequence[]{this.mContext.getResources().getString(2131627742), this.mContext.getResources().getString(2131627427)}, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    WifiApEditDevicePreference.this.createEditDialog();
                } else if (which == 1 && WifiApEditDevicePreference.this.mListener != null) {
                    WifiApEditDevicePreference.this.mListener.onRemoveAllowedDevice(WifiApEditDevicePreference.this.mInfo);
                }
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    private void createEditDialog() {
        View layout = LayoutInflater.from(this.mContext).inflate(2130969261, null);
        AlertDialog dialog = new Builder(getContext()).setTitle(2131627422).setView(layout).setPositiveButton(2131627425, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiApEditDevicePreference.this.mInfo.setDeviceName(WifiApEditDevicePreference.this.mEditName.getText());
                WifiApEditDevicePreference.this.mInfo.setMAC(WifiApEditDevicePreference.this.mEditMac.getText());
                WifiApEditDevicePreference.this.setDeviceName();
                WifiApEditDevicePreference.this.setSummaryMac();
                if (WifiApEditDevicePreference.this.mListener != null) {
                    WifiApEditDevicePreference.this.mListener.onEditAllowedDevice(WifiApEditDevicePreference.this.mInfo);
                }
            }
        }).setNegativeButton(17039360, null).create();
        this.mEditName = (EditText) layout.findViewById(2131887455);
        this.mEditMac = (EditText) layout.findViewById(2131887456);
        this.mEditName.setText(this.mInfo.getDeviceName());
        this.mEditName.setSelection(this.mEditName.getText().length());
        this.mEditName.addTextChangedListener(this.mEditNameTextWatcher);
        this.mEditMac.setText(this.mInfo.getMAC());
        this.mEditMac.addTextChangedListener(this.mEditMacTextWatcher);
        this.mEditMac.setKeyListener(MacKeyListener.getInstance());
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                boolean z = false;
                Button button = ((AlertDialog) dialog).getButton(-1);
                if (WifiApEditDevicePreference.this.mEditName.getText().length() > 0) {
                    z = true;
                }
                button.setEnabled(z);
            }
        });
        this.mEditDialog = dialog;
        dialog.getWindow().setSoftInputMode(5);
        dialog.show();
    }

    protected void enableButton(int whichButton, boolean bEnable) {
        AlertDialog dialog = this.mEditDialog;
        if (dialog != null) {
            Button button = dialog.getButton(whichButton);
            if (button != null) {
                button.setEnabled(bEnable);
            }
        }
    }
}
