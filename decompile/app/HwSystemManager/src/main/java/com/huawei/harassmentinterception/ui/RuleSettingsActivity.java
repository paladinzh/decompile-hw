package com.huawei.harassmentinterception.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.text.format.DateUtils;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.harassmentinterception.update.UpdateService;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.preference.TextArrowPreferenceCompat;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;

public class RuleSettingsActivity extends HsmPreferenceActivity {
    private static final int DLG_SHOW_MAX_TIME = 60000;
    private static final int DLG_SHOW_MIN_TIME = 2000;
    private static final String KEY_BLOCK_RULES = "harassment_rules";
    private static final int MSG_DISMISS_DLG_DELAY_MAX = 1;
    private static final int MSG_DISMISS_DLG_DELAY_MIN = 2;
    private static final int NO_UPDATE_ONE_YEAR = 1;
    public static final String TAG = "RuleSettingsActivity";
    private TextArrowPreferenceCompat mAutoUpdatePref = null;
    private TextArrowPreferenceCompat mBlackListPref = null;
    private Preference mBlockRulesPref;
    private ConnectivityManager mConnectivityManager = null;
    private final HsmSingleExecutor mExecutor = new HsmSingleExecutor();
    private Handler mHandler = new MyHandler(this);
    private Boolean mIsEnableIntelligentEngine = Boolean.valueOf(false);
    private TextArrowPreferenceCompat mKeywordsPref = null;
    private Preference mManualUpdatePref = null;
    private TextArrowPreferenceCompat mNotificationPref = null;
    OnPreferenceChangeListener mPrefChangeListener = new RuleSettingPrefChangeListener();
    OnPreferenceClickListener mPrefClickListener = new RulePrefClickListener();
    private ProgressDialog mProgressDialog = null;
    private SwitchPreference mSettingPref = null;
    private BroadcastReceiver mUpateReceiver = null;
    private TextArrowPreferenceCompat mWhiteListPref = null;

    private class AutoUpdateClicker implements OnPreferenceClickListener {
        private AutoUpdateClicker() {
        }

        public boolean onPreferenceClick(Preference preference) {
            RuleSettingsActivity.this.showChooseUpdateRulesDialog();
            return true;
        }
    }

    private static class DataHolder {
        int blackListNum;
        int keywordsNum;
        int whiteListNum;

