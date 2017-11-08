package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.security.KeyStore;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.fingerprint.utils.FingerprintUtils.FingerRemovalCallback;
import java.util.ArrayList;
import java.util.List;

public class DevelopmentSettingsHwBase extends RestrictedSettingsFragment implements OnPreferenceChangeListener {
    private static final int[] SELECT_LOGD_SIZE_TITLES = new int[]{64, 256, 1, 4, 16};
    protected SwitchPreference mAdbInstallNeedConfirm;
    protected final ArrayList<Preference> mAllPrefs = new ArrayList();
    protected SwitchPreference mAllowCharingAdb;
    protected ChooseLockSettingsHelper mChooseLockSettingsHelper;
    protected DevicePolicyManager mDPM;
    protected String mDebugApp;
    protected SwitchPreference mDebugViewAttributes;
    protected SwitchPreference mEnableAdb;
    protected SwitchPreference mEnabledSwitch;
    protected boolean mIsAdbDialogShowBefore = false;
    protected boolean mIsEnableDialogShowBefore = false;
    protected boolean mLastEnabledState;
    protected ContentObserver mPrivacyModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.d("DevelopmentSettingsHwBase", "Receive privacy mode state changed.");
            DevelopmentSettingsHwBase.this.updateScreenLockSwitch();
        }
    };
    protected final ArrayList<TwoStatePreference> mResetSwitchPrefs = new ArrayList();
    protected CustomSwitchPreference mScreenLockSwitch;
    protected SwitchPreference mSmartBackLightPreference;
    protected SwitchPreference mUsbConnPrompt;
    protected SwitchPreference mWifiDisplayCertification;

    protected int getMetricsCategory() {
        return 39;
    }

    public DevelopmentSettingsHwBase() {
        super("no_debugging_features");
    }

    protected void initPreferences(Bundle icicle, TwoStatePreference forceRtlLayout) {
        PreferenceCategory debugDrawingCatogory = (PreferenceCategory) findPreference("debug_drawing_category");
        if (debugDrawingCatogory != null) {
            debugDrawingCatogory.removePreference(forceRtlLayout);
        }
        Preference procStats = findPreference("proc_stats");
        if (procStats != null) {
            this.mAllPrefs.add(procStats);
        }
        if (icicle != null) {
            this.mLastEnabledState = true;
            this.mIsEnableDialogShowBefore = icicle.getBoolean("com.android.settings.Enable_Dialog", false);
            this.mIsAdbDialogShowBefore = icicle.getBoolean("com.android.settings.Adb_Dialog", false);
        }
        removeWifiDisplayCertificationIfDisabled();
        if (Utils.isMonkeyRunning()) {
            getActivity().finish();
        }
    }

    protected void removeWifiDisplayCertificationIfDisabled() {
        if (!getResources().getBoolean(17956985) || Utils.atLestOneSharingAppExist(getActivity())) {
            MLog.d("DevelopmentSettingsHwBase", "config_enableWifiDisplay=false, wifi display certification not supported");
            PreferenceGroup debugDebuggingCategory = (PreferenceGroup) findPreference("debug_networking_category");
            if (!(debugDebuggingCategory == null || this.mWifiDisplayCertification == null)) {
                debugDebuggingCategory.removePreference(this.mWifiDisplayCertification);
                this.mAllPrefs.remove(this.mWifiDisplayCertification);
                this.mResetSwitchPrefs.remove(this.mWifiDisplayCertification);
            }
        }
    }

    protected boolean hasDebugApps() {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }
        List<ApplicationInfo> pkgs = activity.getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < pkgs.size(); i++) {
            ApplicationInfo ai = (ApplicationInfo) pkgs.get(i);
            if (ai.uid != 1000 && ((ai.flags & 2) != 0 || !"user".equals(Build.TYPE))) {
                return true;
            }
        }
        return false;
    }

    protected void setStatusOfDebugAppPref(Preference debugAppPref) {
        if (this.mLastEnabledState && hasDebugApps()) {
            debugAppPref.setEnabled(true);
        } else {
            debugAppPref.setEnabled(false);
        }
    }

    protected void setStatusOfSomeMeber(TwoStatePreference enabledSwitch, Dialog enableDialog, Dialog adbDialog, TwoStatePreference enableAdb) {
        if (this.mIsEnableDialogShowBefore) {
            enabledSwitch.setChecked(true);
            setPrefsEnabledState(true);
            this.mIsEnableDialogShowBefore = false;
        }
        if (this.mIsAdbDialogShowBefore) {
            createAdbDialog();
            this.mIsAdbDialogShowBefore = false;
        }
        if (enableDialog != null && enableDialog.isShowing()) {
            this.mLastEnabledState = true;
            enabledSwitch.setChecked(true);
            setPrefsEnabledState(true);
        }
        updateAdbStatus();
        if (adbDialog != null && adbDialog.isShowing()) {
            enableAdb.setChecked(true);
        }
    }

    protected void createAdbDialog() {
    }

    private void updateAdbStatus() {
        boolean developmentSettingsEnabled;
        int i = 1;
        if (Global.getInt(getContentResolver(), "development_settings_enabled", 0) != 0) {
            developmentSettingsEnabled = true;
        } else {
            developmentSettingsEnabled = false;
        }
        if (developmentSettingsEnabled) {
            if (SystemProperties.get("sys.usb.state", "").equals("none")) {
                this.mEnableAdb.setEnabled(false);
            } else {
                this.mEnableAdb.setEnabled(true);
            }
            boolean adbEabled = SystemProperties.get("sys.usb.state", "").contains("adb");
            if (isSupportUsbLimit()) {
                adbEabled = false;
            }
            updateSwitchPreference(this.mEnableAdb, adbEabled);
            ContentResolver contentResolver = getActivity().getContentResolver();
            String str = "adb_enabled";
            if (!adbEabled) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
    }

    void updateSwitchPreference(TwoStatePreference checkBox, boolean value) {
        MLog.e("DevelopmentSettingsHwBase", "updateCheckBox must be override by subclass");
    }

    protected boolean isSupportUsbLimit() {
        return "1".equals(SystemProperties.get("persist.sys.cmcc_usb_limit", "0"));
    }

    protected boolean startSimUsbLimitActivity(Context mContext) {
        Intent helpIntent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        helpIntent.setFlags(268435456);
        mContext.startActivity(helpIntent);
        return false;
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, 2131627374).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_RESTORE))).setShowAsAction(1);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                confirmRestoreSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void pokeSystemProperties() {
    }

    private void confirmRestoreSettingsDialog() {
        new Builder(getActivity()).setMessage(2131628155).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DevelopmentSettingsHwBase.this.factoryResetSettings();
                ItemUseStat.getInstance().handleClick(DevelopmentSettingsHwBase.this.getActivity(), 2, "restore_development_settings");
            }
        }).setNegativeButton(17039360, null).create().show();
    }

    private void factoryResetSettings() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                Utils.factoryReset(DevelopmentSettingsHwBase.this.getActivity(), "2");
                return null;
            }

            protected void onPostExecute(Void result) {
                DevelopmentSettingsHwBase.this.pokeSystemProperties();
                Context context = DevelopmentSettingsHwBase.this.getActivity();
                if (context != null) {
                    DevelopmentSettingsHwBase.this.updateAllOptions();
                    DevelopmentSettingsHwBase.this.writeLogdSizeOption(null);
                    DevelopmentSettingsHwBase.this.updateMockLocation();
                    Global.putInt(context.getContentResolver(), "debug_view_attributes", 0);
                    DevelopmentSettingsHwBase.this.updateSwitchPreference(DevelopmentSettingsHwBase.this.mDebugViewAttributes, false);
                    DevelopmentSettingsHwBase.this.updateScreenLockSwitch();
                }
            }
        }.execute(new Void[0]);
    }

    protected void updateAllOptions() {
    }

    protected void updateMockLocation() {
    }

    protected void writeLogdSizeOption(Object newValue) {
    }

    protected void setPrefsEnabledState(boolean enabled) {
    }

    protected void initSmartBackLightPreference() {
        this.mSmartBackLightPreference = (SwitchPreference) findPreference("smart_backlight");
        this.mSmartBackLightPreference.setOnPreferenceChangeListener(null);
        this.mSmartBackLightPreference.setChecked(System.getIntForUser(getContentResolver(), "smart_backlight_enable", 0, -2) == 1);
    }

    protected void updateScreenLockSwitch() {
        if (MdppUtils.isCcModeDisabled()) {
            this.mScreenLockSwitch = (CustomSwitchPreference) findPreference("screen_lock_switch");
            PrivacyModeManager pmm = new PrivacyModeManager(getActivity());
            boolean privacyProtectionOn = pmm.isPrivacyModeEnabled();
            boolean isGuestModeOn = pmm.isGuestModeOn();
            Log.d("DevelopmentSettingsHwBase", "Privacy mode is on: " + privacyProtectionOn);
            Log.d("DevelopmentSettingsHwBase", "Is guest mode: " + isGuestModeOn);
            if (privacyProtectionOn) {
                if (isGuestModeOn) {
                    removePreference("screen_lock_switch");
                    this.mScreenLockSwitch = null;
                } else if (this.mScreenLockSwitch == null) {
                    this.mScreenLockSwitch = new CustomSwitchPreference(getActivity());
                    this.mScreenLockSwitch.setKey("screen_lock_switch");
                    this.mScreenLockSwitch.setPersistent(false);
                    this.mScreenLockSwitch.setTitle(2131627770);
                    this.mScreenLockSwitch.setSummary(2131627771);
                    this.mScreenLockSwitch.setOrder(findPreference("bugreport").getOrder() + 1);
                    getPreferenceScreen().addPreference(this.mScreenLockSwitch);
                }
            }
            if (this.mScreenLockSwitch != null) {
                boolean z;
                this.mScreenLockSwitch.setOnPreferenceChangeListener(null);
                CustomSwitchPreference customSwitchPreference = this.mScreenLockSwitch;
                if (this.mChooseLockSettingsHelper.utils().isLockScreenDisabled(UserHandle.myUserId())) {
                    z = false;
                } else {
                    z = true;
                }
                customSwitchPreference.setChecked(z);
                this.mScreenLockSwitch.setOnPreferenceChangeListener(this);
                this.mScreenLockSwitch.setEnabled(true);
                if ("true".equals(System.getString(getContentResolver(), "isDisableKeyGuard"))) {
                    this.mScreenLockSwitch.setEnabled(false);
                }
                this.mScreenLockSwitch.setSummary(2131627771);
                if (privacyProtectionOn && !isGuestModeOn) {
                    this.mScreenLockSwitch.setEnabled(false);
                    this.mScreenLockSwitch.setSummary(2131627544);
                }
                if (upgradeQuality(0, null) > 0) {
                    this.mScreenLockSwitch.setEnabled(false);
                    this.mScreenLockSwitch.setSummary(2131624744);
                }
                return;
            }
            return;
        }
        removePreference("screen_lock_switch");
        this.mScreenLockSwitch = null;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        registerPrivacyModeObserver();
    }

    public void onDestroy() {
        unregisterPrivacyModeObserver();
        super.onDestroy();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (!"screen_lock_switch".equals(preference.getKey())) {
            return false;
        }
        Boolean valueObj = (Boolean) value;
        if (valueObj.booleanValue() != this.mScreenLockSwitch.isChecked()) {
            if (this.mChooseLockSettingsHelper.utils().isLockScreenDisabled(UserHandle.myUserId()) && valueObj.booleanValue()) {
                setLockScreenDisabled(false);
            } else if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(199, getString(2131624724))) {
                setLockScreenDisabled(true);
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 199 && resultCode == -1) {
            setLockScreenDisabled(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected int upgradeQuality(int quality, MutableBoolean allowBiometric) {
        quality = upgradeQualityForKeyStore(upgradeQualityForDPM(quality));
        if (MdppUtils.isCcModeDisabled()) {
            return quality;
        }
        return upgradeQualityForCCMode(quality);
    }

    private int upgradeQualityForDPM(int quality) {
        int minQuality = this.mDPM.getPasswordQuality(null);
        if (quality < minQuality) {
            return minQuality;
        }
        return quality;
    }

    private int upgradeQualityForKeyStore(int quality) {
        if (KeyStore.getInstance().isEmpty() || quality >= 65536) {
            return quality;
        }
        return 65536;
    }

    private int upgradeQualityForCCMode(int quality) {
        if (quality < 65536) {
            return 65536;
        }
        return quality;
    }

    protected void setLockScreenDisabled(boolean disable) {
        if (disable) {
            Activity activity = getActivity();
            this.mChooseLockSettingsHelper.utils().clearLock(UserHandle.myUserId());
            if (activity != null) {
                FingerprintUtils.removeAllFingerprintTemplates(activity, new FingerRemovalCallback(activity), null);
            } else {
                Log.e("DevelopmentSettingsHwBase", "Failed to get activity, do not remove fingerprints");
            }
        }
        this.mChooseLockSettingsHelper.utils().setLockScreenDisabled(disable, UserHandle.myUserId());
        updateScreenLockSwitch();
    }

    private void registerPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_state"), true, this.mPrivacyModeObserver);
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_on"), true, this.mPrivacyModeObserver);
        }
    }

    private void unregisterPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().unregisterContentObserver(this.mPrivacyModeObserver);
        }
    }

    protected String[] buildSelectLogdSizeTitles(Context context) {
        String[] selectLogdSizeTitles = new String[5];
        selectLogdSizeTitles[0] = String.format(getResources().getString(2131628398, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[0])}), new Object[0]);
        selectLogdSizeTitles[1] = String.format(getResources().getString(2131628399, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[1])}), new Object[0]);
        selectLogdSizeTitles[2] = String.format(getResources().getString(2131628400, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[2])}), new Object[0]);
        selectLogdSizeTitles[3] = String.format(getResources().getString(2131628401, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[3])}), new Object[0]);
        selectLogdSizeTitles[4] = String.format(getResources().getString(2131628402, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[4])}), new Object[0]);
        return selectLogdSizeTitles;
    }

    protected String[] buildSelectLogdSizeLowramTitles(Context context) {
        selectLogdSizeTitles = new String[3];
        selectLogdSizeTitles[0] = String.format(getResources().getString(2131628398, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[0])}), new Object[0]);
        selectLogdSizeTitles[1] = String.format(getResources().getString(2131628399, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[1])}), new Object[0]);
        selectLogdSizeTitles[2] = String.format(getResources().getString(2131628400, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[2])}), new Object[0]);
        return selectLogdSizeTitles;
    }

    protected String[] buildSelectLogdSizeSummaries(Context context) {
        String[] selectLogdSizeTitles = new String[5];
        selectLogdSizeTitles[0] = String.format(getResources().getString(2131628403, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[0])}), new Object[0]);
        selectLogdSizeTitles[1] = String.format(getResources().getString(2131628404, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[1])}), new Object[0]);
        selectLogdSizeTitles[2] = String.format(getResources().getString(2131628405, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[2])}), new Object[0]);
        selectLogdSizeTitles[3] = String.format(getResources().getString(2131628406, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[3])}), new Object[0]);
        selectLogdSizeTitles[4] = String.format(getResources().getString(2131628407, new Object[]{Integer.valueOf(SELECT_LOGD_SIZE_TITLES[4])}), new Object[0]);
        return selectLogdSizeTitles;
    }
}
