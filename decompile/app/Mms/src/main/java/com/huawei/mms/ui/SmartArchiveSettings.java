package com.huawei.mms.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ListView;
import com.android.mms.MmsApp;
import com.android.mms.ui.EmuiSwitchPreference;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import com.huawei.mms.util.SmartArchiveSettingUtils.SmartArchiveSettingItem;
import com.huawei.mms.util.StatisticalHelper;

public class SmartArchiveSettings extends HwPreferenceActivity {
    private static final HwCustSmartArchiveSettings sHwCustSmartArchiveSettings = ((HwCustSmartArchiveSettings) HwCustUtils.createObj(HwCustSmartArchiveSettings.class, new Object[0]));
    private SmartArchiveSettingsFragment mSmartArchiveSettingsFragment;

    public static class SmartArchiveSettingsFragment extends HwPreferenceFragment implements OnPreferenceChangeListener {
        EmuiSwitchPreference mAutoDelete;
        PreferenceCategory mAutoDeleteCategory;
        AlertDialog mAutoDeleteDialog;
        PreferenceCategory mSettingsCategory;
        EmuiSwitchPreference mSmartArchive;

        private static class AutoDeleteConfirmListener implements OnClickListener {
            EmuiSwitchPreference mPreference;

            public AutoDeleteConfirmListener(EmuiSwitchPreference preference) {
                this.mPreference = preference;
            }

            public void onClick(DialogInterface dialog, int which) {
                if (-1 == which) {
                    this.mPreference.setChecked(true);
                    StatisticalHelper.reportEvent(MmsApp.getApplication(), 2050, "on");
                    SmartArchiveSettingUtils.enableAutoDelete();
                } else if (-2 == which) {
                    this.mPreference.setChecked(false);
                }
            }
        }

        private static class UpdateArchiveNumRunnable implements Runnable {
            private UpdateArchiveNumRunnable() {
            }

            public void run() {
                SmartArchiveSettingUtils.updateArchiveNumPrefixs(MmsApp.getApplication());
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.smart_archive_settings);
            setPreference();
        }

        public void onDestroy() {
            recoveryAutoDeleteState();
            super.onDestroy();
        }