        private DataHolder() {
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, DataHolder> {
        private LoadDataTask() {
        }

        protected DataHolder doInBackground(Void... params) {
            Context ctx = RuleSettingsActivity.this.getApplication();
            DataHolder dataHolder = new DataHolder();
            dataHolder.blackListNum = DBAdapter.getBlackListCount(ctx);
            dataHolder.keywordsNum = DBAdapter.getKeywordsList(ctx).size();
            dataHolder.whiteListNum = DBAdapter.getWhiteListCount(ctx);
            return dataHolder;
        }

        protected void onPostExecute(DataHolder dataHolder) {
            if (dataHolder != null) {
                Context ctx = RuleSettingsActivity.this.getApplication();
                RuleSettingsActivity.this.mBlackListPref.setDetail(ctx.getResources().getQuantityString(R.plurals.harassment_number_unit, dataHolder.blackListNum, new Object[]{Integer.valueOf(dataHolder.blackListNum)}));
                RuleSettingsActivity.this.mKeywordsPref.setDetail(ctx.getResources().getQuantityString(R.plurals.harassment_number_unit, dataHolder.keywordsNum, new Object[]{Integer.valueOf(dataHolder.keywordsNum)}));
                RuleSettingsActivity.this.mWhiteListPref.setDetail(ctx.getResources().getQuantityString(R.plurals.harassment_number_unit, dataHolder.whiteListNum, new Object[]{Integer.valueOf(dataHolder.whiteListNum)}));
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<RuleSettingsActivity> mActivity = null;

        MyHandler() {
        }

        MyHandler(RuleSettingsActivity activity) {
            this.mActivity = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RuleSettingsActivity activity = (RuleSettingsActivity) this.mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                    case 2:
                        if (activity.mProgressDialog != null) {
                            activity.mProgressDialog.dismiss();
                            activity.updateSummaryForUpdatePreference();
                            break;
                        }
                        break;
                }
            }
        }
    }

    private class RulePrefClickListener implements OnPreferenceClickListener {
        private RulePrefClickListener() {
        }

        public boolean onPreferenceClick(Preference preference) {
            if (RuleSettingsActivity.KEY_BLOCK_RULES.equals(preference.getKey())) {
                Intent intent = new Intent();
                intent.setClass(RuleSettingsActivity.this, BlockRulesActivity.class);
                RuleSettingsActivity.this.startActivity(intent);
            } else if (ConstValues.UIKEY_MANUAL_UPDATE.equals(preference.getKey())) {
                boolean isNetworkConnected = false;
                if (RuleSettingsActivity.this.mConnectivityManager.getActiveNetworkInfo() != null) {
                    isNetworkConnected = RuleSettingsActivity.this.mConnectivityManager.getActiveNetworkInfo().isConnected();
                }
                if (isNetworkConnected) {
                    RuleSettingsActivity.this.startManualUpdate();
                    RuleSettingsActivity.this.showUpdateProgressDialog();
                    RuleSettingsActivity.this.shouldDismissDlgWithTime(60000);
                } else {
                    RuleSettingsActivity.this.showNoNetworkDialog();
                }
                HsmStat.statE(78);
            } else if (ConstValues.UIKEY_BLACKLIST_ENTRANCE.equals(preference.getKey())) {
                RuleSettingsActivity.this.startBlackWhiteListActivity(0);
                HsmStat.statE(71);
            } else if (ConstValues.UIKEY_KEYWORDS_ENTRANCE.equals(preference.getKey())) {
                RuleSettingsActivity.this.startKeywordsActivity();
            } else if (ConstValues.UIKEY_WHITELIST_ENTRANCE.equals(preference.getKey())) {
                RuleSettingsActivity.this.startBlackWhiteListActivity(1);
                HsmStat.statE(75);
            } else if (ConstValues.UIKEY_NOTIFICATION_SETTING.equals(preference.getKey())) {
                RuleSettingsActivity.this.onChooseNotifyRule();
            }
            return true;
        }
    }

    private class RuleSettingPrefChangeListener implements OnPreferenceChangeListener {
        private RuleSettingPrefChangeListener() {
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (ConstValues.UIKEY_INTERCEPTION_SETTING.equals(preference.getKey())) {
                if (ConstValues.isSupportNB()) {
                    HwLog.e(RuleSettingsActivity.TAG, "interception total setting changed, but current support NB, ignore");
                    return false;
                }
                boolean isChecked = ((Boolean) newValue).booleanValue();
                CommonHelper.setInterceptionSettingOn(RuleSettingsActivity.this, isChecked, true);
                RuleSettingsActivity.this.mNotificationPref.setEnabled(isChecked);
                String[] strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = isChecked ? "1" : "0";
                HsmStat.statE(68, HsmStatConst.constructJsonParams(strArr));
            }
            return true;
        }
    }

    class UpdateBroadcastReceiver extends BroadcastReceiver {
        UpdateBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if (!UpdateService.ACTION_BEGIN_CHECK.equals(action)) {
                    if (UpdateService.ACTION_FINISHED_CHECK.equals(action)) {
                        RuleSettingsActivity.this.updateProgressDialog(RuleSettingsActivity.this.getResources().getString(R.string.harassment_finish_update_message), true);
                        RuleSettingsActivity.this.updateSummaryForUpdatePreference();
                    } else if (UpdateService.ACTION_BEGIN_UPDATE.equals(action)) {
                        RuleSettingsActivity.this.updateProgressDialog(RuleSettingsActivity.this.getResources().getString(R.string.harassment_begin_update_message), false);
                    } else if (UpdateService.ACTION_FINISHED_UPDATE.equals(action)) {
                        RuleSettingsActivity.this.updateProgressDialog(RuleSettingsActivity.this.getResources().getString(R.string.harassment_finish_update_message), true);
                        RuleSettingsActivity.this.updateSummaryForUpdatePreference();
                    } else if (UpdateService.ACTION_NET_ERROR.equals(action)) {
                        String netErrorInf = RuleSettingsActivity.this.getResources().getString(R.string.harassment_net_error_message_Toast);
                        RuleSettingsActivity.this.updateProgressDialog(netErrorInf, true);
                        Toast.makeText(RuleSettingsActivity.this, netErrorInf, 1).show();
                    } else if (UpdateService.ACTION_OVERDUE_ERROR.equals(action)) {
                        RuleSettingsActivity.this.updateProgressDialog("", true);
                        new Builder(RuleSettingsActivity.this).setTitle(RuleSettingsActivity.this.getResources().getString(R.string.harassment_hint)).setMessage(RuleSettingsActivity.this.getResources().getString(R.string.harassment_overdue)).setPositiveButton(R.string.harassmet_confirm, null).show();
                    } else if (UpdateService.ACTION_UPDATE_ERROR.equals(action)) {
                        RuleSettingsActivity.this.updateProgressDialog(RuleSettingsActivity.this.getResources().getString(R.string.msg_update_error_Toast), true);
                    }
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_settings);
        this.mIsEnableIntelligentEngine = Boolean.valueOf(CustomizeWrapper.shouldEnableIntelligentEngine());
        addPreferencesFromResource(R.xml.interception_settings_preference);
        initPreferences();
    }

    protected void onResume() {
        super.onResume();
        ListView lv = (ListView) findViewById(16908298);
        if (lv != null) {
            LayoutParams params = (LayoutParams) lv.getLayoutParams();
            if (params != null) {
                params.topMargin = getResources().getDimensionPixelSize(R.dimen.lv_top_margin);
            }
        }
        if (lv != null) {
            lv.setDivider(null);
        }
        setPreferenceStates();
        refreshData();
    }

    protected void onDestroy() {
        super.onDestroy();
        destroyPreferences();
        this.mExecutor.clearAllTask();
    }

    private void initPreferences() {
        this.mSettingPref = (SwitchPreference) findPreference(ConstValues.UIKEY_INTERCEPTION_SETTING);
        this.mBlockRulesPref = findPreference(KEY_BLOCK_RULES);
        this.mBlackListPref = TextArrowPreferenceCompat.createFromPerfer(findPreference(ConstValues.UIKEY_BLACKLIST_ENTRANCE));
        this.mBlackListPref.setNetherSummary(getString(R.string.harassment_blacklist_setting_des));
        this.mKeywordsPref = TextArrowPreferenceCompat.createFromPerfer(findPreference(ConstValues.UIKEY_KEYWORDS_ENTRANCE));
        this.mKeywordsPref.setNetherSummary(getString(R.string.harassment_keywords_setting_des));
        this.mWhiteListPref = TextArrowPreferenceCompat.createFromPerfer(findPreference(ConstValues.UIKEY_WHITELIST_ENTRANCE));
        this.mWhiteListPref.setNetherSummary(getString(R.string.harassment_whitelist_setting_des));
        this.mAutoUpdatePref = TextArrowPreferenceCompat.createFromPerfer(findPreference(ConstValues.UIKEY_AUTO_UPDATE));
        this.mNotificationPref = TextArrowPreferenceCompat.createFromPerfer(findPreference(ConstValues.UIKEY_NOTIFICATION_SETTING));
        this.mSettingPref.setOnPreferenceChangeListener(this.mPrefChangeListener);
        this.mBlockRulesPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mBlackListPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mKeywordsPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mWhiteListPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mNotificationPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mAutoUpdatePref.setOnPreferenceClickListener(new AutoUpdateClicker());
        initUpdatePreferences();
    }

    private void initUpdatePreferences() {
        if (ConstValues.isSupportNB()) {
            getPreferenceScreen().removePreference(this.mSettingPref);
        }
        PreferenceCategory otherSettingCategory = (PreferenceCategory) findPreference(ConstValues.UIKEY_UPDATE_DEPOT_CATEGORY);
        this.mManualUpdatePref = otherSettingCategory.findPreference(ConstValues.UIKEY_MANUAL_UPDATE);
        Preference splitLinePref = otherSettingCategory.findPreference(ConstValues.UIKEY_LINE_EMUI16);
        otherSettingCategory.removePreference(this.mManualUpdatePref);
        if (this.mIsEnableIntelligentEngine.booleanValue()) {
            this.mManualUpdatePref.setOnPreferenceClickListener(this.mPrefClickListener);
            this.mProgressDialog = new ProgressDialog(this);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
            this.mProgressDialog.setCancelable(false);
            initUpdateBroadcastReceiver();
            return;
        }
        otherSettingCategory.removePreference(this.mAutoUpdatePref.getPrference());
        otherSettingCategory.removePreference(splitLinePref);
    }

    private void destroyPreferences() {
        destroyUpdatePreferences();
    }

    private void destroyUpdatePreferences() {
        if (this.mUpateReceiver != null) {
            unregisterReceiver(this.mUpateReceiver);
        }
        if (this.mProgressDialog != null) {
            this.mProgressDialog = null;
        }
    }

    private void setPreferenceStates() {
        if (!ConstValues.isSupportNB()) {
            boolean isSettingOn = CommonHelper.isInterceptionSettingOn(this);
            this.mSettingPref.setChecked(isSettingOn);
            this.mNotificationPref.setEnabled(isSettingOn);
        }
        this.mNotificationPref.setDetail(getNotifyRuleSummary(CommonHelper.getNotifyRule(this)));
        if (this.mIsEnableIntelligentEngine.booleanValue()) {
            updateSummaryForUpdatePreference();
        }
    }

    private void refreshData() {
        new LoadDataTask().executeOnExecutor(this.mExecutor, new Void[0]);
    }

    private void updateSummaryForUpdatePreference() {
        String summary = getUpdateTimeString();
        this.mAutoUpdatePref.setNetherSummary(summary);
        this.mManualUpdatePref.setSummary(summary);
        this.mAutoUpdatePref.setDetail(getUpdateRuleDes());
    }

    private void startBlackWhiteListActivity(int flag) {
        Intent intent = new Intent();
        intent.putExtra(ConstValues.KEY_BLACKLIST_ENTRANCE_FLAG, flag);
        intent.setClass(this, BlackWhiteListActivity.class);
        try {
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startKeywordsActivity() {
        Intent intent = new Intent();
        intent.setClass(this, KeywordsActivity.class);
        try {
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onChooseNotifyRule() {
        final Context context = getApplicationContext();
        Builder dlgBuilder = new Builder(this);
        dlgBuilder.setIconAttribute(16843605);
        dlgBuilder.setTitle(R.string.harassmentInterception_notify_rule_title);
        dlgBuilder.setSingleChoiceItems(getResources().getStringArray(R.array.harassmentInterception_notify_option), notifyRuleId2DescripIndex(CommonHelper.getNotifyRule(context)), new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int notifyRule = RuleSettingsActivity.this.notifyRuleDescripIndex2Id(whichButton);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(notifyRule));
                HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_NOTIFY, statParam);
                CommonHelper.setNotifyRule(context, notifyRule);
                RuleSettingsActivity.this.mNotificationPref.setDetail(RuleSettingsActivity.this.getNotifyRuleSummary(notifyRule));
                dialog.dismiss();
            }
        });
        dlgBuilder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = dlgBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void initUpdateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdateService.ACTION_BEGIN_CHECK);
        filter.addAction(UpdateService.ACTION_FINISHED_CHECK);
        filter.addAction(UpdateService.ACTION_BEGIN_UPDATE);
        filter.addAction(UpdateService.ACTION_FINISHED_UPDATE);
        filter.addAction(UpdateService.ACTION_NET_ERROR);
        filter.addAction(UpdateService.ACTION_UPDATE_ERROR);
        filter.addAction(UpdateService.ACTION_OVERDUE_ERROR);
        this.mUpateReceiver = new UpdateBroadcastReceiver();
        registerReceiver(this.mUpateReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
    }

    private void updateProgressDialog(String updateMessage, boolean bDimissDlg) {
        if (this.mProgressDialog == null) {
            HwLog.w(TAG, "updateProgressDialog: Progress dialog is not created");
            return;
        }
        this.mProgressDialog.setMessage(updateMessage);
        if (bDimissDlg) {
            Message message = this.mHandler.obtainMessage();
            message.what = 2;
            this.mHandler.sendMessageDelayed(message, 2000);
        }
    }

    private String getNotifyRuleSummary(int notifyRule) {
        if (notifyRule < 0 || notifyRule > 2) {
            return "";
        }
        return getResources().getStringArray(R.array.harassmentInterception_notify_option)[notifyRuleId2DescripIndex(notifyRule)];
    }

    private int notifyRuleId2DescripIndex(int notifyRule) {
        switch (notifyRule) {
            case 0:
                return 2;
            case 1:
                return 0;
            case 2:
                return 1;
            default:
                return -1;
        }
    }

    private int notifyRuleDescripIndex2Id(int descripIndex) {
        switch (descripIndex) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 0;
            default:
                return -1;
        }
    }

