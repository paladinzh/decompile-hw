package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.cust.HwCustUtils;

class WifiDialog extends AlertDialog implements WifiConfigUiBase, OnClickListener {
    private final AccessPoint mAccessPoint;
    public Context mContext;
    private WifiConfigController mController;
    private HwCustWifiDialog mCust;
    private boolean mHideSubmitButton;
    private final WifiDialogListener mListener;
    private final int mMode;
    private View mView;

    public interface WifiDialogListener {
        void onForget(WifiDialog wifiDialog);

        void onSubmit(WifiDialog wifiDialog);
    }

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode, boolean hideSubmitButton) {
        this(context, listener, accessPoint, mode);
        this.mHideSubmitButton = hideSubmitButton;
    }

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode) {
        super(context);
        this.mMode = mode;
        this.mListener = listener;
        this.mAccessPoint = accessPoint;
        this.mHideSubmitButton = false;
        this.mContext = context;
        getWindow().setSoftInputMode(16);
    }

    public WifiConfigController getController() {
        return this.mController;
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.mView = getLayoutInflater().inflate(2130969272, null);
        WifiExtUtils.setSelection(this.mView, savedInstanceState);
        setView(this.mView);
        setInverseBackgroundForced(true);
        this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mMode, true);
        super.onCreate(savedInstanceState);
        if (this.mHideSubmitButton) {
            this.mController.hideSubmitButton();
        }
        if (this.mAccessPoint == null) {
            this.mController.hideForgetButton();
        }
        this.mCust = (HwCustWifiDialog) HwCustUtils.createObj(HwCustWifiDialog.class, new Object[]{this, this.mContext});
        if (this.mCust != null) {
            this.mCust.custDialogButton(this.mAccessPoint);
            this.mCust.setForgetButtonFales(getForgetButton(), this.mAccessPoint);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mController.updatePassword();
    }

    public void dispatchSubmit() {
        if (this.mListener != null) {
            this.mListener.onSubmit(this);
        }
        dismiss();
    }

    public void onClick(DialogInterface dialogInterface, int id) {
        if (this.mListener != null) {
            switch (id) {
                case -3:
                    if (!WifiSettings.isEditabilityLockedDown(getContext(), this.mAccessPoint.getConfig())) {
                        this.mListener.onForget(this);
                        break;
                    } else {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtils.getDeviceOwner(getContext()));
                        return;
                    }
                case -1:
                    this.mListener.onSubmit(this);
                    break;
            }
        }
    }

    public Button getSubmitButton() {
        return getButton(-1);
    }

    public Button getForgetButton() {
        return getButton(-3);
    }

    public void setSubmitButton(CharSequence text) {
        setButton(-1, text, this);
    }

    public void setForgetButton(CharSequence text) {
        setButton(-3, text, this);
    }

    public void setCancelButton(CharSequence text) {
        setButton(-2, text, this);
    }

    public Bundle onSaveInstanceState() {
        Bundle saveInstanceState = super.onSaveInstanceState();
        saveInstanceState.putBoolean("wifi_advanced_togglebox", ((CheckBox) this.mView.findViewById(2131887463)).isChecked());
        saveInstanceState.putInt("wifi_proxy_settings", ((Spinner) this.mView.findViewById(2131887517)).getSelectedItemPosition());
        saveInstanceState.putInt("wifi_ip_settings", ((Spinner) this.mView.findViewById(2131887527)).getSelectedItemPosition());
        return saveInstanceState;
    }

    public void dismiss() {
        super.dismiss();
        this.mController.recycleBitmap();
    }
}