        private void setPreference() {
            this.mSettingsCategory = (PreferenceCategory) findPreference("pref_key_smart_archive_settings_category");
            this.mAutoDeleteCategory = (PreferenceCategory) findPreference("pref_key_smart_archive_storage");
            for (SmartArchiveSettingItem item : SmartArchiveSettingUtils.getSmartArchiveSettingItems()) {
                if (1 != item.getType() || SmartArchiveSettingUtils.isHwNotiReceived()) {
                    EmuiSwitchPreference pref = new EmuiSwitchPreference(getActivity());
                    pref.setPreferenceId(R.id.smart_archive_switch_pre);
                    pref.setKey(item.getKey());
                    pref.setChecked(item.ismEnabled());
                    pref.setTitle(item.getTitle());
                    if ("archive_num_106".equals(item.getKey())) {
                        pref.setSummary(item.getSummary());
                    }
                    if (SmartArchiveSettings.sHwCustSmartArchiveSettings != null) {
                        SmartArchiveSettings.sHwCustSmartArchiveSettings.addPreferenceSummaryForServiceMessage(pref, item.getKey());
                    }
                    pref.setOnPreferenceChangeListener(this);
                    this.mSettingsCategory.addPreference(pref);
                    Preference listDivideLineRcs = new Preference(getActivity());
                    listDivideLineRcs.setPreferenceId(R.id.smart_archive_rcs_divide_line);
                    listDivideLineRcs.setLayoutResource(R.layout.listdivider);
                    this.mSettingsCategory.addPreference(listDivideLineRcs);
                }
            }
            if (this.mSettingsCategory.getPreferenceCount() > 0) {
                this.mSettingsCategory.removePreference(this.mSettingsCategory.getPreference(this.mSettingsCategory.getPreferenceCount() - 1));
            }
            this.mSettingsCategory.setDependency("pref_key_smart_archive_enable");
            this.mAutoDelete = (EmuiSwitchPreference) findPreference("pref_key_smart_archive_auto_delete");
            this.mAutoDelete.setOnPreferenceChangeListener(this);
            this.mAutoDeleteCategory.setDependency("pref_key_smart_archive_enable");
            this.mSmartArchive = (EmuiSwitchPreference) findPreference("pref_key_smart_archive_enable");
            this.mSmartArchive.setChecked(SmartArchiveSettingUtils.isSmartArchiveEnabled(getActivity()));
            this.mSmartArchive.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Context context = getActivity();
            String key = preference.getKey();
            MLog.d("SmartArchiveSettings", "onPreferenceTreeClick " + key);
            if (key == null || !(newValue instanceof Boolean)) {
                return false;
            }
            if (!(preference instanceof EmuiSwitchPreference)) {
                return false;
            }
            boolean isChecked = ((Boolean) newValue).booleanValue();
            MLog.d("SmartArchiveSettings", key + " enabled : " + isChecked);
            String reportStatus = isChecked ? "on" : "off";
            String message = getString(R.string.confirm_auto_delete_noti_message_content_new, new Object[]{Integer.valueOf(30)});
            if ("pref_key_smart_archive_auto_delete".equals(key)) {
                if (isChecked) {
                    StatisticalHelper.incrementReportCount(context, 2170);
                    AutoDeleteConfirmListener autoDeleteConfirmListener = new AutoDeleteConfirmListener((EmuiSwitchPreference) preference);
                    Builder builder = new Builder(context);
                    builder.setCancelable(true);
                    builder.setMessage(message);
                    builder.setPositiveButton(R.string.mms_enable, autoDeleteConfirmListener);
                    builder.setNegativeButton(R.string.no, autoDeleteConfirmListener);
                    builder.setTitle(R.string.confirm_auto_delete_noti_title);
                    this.mAutoDeleteDialog = builder.show();
                    this.mAutoDeleteDialog.setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == 4) {
                                SmartArchiveSettingsFragment.this.recoveryAutoDeleteState();
                            }
                            return false;
                        }
                    });
                    MessageUtils.setButtonTextColor(this.mAutoDeleteDialog, -1, context.getResources().getColor(R.color.mms_unread_text_color));
                    return true;
                }
                SmartArchiveSettingUtils.disableAutoDelete();
                StatisticalHelper.reportEvent(context, 2050, reportStatus);
                return true;
            } else if ("pref_key_smart_archive_enable".equals(key)) {
                StatisticalHelper.reportEvent(context, 2045, reportStatus);
                boolean isSmartArchiveAutoDeleteEnabled = SmartArchiveSettingUtils.isSmartArchiveAutoDeleteEnable(MmsApp.getApplication());
                if (isChecked) {
                    if (isSmartArchiveAutoDeleteEnabled) {
                        SmartArchiveSettingUtils.enableAutoDelete();
                    }
                } else if (isSmartArchiveAutoDeleteEnabled) {
                    SmartArchiveSettingUtils.disableAutoDelete();
                }
                SmartArchiveSettingUtils.setSmartArchiveEnabled(context, isChecked);
                return true;
            } else {
                SmartArchiveSettingUtils.updateSmartArchiveSettingItem(key, isChecked);
                ThreadEx.getSerialExecutor().execute(new UpdateArchiveNumRunnable());
                if ("archive_num_huawei".equals(key)) {
                    StatisticalHelper.reportEvent(context, 2046, reportStatus);
                    SmartArchiveSettingUtils.setHuaweiArchiveEnabled(context, isChecked);
                } else if ("archive_num_106".equals(key)) {
                    StatisticalHelper.reportEvent(context, 2047, reportStatus);
                } else if ("archive_num_bak".equals(key)) {
                    StatisticalHelper.reportEvent(context, 2048, reportStatus);
                } else if ("archive_num_comm_operator".equals(key)) {
                    StatisticalHelper.reportEvent(context, 2049, reportStatus);
                }
                return true;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void recoveryAutoDeleteState() {
            if (!(this.mAutoDeleteDialog == null || this.mAutoDelete == null || !this.mAutoDeleteDialog.isShowing())) {
                this.mAutoDelete.setChecked(false);
            }
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ((ListView) getView().findViewById(16908298)).setDivider(null);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.smart_archive_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.mSmartArchiveSettingsFragment = new SmartArchiveSettingsFragment();
        getFragmentManager().beginTransaction().replace(16908290, this.mSmartArchiveSettingsFragment).commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
