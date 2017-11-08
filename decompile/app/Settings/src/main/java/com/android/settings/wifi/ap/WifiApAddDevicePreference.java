package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.settings.CustomDialogPreference;

public class WifiApAddDevicePreference extends CustomDialogPreference {
    private EditText mEditMac;
    private TextWatcher mEditMacTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            boolean enableButton = WifiApClientUtils.isMacValid(s) && WifiApAddDevicePreference.this.mEditName.getText().length() > 0;
            WifiApAddDevicePreference.this.enableButton(-1, enableButton);
        }
    };
    private EditText mEditName;
    private TextWatcher mEditNameTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            boolean enableButton = WifiApClientUtils.isMacValid(WifiApAddDevicePreference.this.mEditMac.getText()) && s.length() > 0;
            WifiApAddDevicePreference.this.enableButton(-1, enableButton);
        }
    };
    private OnAddDeviceListener mListener;

    interface OnAddDeviceListener {
        void onAddAllowedDevice(WifiApClientInfo wifiApClientInfo);
    }

    public WifiApAddDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayout();
    }

    public WifiApAddDevicePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayout();
    }

    public void setListener(OnAddDeviceListener listener) {
        this.mListener = listener;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        enableButton(-1, false);
        this.mEditName = (EditText) view.findViewById(2131887455);
        this.mEditName.addTextChangedListener(this.mEditNameTextWatcher);
        this.mEditMac = (EditText) view.findViewById(2131887456);
        this.mEditMac.addTextChangedListener(this.mEditMacTextWatcher);
        this.mEditMac.setKeyListener(MacKeyListener.getInstance());
    }

    protected boolean needInputMethod() {
        return true;
    }

    public void onDialogFragmentStart() {
        super.onDialogFragmentStart();
        enableButton(-1, false);
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            WifiApClientInfo info = new WifiApClientInfo();
            info.setDeviceName(this.mEditName.getText());
            info.setMAC(this.mEditMac.getText());
            if (this.mListener != null) {
                this.mListener.onAddAllowedDevice(info);
            }
        }
    }

    private void setLayout() {
        setLayoutResource(2130969262);
        setDialogLayoutResource(2130969261);
        setPositiveButtonText(2131627425);
        setWidgetLayoutResource(2130968998);
        setIcon(2130838177);
    }

    protected void enableButton(int whichButton, boolean bEnable) {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button button = dialog.getButton(whichButton);
            if (button != null) {
                button.setEnabled(bEnable);
            }
        }
    }
}
