package com.android.settings.nfc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.RadioListPreference;
import com.android.settings.RadioListPreferenceManager;
import com.android.settings.RadioListPreferenceManager.OnOptionSelectedListener;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;
import java.util.List;

public class AndroidBeamHwExt extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, OnOptionSelectedListener {
    private static final boolean IS_ORANGE_NFC = SystemProperties.getBoolean("ro.config.hw_nfc_gsma", false);
    private static final boolean IS_SUPPORT_HUAWEI_BEAM = SystemProperties.getBoolean("ro.config.support_huawei_beam", true);
    private static final boolean mCardReaderEnable = SystemProperties.getBoolean("ro.config.nfc_cardreader", false);
    private static final boolean mNfcMsimce = SystemProperties.getBoolean("ro.config.hw_nfc_msimce", false);
    private RadioListPreference card1Preference;
    private RadioListPreference card2Preference;
    private ListPreference defaultPaySettings;
    private RadioListPreference embededPreference;
    private NfcForegroundPreference foreground;
    private RestrictedPreference huaweiBeam;
    private Activity mActivity;
    private boolean mBeamShouldOpen = false;
    private CardEmulation mCardEmuManager;
    private SwitchPreference mCardReadPreference;
    private IntentFilter mIntentFilter;
    private boolean mIsDisabled;
    private String[] mMsg;
    private SwitchPreference mNFCPreference;
    private NfcAdapter mNfcAdapter;
    private int mNfcIndex = 0;
    private ListPreference mNfcPayBothCardPreference;
    private String[] mNfcPayEntries;
    private PreferenceCategory mNfcPayListPreference;
    private PreferenceScreen mNfcPayNoCardPreference;
    private Preference mNfcPayOneCardPreference;
    private PaymentBackend mPaymentBackend;
    private ProgressDialog mProgressDialog = null;
    private ArrayList<RadioListPreference> mRadioPreferenceList;
    private RadioListPreferenceManager mRadioPreferenceManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.nfc.action.ADAPTER_STATE_CHANGED".equals(action)) {
                Log.d("HwCustAndroidBeam", "Received Broadcast NfcAdapter.EXTRA_ADAPTER_STATE" + intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1) + "IS_CE_SWITCH is" + intent.getBooleanExtra("isCeSwitch", false));
                AndroidBeamHwExt.this.handleNfcStateChanged(intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1), intent.getBooleanExtra("isCeSwitch", false));
            } else if ("com.huawei.android.nfc.SWITCH_CE_STATE".equals(action)) {
                Log.d("HwCustAndroidBeam", "Received Broadcast SWITH_CE_SWITCH_ACTION");
                AndroidBeamHwExt.this.handleNfcSwitchResult(intent.getIntExtra("com.huawei.android.nfc.CE_SELECTED_STATE", -1));
                if (AndroidBeamHwExt.this.mNfcAdapter != null) {
                    AndroidBeamHwExt.this.handleNfcStateChanged(AndroidBeamHwExt.this.mNfcAdapter.isEnabled() ? 3 : 1, true);
                }
            }
        }
    };
    private int mSelectCard = 0;
    private List<ApduServiceInfo> mServiceInfos = null;
    private int mSupportCard = 0;
    private TelephonyManager mTelephonyManager;
    private int nfcRouteApk = 0;
    private PreferenceCategory nfc_more_settings;
    private NfcPaymentPreference nfc_payment_settings;
    private Preference preDefaultAppPay;
    private Preference preNfcRouteTable;

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked;
        if ("nfc_switch".equals(preference.getKey())) {
            if (this.mNfcAdapter == null) {
                return false;
            }
            String str;
            isChecked = ((Boolean) newValue).booleanValue();
            if (isChecked) {
                this.mNfcAdapter.enable();
            } else {
                this.mNfcAdapter.disable();
            }
            ItemUseStat instance = ItemUseStat.getInstance();
            Context context = this.mActivity;
            String str2 = "Nfc status";
            if (isChecked) {
                str = "on";
            } else {
                str = "off";
            }
            instance.handleClick(context, 3, str2, str);
        } else if (preference == this.mNfcPayBothCardPreference) {
            current = this.mNfcPayBothCardPreference.getValue();
            if (current == null || current.isEmpty()) {
                this.mNfcIndex = 0;
            } else {
                this.mNfcIndex = Integer.valueOf(current).intValue();
            }
            if (Integer.valueOf((String) newValue).intValue() == this.mNfcIndex) {
                return true;
            }
            this.mNfcAdapter.selectCardEmulation(Integer.valueOf((String) newValue).intValue());
            createDialog(0);
            this.mProgressDialog.show();
            return true;
        } else if (preference == this.mCardReadPreference) {
            if (this.mNfcAdapter == null) {
                return false;
            }
            isChecked = ((Boolean) newValue).booleanValue();
            Log.d("HwCustAndroidBeam", "mCardReadPreference change " + isChecked);
            if (isChecked) {
                this.mNfcAdapter.enableTagRw();
                this.mBeamShouldOpen = true;
                this.huaweiBeam.setEnabled(true);
            } else {
                this.mNfcAdapter.disableTagRw();
                this.huaweiBeam.setEnabled(false);
            }
            setPreferenceEnableOrNotByRestriction(this.huaweiBeam, "no_outgoing_beam", this.mActivity);
            return true;
        } else if (preference == this.defaultPaySettings) {
            boolean value;
            current = String.valueOf(newValue);
            boolean currentMode = this.mPaymentBackend.isForegroundMode();
            if (current.isEmpty()) {
                value = true;
            } else {
                value = Boolean.valueOf(current).booleanValue();
            }
            if (value != currentMode) {
                this.mPaymentBackend.setForegroundMode(value);
            }
            this.defaultPaySettings.setValue(String.valueOf(value));
            if (value) {
                ItemUseStat.getInstance().handleClick(this.mActivity, 2, String.valueOf(value));
                this.defaultPaySettings.setSummary(2131628482);
            } else {
                this.defaultPaySettings.setSummary(2131628491);
            }
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mNfcPayOneCardPreference) {
            if (this.mNfcAdapter == null) {
                return false;
            }
            try {
                this.mSupportCard = this.mNfcAdapter.getSupportCardEmulation();
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            if (this.mSupportCard == 1) {
                Toast.makeText(this.mActivity, String.format(getResources().getString(2131628495), new Object[]{getResources().getString(2131627386)}), 0).show();
            } else if (this.mSupportCard == 2) {
                Toast.makeText(this.mActivity, String.format(getResources().getString(2131628495), new Object[]{getResources().getString(2131627387)}), 0).show();
            }
        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230822);
        this.mActivity = getActivity();
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mActivity);
        if (this.mNfcAdapter == null) {
            getActivity().finish();
            return;
        }
        ItemUseStat.getInstance().handleClick(this.mActivity, 2, "Nfc Menu clicked");
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        this.mIntentFilter.addAction("com.huawei.android.nfc.SWITCH_CE_STATE");
        this.mNfcPayEntries = new String[2];
        this.mMsg = new String[this.mNfcPayEntries.length];
        defaultPayAppSettingEntries = new String[2];
        CharSequence[] defaultPayAppValue = new CharSequence[]{"false", "true"};
        boolean nfcStatus = this.mNfcAdapter.isEnabled();
        this.mNFCPreference = (SwitchPreference) findPreference("nfc_switch");
        this.mNFCPreference.setOnPreferenceChangeListener(this);
        this.mNFCPreference.setChecked(nfcStatus);
        if (!IS_SUPPORT_HUAWEI_BEAM) {
            this.mNFCPreference.setSummary(2131628497);
        }
        this.mNfcPayOneCardPreference = findPreference("nfc_pay_one_card");
        this.mNfcPayOneCardPreference.setOnPreferenceClickListener(this);
        this.mNfcPayNoCardPreference = (PreferenceScreen) findPreference("nfc_pay_no_card");
        this.mNfcPayBothCardPreference = (ListPreference) findPreference("nfc_pay_both_card");
        this.mNfcPayBothCardPreference.setOnPreferenceChangeListener(this);
        initCardPreferences(nfcStatus);
        if (mNfcMsimce) {
            try {
                this.mSupportCard = this.mNfcAdapter.getSupportCardEmulation();
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            if (this.mSupportCard == 0) {
                removePreference("nfc_pay_one_card");
                removePreference("nfc_pay_both_card");
                this.mNfcPayNoCardPreference.setEnabled(false);
            } else if (this.mSupportCard == 3) {
                removePreference("nfc_pay_no_card");
                removePreference("nfc_pay_one_card");
                this.mSelectCard = this.mNfcAdapter.getSelectedCardEmulation();
                this.mNfcPayBothCardPreference.setEnabled(nfcStatus);
                if (this.mSelectCard == 1) {
                    this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131627386));
                } else if (this.mSelectCard == 2) {
                    if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                        this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131628498));
                    } else {
                        this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131627387));
                    }
                }
            } else {
                removePreference("nfc_pay_no_card");
                removePreference("nfc_pay_both_card");
                if (this.mSupportCard == 1) {
                    this.mNfcPayOneCardPreference.setSummary(getResources().getString(2131627386));
                } else if (this.mSupportCard == 2) {
                    this.mNfcPayOneCardPreference.setSummary(getResources().getString(2131627387));
                }
                this.mNfcPayOneCardPreference.setEnabled(nfcStatus);
            }
        } else {
            removePreference("nfc_pay_title");
            removePreference("nfc_pay_no_card");
            removePreference("nfc_pay_one_card");
            removePreference("nfc_pay_both_card");
        }
        removePreference("nfc_pay_title");
        removePreference("nfc_pay_no_card");
        removePreference("nfc_pay_one_card");
        removePreference("nfc_pay_both_card");
        this.mNfcPayListPreference = (PreferenceCategory) findPreference("payment_setting_title");
        this.mPaymentBackend = new PaymentBackend(this.mActivity);
        this.nfc_payment_settings = new NfcPaymentPreference(getActivity(), this.mPaymentBackend);
        this.foreground = new NfcForegroundPreference(getActivity(), this.mPaymentBackend);
        this.preDefaultAppPay = findPreference("pre_default_pay_settings");
        this.preDefaultAppPay.setLayoutResource(2130968977);
        this.preDefaultAppPay.setWidgetLayoutResource(2130968998);
        this.preDefaultAppPay.setFragment(DefaultPayAPPSetting.class.getName());
        this.preNfcRouteTable = findPreference("pre_nfc_route_table");
        this.preNfcRouteTable.setLayoutResource(2130968977);
        this.preNfcRouteTable.setWidgetLayoutResource(2130968998);
        this.preNfcRouteTable.setFragment(NfcRouteTable.class.getName());
        this.defaultPaySettings = (ListPreference) findPreference("pay_default_settings");
        this.defaultPaySettings.setLayoutResource(2130968977);
        this.defaultPaySettings.setWidgetLayoutResource(2130968998);
        defaultPayAppSettingEntries[0] = getResources().getString(2131628491);
        defaultPayAppSettingEntries[1] = getResources().getString(2131628482);
        this.defaultPaySettings.setEntries(defaultPayAppSettingEntries);
        this.defaultPaySettings.setEntryValues(defaultPayAppValue);
        this.defaultPaySettings.setValue(String.valueOf(this.mPaymentBackend.isForegroundMode()));
        this.defaultPaySettings.setSummary(this.defaultPaySettings.getEntry());
        this.defaultPaySettings.setOnPreferenceChangeListener(this);
        this.nfc_more_settings = new PreferenceCategory(this.mActivity);
        this.nfc_more_settings.setLayoutResource(2130968916);
        this.nfc_more_settings.setTitle(2131628487);
        this.nfc_more_settings.setPersistent(false);
        getPreferenceScreen().addPreference(this.nfc_more_settings);
        if (mCardReaderEnable) {
            this.mCardReadPreference = new SwitchPreference(this.mActivity);
            getPreferenceScreen().addPreference(this.mCardReadPreference);
            this.mCardReadPreference.setOnPreferenceChangeListener(this);
            this.mCardReadPreference.setTitle(2131628494);
            this.mCardReadPreference.setSummary(2131628496);
            this.mCardReadPreference.setChecked(this.mNfcAdapter.isTagRwEnabled());
            this.mCardReadPreference.setEnabled(nfcStatus);
        }
        this.huaweiBeam = new RestrictedPreference(this.mActivity);
        this.huaweiBeam.setLayoutResource(2130968977);
        this.huaweiBeam.setWidgetLayoutResource(2130968998);
        this.huaweiBeam.setTitle(2131627444);
        this.huaweiBeam.setPersistent(false);
        this.huaweiBeam.setFragment(HuaweiBeam.class.getName());
        getPreferenceScreen().addPreference(this.huaweiBeam);
        this.huaweiBeam.setEnabled(nfcStatus);
        setPreferenceEnableOrNotByRestriction(this.huaweiBeam, "no_outgoing_beam", this.mActivity);
        if (!IS_SUPPORT_HUAWEI_BEAM) {
            getPreferenceScreen().removePreference(this.huaweiBeam);
            getPreferenceScreen().removePreference(this.nfc_more_settings);
        }
    }

    private void initCardPreferences(boolean nfcStatus) {
        this.card1Preference = (RadioListPreference) findPreference("card1");
        this.card1Preference.setEnabled(nfcStatus);
        this.card2Preference = (RadioListPreference) findPreference("card2");
        this.card2Preference.setEnabled(nfcStatus);
        this.embededPreference = (RadioListPreference) findPreference("embeded");
        this.embededPreference.setEnabled(nfcStatus);
        if (mNfcMsimce) {
            this.mRadioPreferenceList = new ArrayList();
            if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                removePreference("card2");
                this.mRadioPreferenceList.add(this.card1Preference);
                this.mRadioPreferenceList.add(this.embededPreference);
            } else {
                removePreference("embeded");
                this.mRadioPreferenceList.add(this.card1Preference);
                this.mRadioPreferenceList.add(this.card2Preference);
            }
        } else {
            removePreference("security_chip_title");
            removePreference("card1");
            removePreference("card2");
            removePreference("embeded");
        }
        if (this.mRadioPreferenceList != null) {
            this.mRadioPreferenceManager = new RadioListPreferenceManager(this.mRadioPreferenceList);
            this.mRadioPreferenceManager.setOnOptionSelectedListener(this);
        }
    }

    private void setPreferenceEnableOrNotByRestriction(RestrictedPreference preference, String userRestriction, Context context) {
        if (preference != null && context != null) {
            boolean beamDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(context, userRestriction, UserHandle.myUserId());
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(context, userRestriction, UserHandle.myUserId());
            if (!(beamDisallowedBySystem || admin == null)) {
                preference.setDisabledByAdmin(admin);
            }
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mNfcAdapter != null) {
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (savedInstanceState != null) {
                this.mIsDisabled = savedInstanceState.getBoolean("is_switch_disabled", false);
                this.mNFCPreference.setEnabled(!this.mIsDisabled);
                if (mNfcMsimce) {
                    this.mNfcPayEntries[0] = savedInstanceState.getString("card1", "");
                    this.mNfcPayEntries[1] = savedInstanceState.getString("card2", "");
                    this.mNfcPayBothCardPreference.setEntries(this.mNfcPayEntries);
                    this.mNfcPayBothCardPreference.setEntryValues(2131361968);
                    this.mNfcPayBothCardPreference.setValue(String.valueOf(this.mNfcAdapter.getSelectedCardEmulation()));
                }
            }
            setHasOptionsMenu(true);
        }
    }

    private void updateRadioButtons(RadioListPreference activated) {
        if (this.mNfcAdapter != null) {
            try {
                this.mSupportCard = this.mNfcAdapter.getSupportCardEmulation();
                Log.d("HwCustAndroidBeam", "mSupportCard=" + this.mSupportCard);
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            if (this.mSupportCard == 1) {
                Toast.makeText(this.mActivity, String.format(getResources().getString(2131628495), new Object[]{getResources().getString(2131627386)}), 0).show();
            } else if (this.mSupportCard == 2) {
                Toast.makeText(this.mActivity, String.format(getResources().getString(2131628495), new Object[]{getResources().getString(2131627387)}), 0).show();
            }
            if (activated == null) {
                this.card1Preference.setChecked(false);
                this.card2Preference.setChecked(false);
                this.embededPreference.setChecked(false);
            } else if (activated == this.card1Preference) {
                this.card1Preference.setChecked(true);
                this.card2Preference.setChecked(false);
                this.embededPreference.setChecked(false);
            } else if (activated == this.card2Preference) {
                this.card1Preference.setChecked(false);
                this.card2Preference.setChecked(true);
                this.embededPreference.setChecked(false);
            } else if (activated == this.embededPreference) {
                this.card1Preference.setChecked(false);
                this.card2Preference.setChecked(false);
                this.embededPreference.setChecked(true);
            }
        }
    }

    public void onRadioButtonClicked(RadioListPreference emiter) {
        int mode = 1;
        if (this.mNfcAdapter != null) {
            if (emiter == this.card1Preference) {
                mode = 1;
                ItemUseStat.getInstance().handleClick(this.mActivity, 2, "select card1 slot");
            } else if (emiter == this.card2Preference) {
                mode = 2;
                ItemUseStat.getInstance().handleClick(this.mActivity, 2, "select card2 slot");
            } else if (emiter == this.embededPreference) {
                mode = 2;
                ItemUseStat.getInstance().handleClick(this.mActivity, 2, "select embeded");
            }
            if (mode != this.mSelectCard) {
                this.mSelectCard = mode;
                this.mNfcAdapter.selectCardEmulation(mode);
                createDialog(0);
                this.mProgressDialog.show();
            }
        }
    }

    private void createDialog(int dialogId) {
        switch (dialogId) {
            case 0:
                this.mProgressDialog = new ProgressDialog(this.mActivity);
                this.mProgressDialog.setMessage(getString(2131628483));
                this.mProgressDialog.setCancelable(false);
                return;
            default:
                return;
        }
    }

    private void handleNfcSwitchResult(int Recult) {
        if (mNfcMsimce && this.mNfcAdapter != null) {
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            if (Recult == -1) {
                Toast.makeText(this.mActivity, 2131628479, 1).show();
            }
            this.mSelectCard = this.mNfcAdapter.getSelectedCardEmulation();
            Log.d("HwCustAndroidBeam", "getSelectedCardEmulation=" + this.mSelectCard);
            this.mNfcPayBothCardPreference.setValue(String.valueOf(this.mSelectCard));
            if (this.mSelectCard == 1) {
                this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131627386));
            } else if (this.mSelectCard == 2) {
                if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                    this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131628498));
                } else {
                    this.mNfcPayBothCardPreference.setSummary(getResources().getString(2131627387));
                }
            }
        }
    }

    private void initNfcPayData() {
        if (this.mNfcAdapter != null) {
            try {
                this.mSupportCard = this.mNfcAdapter.getSupportCardEmulation();
            } catch (NoExtAPIException e) {
                e.printStackTrace();
            }
            if (this.mSupportCard == 3) {
                this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
                if (this.mTelephonyManager != null) {
                    if (this.mTelephonyManager.hasIccCard(0) && this.mTelephonyManager.hasIccCard(1)) {
                        this.mMsg[0] = this.mTelephonyManager.getNetworkOperatorName(0);
                        this.mMsg[1] = this.mTelephonyManager.getNetworkOperatorName(1);
                    } else if (this.mTelephonyManager.hasIccCard(0) && !this.mTelephonyManager.hasIccCard(1)) {
                        this.mMsg[0] = this.mTelephonyManager.getNetworkOperatorName(0);
                        this.mMsg[1] = "";
                    } else if (!this.mTelephonyManager.hasIccCard(1) || this.mTelephonyManager.hasIccCard(0)) {
                        this.mMsg[0] = "";
                        this.mMsg[1] = "";
                    } else {
                        this.mMsg[0] = "";
                        this.mMsg[1] = this.mTelephonyManager.getNetworkOperatorName(1);
                    }
                }
                if (getActivity() != null && isAdded()) {
                    if (TextUtils.isEmpty(this.mMsg[0])) {
                        this.mNfcPayEntries[0] = getResources().getString(2131627386);
                    } else {
                        this.mNfcPayEntries[0] = getResources().getString(2131627386) + ":" + this.mMsg[0];
                    }
                    if (TextUtils.isEmpty(this.mMsg[1])) {
                        this.mNfcPayEntries[1] = getResources().getString(2131627387);
                    } else {
                        this.mNfcPayEntries[1] = getResources().getString(2131627387) + ":" + this.mMsg[1];
                    }
                    if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                        this.mNfcPayEntries[1] = getResources().getString(2131628498);
                    }
                }
                this.mNfcPayBothCardPreference.setEntries(this.mNfcPayEntries);
                this.mNfcPayBothCardPreference.setEntryValues(2131361968);
                this.mNfcPayBothCardPreference.setValue(String.valueOf(this.mSelectCard));
                this.card1Preference.setTitle(this.mNfcPayEntries[0]);
                this.card2Preference.setTitle(this.mNfcPayEntries[1]);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mNfcAdapter != null) {
            outState.putString("card1", this.mNfcPayEntries[0]);
            outState.putString("card2", this.mNfcPayEntries[1]);
            outState.putBoolean("is_switch_disabled", this.mIsDisabled);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        try {
            if (this.mNfcAdapter == null) {
                this.mCardEmuManager = null;
                return;
            }
            this.mCardEmuManager = CardEmulation.getInstance(this.mNfcAdapter);
            if (this.mCardEmuManager != null) {
                this.mServiceInfos = this.mCardEmuManager.getServices("other");
            }
            if (this.mServiceInfos != null) {
                this.nfcRouteApk = this.mServiceInfos.size();
            }
            boolean showNfcAll = this.mActivity.getSharedPreferences("is_show_nfc_gsma", 0).getBoolean("isShow", false);
            if (!IS_ORANGE_NFC || !showNfcAll) {
                removePreference("pre_nfc_route_table");
            } else if (this.nfcRouteApk == 0) {
                removePreference("pre_nfc_route_table");
            } else if (getPreferenceScreen().findPreference("pre_nfc_route_table") == null) {
                getPreferenceScreen().addPreference(this.preNfcRouteTable);
            }
            boolean nfcStatus = this.mNfcAdapter.isEnabled();
            this.mSelectCard = this.mNfcAdapter.getSelectedCardEmulation();
            if (this.mSelectCard == 1) {
                updateRadioButtons(this.card1Preference);
            } else if (this.mSelectCard == 2) {
                if (SystemProperties.getBoolean("ro.config.nfc_hasese", false)) {
                    updateRadioButtons(this.embededPreference);
                } else {
                    updateRadioButtons(this.card2Preference);
                }
            }
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            this.mPaymentBackend.onResume();
            PaymentAppInfo defaultApp = this.mPaymentBackend.getDefaultApp();
            List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
            if (defaultApp != null) {
                this.preDefaultAppPay.setSummary(defaultApp.label);
                Log.d("HwCustAndroidBeam", "preDefaultAppPay setSummary =  " + defaultApp.label);
            } else if (appInfos == null || appInfos.size() <= 0) {
                this.preDefaultAppPay.setSummary(2131628484);
            } else {
                this.preDefaultAppPay.setSummary(2131626504);
            }
            initNfcPayData();
            this.huaweiBeam.setSummary(1 == System.getInt(getActivity().getContentResolver(), "androidBeam", 1) ? 2131625876 : 2131625877);
            if (appInfos != null) {
                Log.d("HwCustAndroidBeam", "onResume appInfos.size() =" + appInfos.size());
            }
            if (appInfos != null && appInfos.size() <= 0) {
                this.nfc_payment_settings.setEnabled(false);
                this.foreground.setEnabled(false);
                removePreference("pay_default_settings");
            } else if (appInfos != null && appInfos.size() <= 1) {
                this.mPaymentBackend.setForegroundMode(true);
                removePreference("pay_default_settings");
            } else if (appInfos != null && appInfos.size() > 1 && ((ListPreference) getPreferenceScreen().findPreference("pay_default_settings")) == null) {
                getPreferenceScreen().addPreference(this.defaultPaySettings);
            }
            if (nfcStatus) {
                this.mNFCPreference.setChecked(true);
                this.card1Preference.setEnabled(true);
                this.card2Preference.setEnabled(true);
                this.nfc_payment_settings.setEnabled(true);
                this.foreground.setEnabled(true);
                this.preDefaultAppPay.setEnabled(true);
                this.preNfcRouteTable.setEnabled(true);
                this.defaultPaySettings.setEnabled(true);
                this.embededPreference.setEnabled(true);
            } else {
                this.mNFCPreference.setChecked(false);
                this.card1Preference.setEnabled(false);
                this.card2Preference.setEnabled(false);
                this.nfc_payment_settings.setEnabled(false);
                this.foreground.setEnabled(false);
                this.preDefaultAppPay.setEnabled(false);
                this.preNfcRouteTable.setEnabled(false);
                this.defaultPaySettings.setEnabled(false);
                this.embededPreference.setEnabled(false);
            }
            int nfcState = this.mNfcAdapter.getAdapterState();
            Log.d("HwCustAndroidBeam", "Current nfc state is: " + nfcState);
            if (nfcState == 2 || nfcState == 4) {
                this.mNFCPreference.setEnabled(false);
            } else {
                this.mNFCPreference.setEnabled(true);
            }
            if (this.mCardReadPreference != null) {
                this.mCardReadPreference.setEnabled(nfcStatus);
                if (this.mCardReadPreference.isChecked() && this.mCardReadPreference.isEnabled()) {
                    this.huaweiBeam.setEnabled(true);
                } else {
                    this.huaweiBeam.setEnabled(false);
                }
            } else {
                this.huaweiBeam.setEnabled(nfcStatus);
            }
            setPreferenceEnableOrNotByRestriction(this.huaweiBeam, "no_outgoing_beam", this.mActivity);
            if (this.mActivity != null) {
                getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
            }
        } catch (UnsupportedOperationException e) {
            MLog.e("HwCustAndroidBeam", "This device does not support card emulation");
            this.mCardEmuManager = null;
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mNfcAdapter != null) {
            this.mPaymentBackend.onPause();
            if (this.mActivity != null) {
                this.mActivity.unregisterReceiver(this.mReceiver);
            }
        }
    }

    private void handleNfcStateChanged(int newState, boolean isCeSwitch) {
        switch (newState) {
            case 1:
                this.mNFCPreference.setChecked(false);
                this.mNFCPreference.setEnabled(true);
                this.mIsDisabled = false;
                if (this.mCardReadPreference != null) {
                    this.mCardReadPreference.setEnabled(false);
                }
                if (this.mNfcPayOneCardPreference != null) {
                    this.mNfcPayOneCardPreference.setEnabled(false);
                }
                if (this.mNfcPayBothCardPreference != null) {
                    this.mNfcPayBothCardPreference.setEnabled(false);
                }
                this.nfc_payment_settings.setEnabled(false);
                this.foreground.setEnabled(false);
                this.huaweiBeam.setEnabled(false);
                this.card1Preference.setEnabled(false);
                this.card2Preference.setEnabled(false);
                this.embededPreference.setEnabled(false);
                this.preDefaultAppPay.setEnabled(false);
                this.preNfcRouteTable.setEnabled(false);
                this.defaultPaySettings.setEnabled(false);
                return;
            case 2:
                this.mNFCPreference.setChecked(true);
                this.mNFCPreference.setEnabled(false);
                this.mIsDisabled = true;
                this.huaweiBeam.setEnabled(false);
                if (this.mCardReadPreference != null) {
                    this.mCardReadPreference.setEnabled(false);
                    return;
                }
                return;
            case 3:
                this.mNFCPreference.setChecked(true);
                this.mNFCPreference.setEnabled(true);
                this.mIsDisabled = false;
                if (this.mNfcPayOneCardPreference != null) {
                    this.mNfcPayOneCardPreference.setEnabled(true);
                }
                if (this.mNfcPayBothCardPreference != null) {
                    this.mNfcPayBothCardPreference.setEnabled(true);
                }
                if (this.mCardReadPreference != null) {
                    this.mCardReadPreference.setEnabled(true);
                    if (this.mCardReadPreference.isChecked()) {
                        this.huaweiBeam.setEnabled(true);
                    } else {
                        this.huaweiBeam.setEnabled(false);
                    }
                } else {
                    this.huaweiBeam.setEnabled(true);
                }
                setPreferenceEnableOrNotByRestriction(this.huaweiBeam, "no_outgoing_beam", this.mActivity);
                List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
                if (appInfos != null && appInfos.size() > 0) {
                    this.nfc_payment_settings.setEnabled(true);
                    this.foreground.setEnabled(true);
                }
                this.card1Preference.setEnabled(true);
                this.card2Preference.setEnabled(true);
                this.embededPreference.setEnabled(true);
                this.preDefaultAppPay.setEnabled(true);
                this.preNfcRouteTable.setEnabled(true);
                this.defaultPaySettings.setEnabled(true);
                return;
            case 4:
                this.mNFCPreference.setChecked(false);
                this.mNFCPreference.setEnabled(false);
                this.mIsDisabled = true;
                this.huaweiBeam.setEnabled(false);
                if (this.mCardReadPreference != null) {
                    this.mCardReadPreference.setEnabled(false);
                }
                this.nfc_payment_settings.setEnabled(false);
                this.foreground.setEnabled(false);
                this.huaweiBeam.setEnabled(false);
                this.card1Preference.setEnabled(false);
                this.card2Preference.setEnabled(false);
                this.embededPreference.setEnabled(false);
                this.preDefaultAppPay.setEnabled(false);
                this.preNfcRouteTable.setEnabled(false);
                this.defaultPaySettings.setEnabled(false);
                return;
            default:
                Log.d("HwCustAndroidBeam", "received unknow event, just return");
                return;
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 3, 0, 2131627944).setIcon(2130838282);
        MenuItem menuItem = menu.findItem(3);
        menuItem.setIntent(new Intent(getActivity(), HowItWorks.class));
        menuItem.setShowAsActionFlags(2);
        if (Utils.hasIntentActivity(this.mActivity.getPackageManager(), "android.nfc.action.SHOW_RECEIVED") && IS_SUPPORT_HUAWEI_BEAM) {
            menu.add(0, 1, 0, 2131624450);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent = new Intent("android.nfc.action.SHOW_RECEIVED");
                ItemUseStat.getInstance().handleClick(this.mActivity, 2, "show received files");
                this.mActivity.startActivity(intent);
                break;
            default:
                Log.d("HwCustAndroidBeam", "received unknow event, just return");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected int getMetricsCategory() {
        return 69;
    }

    public void onOptionSelected(RadioListPreference preference, int index) {
        onRadioButtonClicked(preference);
    }

    public boolean isSelectEnabled() {
        return true;
    }
}