    private String getUpdateTimeString() {
        if (PreferenceHelper.hasUpdate(this)) {
            long lastUpdateTime = PreferenceHelper.getLastAlarmTime(this);
            if (lastUpdateTime > 0) {
                HwLog.i(TAG, "Last update : " + CommonHelper.getSystemDateStyle(this, lastUpdateTime));
            } else {
                HwLog.i(TAG, "lastUpdateTime value may be not correct " + lastUpdateTime);
            }
            long noUpdateTime = System.currentTimeMillis() - lastUpdateTime;
            HwLog.i(TAG, "Update time elapse : " + noUpdateTime);
            String updateSummary = getResources().getString(R.string.harassment_update_just_now_summary);
            if (noUpdateTime >= 3600000) {
                if (noUpdateTime < 86400000) {
                    long hours = noUpdateTime / 3600000;
                    updateSummary = getResources().getQuantityString(R.plurals.harassment_noupdate_hours_summary_format, (int) hours, new Object[]{Integer.valueOf((int) hours)});
                } else if (noUpdateTime < SpaceConst.LARGE_FILE_EXCEED_INTERVAL_TIME) {
                    long days = noUpdateTime / 86400000;
                    updateSummary = getResources().getQuantityString(R.plurals.harassment_noupdate_days_summary_format, (int) days, new Object[]{Integer.valueOf((int) days)});
                } else if (noUpdateTime < 31536000000L) {
                    long months = noUpdateTime / SpaceConst.LARGE_FILE_EXCEED_INTERVAL_TIME;
                    updateSummary = getResources().getQuantityString(R.plurals.harassment_noupdate_months_summary_format, (int) months, new Object[]{Integer.valueOf((int) months)});
                } else {
                    updateSummary = String.format(getResources().getString(R.string.harassment_noupdate_over_year_summary_format), new Object[]{Integer.valueOf(1)});
                }
            }
            String latestScanTime = getString(R.string.harassment_update_end_texttime, new Object[]{DateUtils.formatDateTime(GlobalContext.getContext(), lastUpdateTime, 17)});
            HwLog.i(TAG, updateSummary);
            return latestScanTime;
        }
        HwLog.i(TAG, "has not updated before");
        return "";
    }

