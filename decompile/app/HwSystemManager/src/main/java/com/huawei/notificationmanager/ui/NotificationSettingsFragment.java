package com.huawei.notificationmanager.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.huawei.android.app.ActionBarEx;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.common.NotificationBackend;
import com.huawei.notificationmanager.common.NotificationUtils;
import com.huawei.notificationmanager.db.DBAdapter;
import com.huawei.notificationmanager.util.Const;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppDialog;
import com.huawei.systemmanager.comm.daulapp.DualAppDialogCallBack;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class NotificationSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    private static final String HW_EMAIL_ACCOUNT_MANAGER_ENTRY = "com.android.email.activity.setup.ACCOUNT_MANAGER_ENTRY";
    private static final String KEY_BANNER_SWITCH = "banner_type";
    private static final String KEY_CATEGORY = "category_open_close_switch";
    private static final String KEY_HIDE_CONTENT_DIVIDER = "notification_hide_content_divide_line";
    private static final String KEY_HIDE_CONTENT_SWITCH = "hide_content_type";
    private static final String KEY_LOCK_SCREEN_SWITCH = "lock_screen_type";
    private static final String KEY_MAIN_NOTIFI_SWITCH = "notification_main_switch";
    private static final String KEY_PRIORITY_SWITCH = "notification_priority_switch";
    private static final String KEY_SOUND_SWITCH = "sound_type";
    private static final String KEY_STATUS_BAR_SWITCH = "status_bar_type";
    private static final String KEY_VIBRATE_SWITCH = "vibrate_type";
    private static final String KEY_VIPMAIL = "vip_mail";
    private static final String PACKAGE_NAME_EMAIL = "com.android.email";
    private static final String PKG_NAME_SYSTEM_MANAGER = "com.huawei.systemmanager";
    private static final String TAG = "NotificationSettingsActivity";
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(NotificationSettingsFragment.TAG, "start app details pkg:" + NotificationSettingsFragment.this.mAppCfgInfo.mPkgName);
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(67108864);
                intent.setData(Uri.fromParts("package", NotificationSettingsFragment.this.mAppCfgInfo.mPkgName, null));
                NotificationSettingsFragment.this.getActivity().startActivity(intent);
            } catch (RuntimeException e) {
                HwLog.e(NotificationSettingsFragment.TAG, "start app details error.", e);
            }
        }
    };
    private NotificationCfgInfo mAppCfgInfo = new NotificationCfgInfo();
    private final NotificationBackend mBackend = new NotificationBackend();
    private SwitchPreference mBannerSwitchPref = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NotificationSettingsFragment.this.loadData();
        }
    };
    private OnChangeLinstener mChangeLinstener;
    private LoadAppCfgTask mDataLoadTask = null;
    private DBAdapter mDatabaseAdapter;
    private Preference mHideContentDividerPref = null;
    private SwitchPreference mHideContentSwitchPref = null;
    private SwitchPreference mLockScreenSwitchPref = null;
    private SwitchPreference mMainMotifiSwitchPref = null;
    private PreferenceCategory mNotifyCategory = null;
    OnPreferenceClickListener mPrefClickListener = new NotificationSettingPrefClickListener();
    private SwitchPreference mPrioritySwitchPref = null;
    private SwitchPreference mSoundPref;
    private SwitchPreference mStatusBarSwitchPref = null;
    private SwitchPreference mVibratePref;
    private PreferenceCategory mVipEmailCategory;
    private Preference mVipEmailPref = null;

    public interface OnChangeLinstener {
        void doChange();
    }

    class LoadAppCfgTask extends AsyncTask<String, Void, NotificationCfgInfo> {
        LoadAppCfgTask() {
        }

        protected NotificationCfgInfo doInBackground(String... pkgNames) {
            String pkg = (pkgNames == null || pkgNames.length <= 0) ? "" : pkgNames[0];
            HwLog.i(NotificationSettingsFragment.TAG, "LoadAppCfgTask pkg=" + pkg);
            if (TextUtils.isEmpty(pkg)) {
                return null;
            }
            return NotificationSettingsFragment.this.mDatabaseAdapter.getNotificationCfgInfo(pkg);
        }

        protected void onPostExecute(NotificationCfgInfo result) {
            if (!isCancelled()) {
                if (result == null) {
                    HwLog.w(NotificationSettingsFragment.TAG, "LoadAppCfgTask result is null");
                    return;
                }
                NotificationSettingsFragment.this.mAppCfgInfo = result;
                NotificationSettingsFragment.this.updateAllSwitchPreference();
                NotificationSettingsFragment.this.mDataLoadTask = null;
            }
        }
    }

    class NotificationSettingPrefClickListener implements OnPreferenceClickListener {
        NotificationSettingPrefClickListener() {
        }

        public boolean onPreferenceClick(Preference preference) {
            if (!NotificationSettingsFragment.KEY_VIPMAIL.equals(preference.getKey())) {
                return false;
            }
            NotificationSettingsFragment.this.startVipMailSetting();
            return true;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareStartParamsAndAction();
        addPreferencesFromResource(R.xml.notification_style_settings);
        initPreferences();
        updateAllSwitchPreference();
        Activity activity = getActivity();
        ActionBarEx.setEndIcon(activity.getActionBar(), true, activity.getDrawable(R.drawable.ic_public_detail), this.mActionBarListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        ListView lv = (ListView) getView().findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
        registerReceiver(getActivity());
        if (findPackageInfo(this.mAppCfgInfo.mPkgName) == null) {
            HwLog.w(TAG, "onResume packageInfo is null for " + this.mAppCfgInfo.mPkgName);
            finishActivity();
            return;
        }
        loadData();
    }

    private void loadData() {
        if (this.mDataLoadTask != null) {
            this.mDataLoadTask.cancel(false);
        }
        this.mDataLoadTask = new LoadAppCfgTask();
        this.mDataLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{this.mAppCfgInfo.mPkgName});
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(getActivity());
    }

    private void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstValues.CFG_CHANGE_BACKGROUND_INTENT);
        context.registerReceiver(this.mBroadcastReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void unregisterReceiver(Context context) {
        context.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void onDestroy() {
        if (this.mDataLoadTask != null) {
            this.mDataLoadTask.cancel(false);
            this.mDataLoadTask = null;
        }
        super.onDestroy();
    }

    private void prepareStartParamsAndAction() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            HwLog.e(TAG, "prepareStartParamsAndAction: Invalid bundle");
            toastAndFinish();
            return;
        }
        NotificationCfgInfo info = getCfgInfoFromArguments(bundle);
        if (info == null) {
            HwLog.w(TAG, "prepareStartParamsAndAction: Fail to get cfg info, Exit");
            toastAndFinish();
            return;
        }
        this.mAppCfgInfo = info;
        if (!(bundle.getBoolean(Const.KEY_FROM_INNER_SPLIT, false) || getActivity() == null)) {
            getActivity().setTitle(this.mAppCfgInfo.mAppLabel);
        }
        specialHandleForVipMailSetting(bundle);
    }

    private NotificationCfgInfo getCfgInfoFromArguments(Bundle bundle) {
        String pkgName = bundle.getString("packageName");
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.w(TAG, "getCfgInfoFromIntent: Fail to get package name from intent");
            return null;
        } else if (findPackageInfo(pkgName) == null) {
            HwLog.w(TAG, "getCfgInfoFromArguments packageInfo is null for " + pkgName);
            return null;
        } else {
            if (this.mDatabaseAdapter == null) {
                this.mDatabaseAdapter = new DBAdapter(GlobalContext.getContext());
            }
            NotificationCfgInfo info = this.mDatabaseAdapter.getNotificationCfgInfo(pkgName);
            HwLog.i(TAG, "getCfgInfoFromIntent: pkgName = " + pkgName + ", info is null?" + (info == null));
            if (info == null) {
                info = new NotificationCfgInfo(HsmPkgUtils.getPackageUid(pkgName), pkgName);
                localUpdateForMVersion(info);
                this.mDatabaseAdapter.addHuaWei(info, false);
            } else {
                updateForMVersion(info);
            }
            return info;
        }
    }

    private void localUpdateForMVersion(NotificationCfgInfo info) {
        DBAdapter.updateInfoForMVersion(info, this.mBackend);
        DBAdapter.putLockscreenNotificationEnable(info, this.mBackend);
        HwLog.d(TAG, "update outofcontrolsystemapp");
    }

    private void updateForMVersion(NotificationCfgInfo info) {
        DBAdapter.updateInfoForMVersion(info, this.mBackend);
    }

    private void specialHandleForVipMailSetting(Bundle bundle) {
        if (PACKAGE_NAME_EMAIL.equals(this.mAppCfgInfo.mPkgName)) {
            if (PACKAGE_NAME_EMAIL.equals(bundle.getString(Const.KEY_FROM_PACKAGE)) && Helper.areNotificationsEnabledForPackage(this.mAppCfgInfo.mPkgName, this.mAppCfgInfo.mUid)) {
                HwLog.i(TAG, "specialHandleForVipMailSetting: Start from email and notification is eabled  redirect back to email settings");
                startVipMailSetting();
                finishActivity();
            }
        }
    }

    private void initPreferences() {
        this.mMainMotifiSwitchPref = (SwitchPreference) findPreference(KEY_MAIN_NOTIFI_SWITCH);
        this.mMainMotifiSwitchPref.setOnPreferenceChangeListener(this);
        this.mNotifyCategory = (PreferenceCategory) findPreference(KEY_CATEGORY);
        this.mPrioritySwitchPref = (SwitchPreference) findPreference(KEY_PRIORITY_SWITCH);
        this.mLockScreenSwitchPref = (SwitchPreference) findPreference(KEY_LOCK_SCREEN_SWITCH);
        this.mHideContentSwitchPref = (SwitchPreference) findPreference(KEY_HIDE_CONTENT_SWITCH);
        this.mHideContentDividerPref = findPreference(KEY_HIDE_CONTENT_DIVIDER);
        this.mStatusBarSwitchPref = (SwitchPreference) findPreference(KEY_STATUS_BAR_SWITCH);
        this.mBannerSwitchPref = (SwitchPreference) findPreference(KEY_BANNER_SWITCH);
        this.mPrioritySwitchPref.setOnPreferenceChangeListener(this);
        this.mLockScreenSwitchPref.setOnPreferenceChangeListener(this);
        this.mHideContentSwitchPref.setOnPreferenceChangeListener(this);
        this.mStatusBarSwitchPref.setOnPreferenceChangeListener(this);
        this.mBannerSwitchPref.setOnPreferenceChangeListener(this);
        this.mSoundPref = (SwitchPreference) findPreference(KEY_SOUND_SWITCH);
        this.mSoundPref.setOnPreferenceChangeListener(this);
        this.mVibratePref = (SwitchPreference) findPreference(KEY_VIBRATE_SWITCH);
        this.mVibratePref.setOnPreferenceChangeListener(this);
        if (PACKAGE_NAME_EMAIL.equals(this.mAppCfgInfo.mPkgName)) {
            initVipMailPreference();
        }
    }

    private void initVipMailPreference() {
        PreferenceScreen screen = getPreferenceScreen();
        this.mVipEmailCategory = new PreferenceCategory(GlobalContext.getContext());
        this.mVipEmailCategory.setTitle(R.string.notificationcenter_category_vipemail);
        this.mVipEmailCategory.setOrder(100);
        this.mVipEmailPref = new Preference(GlobalContext.getContext());
        this.mVipEmailPref.setKey(KEY_VIPMAIL);
        this.mVipEmailPref.setWidgetLayoutResource(R.layout.preference_widget_arrow);
        this.mVipEmailPref.setTitle(R.string.notificationcenter_pref_vipemail_title);
        this.mVipEmailPref.setSummary(R.string.notificationcenter_pref_vipemail_summary);
        this.mVipEmailPref.setOnPreferenceClickListener(this.mPrefClickListener);
        this.mVipEmailPref.setPreferenceId(R.id.systemmanager_pref_notification_settings_fragment);
        screen.addPreference(this.mVipEmailCategory);
        this.mVipEmailCategory.addPreference(this.mVipEmailPref);
    }

    private void updateVipMailPreference(PreferenceScreen screen, boolean mainValue) {
        if (this.mVipEmailCategory != null) {
            HwLog.i(TAG, "updateVipMailPreference: setEnabled " + mainValue);
            if (mainValue) {
                screen.addPreference(this.mVipEmailCategory);
            } else {
                screen.removePreference(this.mVipEmailCategory);
            }
        }
    }

    private void startVipMailSetting() {
        HwLog.i(TAG, "startVipMailSetting");
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setPackage(PACKAGE_NAME_EMAIL);
        intent.setAction(HW_EMAIL_ACCOUNT_MANAGER_ENTRY);
        intent.putExtra("packageName", "com.huawei.systemmanager");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HwLog.e(TAG, "startVipMailSetting: ActivityNotFoundException", e);
        } catch (Exception e2) {
            HwLog.e(TAG, "startVipMailSetting: Exception", e2);
        }
    }

    private void updateAllSwitchPreference() {
        boolean mainValue;
        PreferenceScreen screen = getPreferenceScreen();
        if (this.mAppCfgInfo.canForbid()) {
            screen.addPreference(this.mMainMotifiSwitchPref);
            mainValue = this.mBackend.getNotificationsBanned(this.mAppCfgInfo.mPkgName, this.mAppCfgInfo.mUid);
            this.mMainMotifiSwitchPref.setChecked(mainValue);
        } else {
            screen.removePreference(this.mMainMotifiSwitchPref);
            this.mNotifyCategory.setLayoutResource(R.layout.notification_invisible_category);
            mainValue = true;
        }
        HwLog.i(TAG, "updateAllSwitchPreference mainValue=" + mainValue);
        updateVipMailPreference(screen, mainValue);
        if (mainValue) {
            screen.addPreference(this.mNotifyCategory);
            this.mStatusBarSwitchPref.setChecked(this.mAppCfgInfo.isStatusbarNotificationEnabled());
            this.mBannerSwitchPref.setChecked(this.mAppCfgInfo.isHeadsupNotificationEnabled());
            this.mLockScreenSwitchPref.setChecked(this.mAppCfgInfo.isLockscreenNotificationEnabled());
            updateHideContentPreference();
            this.mPrioritySwitchPref.setChecked(this.mBackend.getHighPriority(this.mAppCfgInfo.mPkgName, this.mAppCfgInfo.mUid));
            this.mSoundPref.setChecked(this.mAppCfgInfo.isSoundEnable());
            this.mVibratePref.setChecked(this.mAppCfgInfo.isVibrateEnable());
            return;
        }
        screen.removePreference(this.mNotifyCategory);
    }

    private void updateHideContentPreference() {
        if (this.mAppCfgInfo.isLockscreenNotificationEnabled()) {
            this.mNotifyCategory.addPreference(this.mHideContentSwitchPref);
            this.mNotifyCategory.addPreference(this.mHideContentDividerPref);
            this.mHideContentSwitchPref.setChecked(this.mAppCfgInfo.isHideContent());
            return;
        }
        this.mNotifyCategory.removePreference(this.mHideContentSwitchPref);
        this.mNotifyCategory.removePreference(this.mHideContentDividerPref);
    }

    private void setNotificationBannedByDialog() {
        Activity activity = getActivity();
        if (activity != null) {
            new DualAppDialog(activity, getString(R.string.notify_dialog_title_for_dual_app), getString(R.string.notify_dialog_description_for_dual_app), getString(R.string.notify_dialog_forbid_for_dual_app), getString(R.string.notify_dialog_cancel_for_dual_app), new DualAppDialogCallBack() {
                public void onPositiveBtnClick() {
                    NotificationSettingsFragment.this.setNotificationBanned(false, false);
                }

                public void onNegativeBtnClick() {
                    NotificationSettingsFragment.this.setNotificationBanned(true, true);
                }
            }).show();
        }
    }

    private void setNotificationBanned(boolean checked, boolean cancel) {
        this.mBackend.setNotificationsBanned(this.mAppCfgInfo.mPkgName, this.mAppCfgInfo.mUid, checked);
        updateAllSwitchPreference();
        this.mAppCfgInfo.mFirstStartCfg = 1;
        updateNotification("0", checked, cancel);
    }

    private void updateNotification(String statType, boolean checked, boolean cancel) {
        this.mDatabaseAdapter.updateCfg(this.mAppCfgInfo);
        Helper.setCfgChangeFlag(GlobalContext.getContext(), true);
        NotificationUtils.notifyCfgChange(GlobalContext.getContext(), true);
        if (!cancel && !TextUtils.isEmpty(statType)) {
            int i;
            String[] strArr = new String[6];
            strArr[0] = HsmStatConst.PARAM_PKG;
            strArr[1] = this.mAppCfgInfo.mPkgName;
            strArr[2] = HsmStatConst.PARAM_KEY;
            strArr[3] = statType;
            strArr[4] = HsmStatConst.PARAM_VAL;
            if (checked) {
                i = 1;
            } else {
                i = 0;
            }
            strArr[5] = String.valueOf(i);
            HsmStat.statE(43, HsmStatConst.constructJsonParams(strArr));
        }
    }

    private void finishActivity() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    protected void toastAndFinish() {
        Activity activity = getActivity();
        if (activity != null) {
            Toast.makeText(activity, R.string.app_not_found_dlg_text, 0).show();
            activity.finish();
        }
    }

    private PackageInfo findPackageInfo(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return null;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        PackageManager pm = activity.getPackageManager();
        if (pm == null) {
            return null;
        }
        try {
            return PackageManagerWrapper.getPackageInfo(pm, pkg, 0);
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "findPackageInfo pkg not found:" + pkg);
            return null;
        }
    }

    protected void setOnChangeListener(OnChangeLinstener linstener) {
        this.mChangeLinstener = linstener;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String prefKey = preference.getKey();
        if (newValue instanceof Boolean) {
            String statKey;
            boolean checked = ((Boolean) newValue).booleanValue();
            HwLog.i(TAG, "onPreferenceChange key=" + prefKey + ", newValue=" + newValue);
            if (prefKey.equals(KEY_MAIN_NOTIFI_SWITCH)) {
                onMainPrefChanged(this.mAppCfgInfo, checked);
                statKey = "";
            } else if (prefKey.equals(KEY_STATUS_BAR_SWITCH)) {
                onStatusbarChanged(this.mAppCfgInfo, checked);
                statKey = "1";
            } else if (prefKey.equals(KEY_BANNER_SWITCH)) {
                onBannerChanged(this.mAppCfgInfo, checked);
                statKey = "2";
            } else if (prefKey.equals(KEY_LOCK_SCREEN_SWITCH)) {
                onLockscreenChanged(this.mAppCfgInfo, checked);
                statKey = "3";
            } else if (prefKey.equals(KEY_HIDE_CONTENT_SWITCH)) {
                onHideContentChanged(this.mAppCfgInfo, checked);
                statKey = "5";
            } else if (prefKey.equals(KEY_PRIORITY_SWITCH)) {
                onDisturbPriorityChanged(this.mAppCfgInfo, checked);
                statKey = "4";
            } else if (prefKey.equals(KEY_SOUND_SWITCH)) {
                onSoundChanged(this.mAppCfgInfo, checked);
                statKey = "6";
            } else if (prefKey.equals(KEY_VIBRATE_SWITCH)) {
                onVibrateChanged(this.mAppCfgInfo, checked);
                statKey = "7";
            } else {
                statKey = "";
            }
            updateNotification(statKey, checked, false);
            if (this.mChangeLinstener != null) {
                this.mChangeLinstener.doChange();
            }
            return true;
        }
        HwLog.w(TAG, "onPreferenceChange newValue is not a Boolean, key=" + prefKey);
        return true;
    }

    private void onMainPrefChanged(NotificationCfgInfo info, boolean checked) {
        if (checked || !DualAppUtil.isPackageCloned(GlobalContext.getContext(), info.mPkgName)) {
            setNotificationBanned(checked, false);
        } else {
            setNotificationBannedByDialog();
        }
    }

    private void onStatusbarChanged(NotificationCfgInfo info, boolean checked) {
        HwLog.i(TAG, "onStatusbarChanged enable:" + checked + ", info:" + info.mPkgName);
        info.setStatusbarNotificationEnabled(checked);
    }

    private void onBannerChanged(NotificationCfgInfo info, boolean checked) {
        HwLog.i(TAG, "onBannerChanged enable:" + checked + ", info:" + info.mPkgName);
        info.setHeadsupNotificationEnable(checked);
    }

    private void onLockscreenChanged(NotificationCfgInfo info, boolean checked) {
        HwLog.i(TAG, "onLockscreenChanged enable:" + checked + ", info:" + info.mPkgName);
        info.setLockscreenNotificationEnable(checked);
        updateHideContentPreference();
        this.mBackend.setSensitive(info.mPkgName, info.mUid, checked, info.isHideContent());
    }

    private void onHideContentChanged(NotificationCfgInfo info, boolean checked) {
        HwLog.i(TAG, "onHideContentChanged enable:" + checked + ", info:" + info.mPkgName);
        info.setHideContent(checked);
        this.mBackend.setSensitive(info.mPkgName, info.mUid, true, checked);
    }

    private void onDisturbPriorityChanged(NotificationCfgInfo info, boolean enable) {
        HwLog.i(TAG, "onDisturbPriorityChanged enable:" + enable + ", info:" + info.mPkgName);
        this.mBackend.setHighPriority(info.mPkgName, info.mUid, enable);
    }

    private void onSoundChanged(NotificationCfgInfo info, boolean enable) {
        HwLog.i(TAG, "onSoundChanged enable:" + enable + ", info:" + info.mPkgName);
        info.setSoundEnable(enable);
    }

    private void onVibrateChanged(NotificationCfgInfo info, boolean enable) {
        HwLog.i(TAG, "onSoundChanged enable:" + enable + ", info:" + info.mPkgName);
        info.setVibrateEnable(enable);
    }
}
