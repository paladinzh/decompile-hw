package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class PowerSettingActivity extends HsmActivity {
    private static String TAG = "PowerSettingActivity";
    private PowerSettingsFragment mPowerSettingsFragment;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    PowerSettingActivity.this.mPowerSettingsFragment.enableOrDisableDataPrefs();
                } else if (ActionConst.INTENT_REMIND_CHECKBOX_CHANGE.equals(action)) {
                    HwLog.i(PowerSettingActivity.TAG, "INTENT_REMIND_CHECKBOX_CHANGE broadcast receive, do nothing.");
                }
            }
        }
    };
    private ProgressDialog progressDialog = null;

    public static class PowerSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
        private static final int DIALOG_INDEX_DATA = 0;
        private static final int DIALOG_INDEX_LENGHT = 1;
        private PowerSettingActivity mActivity = null;
        private Context mAppContext = null;
        private boolean[] mDialogClickOK = new boolean[1];
        private SwitchPreference mHighConsumePref = null;
        private AlertDialog mShowingDialog = null;
        private PreferenceCategory mSleepCategory = null;
        private SwitchPreference mSleepDataPref = null;
        private ListPreference mSleepWlanPref = null;
        private SwitchPreference mSuperHighPowerTitleKey = null;
        private PreferenceCategory mSuperPowerSavingCategory;

        private abstract class MyAbsDialogListener implements OnDismissListener, OnClickListener {
            protected abstract void subClickPositive();

            protected abstract void subDismiss();

            private MyAbsDialogListener() {
            }

            public void onDismiss(DialogInterface dialog) {
                subDismiss();
                PowerSettingsFragment.this.mShowingDialog = null;
            }

            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    subClickPositive();
                }
            }
        }

        private class DataDialogListener extends MyAbsDialogListener {
            private DataDialogListener() {
                super();
            }

            protected void subDismiss() {
                if (!PowerSettingsFragment.this.mDialogClickOK[0]) {
                    PowerSettingsFragment.this.mSleepDataPref.setChecked(true);
                }
            }

            protected void subClickPositive() {
                try {
                    System.putInt(PowerSettingsFragment.this.mAppContext.getContentResolver(), SharedPrefKeyConst.POWER_SAVING_ON, 1);
                } catch (NoExtAPIException e) {
                    HwLog.e(PowerSettingActivity.TAG, "onClick->NoExtAPIException!");
                }
                PowerSettingsFragment.this.mDialogClickOK[0] = true;
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.power_setting_preference);
            this.mSleepCategory = (PreferenceCategory) findPreference(SharedPrefKeyConst.SLEEP_CATEGORY_KEY);
            this.mSuperPowerSavingCategory = (PreferenceCategory) findPreference(SharedPrefKeyConst.SUPER_POWER_SAVING_CATEGORY_KEY);
            ((TextArrowPreference) findPreference(SharedPrefKeyConst.HIGH_COMSUME_HISTORY_KEY)).setOnPreferenceClickListener(this);
            this.mHighConsumePref = (SwitchPreference) findPreference(SharedPrefKeyConst.HIGH_COMSUME_REMIND_KEY);
            this.mHighConsumePref.setOnPreferenceChangeListener(this);
            this.mSleepWlanPref = (ListPreference) findPreference(SharedPrefKeyConst.SLEEP_WLAN_KEY);
            this.mSleepDataPref = (SwitchPreference) findPreference(SharedPrefKeyConst.SLEEP_DATA_KEY);
            this.mSleepDataPref.setOnPreferenceChangeListener(this);
            ((TextArrowPreference) findPreference(SharedPrefKeyConst.SUPER_POWER_SAVING_ABOUT_KEY)).setOnPreferenceClickListener(this);
            this.mSuperHighPowerTitleKey = (SwitchPreference) findPreference(SharedPrefKeyConst.SUPER_HIGH_POWER_TITLE_KEY);
            this.mSuperHighPowerTitleKey.setOnPreferenceChangeListener(this);
            ((TextArrowPreference) findPreference(SharedPrefKeyConst.SUPER_HIGH_POWER_RECORD_KEY)).setOnPreferenceClickListener(this);
        }

        public void onAttach(Activity activity) {
            if (activity instanceof PowerSettingActivity) {
                this.mActivity = (PowerSettingActivity) activity;
                this.mAppContext = this.mActivity.getApplicationContext();
            }
            super.onAttach(activity);
        }

        public void onResume() {
            super.onResume();
            updateConsumePreference();
            updateSleepConnectGroup();
            updateSuperPreference();
            updateSuperHighPowerPreference();
        }

        private void updateConsumePreference() {
            this.mHighConsumePref.setChecked(SharePrefWrapper.getPrefValue(this.mAppContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true));
        }

        private void updateSuperHighPowerPreference() {
            this.mSuperHighPowerTitleKey.setChecked(SysCoreUtils.getSuperHighPowerSwitchState(this.mAppContext));
        }

        private void updateWlanPreference() {
            if (this.mSleepWlanPref != null) {
                if (Utility.isWifiOnlyMode()) {
                    this.mSleepWlanPref.setEntries(R.array.power_setting_wlan_list_preference_options_wifi_only);
                }
                this.mSleepWlanPref.setOnPreferenceChangeListener(this);
                String stringValue = String.valueOf(Global.getInt(this.mAppContext.getContentResolver(), "wifi_sleep_policy", 2));
                this.mSleepWlanPref.setValue(stringValue);
                updateWlanPolicySummary(this.mSleepWlanPref, stringValue);
            }
        }

        private void updateWlanPolicySummary(Preference sleepPolicyPref, String value) {
            if (value != null) {
                int summaryArrayResId;
                String[] values = getResources().getStringArray(R.array.power_setting_wlan_list_preference_keys);
                if (Utility.isWifiOnlyMode()) {
                    summaryArrayResId = R.array.power_setting_wlan_list_preference_options_wifi_only;
                } else {
                    summaryArrayResId = R.array.power_setting_wlan_list_preference_options_emui305;
                }
                String[] summaries = getResources().getStringArray(summaryArrayResId);
                int i = 0;
                while (i < values.length) {
                    if (!value.equals(values[i]) || i >= summaries.length) {
                        i++;
                    } else {
                        sleepPolicyPref.setSummary(summaries[i]);
                        return;
                    }
                }
            }
            sleepPolicyPref.setSummary("");
            HwLog.e(PowerSettingActivity.TAG, "Invalid sleep policy value: " + value);
        }

        private void updateDataPreference() {
            if (Utility.isWifiOnlyMode() && this.mSleepCategory != null) {
                this.mSleepCategory.removePreference(this.mSleepDataPref);
            } else if (enableOrDisableDataPrefs()) {
                int power_saving_on = 0;
                try {
                    power_saving_on = System.getInt(this.mAppContext.getContentResolver(), SharedPrefKeyConst.POWER_SAVING_ON, 0);
                } catch (Exception e) {
                    HwLog.e(PowerSettingActivity.TAG, "onResume->NoExtAPIException!");
                }
                HwLog.d(PowerSettingActivity.TAG, "power_saving_on =" + power_saving_on);
                if (this.mShowingDialog == null || !this.mShowingDialog.isShowing()) {
                    if (1 == power_saving_on) {
                        this.mSleepDataPref.setChecked(false);
                    } else {
                        this.mSleepDataPref.setChecked(true);
                    }
                }
            }
        }

        private void updateSuperPreference() {
            if (!Utility.superPowerEntryEnable() || Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
                getPreferenceScreen().removePreference(this.mSuperPowerSavingCategory);
            }
        }

        private void updateSuperRemindPrefSummary(Preference preference, String newThreshVal) {
            CharSequence string;
            String threshValue = NumberLocationPercent.getPercentage(Double.valueOf(newThreshVal).doubleValue(), 0);
            if ("0".equals(newThreshVal)) {
                string = getResources().getString(R.string.ListViewFirstLine_SystemManager_SuperSavingSettings);
            } else {
                string = threshValue.replace("%", "%%");
            }
            preference.setSummary(string);
        }

        private boolean enableOrDisableDataPrefs() {
            if (PowerSettingActivity.isAirplaneModeOn(this.mAppContext) || !PowerSettingActivity.getSimState()) {
                this.mSleepDataPref.setEnabled(false);
                return false;
            }
            this.mSleepDataPref.setEnabled(true);
            return true;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue == null) {
                return false;
            }
            String key = preference.getKey();
            String statParam;
            if (SharedPrefKeyConst.SLEEP_WLAN_KEY.equals(key)) {
                try {
                    String stringValue = (String) newValue;
                    Global.putInt(this.mAppContext.getContentResolver(), "wifi_sleep_policy", Integer.parseInt(stringValue));
                    updateWlanPolicySummary(preference, stringValue);
                    statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, stringValue);
                    HsmStat.statE(29, statParam);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if (SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY.equals(key)) {
                String newThreshVal = (String) newValue;
                SharePrefWrapper.setPrefValue(this.mAppContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_POWER_SAVING_THRESHOLD_KEY, newThreshVal);
                updateSuperRemindPrefSummary(preference, newThreshVal);
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, newThreshVal);
                HsmStat.statE(31, statParam);
            } else if (SharedPrefKeyConst.SLEEP_DATA_KEY.equals(key)) {
                isChecked = ((Boolean) newValue).booleanValue();
                if (isChecked != this.mSleepDataPref.isChecked()) {
                    if (isChecked) {
                        try {
                            System.putInt(this.mAppContext.getContentResolver(), SharedPrefKeyConst.POWER_SAVING_ON, 0);
                        } catch (NoExtAPIException e2) {
                            HwLog.e(PowerSettingActivity.TAG, "onPreferenceTreeClick->NoExtAPIException!");
                            return false;
                        }
                    }
                    createAndShowDialog(0);
                }
                r9 = new String[2];
                r9[0] = HsmStatConst.PARAM_OP;
                r9[1] = isChecked ? "1" : "0";
                HsmStat.statE(30, HsmStatConst.constructJsonParams(r9));
            } else if (SharedPrefKeyConst.HIGH_COMSUME_REMIND_KEY.equals(key)) {
                isChecked = ((Boolean) newValue).booleanValue();
                this.mHighConsumePref.setChecked(isChecked);
                SharePrefWrapper.setPrefValue(this.mAppContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, isChecked);
                r9 = new String[2];
                r9[0] = HsmStatConst.PARAM_OP;
                r9[1] = isChecked ? "1" : "0";
                HsmStat.statE(27, HsmStatConst.constructJsonParams(r9));
            } else if (SharedPrefKeyConst.SUPER_HIGH_POWER_TITLE_KEY.equals(key)) {
                isChecked = ((Boolean) newValue).booleanValue();
                this.mSuperHighPowerTitleKey.setChecked(isChecked);
                HwLog.i(PowerSettingActivity.TAG, "SUPER_HIGH_POWER_TITLE_KEY newValue =" + isChecked);
                if (isChecked) {
                    SysCoreUtils.setSuperHighPowerSwitchState(this.mAppContext, true);
                } else {
                    SysCoreUtils.setSuperHighPowerSwitchState(this.mAppContext, false);
                }
                r9 = new String[2];
                r9[0] = HsmStatConst.PARAM_OP;
                r9[1] = isChecked ? "1" : "0";
                HsmStat.statE((int) Events.E_POWER_SUPER_POWERCONSUME_APP_AUTOCLEAR, HsmStatConst.constructJsonParams(r9));
            }
            return true;
        }

        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (SharedPrefKeyConst.HIGH_COMSUME_HISTORY_KEY.equals(key)) {
                startActivity(new Intent(this.mActivity, HistoryOfHighPowerAppActivity.class));
                HsmStat.statE(Events.E_POWER_SUPERPOWER_REMIND_LIST);
                return true;
            } else if (!SharedPrefKeyConst.SUPER_POWER_SAVING_ABOUT_KEY.equals(key)) {
                if (SharedPrefKeyConst.SUPER_HIGH_POWER_RECORD_KEY.equals(key)) {
                    startActivity(new Intent(this.mActivity, SuperHighPowerActivity.class));
                    HsmStat.statE(Events.E_POWER_APP_AUTOCLEAR_LIST);
                }
                return false;
            } else if (!Utility.isOwnerUser()) {
                return false;
            } else {
                startActivity(new Intent(this.mActivity, SuperPowerSavingModeAboutActivity.class));
                HsmStat.statE(32);
                return true;
            }
        }

        private void updateSleepConnectGroup() {
            updateWlanPreference();
            updateDataPreference();
        }

        private void createAndShowDialog(int dialogIndex) {
            resetDialogOKRecord();
            switch (dialogIndex) {
                case 0:
                    DataDialogListener dataDialogListener = new DataDialogListener();
                    this.mShowingDialog = new Builder(this.mActivity).setMessage(getResources().getString(R.string.network_always_on_warning)).setTitle(17039380).setIcon(17301543).setPositiveButton(17039379, dataDialogListener).setNegativeButton(17039369, dataDialogListener).setOnDismissListener(dataDialogListener).show();
                    return;
                default:
                    HwLog.e(PowerSettingActivity.TAG, "createDialog failed of invalid index: " + dialogIndex);
                    return;
            }
        }

        private void resetDialogOKRecord() {
            for (int i = 0; i < 1; i++) {
                this.mDialogClickOK[i] = false;
            }
        }

        public void showCustomDialog(final Context context) {
            Builder builder = new Builder(context);
            View customView = LayoutInflater.from(context).inflate(R.layout.super_high_power_dialog_layout, null);
            builder.setTitle(R.string.super_high_power_dialog_title);
            builder.setView(customView);
            builder.setPositiveButton(context.getText(R.string.superconsume_remind_turn_on), new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, true);
                    PowerSettingsFragment.this.mSuperHighPowerTitleKey.setChecked(true);
                    dialog.dismiss();
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                    HsmStat.statE((int) Events.E_POWER_SUPER_POWERCONSUME_APP_AUTOCLEAR_DIALOG, statParam);
                }
            });
            builder.setNegativeButton(context.getText(R.string.cancel), new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SharePrefWrapper.setPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, false);
                    PowerSettingsFragment.this.mSuperHighPowerTitleKey.setChecked(false);
                    dialog.dismiss();
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
                    HsmStat.statE((int) Events.E_POWER_SUPER_POWERCONSUME_APP_AUTOCLEAR_DIALOG, statParam);
                }
            });
            builder.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialoginterface) {
                    PowerSettingsFragment.this.mSuperHighPowerTitleKey.setChecked(false);
                    HwLog.i(PowerSettingActivity.TAG, "onCancel");
                }
            });
            builder.create().show();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_main_frame);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.menu_settings);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initUI();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionConst.INTENT_REMIND_CHECKBOX_CHANGE);
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        registerReceiver(this.mReceiver, intentFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    protected void onResume() {
        super.onResume();
        ListView listview = (ListView) findViewById(16908298);
        if (listview != null) {
            listview.setDivider(null);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
    }

    protected void onDestroy() {
        unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        if (this.mPowerSettingsFragment == null) {
            this.mPowerSettingsFragment = new PowerSettingsFragment();
        }
        if (!this.mPowerSettingsFragment.isAdded()) {
            getFragmentManager().beginTransaction().replace(R.id.power_frame, this.mPowerSettingsFragment).commit();
        }
    }

    private static boolean getSimState() {
        return !getSingleSimState(0) ? getSingleSimState(1) : true;
    }

    private static boolean getSingleSimState(int sub) {
        boolean enableCard = false;
        try {
            int simState = MSimTelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotId(sub));
            if (simState == 1 || simState == 0 || simState == 2 || simState == 3 || simState == 4 || simState == 7 || simState == 8) {
                enableCard = false;
            } else {
                enableCard = true;
            }
            HwLog.d(TAG, "sub = " + sub + " enableCard = " + enableCard);
        } catch (NoExtAPIException e) {
            HwLog.e(TAG, "getSingleSimState NoExtAPIException, subId: " + sub);
        }
        return enableCard;
    }

    private static boolean isAirplaneModeOn(Context context) {
        return Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }
}