    private String getUpdateRuleDes() {
        int strategy = UpdateHelper.getAutoUpdateStrategy(getApplication());
        if (strategy == 3) {
            return getString(R.string.harassment_auto_update_intell_all_network);
        }
        if (strategy == 2) {
            return getString(R.string.harassment_auto_update_intell_only_wifi);
        }
        return getString(R.string.harassment_auto_update_intell_close);
    }

    private void startManualUpdate() {
        HwLog.i(TAG, "startManualUpdate");
        Utility.initSDK(GlobalContext.getContext());
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(ConstValues.KEY_AUTOUPDATE_FLAG, false);
        startServiceAsUser(intent, UserHandle.OWNER);
    }

    private void showUpdateProgressDialog() {
        if (this.mProgressDialog == null) {
            HwLog.w(TAG, "showUpdateProgressDialog: progress dialog is not created");
            return;
        }
        this.mProgressDialog.setMessage(getResources().getString(R.string.harassment_begin_chech_message));
        this.mProgressDialog.setButton(-1, getResources().getString(R.string.harassment_cancle), new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                RuleSettingsActivity.this.mProgressDialog.dismiss();
                RuleSettingsActivity.this.stopService(new Intent(RuleSettingsActivity.this, UpdateService.class));
            }
        });
        this.mProgressDialog.show();
    }

    private void shouldDismissDlgWithTime(long time) {
        HwLog.i(TAG, "shouldDismissDlgWithTime");
        Message message = this.mHandler.obtainMessage();
        message.what = 1;
        this.mHandler.sendMessageDelayed(message, time);
    }

    private void showNoNetworkDialog() {
        Builder alertDialog = new Builder(this);
        alertDialog.setTitle(R.string.harassment_hint);
        alertDialog.setMessage(R.string.harassment_network_disable_hint);
        alertDialog.setNegativeButton(getResources().getString(R.string.harassment_cancle), null);
        alertDialog.setPositiveButton(R.string.harassment_set, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                intent.addCategory("android.intent.category.DEFAULT");
                try {
                    RuleSettingsActivity.this.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        alertDialog.show();
    }

    private void showChooseUpdateRulesDialog() {
        Builder dlgBuilder = new Builder(this);
        dlgBuilder.setIconAttribute(16843605);
        dlgBuilder.setTitle(R.string.harassment_auto_update_intell_title);
        dlgBuilder.setSingleChoiceItems(new String[]{getString(R.string.harassment_auto_update_intell_only_wifi), getString(R.string.harassment_auto_update_intell_all_network), getString(R.string.harassment_auto_update_intell_close)}, intellUpdateValueToIndex(UpdateHelper.getAutoUpdateStrategy(getApplication())), new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int updateStrategy = RuleSettingsActivity.this.intellUpdateIndexToValue(whichButton);
                boolean res = UpdateHelper.setAutoUpdateStrategy(RuleSettingsActivity.this, updateStrategy);
                RuleSettingsActivity.this.updateSummaryForUpdatePreference();
                dialog.dismiss();
                HwLog.i(RuleSettingsActivity.TAG, "user choose update rule:" + updateStrategy + ", res:" + res);
                HsmStat.statE((int) Events.E_HARASSMENT_SELECT_INTELL_UPDATE, HsmStatConst.PARAM_VAL, String.valueOf(updateStrategy));
            }
        });
        dlgBuilder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = dlgBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private int intellUpdateIndexToValue(int index) {
        if (index == 0) {
            return 2;
        }
        if (index == 1) {
            return 3;
        }
        return index == 2 ? 1 : 1;
    }

    private int intellUpdateValueToIndex(int value) {
        if (value == 2) {
            return 0;
        }
        if (value == 3) {
            return 1;
        }
        return value == 1 ? 2 : 2;
    }
}
