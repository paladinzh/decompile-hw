package com.huawei.systemmanager.antivirus.ui;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antimal.MalwareConst;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.notify.TimerRemindNotify;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.WrappingRadioPreference;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.text.NumberFormat;

public class AntiVirusSettingsActivity extends HsmPreferenceActivity {
    private static final String CATEGORY_SCAN_MODE = "category_antivirus_scan_mode";
    private static final String CATEGORY_VIRUS_LIB = "category_virus_lib";
    private static final int GLOBAL_SCAN_SELECTED = 1;
    private static final int QUICK_SCAN_SELECTED = 0;
    private static final int REMIND_DAYS_NUM = 30;
    private static final int UPDATE_TIMEOUT = 90000;
    private String TAG = "AntiVirusSettingsActivity";
    private IAntiVirusEngine mAntiVirusEngine;
    private SwitchPreference mAutoUpdatePref = null;
    private SwitchPreference mCloudScanSwitchPref = null;
    private Context mContext = null;
    private SwitchPreference mGloablTimerRemindPref = null;
    private WrappingRadioPreference mGlobalScanPref = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (AntiVirusSettingsActivity.this.mProgressDialog.isShowing()) {
                        AntiVirusSettingsActivity.this.mProgressDialog.dismiss();
                    }
                    Toast.makeText(AntiVirusSettingsActivity.this.mContext, R.string.msg_network_error_Toast, 3000).show();
                    return;
                case 3:
                    AntiVirusSettingsActivity.this.updateVirusLib();
                    return;
                case 4:
                    if (AntiVirusSettingsActivity.this.mProgressDialog.isShowing()) {
                        AntiVirusSettingsActivity.this.mProgressDialog.dismiss();
                        return;
                    }
                    return;
                case 5:
                    AntiVirusSettingsActivity.this.mProgressDialog.setIndeterminate(true);
                    AntiVirusSettingsActivity.this.mProgressDialog.setMessage(AntiVirusSettingsActivity.this.mContext.getResources().getString(R.string.msg_updating));
                    return;
                case 8:
                case 12:
                    if (AntiVirusSettingsActivity.this.mProgressDialog.isShowing()) {
                        AntiVirusSettingsActivity.this.mProgressDialog.dismiss();
                    }
                    Toast.makeText(AntiVirusSettingsActivity.this.mContext, R.string.msg_update_error_Toast, 3000).show();
                    return;
                case 9:
                    AntiVirusSettingsActivity.this.mIsNormalUpdateComplete = true;
                    if (AntiVirusSettingsActivity.this.mProgressDialog.isShowing()) {
                        AntiVirusSettingsActivity.this.mProgressDialog.dismiss();
                    }
                    AntiVirusTools.setManualUpdateStamp(AntiVirusSettingsActivity.this.mContext, System.currentTimeMillis());
                    AntiVirusSettingsActivity.this.setUpdatePrefSummary();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsAutoUpdate;
    private boolean mIsCloudScan;
    private boolean mIsGlobalTimerRemind;
    private boolean mIsNormalUpdateComplete = false;
    private boolean mIsRemoved = false;
    private boolean mIsWifiOnlyUpdate;
    private OnPreferenceClickListener mManualUpdateClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference arg0) {
            ConnectivityManager connectivityManager = (ConnectivityManager) AntiVirusSettingsActivity.this.getSystemService("connectivity");
            boolean flag = false;
            if (connectivityManager.getActiveNetworkInfo() != null) {
                flag = connectivityManager.getActiveNetworkInfo().isConnected();
            }
            if (flag) {
                AntiVirusSettingsActivity.this.mProgressDialog.setIndeterminate(true);
                AntiVirusSettingsActivity.this.mProgressDialog.setMessage(AntiVirusSettingsActivity.this.mContext.getResources().getString(R.string.msg_checking));
                AntiVirusSettingsActivity.this.mProgressDialog.show();
                AntiVirusSettingsActivity.this.checkVirusLibVersion();
                AntiVirusSettingsActivity.this.mHandler.removeMessages(12);
                AntiVirusSettingsActivity.this.mHandler.sendEmptyMessageDelayed(12, MalwareConst.MAX_INSTALL_SPACE_TIME);
                HsmStat.statE(85);
            } else {
                new Builder(AntiVirusSettingsActivity.this).setTitle(R.string.antivirus_title_tips).setMessage(R.string.msg_network_error_Toast).setPositiveButton(R.string.antivirus_settings, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                            intent.addCategory("android.intent.category.DEFAULT");
                            AntiVirusSettingsActivity.this.startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).setNegativeButton(AntiVirusSettingsActivity.this.getResources().getString(R.string.cancel), null).show();
            }
            return false;
        }
    };
    private Preference mManualUpdatePref = null;
    private OnPreferenceChangeListener mOnPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            HwLog.d(AntiVirusSettingsActivity.this.TAG, "newValue = " + newValue);
            String[] strArr;
            if (preference == AntiVirusSettingsActivity.this.mAutoUpdatePref) {
                AntiVirusSettingsActivity.this.mIsAutoUpdate = ((Boolean) newValue).booleanValue();
                AntiVirusSettingsActivity.this.handleAutoUpdatePrefChange();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = AntiVirusSettingsActivity.this.mIsAutoUpdate ? "1" : "0";
                HsmStat.statE(86, HsmStatConst.constructJsonParams(strArr));
            } else if (preference == AntiVirusSettingsActivity.this.mWifiOnlyUpdatePref) {
                AntiVirusSettingsActivity.this.mIsWifiOnlyUpdate = ((Boolean) newValue).booleanValue();
                AntiVirusSettingsActivity.this.handleWifiOnlyUpdatePrefChange();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = AntiVirusSettingsActivity.this.mIsWifiOnlyUpdate ? "1" : "0";
                HsmStat.statE(87, HsmStatConst.constructJsonParams(strArr));
            } else if (preference == AntiVirusSettingsActivity.this.mCloudScanSwitchPref) {
                AntiVirusSettingsActivity.this.mIsCloudScan = ((Boolean) newValue).booleanValue();
                AntiVirusTools.setCloudScan(AntiVirusSettingsActivity.this.mContext, AntiVirusSettingsActivity.this.mIsCloudScan);
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = AntiVirusSettingsActivity.this.mIsCloudScan ? "1" : "0";
                HsmStat.statE((int) Events.E_VIRUS_CLOUD_SCAN_SWITCH, HsmStatConst.constructJsonParams(strArr));
            } else if (preference == AntiVirusSettingsActivity.this.mGloablTimerRemindPref) {
                AntiVirusSettingsActivity.this.mIsGlobalTimerRemind = ((Boolean) newValue).booleanValue();
                TimerRemindNotify notify = new TimerRemindNotify();
                if (AntiVirusSettingsActivity.this.mIsGlobalTimerRemind) {
                    AntiVirusTools.updateTimerRemindTimeStamp(AntiVirusSettingsActivity.this.mContext);
                    notify.schduleTimingNotify(AntiVirusSettingsActivity.this.mContext);
                } else {
                    notify.cancelTimingNotify(AntiVirusSettingsActivity.this.mContext);
                }
                AntiVirusTools.setGlobalTimerRemind(AntiVirusSettingsActivity.this.mContext, AntiVirusSettingsActivity.this.mIsGlobalTimerRemind);
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = AntiVirusSettingsActivity.this.mIsGlobalTimerRemind ? "1" : "0";
                HsmStat.statE((int) Events.E_VIRUS_GLOBAL_TIMER_REMAIND, HsmStatConst.constructJsonParams(strArr));
            }
            return true;
        }
    };
    private ProgressDialog mProgressDialog;
    private WrappingRadioPreference mQuickScanPref = null;
    private PreferenceCategory mScanModeCategory = null;
    private OnPreferenceClickListener mScanModeClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference prefenence) {
            String key = prefenence.getKey();
            int scanModeItem = 0;
            if (key != null) {
                if (AntiVirusTools.QUICK_SCAN_MODE.equals(key)) {
                    scanModeItem = 0;
                } else if (AntiVirusTools.GLOBAL_SCAN_MODE.equals(key)) {
                    scanModeItem = 1;
                }
            }
            AntiVirusSettingsActivity.this.setScanModeChecked(scanModeItem);
            AntiVirusTools.setScanMode(AntiVirusSettingsActivity.this.mContext, scanModeItem);
            return true;
        }
    };
    private int mUpdateRate;
    private PreferenceCategory mVirusLibCategory = null;
    private SwitchPreference mWifiOnlyUpdatePref = null;
    private int scanMode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);
        this.mContext = getApplicationContext();
        try {
            this.mAntiVirusEngine = AntiVirusEngineFactory.newInstance();
            this.mAntiVirusEngine.onInit(this.mContext);
        } catch (NullPointerException e) {
            HwLog.e(this.TAG, "NullPointerException found ");
            finish();
        }
        initActionBar();
        initViews();
    }

    private void initViews() {
        this.mVirusLibCategory = (PreferenceCategory) findPreference(CATEGORY_VIRUS_LIB);
        this.mCloudScanSwitchPref = (SwitchPreference) findPreference(AntiVirusTools.CLOUD_SCAN_SWITCH);
        this.mGloablTimerRemindPref = (SwitchPreference) findPreference(AntiVirusTools.GLOBAL_TIMER_REMIND);
        this.mGloablTimerRemindPref.setSummary(getString(R.string.virus_global_scan_timer_remind_summery).replace(String.valueOf(30), NumberFormat.getInstance().format(30)));
        this.mAutoUpdatePref = (SwitchPreference) findPreference(AntiVirusTools.IS_AUTO_UPDATE_VIRUS_LIB);
        this.mWifiOnlyUpdatePref = (SwitchPreference) findPreference(AntiVirusTools.IS_WIFI_ONLY_UPDATE);
        this.mManualUpdatePref = findPreference(AntiVirusTools.MANUAL_UPDATE_VIRUS_LIB);
        this.mManualUpdatePref.setOnPreferenceClickListener(this.mManualUpdateClickListener);
        this.mAutoUpdatePref.setOnPreferenceChangeListener(this.mOnPreferenceChangeListener);
        this.mWifiOnlyUpdatePref.setOnPreferenceChangeListener(this.mOnPreferenceChangeListener);
        this.mCloudScanSwitchPref.setOnPreferenceChangeListener(this.mOnPreferenceChangeListener);
        this.mGloablTimerRemindPref.setOnPreferenceChangeListener(this.mOnPreferenceChangeListener);
        this.mScanModeCategory = (PreferenceCategory) findPreference(CATEGORY_SCAN_MODE);
        this.mQuickScanPref = (WrappingRadioPreference) findPreference(AntiVirusTools.QUICK_SCAN_MODE);
        this.mGlobalScanPref = (WrappingRadioPreference) findPreference(AntiVirusTools.GLOBAL_SCAN_MODE);
        this.mQuickScanPref.setOnPreferenceClickListener(this.mScanModeClickListener);
        this.mGlobalScanPref.setOnPreferenceClickListener(this.mScanModeClickListener);
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                if (!AntiVirusSettingsActivity.this.mIsNormalUpdateComplete) {
                    AntiVirusSettingsActivity.this.mAntiVirusEngine.onCancelCheckOrUpdate();
                }
                AntiVirusSettingsActivity.this.mIsNormalUpdateComplete = false;
                AntiVirusSettingsActivity.this.mHandler.removeMessages(12);
            }
        });
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.antivirus_settings);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected void onResume() {
        ListView lv = (ListView) findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
        this.mIsAutoUpdate = AntiVirusTools.isAutoUpdate(this.mContext);
        this.mIsWifiOnlyUpdate = AntiVirusTools.isWiFiOnlyUpdate(this.mContext);
        this.mIsCloudScan = AntiVirusTools.isCloudScanSwitchOn(this.mContext);
        this.mIsGlobalTimerRemind = AntiVirusTools.isGlobalTimerSwitchOn(this.mContext);
        this.mUpdateRate = AntiVirusTools.getUpdateRate(this.mContext);
        setUpdatePrefSummary();
        this.mAutoUpdatePref.setChecked(this.mIsAutoUpdate);
        this.mWifiOnlyUpdatePref.setChecked(this.mIsWifiOnlyUpdate);
        this.mGloablTimerRemindPref.setChecked(this.mIsGlobalTimerRemind);
        this.mCloudScanSwitchPref.setChecked(this.mIsCloudScan);
        this.scanMode = AntiVirusTools.getScanMode(this.mContext);
        setScanModeChecked(this.scanMode);
        setAutoUpdateVisible();
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
        this.mHandler.removeMessages(12);
        super.onDestroy();
    }

    private void checkVirusLibVersion() {
        new Thread(new Runnable() {
            public void run() {
                AntiVirusSettingsActivity.this.mAntiVirusEngine.onCheckVirusLibVersion(AntiVirusSettingsActivity.this.mHandler);
            }
        }, "AntiVirus_checkVirusLibVersion").start();
    }

    private void updateVirusLib() {
        new Thread(new Runnable() {
            public void run() {
                AntiVirusSettingsActivity.this.mAntiVirusEngine.onUpdateVirusLibVersion(AntiVirusSettingsActivity.this.mHandler);
            }
        }, "AntiVirus_updateVirusLib_UI").start();
    }

    private void setScanModeChecked(int selectedItem) {
        int modifySelectedItem = 0;
        int count = this.mScanModeCategory.getPreferenceCount();
        if (selectedItem == 1) {
            modifySelectedItem = selectedItem + 1;
        } else if (selectedItem == 0) {
            modifySelectedItem = selectedItem;
        }
        for (int item = 0; item < count; item++) {
            if (item != 1) {
                if (modifySelectedItem == item) {
                    ((WrappingRadioPreference) this.mScanModeCategory.getPreference(item)).setChecked(true);
                } else {
                    ((WrappingRadioPreference) this.mScanModeCategory.getPreference(item)).setChecked(false);
                }
            }
        }
    }

    private void handleAutoUpdatePrefChange() {
        setAutoUpdateVisible();
        AntiVirusTools.setAutoUpdate(this.mContext, this.mIsAutoUpdate);
        if (this.mIsAutoUpdate) {
            AntiVirusTools.startAutoUpdateVirusLibAlarm(this.mContext, this.mUpdateRate);
        } else {
            AntiVirusTools.cancelAutoUpdateVirusLibAlarm(this.mContext);
        }
    }

    private void handleWifiOnlyUpdatePrefChange() {
        this.mWifiOnlyUpdatePref.setChecked(this.mIsWifiOnlyUpdate);
        AntiVirusTools.setWifiOnlyUpdate(this.mContext, this.mIsWifiOnlyUpdate);
    }

    private void setAutoUpdateVisible() {
        if (Utility.isWifiOnlyMode()) {
            if (!this.mIsRemoved) {
                this.mVirusLibCategory.removePreference(this.mWifiOnlyUpdatePref);
                this.mIsRemoved = true;
            }
            return;
        }
        this.mAutoUpdatePref.setChecked(this.mIsAutoUpdate);
        if (this.mIsAutoUpdate && this.mIsRemoved) {
            this.mVirusLibCategory.addPreference(this.mWifiOnlyUpdatePref);
            this.mIsRemoved = false;
        } else if (!(this.mIsRemoved || this.mIsAutoUpdate)) {
            this.mVirusLibCategory.removePreference(this.mWifiOnlyUpdatePref);
            this.mIsRemoved = true;
        }
    }

    private void setUpdatePrefSummary() {
        if (TextUtils.isEmpty(this.mAntiVirusEngine.onGetVirusLibVersion(this.mContext))) {
            HwLog.e(this.TAG, "virus lib version is invalid!");
            return;
        }
        String virusLibVersionSummary = "";
        long now = System.currentTimeMillis();
        long autoUpdateStamp = AntiVirusTools.getAutoUpdateStamp(this.mContext);
        long manuUpdateStamp = AntiVirusTools.getManualUpdateStamp(this.mContext);
        if (now - autoUpdateStamp <= 86400000 || now - manuUpdateStamp <= 86400000) {
            virusLibVersionSummary = String.format(this.mContext.getResources().getString(R.string.summary_virus_lib_version), new Object[]{virusLibVersion});
        } else {
            virusLibVersionSummary = String.format(this.mContext.getResources().getString(R.string.summary_virus_lib_version1), new Object[]{virusLibVersion});
        }
        this.mManualUpdatePref.setSummary(virusLibVersionSummary);
        this.mAutoUpdatePref.setSummary(virusLibVersionSummary);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
