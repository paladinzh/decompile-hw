package com.android.settings;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.HwAGPSManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class AGPSSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String AGPS_GOOGLE_SERVER_PORT = SystemProperties.get("ro.build.google.port", "7275");
    private static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static Map<String, String> PROFILE_SETTINGS = new HashMap();
    private static Map<String, String> PROFILE_SETTINGS_PORT = new HashMap();
    private String[] PROFILE_IDX = new String[]{"GOOGLE", "China Mobile", "Others"};
    private ListPreference mAGPSDataConnectivity;
    private CharSequence[] mAGPSDataConnectivityEntries;
    private CharSequence[] mAGPSDataConnectivityValues;
    private ListPreference mAGPSNetworkUsed;
    private CharSequence[] mAGPSNetworkUsedEntries;
    private ListPreference mAGPSSettings;
    private CharSequence[] mAGPSSettingsEntries;
    private Context mContext;
    private Editor mEditor;
    private ListPreference mGpsStartMode;
    private CharSequence[] mGpsStartModeEntries;
    private HwAGPSManager mHwAGPSManager;
    private boolean mIsProjectMode = false;
    private EditTextPreference mPortET;
    private EditTextPreference mSLPAddressET;
    private ListPreference mSelectProfileListPref;
    private CharSequence[] mSelectProfileListPrefEntries;
    private String mSelectedKey;
    private SharedPreferences mSharedPref;
    private int mSubscription = 0;

    static {
        PROFILE_SETTINGS.put("GOOGLE", "supl.google.com");
        PROFILE_SETTINGS.put("China Mobile", "221.176.0.55");
        PROFILE_SETTINGS.put("Others", "0.0.0.0");
        PROFILE_SETTINGS_PORT.put("GOOGLE", AGPS_GOOGLE_SERVER_PORT);
        PROFILE_SETTINGS_PORT.put("China Mobile", "7275");
        PROFILE_SETTINGS_PORT.put("Others", "7275");
    }

    public void onCreate(Bundle savedInstanceState) {
        String initProfileStr;
        String agpsServerAddress;
        String agpsServerPort;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230732);
        this.mContext = getActivity();
        this.mSharedPref = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mEditor = this.mSharedPref.edit();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if ("ProjectMode".equals(bundle.getString("Mode"))) {
                this.mIsProjectMode = true;
            }
        }
        this.mHwAGPSManager = new HwAGPSManager(this.mContext);
        this.mAGPSSettings = (ListPreference) findPreference("agps_settings");
        this.mAGPSNetworkUsed = (ListPreference) findPreference("agps_network_used");
        this.mAGPSDataConnectivity = (ListPreference) findPreference("agps_data_connectivity");
        this.mSelectProfileListPref = (ListPreference) findPreference("select_profile");
        this.mSelectProfileListPref.setOnPreferenceChangeListener(this);
        this.mSelectProfileListPrefEntries = this.mSelectProfileListPref.getEntries();
        this.mSLPAddressET = (EditTextPreference) findPreference("agps_service_address");
        this.mPortET = (EditTextPreference) findPreference("agps_service_port");
        if (!this.mSharedPref.contains("pref_key_agps_profile_selected")) {
            this.mEditor.putString("pref_key_agps_profile_selected", "GOOGLE");
            this.mEditor.commit();
        }
        if (this.mSharedPref.contains("pref_key_agps_Server_Addr")) {
            PROFILE_SETTINGS.put("Others", this.mSharedPref.getString("pref_key_agps_Server_Addr", "0.0.0.0"));
        }
        if (this.mSharedPref.contains("pref_key_agps_Server_Port")) {
            PROFILE_SETTINGS_PORT.put("Others", this.mSharedPref.getString("pref_key_agps_Server_Port", "7275"));
        }
        try {
            initProfileStr = this.mSharedPref.getString("pref_key_agps_profile_selected", "GOOGLE");
            if (initProfileStr.equals("China Mobile AGPS Server")) {
                initProfileStr = "China Mobile";
                this.mEditor.putString("pref_key_agps_profile_selected", initProfileStr);
                this.mEditor.commit();
            }
            agpsServerAddress = "";
            agpsServerPort = "";
            agpsServerAddress = this.mHwAGPSManager.getAGPSServiceAddress();
            agpsServerPort = this.mHwAGPSManager.getAGPSServicePort();
            if (!(agpsServerAddress == null || agpsServerPort == null)) {
                if (agpsServerAddress.equals(PROFILE_SETTINGS.get("China Mobile")) && agpsServerPort.equals(PROFILE_SETTINGS_PORT.get("China Mobile"))) {
                    this.mEditor.putString("pref_key_agps_profile_selected", "China Mobile");
                    this.mEditor.commit();
                } else if (agpsServerAddress.equals(PROFILE_SETTINGS.get("GOOGLE")) && agpsServerPort.equals(PROFILE_SETTINGS_PORT.get("GOOGLE"))) {
                    this.mEditor.putString("pref_key_agps_profile_selected", "GOOGLE");
                    this.mEditor.commit();
                }
                initProfileStr = this.mSharedPref.getString("pref_key_agps_profile_selected", "GOOGLE");
            }
        } catch (ClassCastException e) {
            this.mEditor.remove("pref_key_agps_profile_selected");
            this.mEditor.putString("pref_key_agps_profile_selected", "GOOGLE");
            this.mEditor.commit();
            initProfileStr = "GOOGLE";
            this.mHwAGPSManager.setAGPSServiceAddress((String) PROFILE_SETTINGS.get("GOOGLE"));
            this.mHwAGPSManager.setAGPSServicePort((String) PROFILE_SETTINGS_PORT.get("GOOGLE"));
        }
        int initProfile = this.mSelectProfileListPref.findIndexOfValue(initProfileStr);
        if (initProfile == -1) {
            initProfile = 0;
            this.mHwAGPSManager.setAGPSServiceAddress((String) PROFILE_SETTINGS.get("GOOGLE"));
            this.mHwAGPSManager.setAGPSServicePort((String) PROFILE_SETTINGS_PORT.get("GOOGLE"));
        }
        this.mSelectProfileListPref.setValueIndex(initProfile);
        this.mSelectProfileListPref.setSummary(this.mSelectProfileListPrefEntries[initProfile]);
        agpsServerAddress = this.mHwAGPSManager.getAGPSServiceAddress();
        agpsServerPort = this.mHwAGPSManager.getAGPSServicePort();
        boolean isSelectOthers = initProfile == this.mSelectProfileListPrefEntries.length + -1;
        this.mSLPAddressET.setEnabled(isSelectOthers);
        this.mPortET.setEnabled(isSelectOthers);
        this.mSLPAddressET.setSummary((CharSequence) agpsServerAddress);
        this.mHwAGPSManager.setAGPSServiceAddress(agpsServerAddress);
        this.mSLPAddressET.setText(agpsServerAddress);
        this.mPortET.setSummary((CharSequence) agpsServerPort);
        this.mHwAGPSManager.setAGPSServicePort(agpsServerPort);
        this.mPortET.setText(agpsServerPort);
        if (!this.mIsProjectMode) {
            getPreferenceScreen().removePreference(this.mAGPSSettings);
        }
        this.mAGPSSettings.setOnPreferenceChangeListener(this);
        this.mAGPSNetworkUsed.setOnPreferenceChangeListener(this);
        this.mSLPAddressET.setOnPreferenceChangeListener(this);
        this.mPortET.setOnPreferenceChangeListener(this);
        this.mAGPSSettingsEntries = this.mAGPSSettings.getEntries();
        this.mAGPSNetworkUsedEntries = this.mAGPSNetworkUsed.getEntries();
        int agpsModeSettingsIndex = this.mAGPSSettings.findIndexOfValue(Integer.toString(this.mHwAGPSManager.getAGPSModeSettings()));
        this.mAGPSSettings.setValueIndex(agpsModeSettingsIndex);
        this.mAGPSSettings.setSummary(this.mAGPSSettingsEntries[agpsModeSettingsIndex]);
        int agpsNetworkUsedIndex = this.mAGPSNetworkUsed.findIndexOfValue(Integer.toString(this.mHwAGPSManager.getAGPSRoamingEnable()));
        this.mAGPSNetworkUsed.setValueIndex(agpsNetworkUsedIndex);
        this.mAGPSNetworkUsed.setSummary(this.mAGPSNetworkUsedEntries[agpsNetworkUsedIndex]);
        initAGPSDataConnectivity();
        if (isPlatformSupportVsim() && getVSimSubId() != -1) {
            this.mAGPSDataConnectivity.setEnabled(false);
            this.mAGPSDataConnectivity.setEntries(2131361961);
            this.mAGPSDataConnectivity.setEntryValues(2131361962);
        } else if (this.mAGPSDataConnectivityEntries == null || this.mAGPSDataConnectivityValues == null) {
            this.mAGPSDataConnectivity.setSummary(2131628013);
            this.mAGPSDataConnectivity.setEnabled(false);
            this.mAGPSDataConnectivity.setEntries(2131361961);
            this.mAGPSDataConnectivity.setEntryValues(2131361962);
        } else {
            this.mSelectedKey = getSelectedApnKey();
            this.mAGPSDataConnectivity.setEntries(this.mAGPSDataConnectivityEntries);
            this.mAGPSDataConnectivity.setEntryValues(this.mAGPSDataConnectivityValues);
            if (this.mSelectedKey != null) {
                int agpsDataConnectivityIndex = this.mAGPSDataConnectivity.findIndexOfValue(this.mSelectedKey);
                if (agpsDataConnectivityIndex != -1) {
                    this.mAGPSDataConnectivity.setValueIndex(agpsDataConnectivityIndex);
                    this.mAGPSDataConnectivity.setSummary(this.mAGPSDataConnectivityEntries[agpsDataConnectivityIndex]);
                } else {
                    this.mAGPSDataConnectivity.setValueIndex(0);
                    this.mAGPSDataConnectivity.setSummary(this.mAGPSDataConnectivityEntries[0]);
                }
            }
            this.mAGPSDataConnectivity.setOnPreferenceChangeListener(this);
        }
        initGpsStartMode();
    }

    private void initGpsStartMode() {
        this.mGpsStartMode = (ListPreference) findPreference("gps_start");
        if (this.mIsProjectMode) {
            this.mGpsStartMode.setOnPreferenceChangeListener(this);
            this.mGpsStartModeEntries = this.mGpsStartMode.getEntries();
            int index = this.mHwAGPSManager.getGpsStartModeSettings();
            this.mGpsStartMode.setValueIndex(index);
            this.mGpsStartMode.setSummary(this.mGpsStartModeEntries[index]);
            return;
        }
        getPreferenceScreen().removePreference(this.mGpsStartMode);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String newEntryValue = newValue.toString();
        if (this.mAGPSSettings == preference) {
            this.mHwAGPSManager.setAGPSModeSettings(Integer.parseInt(newEntryValue));
            Utils.refreshListPreferenceSummary((ListPreference) preference, newEntryValue);
            return true;
        } else if (this.mGpsStartMode == preference) {
            setGpsStartMode(this.mGpsStartMode.findIndexOfValue(newEntryValue));
            Utils.refreshListPreferenceSummary((ListPreference) preference, newEntryValue);
            return true;
        } else if (this.mAGPSNetworkUsed == preference) {
            this.mHwAGPSManager.setAGPSRoamingEnable(Integer.parseInt(newEntryValue));
            Utils.refreshListPreferenceSummary((ListPreference) preference, newEntryValue);
            return true;
        } else if (this.mAGPSDataConnectivity == preference) {
            setSelectedApnKey(newEntryValue);
            Utils.refreshListPreferenceSummary((ListPreference) preference, newEntryValue);
            return true;
        } else {
            if (this.mSLPAddressET == preference) {
                this.mHwAGPSManager.setAGPSServiceAddress(newEntryValue);
                this.mSLPAddressET.setSummary((CharSequence) newEntryValue);
                this.mSLPAddressET.setText(newEntryValue);
                this.mEditor.putString("pref_key_agps_Server_Addr", newEntryValue);
                this.mEditor.commit();
                PROFILE_SETTINGS.put("Others", newEntryValue);
            } else if (this.mPortET == preference) {
                if (isValidPortValue(newEntryValue)) {
                    this.mHwAGPSManager.setAGPSServicePort(newEntryValue);
                    this.mPortET.setSummary((CharSequence) newEntryValue);
                    this.mPortET.setText(newEntryValue);
                    this.mEditor.putString("pref_key_agps_Server_Port", newEntryValue);
                    this.mEditor.commit();
                    PROFILE_SETTINGS_PORT.put("Others", newEntryValue);
                } else {
                    showToast(getString(2131628014));
                }
            } else if (preference == this.mSelectProfileListPref) {
                int index = this.mSelectProfileListPref.findIndexOfValue(newEntryValue);
                String agpsServiceAddress = String.valueOf(PROFILE_SETTINGS.get(newEntryValue));
                String agpsPort = String.valueOf(PROFILE_SETTINGS_PORT.get(newEntryValue));
                boolean isSelectOthers = index == this.mSelectProfileListPrefEntries.length + -1;
                this.mSLPAddressET.setEnabled(isSelectOthers);
                this.mPortET.setEnabled(isSelectOthers);
                this.mSLPAddressET.setSummary((CharSequence) agpsServiceAddress);
                this.mHwAGPSManager.setAGPSServiceAddress(agpsServiceAddress);
                this.mSLPAddressET.setText(agpsServiceAddress);
                this.mPortET.setSummary((CharSequence) agpsPort);
                this.mHwAGPSManager.setAGPSServicePort(agpsPort);
                this.mPortET.setText(agpsPort);
                this.mEditor.putString("pref_key_agps_profile_selected", this.PROFILE_IDX[index]);
                if (isSelectOthers) {
                    this.mEditor.putString("pref_key_agps_Server_Addr", agpsServiceAddress);
                    this.mEditor.putString("pref_key_agps_Server_Port", agpsPort);
                }
                this.mEditor.commit();
                Utils.refreshListPreferenceSummary((ListPreference) preference, newEntryValue);
                return true;
            }
            return false;
        }
    }

    private void setGpsStartMode(int index) {
        this.mHwAGPSManager.setGpsStartModeSettings(index);
    }

    private void showToast(String toastPrompt) {
        if (this.mContext != null) {
            Toast.makeText(this.mContext, toastPrompt, 0).show();
        }
    }

    private void initAGPSDataConnectivity() {
        String where;
        if (Utils.isMultiSimEnabled()) {
            this.mSubscription = MSimTelephonyManager.getDefault().getPreferredDataSubscription();
            where = "numeric=\"" + MSimTelephonyManager.getTelephonyProperty("gsm.sim.operator.numeric", this.mSubscription, "") + "\"" + "and name != \"CMDM\"";
        } else {
            where = "numeric=\"" + SystemProperties.get("gsm.sim.operator.numeric", "") + "\"" + "and name != \"CMDM\"";
        }
        Cursor cursor = null;
        try {
            CharSequence[] entries;
            CharSequence[] charSequenceArr;
            cursor = getContentResolver().query(Carriers.CONTENT_URI, new String[]{"_id", "name", "apn", "type"}, where, null, "name ASC");
            int arraySize = 0;
            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    if (!"mms".equals(cursor.getString(3))) {
                        arraySize++;
                    }
                    cursor.moveToNext();
                }
            }
            if (arraySize != 0) {
                entries = new CharSequence[arraySize];
                charSequenceArr = new CharSequence[arraySize];
                int index = 0;
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String apn = cursor.getString(2);
                    String key = cursor.getString(0);
                    if (!"mms".equals(cursor.getString(3))) {
                        entries[index] = apn;
                        charSequenceArr[index] = key;
                        index++;
                    }
                    cursor.moveToNext();
                }
            } else {
                entries = null;
                charSequenceArr = null;
            }
            this.mAGPSDataConnectivityEntries = entries;
            this.mAGPSDataConnectivityValues = charSequenceArr;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getSelectedApnKey() {
        Cursor cursor;
        String key = null;
        if (Utils.isMultiSimEnabled()) {
            this.mSubscription = MSimTelephonyManager.getDefault().getPreferredDataSubscription();
            cursor = getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), new String[]{"_id"}, null, null, "name ASC");
        } else {
            cursor = getContentResolver().query(PREFERAPN_URI, new String[]{"_id"}, null, null, "name ASC");
        }
        if (cursor != null && cursor.moveToFirst()) {
            key = cursor.getString(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return key;
    }

    private void setSelectedApnKey(String key) {
        this.mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", this.mSelectedKey);
        if (Utils.isMultiSimEnabled()) {
            this.mSubscription = MSimTelephonyManager.getDefault().getPreferredDataSubscription();
            resolver.update(ContentUris.withAppendedId(PREFERAPN_URI, (long) this.mSubscription), values, null, null);
        } else {
            resolver.update(PREFERAPN_URI, values, null, null);
        }
        Intent intent = new Intent("com.android.huawei.APN_ACTION_SETTING_CHANGED");
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent);
        }
    }

    private boolean isPlatformSupportVsim() {
        return SystemProperties.getBoolean("ro.radio.vsim_support", false);
    }

    private int getVSimSubId() {
        return System.getInt(getContentResolver(), "vsim_enabled_subid", -1);
    }

    private boolean isValidPortValue(String str) {
        return (TextUtils.isEmpty(str) || str.charAt(0) == '0') ? false : true;
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
