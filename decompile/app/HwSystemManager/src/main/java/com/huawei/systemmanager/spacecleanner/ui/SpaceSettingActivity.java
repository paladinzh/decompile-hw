package com.huawei.systemmanager.spacecleanner.ui;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.text.format.DateUtils;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.util.FileAtTimeCheckUtils;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference.SwitchKey;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSwitchSetting;
import com.huawei.systemmanager.spacecleanner.setting.UpdateSetting;
import com.huawei.systemmanager.spacecleanner.ui.ProcessWhiteListFragment.ProcessWhiteListActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SpaceSettingActivity extends HsmPreferenceActivity implements IUpdateListener, MessageHandler {
    private static final int MSG_UPDATE_FINISH_DELAY_DISMISS = 200;
    private static final String TAG = "SpaceSettingActivity";
    private static final int TIMING_NOTIFY_PERIOD = 7;
    private PreferenceCategory mCategorySpaceLib;
    private Context mContext;
    private GenericHandler mHandler;
    private OnPreferenceClickListener mManualUpdateListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            HsmStat.statE(Events.E_OPTMIZE_MANUAL_UPDATE_LIB);
            UpdateSetting setting = SpaceSettingPreference.getDefault().getUpdateSetting();
            HwLog.i(SpaceSettingActivity.TAG, "ManualUpdateListener, do action");
            setting.doAction(SpaceSettingActivity.this);
            return true;
        }
    };
    private SwitchPreference mNotCommonlyUsedNotify;
    private SwitchPreference mPrefCleanCacheDaily;
    private SwitchPreference mPrefIsAutoUpdateLib;
    private SwitchPreference mPrefIsWifiOnlyUpdate;
    private Preference mPrefIsWifiOnlyUpdateDivider;
    private Preference mPrefManualUpdateLib;
    private OnPreferenceClickListener mProcessWhiteListListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            HwLog.i(SpaceSettingActivity.TAG, "protected activity button click");
            HsmStat.statE(Events.E_OPTMIZE_WHITLIST_FROM_SPACE_CLEAN);
            SpaceSettingActivity.this.startActivity(new Intent(SpaceSettingActivity.this, ProcessWhiteListActivity.class));
            return true;
        }
    };
    private ProgressDialog mProgressDialog;
    private OnPreferenceChangeListener mSwitchPreferenceListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SpaceSwitchSetting setting = SpaceSettingPreference.getDefault().getSwitchSetting(preference.getKey());
            if (setting == null) {
                return true;
            }
            boolean bValue = ((Boolean) newValue).booleanValue();
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_OP;
            strArr[1] = bValue ? "1" : "0";
            String statParam = HsmStatConst.constructJsonParams(strArr);
            setting.doSettingChanged(Boolean.valueOf(bValue), true);
            if (SwitchKey.KEY_IS_CLEAN_CACHE_DAILY.equalsIgnoreCase(preference.getKey())) {
                HsmStat.statE((int) Events.E_OPTMIZE_CLEAN_CACHE_DAILY, statParam);
            } else if (SwitchKey.KEY_IS_AUTO_UPDATE_LIB.equalsIgnoreCase(preference.getKey())) {
                HsmStat.statE((int) Events.E_OPTMIZE_AUTO_UPDATE_LIB, statParam);
            } else if (SwitchKey.KEY_IS_WIFI_ONLY_UPDATE.equalsIgnoreCase(preference.getKey())) {
                HsmStat.statE((int) Events.E_OPTMIZE_WIFI_ONLY_UPDATE, statParam);
            }
            if (setting instanceof UpdateSetting) {
                SpaceSettingActivity.this.displayWifiUpdatePreference(bValue);
            }
            return true;
        }
    };
    private Map<String, SwitchPreference> mSwitchPrefers = HsmCollections.newArrayMap();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpaceCleannerManager.getInstance();
        if (SpaceCleannerManager.isSupportHwFileAnalysis() && FileAtTimeCheckUtils.isChangeAtTimeSuccess()) {
            addPreferencesFromResource(R.xml.space_setting_preference_new);
        } else {
            addPreferencesFromResource(R.xml.space_setting_preference);
        }
        getPreferenceScreen().getPreferenceManager().setSharedPreferencesName("space_prefence");
        this.mContext = this;
        this.mHandler = new GenericHandler(this);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.main_screen_page_settings);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ListView lv = (ListView) findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
        initViews();
    }

    private void initViews() {
        this.mCategorySpaceLib = (PreferenceCategory) findPreference(SpaceSettingPreference.CATEGORY_SPACE_LIB);
        this.mPrefManualUpdateLib = findPreference(SpaceSettingPreference.KEY_MANUAL_UPDATE_LIB);
        this.mPrefIsAutoUpdateLib = (SwitchPreference) findPreference(SwitchKey.KEY_IS_AUTO_UPDATE_LIB);
        this.mPrefIsWifiOnlyUpdate = (SwitchPreference) findPreference(SwitchKey.KEY_IS_WIFI_ONLY_UPDATE);
        this.mPrefIsWifiOnlyUpdateDivider = findPreference(SwitchKey.KEY_IS_WIFI_ONLY_UPDATE_DIVIDER);
        this.mPrefCleanCacheDaily = (SwitchPreference) findPreference(SwitchKey.KEY_IS_CLEAN_CACHE_DAILY);
        Preference preference = findPreference(SwitchKey.KEY_IS_NOT_COMMONLY_USED_NOTIFY);
        if (preference != null) {
            this.mNotCommonlyUsedNotify = (SwitchPreference) preference;
        }
        this.mPrefManualUpdateLib.setOnPreferenceClickListener(this.mManualUpdateListener);
        findPreference(SpaceSettingPreference.KEY_MEMORY_WHITE_LIST).setOnPreferenceClickListener(this.mProcessWhiteListListener);
        initSwitchPreference();
        this.mProgressDialog = new ProgressDialog(this);
        if (!TMSEngineFeature.isSupportTMS()) {
            getPreferenceScreen().removePreference(this.mCategorySpaceLib);
        }
    }

    private void initSwitchPreference() {
        SwitchPreference phoneSlow = (SwitchPreference) findPreference("processmanagersetting");
        List<SwitchPreference> perfres;
        if (this.mNotCommonlyUsedNotify == null) {
            perfres = HsmCollections.newArrayList(this.mPrefIsAutoUpdateLib, this.mPrefIsWifiOnlyUpdate, this.mPrefCleanCacheDaily, phoneSlow);
        } else {
            perfres = HsmCollections.newArrayList(this.mPrefIsAutoUpdateLib, this.mPrefIsWifiOnlyUpdate, this.mPrefCleanCacheDaily, this.mNotCommonlyUsedNotify, phoneSlow);
        }
        for (SwitchPreference perfer : perfres) {
            perfer.setOnPreferenceChangeListener(this.mSwitchPreferenceListener);
            this.mSwitchPrefers.put(perfer.getKey(), perfer);
        }
    }

    protected void onResume() {
        super.onResume();
        for (Entry<String, SwitchPreference> entry : this.mSwitchPrefers.entrySet()) {
            ((SwitchPreference) entry.getValue()).setChecked(SpaceSettingPreference.getDefault().getSwitchSetting((String) entry.getKey()).isSwitchOn());
        }
        UpdateSetting updateSetting = SpaceSettingPreference.getDefault().getUpdateSetting();
        setUpdateLibStatus(updateSetting.getUpdateTimeStamp());
        displayWifiUpdatePreference(updateSetting.isSwitchOn());
    }

    private void setUpdateLibStatus(long updateTime) {
        long currentTime = System.currentTimeMillis();
        HwLog.i(TAG, "setUpdateLibStatus, udpateTime:" + updateTime + ",currentTime:" + currentTime);
        if (updateTime <= 0) {
            this.mPrefManualUpdateLib.setSummary("");
            this.mPrefIsAutoUpdateLib.setSummary("");
            return;
        }
        long diffTime = currentTime - updateTime;
        if (diffTime < 86400000) {
            String des = getString(R.string.space_clean_update_lastest_time, new Object[]{DateUtils.formatDateTime(this, updateTime, 17)});
            this.mPrefManualUpdateLib.setSummary(des);
            this.mPrefIsAutoUpdateLib.setSummary(des);
            return;
        }
        int updateDayBefore = 0;
        try {
            updateDayBefore = (int) (diffTime / 86400000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (updateDayBefore > 0) {
            this.mPrefManualUpdateLib.setSummary(getResources().getQuantityString(R.plurals.update_before, updateDayBefore, new Object[]{Integer.valueOf(updateDayBefore)}));
            this.mPrefIsAutoUpdateLib.setSummary(getResources().getQuantityString(R.plurals.update_before, updateDayBefore, new Object[]{Integer.valueOf(updateDayBefore)}));
        } else {
            this.mPrefManualUpdateLib.setSummary("");
            this.mPrefIsAutoUpdateLib.setSummary("");
        }
    }

    private void displayWifiUpdatePreference(boolean shouldDisplay) {
        if (shouldDisplay) {
            this.mCategorySpaceLib.addPreference(this.mPrefIsWifiOnlyUpdate);
            this.mCategorySpaceLib.addPreference(this.mPrefIsWifiOnlyUpdateDivider);
            return;
        }
        this.mCategorySpaceLib.removePreference(this.mPrefIsWifiOnlyUpdate);
        this.mCategorySpaceLib.removePreference(this.mPrefIsWifiOnlyUpdateDivider);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onUpdateStarted() {
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setMessage(this.mContext.getResources().getString(R.string.msg_updating));
        this.mProgressDialog.show();
    }

    public void onUpdateFinished() {
        updateTimeStamp();
        refreshUpdateLibStatus();
        this.mProgressDialog.setMessage(this.mContext.getResources().getString(R.string.harassment_finish_update_message));
        this.mHandler.sendEmptyMessageDelayed(200, 2000);
    }

    public void onError(int errorCode) {
        switch (errorCode) {
            case IUpdateListener.ERROR_CODE_NO_NETWORK /*300*/:
                new Builder(this).setTitle(R.string.antivirus_title_tips).setMessage(R.string.msg_network_error_Toast).setPositiveButton(R.string.antivirus_settings, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                        intent.addCategory("android.intent.category.DEFAULT");
                        try {
                            SpaceSettingActivity.this.startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel), null).show();
                return;
            case 301:
                this.mProgressDialog.setMessage(this.mContext.getResources().getString(R.string.msg_update_error_Toast));
                this.mProgressDialog.dismiss();
                return;
            default:
                return;
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 200:
                this.mProgressDialog.dismiss();
                refreshUpdateLibStatus();
                return;
            default:
                return;
        }
    }

    private void updateTimeStamp() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            SpaceSettingPreference.getDefault().getUpdateSetting().setUpdateTimeStamp();
        }
    }

    private void refreshUpdateLibStatus() {
        setUpdateLibStatus(SpaceSettingPreference.getDefault().getUpdateSetting().getUpdateTimeStamp());
    }
}
