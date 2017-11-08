package com.android.settings;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.Toast;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class MSimIccLockSettingsFragment extends SettingsPreferenceFragment implements OnPinEnteredListener, OnPreferenceChangeListener {
    private static final boolean IS_UMTS_GSM = SystemProperties.get("ro.config.dsds_mode").equals("umts_gsm");
    private Activity mContext;
    private int mDialogState = 0;
    private String mError;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = msg.obj;
            switch (msg.what) {
                case 100:
                    MSimIccLockSettingsFragment.this.iccLockChanged(ar);
                    return;
                case 101:
                    MSimIccLockSettingsFragment.this.iccPinChanged(ar);
                    return;
                case 102:
                    MSimIccLockSettingsFragment.this.updatePreferences();
                    MSimIccLockSettingsFragment.this.updateEnableState();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsCdmaPhone = false;
    private boolean mIsChecked;
    private String mNewPin;
    private String mOldPin;
    private Phone mPhone;
    private String mPin;
    private EditPinPreference mPinDialog;
    private SwitchPreference mPinToggle;
    private Resources mRes;
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                MSimIccLockSettingsFragment.this.mHandler.sendMessage(MSimIccLockSettingsFragment.this.mHandler.obtainMessage(102));
            }
        }
    };
    private int mSubscription = 0;

    public MSimIccLockSettingsFragment(int subscription) {
        this.mSubscription = subscription;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    private void updateEnableState() {
        boolean enable = Utils.isSimCardLockStateChangeAble(this.mSubscription);
        this.mPinToggle.setEnabled(enable);
        if (enable) {
            this.mPinDialog.setEnabled(isIccLockEnabled());
            if (isPukLocked() || isPinLocked()) {
                this.mPinDialog.setEnabled(false);
                return;
            }
            return;
        }
        this.mPinDialog.setEnabled(enable);
    }

    boolean isIccLockEnabled() {
        if (isPinLocked() || isPukLocked()) {
            return true;
        }
        return this.mPhone != null ? this.mPhone.getIccCard().getIccLockEnabled() : false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230889);
        try {
            this.mPhone = PhoneFactory.getPhone(this.mSubscription);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.mPinDialog = (EditPinPreference) findPreference("sim_pin");
        this.mPinToggle = (SwitchPreference) findPreference("sim_toggle");
        this.mPinToggle.setOnPreferenceChangeListener(this);
        if (Utils.isMonkeyRunning()) {
            this.mContext.finish();
            return;
        }
        if (isIccLockEnabled()) {
            this.mPinDialog.setEnabled(true);
            if (isPukLocked() || isPinLocked()) {
                this.mPinDialog.setEnabled(false);
            }
        } else {
            this.mPinDialog.setEnabled(false);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("dialogState")) {
            this.mDialogState = savedInstanceState.getInt("dialogState");
            this.mPin = savedInstanceState.getString("dialogPin");
            this.mError = savedInstanceState.getString("dialogError");
            this.mIsChecked = savedInstanceState.getBoolean("enableState");
            switch (this.mDialogState) {
                case 3:
                    this.mOldPin = savedInstanceState.getString("oldPinCode");
                    break;
                case 4:
                    this.mOldPin = savedInstanceState.getString("oldPinCode");
                    this.mNewPin = savedInstanceState.getString("newPinCode");
                    break;
            }
        }
        this.mPinDialog.setOnPinEnteredListener(this);
        getPreferenceScreen().setPersistent(false);
        this.mRes = getResources();
        if (this.mPhone != null) {
            this.mIsCdmaPhone = 2 == this.mPhone.getPhoneType();
        }
        if (this.mIsCdmaPhone) {
            this.mPinToggle.setTitle(2131627318);
            this.mPinDialog.setTitle(2131627319);
        } else {
            int i;
            SwitchPreference switchPreference = this.mPinToggle;
            if (this.mSubscription == 0 && IS_UMTS_GSM) {
                i = 2131627391;
            } else {
                i = 2131625186;
            }
            switchPreference.setTitle(i);
            EditPinPreference editPinPreference = this.mPinDialog;
            if (this.mSubscription == 0 && IS_UMTS_GSM) {
                i = 2131627392;
            } else {
                i = 2131625189;
            }
            editPinPreference.setTitle(i);
        }
        updatePreferences();
    }

    private void updatePreferences() {
        this.mPinToggle.setChecked(this.mPhone != null ? isIccLockEnabled() : false);
    }

    public void onResume() {
        super.onResume();
        this.mContext.registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        updatePreferences();
        if (this.mDialogState != 0) {
            showPinDialog();
        } else {
            resetDialogState();
        }
        updateEnableState();
    }

    public void onPause() {
        super.onPause();
        this.mContext.unregisterReceiver(this.mSimStateReceiver);
    }

    public void onSaveInstanceState(Bundle out) {
        if (this.mPinDialog.isDialogOpen()) {
            out.putInt("dialogState", this.mDialogState);
            out.putString("dialogError", this.mError);
            out.putBoolean("enableState", this.mIsChecked);
            EditText text = this.mPinDialog.getEditText();
            if (text != null) {
                out.putString("dialogPin", text.getText().toString());
            }
            switch (this.mDialogState) {
                case 3:
                    out.putString("oldPinCode", this.mOldPin);
                    return;
                case 4:
                    out.putString("oldPinCode", this.mOldPin);
                    out.putString("newPinCode", this.mNewPin);
                    return;
                default:
                    return;
            }
        }
        super.onSaveInstanceState(out);
    }

    private void showPinDialog() {
        if (this.mDialogState != 0) {
            setDialogValues();
            this.mPinDialog.showPinDialog();
        }
    }

    private void setDialogValues() {
        int i = 2131627395;
        this.mPinDialog.setText(this.mPin);
        String message = "";
        EditPinPreference editPinPreference;
        Resources resources;
        switch (this.mDialogState) {
            case 1:
                CharSequence string;
                message = this.mRes.getString(2131625190);
                EditPinPreference editPinPreference2 = this.mPinDialog;
                if (this.mIsChecked) {
                    string = this.mRes.getString(2131625191);
                } else {
                    string = this.mRes.getString(2131625192);
                }
                editPinPreference2.setDialogTitle(string);
                if (!this.mIsCdmaPhone) {
                    message = this.mRes.getString(2131625190);
                    editPinPreference2 = this.mPinDialog;
                    Resources resources2;
                    if (this.mIsChecked) {
                        resources2 = this.mRes;
                        i = (this.mSubscription == 0 && IS_UMTS_GSM) ? 2131627396 : 2131625191;
                        string = resources2.getString(i);
                    } else {
                        resources2 = this.mRes;
                        i = (this.mSubscription == 0 && IS_UMTS_GSM) ? 2131627397 : 2131625192;
                        string = resources2.getString(i);
                    }
                    editPinPreference2.setDialogTitle(string);
                    break;
                }
                message = this.mRes.getString(2131627320);
                editPinPreference2 = this.mPinDialog;
                if (this.mIsChecked) {
                    string = this.mRes.getString(2131627321);
                } else {
                    string = this.mRes.getString(2131627322);
                }
                editPinPreference2.setDialogTitle(string);
                break;
                break;
            case 2:
                if (!this.mIsCdmaPhone) {
                    message = this.mRes.getString(2131625193);
                    editPinPreference = this.mPinDialog;
                    resources = this.mRes;
                    if (!(this.mSubscription == 0 && IS_UMTS_GSM)) {
                        i = 2131625196;
                    }
                    editPinPreference.setDialogTitle(resources.getString(i));
                    break;
                }
                message = this.mRes.getString(2131627323);
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131627325));
                break;
            case 3:
                if (!this.mIsCdmaPhone) {
                    message = this.mRes.getString(2131625194);
                    editPinPreference = this.mPinDialog;
                    resources = this.mRes;
                    if (!(this.mSubscription == 0 && IS_UMTS_GSM)) {
                        i = 2131625196;
                    }
                    editPinPreference.setDialogTitle(resources.getString(i));
                    break;
                }
                message = this.mRes.getString(2131627324);
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131627325));
                break;
            case 4:
                message = this.mRes.getString(2131625195);
                if (!this.mIsCdmaPhone) {
                    editPinPreference = this.mPinDialog;
                    resources = this.mRes;
                    if (!(this.mSubscription == 0 && IS_UMTS_GSM)) {
                        i = 2131625196;
                    }
                    editPinPreference.setDialogTitle(resources.getString(i));
                    break;
                }
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131627325));
                break;
        }
        if (this.mError != null) {
            this.mPinDialog.setErrorText(this.mError);
            this.mError = null;
        } else {
            this.mPinDialog.setErrorText("");
        }
        this.mPinDialog.setEditTextHint(message);
    }

    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (positiveResult) {
            this.mPin = preference.getText();
            if (reasonablePin(this.mPin)) {
                switch (this.mDialogState) {
                    case 1:
                        tryChangeIccLockState();
                        break;
                    case 2:
                        this.mOldPin = this.mPin;
                        this.mDialogState = 3;
                        this.mError = null;
                        this.mPin = null;
                        showPinDialog();
                        break;
                    case 3:
                        this.mNewPin = this.mPin;
                        this.mDialogState = 4;
                        this.mPin = null;
                        showPinDialog();
                        break;
                    case 4:
                        if (!this.mPin.equals(this.mNewPin)) {
                            this.mError = this.mRes.getString(2131625198);
                            this.mDialogState = 3;
                            this.mPin = null;
                            showPinDialog();
                            break;
                        }
                        this.mError = null;
                        tryChangePin();
                        break;
                }
                return;
            }
            this.mError = this.mRes.getString(2131625197);
            showPinDialog();
            return;
        }
        resetDialogState();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (isPinLocked() || isPukLocked()) {
            return true;
        }
        if (preference != this.mPinDialog) {
            return super.onPreferenceTreeClick(preference);
        }
        this.mDialogState = 2;
        return false;
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (isPinLocked()) {
            unlockPin();
            return false;
        } else if (isPukLocked()) {
            unlockPuk();
            return false;
        } else {
            if (pref == this.mPinToggle) {
                this.mIsChecked = ((Boolean) newValue).booleanValue();
                this.mPinToggle.setChecked(!this.mIsChecked);
                this.mDialogState = 1;
                this.mPin = null;
                showPinDialog();
            }
            return false;
        }
    }

    private void tryChangeIccLockState() {
        Message callback = Message.obtain(this.mHandler, 100);
        if (this.mPhone != null) {
            this.mPhone.getIccCard().setIccLockEnabled(this.mIsChecked, this.mPin, callback);
        }
    }

    private void handleException(Throwable exception, int requestType) {
        if (exception instanceof CommandException) {
            if (((CommandException) exception).getCommandError() == Error.REQUEST_NOT_SUPPORTED) {
                int id;
                if (requestType == 100) {
                    id = 2131627289;
                } else {
                    id = 2131627354;
                }
                showShortToast(this.mRes.getString(id));
                return;
            }
            Utils.showRetryCounterToast(this.mContext, this.mSubscription, this.mIsCdmaPhone);
        } else if (exception instanceof RuntimeException) {
            showShortToast(exception.getMessage());
        }
    }

    private void iccLockChanged(AsyncResult ar) {
        if (ar.exception == null) {
            this.mPinDialog.setEnabled(this.mIsChecked);
            this.mPinToggle.setChecked(this.mIsChecked);
        } else {
            handleException(ar.exception, 100);
        }
        resetDialogState();
    }

    private void iccPinChanged(AsyncResult ar) {
        if (ar.exception != null) {
            if (((CommandException) ar.exception).getCommandError() == Error.REQUEST_NOT_SUPPORTED) {
                showShortToast(this.mRes.getString(2131627354));
            } else {
                Utils.showRetryCounterToast(this.mContext, this.mSubscription, this.mIsCdmaPhone);
            }
        } else if (this.mIsCdmaPhone) {
            showShortToast(this.mRes.getString(2131627326));
        } else {
            showShortToast(this.mRes.getString(2131627288));
        }
        resetDialogState();
    }

    private void tryChangePin() {
        Message callback = Message.obtain(this.mHandler, 101);
        if (this.mPhone != null) {
            this.mPhone.getIccCard().changeIccLockPassword(this.mOldPin, this.mNewPin, callback);
        }
    }

    private boolean reasonablePin(String pin) {
        if (pin == null || pin.length() < 4 || pin.length() > 8) {
            return false;
        }
        return true;
    }

    private void resetDialogState() {
        this.mError = null;
        this.mDialogState = 2;
        this.mPin = "";
        setDialogValues();
        this.mDialogState = 0;
    }

    private void showShortToast(String str) {
        Toast.makeText(this.mContext, str, 0).show();
    }

    private boolean isPukLocked() {
        boolean z = false;
        try {
            if (MSimTelephonyManager.getDefault().getSimState(this.mSubscription) == 3) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void unlockPuk() {
        String value = "LOCKED";
        String reason = "PUK";
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mSubscription);
        MLog.d("MSimIccLockSettingsFragment", "Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " for mCardIndex : " + this.mSubscription);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private boolean isPinLocked() {
        boolean z = false;
        try {
            if (TelephonyManager.getDefault().getSimState(this.mSubscription) == 2) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void unlockPin() {
        String value = "LOCKED";
        String reason = "PIN";
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra("reason", reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mSubscription);
        MLog.d("MSimIccLockSettingsFragment", "Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " for mCardIndex : " + this.mSubscription);
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    protected int getMetricsCategory() {
        return 56;
    }
}
