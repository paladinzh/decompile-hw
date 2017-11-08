package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;
import com.huawei.cust.HwCustUtils;

public class WifiCallingSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private ListPreference mButtonWfcMode;
    private boolean mEditableWfcMode = true;
    private TextView mEmptyView;
    private HwCustWifiCallingSettings mHwCustWifiCallingSettings;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.ims.REGISTRATION_ERROR")) {
                setResultCode(0);
                if (WifiCallingSettings.this.mWfcSwtichPrefs != null) {
                    WifiCallingSettings.this.mWfcSwtichPrefs.setChecked(false);
                }
                WifiCallingSettings.this.showAlert(intent);
            }
        }
    };
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            boolean z = false;
            boolean isNonTtyOrTtyOnVolteEnabled = ImsManager.isNonTtyOrTtyOnVolteEnabled((SettingsActivity) WifiCallingSettings.this.getActivity());
            boolean z2 = false;
            if (WifiCallingSettings.this.mWfcSwtichPrefs != null) {
                z2 = WifiCallingSettings.this.mWfcSwtichPrefs.isChecked() ? isNonTtyOrTtyOnVolteEnabled : false;
                SwitchPreference -get2 = WifiCallingSettings.this.mWfcSwtichPrefs;
                if (state != 0) {
                    isNonTtyOrTtyOnVolteEnabled = false;
                }
                -get2.setEnabled(isNonTtyOrTtyOnVolteEnabled);
            }
            Preference pref = WifiCallingSettings.this.getPreferenceScreen().findPreference("wifi_calling_mode");
            if (pref != null) {
                if (z2 && state == 0) {
                    z = true;
                }
                pref.setEnabled(z);
            }
        }
    };
    private boolean mPositiveResult;
    private CheckBox mRememberChoiceCheckBox;
    private boolean mValidListener = false;
    private SwitchPreference mWfcSwtichPrefs;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mEmptyView = (TextView) getView().findViewById(16908292);
        setEmptyView(this.mEmptyView);
        this.mEmptyView.setText(2131625098);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showAlert(Intent intent) {
        Context context = getActivity();
        CharSequence title = intent.getCharSequenceExtra("alertTitle");
        CharSequence message = intent.getCharSequenceExtra("alertMessage");
        Builder builder = new Builder(context);
        builder.setMessage(message).setTitle(title).setIcon(17301543).setPositiveButton(17039370, null);
        builder.create().show();
    }

    protected int getMetricsCategory() {
        return 105;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230938);
        this.mWfcSwtichPrefs = (SwitchPreference) findPreference("wifi_calling_switch");
        this.mWfcSwtichPrefs.setOnPreferenceChangeListener(this);
        this.mButtonWfcMode = (ListPreference) findPreference("wifi_calling_mode");
        this.mButtonWfcMode.setOnPreferenceChangeListener(this);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("com.android.ims.REGISTRATION_ERROR");
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        boolean z = true;
        boolean isCellularSupported = false;
        if (configManager != null) {
            PersistableBundle b = configManager.getConfig();
            if (b != null) {
                this.mEditableWfcMode = b.getBoolean("editable_wfc_mode_bool");
                z = b.getBoolean("carrier_wfc_supports_wifi_only_bool", true);
                isCellularSupported = b.getBoolean("carrier_wfc_supports_cellular_preferred_bool", false);
            }
        }
        if (!isCellularSupported && !z) {
            this.mButtonWfcMode.setEntries(2131362006);
            this.mButtonWfcMode.setEntryValues(2131362007);
        } else if (!z) {
            this.mButtonWfcMode.setEntries(2131362002);
            this.mButtonWfcMode.setEntryValues(2131362003);
        } else if (!isCellularSupported) {
            this.mButtonWfcMode.setEntries(2131362004);
            this.mButtonWfcMode.setEntryValues(2131362005);
        }
        this.mHwCustWifiCallingSettings = (HwCustWifiCallingSettings) HwCustUtils.createObj(HwCustWifiCallingSettings.class, new Object[0]);
        if (this.mHwCustWifiCallingSettings != null) {
            this.mHwCustWifiCallingSettings.init(this, this.mEditableWfcMode);
        }
    }

    public void onResume() {
        boolean isNonTtyOrTtyOnVolteEnabled;
        super.onResume();
        Context context = getActivity();
        if (ImsManager.isWfcEnabledByPlatform(context)) {
            ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 32);
            this.mValidListener = true;
        }
        if (ImsManager.isWfcEnabledByUser(context)) {
            isNonTtyOrTtyOnVolteEnabled = ImsManager.isNonTtyOrTtyOnVolteEnabled(context);
        } else {
            isNonTtyOrTtyOnVolteEnabled = false;
        }
        this.mWfcSwtichPrefs.setOnPreferenceChangeListener(null);
        this.mWfcSwtichPrefs.setChecked(isNonTtyOrTtyOnVolteEnabled);
        this.mWfcSwtichPrefs.setOnPreferenceChangeListener(this);
        updateButtonWfcMode(context, isNonTtyOrTtyOnVolteEnabled, ImsManager.getWfcMode(context, TelephonyManager.getDefault().isNetworkRoaming()));
        context.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        if (this.mHwCustWifiCallingSettings != null) {
            this.mHwCustWifiCallingSettings.updateSwitchBtnTitle();
            this.mHwCustWifiCallingSettings.updatePrefData();
            this.mHwCustWifiCallingSettings.registerCarrierReceiver();
        }
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("alertShow", false)) {
            showAlert(intent);
        }
    }

    public void onPause() {
        super.onPause();
        Context context = getActivity();
        if (this.mValidListener) {
            this.mValidListener = false;
            ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 0);
        }
        context.unregisterReceiver(this.mIntentReceiver);
        if (this.mHwCustWifiCallingSettings != null) {
            this.mHwCustWifiCallingSettings.unregisterCarrierReceiver();
        }
    }

    public void updateSwitchChanged(SwitchPreference switchPreference, boolean isChecked) {
        Context context = getActivity();
        ImsManager.setWfcSetting(context, isChecked);
        int wfcMode = ImsManager.getWfcMode(context, TelephonyManager.getDefault().isNetworkRoaming());
        updateButtonWfcMode(context, isChecked, wfcMode);
        if (isChecked) {
            MetricsLogger.action(getActivity(), getMetricsCategory(), wfcMode);
        } else {
            MetricsLogger.action(getActivity(), getMetricsCategory(), -1);
        }
        if (switchPreference != null) {
            switchPreference.setChecked(isChecked);
        }
    }

    private void updateButtonWfcMode(Context context, boolean wfcEnabled, int wfcMode) {
        this.mButtonWfcMode.setValue(Integer.toString(wfcMode));
        Utils.refreshListPreferenceSummary(this.mButtonWfcMode, String.valueOf(wfcMode));
        this.mButtonWfcMode.setEnabled(wfcEnabled);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (Utils.isHideWfcPreferenceForMcc(context)) {
            preferenceScreen.removePreference(this.mButtonWfcMode);
        } else if (this.mHwCustWifiCallingSettings == null || !this.mHwCustWifiCallingSettings.removeOrAddPrefBtnDyn(wfcEnabled)) {
            if (wfcEnabled && this.mEditableWfcMode) {
                preferenceScreen.addPreference(this.mButtonWfcMode);
            } else {
                preferenceScreen.removePreference(this.mButtonWfcMode);
            }
        }
        if (this.mHwCustWifiCallingSettings != null) {
            this.mHwCustWifiCallingSettings.removeWifiCallingModeIfNeeded(this);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        Context context = getActivity();
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("440")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("826");
        } else {
            equals = false;
        }
        if (preference == this.mButtonWfcMode) {
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mButtonWfcMode, ItemUseStat.KEY_WFC_MODE, (String) newValue);
            this.mButtonWfcMode.setValue((String) newValue);
            int buttonMode = Integer.valueOf((String) newValue).intValue();
            boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming();
            if (buttonMode != ImsManager.getWfcMode(context, isRoaming)) {
                ImsManager.setWfcMode(context, buttonMode, isRoaming);
                Utils.refreshListPreferenceSummary(this.mButtonWfcMode, (String) newValue);
                MetricsLogger.action(getActivity(), getMetricsCategory(), buttonMode);
            }
        } else if ("wifi_calling_switch".equals(preference.getKey())) {
            boolean isChecked = ((Boolean) newValue).booleanValue();
            boolean rememberChoiceNotChecked = Global.getInt(context.getContentResolver(), "wifi_calling_emergency_call_warning_remember_choice", 0) != 1;
            if (!isChecked || !rememberChoiceNotChecked || r0 || Utils.isHideWifiCallingForMcc(context)) {
                updateSwitchChanged((SwitchPreference) preference, isChecked);
            } else {
                showDialog(128);
            }
        }
        return true;
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 128:
                return createEmergencyCallWarningDialog();
            default:
                return super.onCreateDialog(id);
        }
    }

    private Dialog createEmergencyCallWarningDialog() {
        View view = getActivity().getLayoutInflater().inflate(2130968618, null);
        this.mRememberChoiceCheckBox = (CheckBox) view.findViewById(2131886216);
        this.mRememberChoiceCheckBox.setChecked(Global.getInt(getContentResolver(), "wifi_calling_emergency_call_warning_remember_choice", 0) == 1);
        ((TextView) view.findViewById(16908299)).setText(2131628514);
        Builder builder = new Builder(getActivity()).setView(view).setTitle(2131628513).setPositiveButton(2131628515, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                WifiCallingSettings.this.mPositiveResult = true;
                Global.putInt(WifiCallingSettings.this.getContentResolver(), "wifi_calling_emergency_call_warning_remember_choice", WifiCallingSettings.this.mRememberChoiceCheckBox.isChecked() ? 1 : 0);
                WifiCallingSettings.this.updateSwitchChanged(WifiCallingSettings.this.mWfcSwtichPrefs, true);
            }
        }).setNegativeButton(17039360, null);
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                SettingsDialogFragment dialogFragment = (SettingsDialogFragment) WifiCallingSettings.this.getDialogFragment();
                if (dialogFragment != null && dialogFragment.getDialogId() == 128) {
                    if (!WifiCallingSettings.this.mPositiveResult) {
                        WifiCallingSettings.this.mWfcSwtichPrefs.setOnPreferenceChangeListener(null);
                        WifiCallingSettings.this.mWfcSwtichPrefs.setChecked(false);
                        WifiCallingSettings.this.mWfcSwtichPrefs.setOnPreferenceChangeListener(WifiCallingSettings.this);
                    }
                    WifiCallingSettings.this.mPositiveResult = false;
                }
            }
        });
        return builder.create();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }
}
