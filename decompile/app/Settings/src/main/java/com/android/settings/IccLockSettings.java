package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class IccLockSettings extends SettingsPreferenceFragment implements OnPinEnteredListener, OnPreferenceChangeListener {
    private int mDialogState = 0;
    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        public View createTabContent(String tag) {
            return new View(IccLockSettings.this.mTabHost.getContext());
        }
    };
    private String mError;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            AsyncResult ar = msg.obj;
            IccLockSettings iccLockSettings;
            switch (msg.what) {
                case 100:
                    iccLockSettings = IccLockSettings.this;
                    if (ar.exception != null) {
                        z = false;
                    }
                    iccLockSettings.iccLockChanged(z, msg.arg1);
                    return;
                case 101:
                    iccLockSettings = IccLockSettings.this;
                    if (ar.exception != null) {
                        z = false;
                    }
                    iccLockSettings.iccPinChanged(z, msg.arg1);
                    return;
                case 102:
                    IccLockSettings.this.updatePreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private ListView mListView;
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
                IccLockSettings.this.mHandler.sendMessage(IccLockSettings.this.mHandler.obtainMessage(102));
            }
        }
    };
    private TabHost mTabHost;
    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        public void onTabChanged(String tabId) {
            Phone phone = null;
            SubscriptionInfo sir = SubscriptionManager.from(IccLockSettings.this.getActivity().getBaseContext()).getActiveSubscriptionInfoForSimSlotIndex(Integer.parseInt(tabId));
            IccLockSettings iccLockSettings = IccLockSettings.this;
            if (sir != null) {
                phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(sir.getSubscriptionId()));
            }
            iccLockSettings.mPhone = phone;
            IccLockSettings.this.updatePreferences();
        }
    };
    private TabWidget mTabWidget;
    private boolean mToState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230889);
        this.mPinDialog = (EditPinPreference) findPreference("sim_pin");
        this.mPinToggle = (SwitchPreference) findPreference("sim_toggle");
        this.mPinToggle.setOnPreferenceChangeListener(this);
        if (savedInstanceState != null && savedInstanceState.containsKey("dialogState")) {
            this.mDialogState = savedInstanceState.getInt("dialogState");
            this.mPin = savedInstanceState.getString("dialogPin");
            this.mError = savedInstanceState.getString("dialogError");
            this.mToState = savedInstanceState.getBoolean("enableState");
            switch (this.mDialogState) {
                case 3:
                    this.mOldPin = savedInstanceState.getString("oldPinCode");
                    break;
                case 4:
                    this.mOldPin = savedInstanceState.getString("oldPinCode");
                    this.mNewPin = savedInstanceState.getString("newPinCode");
                    break;
                default:
                    Log.w("IccLockSettings", "onCreate unknown DialogState:" + this.mDialogState);
                    break;
            }
        }
        this.mPinDialog.setOnPinEnteredListener(this);
        getPreferenceScreen().setPersistent(false);
        this.mRes = getResources();
        if (Utils.isMonkeyRunning()) {
            finish();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int numSims = ((TelephonyManager) getContext().getSystemService("phone")).getSimCount();
        if (numSims > 1) {
            Phone phone;
            View view = inflater.inflate(2130968831, container, false);
            ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
            Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
            prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
            this.mTabHost = (TabHost) view.findViewById(16908306);
            this.mTabWidget = (TabWidget) view.findViewById(16908307);
            this.mListView = (ListView) view.findViewById(16908298);
            this.mTabHost.setup();
            this.mTabHost.setOnTabChangedListener(this.mTabListener);
            this.mTabHost.clearAllTabs();
            SubscriptionManager sm = SubscriptionManager.from(getContext());
            for (int i = 0; i < numSims; i++) {
                Object string;
                SubscriptionInfo subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(i);
                TabHost tabHost = this.mTabHost;
                String valueOf = String.valueOf(i);
                if (subInfo == null) {
                    string = getContext().getString(2131626614, new Object[]{Integer.valueOf(i + 1)});
                } else {
                    string = subInfo.getDisplayName();
                }
                tabHost.addTab(buildTabSpec(valueOf, String.valueOf(string)));
            }
            SubscriptionInfo sir = sm.getActiveSubscriptionInfoForSimSlotIndex(0);
            if (sir == null) {
                phone = null;
            } else {
                phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(sir.getSubscriptionId()));
            }
            this.mPhone = phone;
            return view;
        }
        this.mPhone = PhoneFactory.getDefaultPhone();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        updatePreferences();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    private void updatePreferences() {
        boolean z;
        boolean z2 = true;
        EditPinPreference editPinPreference = this.mPinDialog;
        if (this.mPhone != null) {
            z = true;
        } else {
            z = false;
        }
        editPinPreference.setEnabled(z);
        SwitchPreference switchPreference = this.mPinToggle;
        if (this.mPhone == null) {
            z2 = false;
        }
        switchPreference.setEnabled(z2);
        if (this.mPhone != null) {
            this.mPinToggle.setChecked(this.mPhone.getIccCard().getIccLockEnabled());
        }
    }

    protected int getMetricsCategory() {
        return 56;
    }

    public void onResume() {
        super.onResume();
        getContext().registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        if (this.mDialogState != 0) {
            showPinDialog();
        } else {
            resetDialogState();
        }
    }

    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(this.mSimStateReceiver);
    }

    public void onSaveInstanceState(Bundle out) {
        if (this.mPinDialog.isDialogOpen()) {
            out.putInt("dialogState", this.mDialogState);
            out.putString("dialogError", this.mError);
            out.putBoolean("enableState", this.mToState);
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
                    Log.w("IccLockSettings", "onSaveInstanceState unknown DialogState:" + this.mDialogState);
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
        this.mPinDialog.setText(this.mPin);
        String message = "";
        switch (this.mDialogState) {
            case 1:
                CharSequence string;
                message = this.mRes.getString(2131625190);
                EditPinPreference editPinPreference = this.mPinDialog;
                if (this.mToState) {
                    string = this.mRes.getString(2131625191);
                } else {
                    string = this.mRes.getString(2131625192);
                }
                editPinPreference.setDialogTitle(string);
                break;
            case 2:
                message = this.mRes.getString(2131625193);
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131625196));
                break;
            case 3:
                message = this.mRes.getString(2131625194);
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131625196));
                break;
            case 4:
                message = this.mRes.getString(2131625195);
                this.mPinDialog.setDialogTitle(this.mRes.getString(2131625196));
                break;
            default:
                Log.w("IccLockSettings", "setDialogValues unknown DialogState:" + this.mDialogState);
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
                    default:
                        Log.w("IccLockSettings", "onPinEntered unknown DialogState:" + this.mDialogState);
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
        if (preference != this.mPinDialog) {
            return true;
        }
        this.mDialogState = 2;
        return false;
    }

    private void tryChangeIccLockState() {
        this.mPhone.getIccCard().setIccLockEnabled(this.mToState, this.mPin, Message.obtain(this.mHandler, 100));
        this.mPinToggle.setEnabled(false);
    }

    private void iccLockChanged(boolean success, int attemptsRemaining) {
        if (getContext() != null) {
            if (success) {
                this.mPinToggle.setChecked(this.mToState);
            } else {
                Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining), 1).show();
            }
            this.mPinToggle.setEnabled(true);
            resetDialogState();
        }
    }

    private void iccPinChanged(boolean success, int attemptsRemaining) {
        if (getContext() != null) {
            if (success) {
                Toast.makeText(getContext(), this.mRes.getString(2131625200), 0).show();
            } else {
                Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining), 1).show();
            }
            resetDialogState();
        }
    }

    private void tryChangePin() {
        this.mPhone.getIccCard().changeIccLockPassword(this.mOldPin, this.mNewPin, Message.obtain(this.mHandler, 101));
    }

    private String getPinPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;
        if (attemptsRemaining == 0) {
            displayMessage = this.mRes.getString(2131625210);
        } else if (attemptsRemaining > 0) {
            displayMessage = this.mRes.getQuantityString(2131689482, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)});
        } else {
            displayMessage = this.mRes.getString(2131625211);
        }
        Log.d("IccLockSettings", "getPinPasswordErrorMessage: attemptsRemaining=" + attemptsRemaining + " displayMessage=" + displayMessage);
        return displayMessage;
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

    private TabSpec buildTabSpec(String tag, String title) {
        return this.mTabHost.newTabSpec(tag).setIndicator(title).setContent(this.mEmptyTabContent);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("sim_toggle".equals(preference.getKey())) {
            boolean z;
            this.mToState = ((Boolean) newValue).booleanValue();
            SwitchPreference switchPreference = this.mPinToggle;
            if (this.mToState) {
                z = false;
            } else {
                z = true;
            }
            switchPreference.setChecked(z);
            this.mDialogState = 1;
            showPinDialog();
        }
        return false;
    }
}
