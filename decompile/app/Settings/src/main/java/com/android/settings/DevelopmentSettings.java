package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.app.backup.IBackupManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IShortcutService;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.persistentdata.PersistentDataBlockManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.IWebViewUpdateService;
import android.webkit.WebViewProviderInfo;
import android.widget.Toast;
import com.android.settings.applications.BackgroundCheckSummary;
import com.android.settings.fuelgauge.InactiveApps;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class DevelopmentSettings extends DevelopmentSettingsHwBase implements OnClickListener, OnDismissListener, OnPreferenceChangeListener, Indexable {
    private static final int[] APP_PROCESS_LIMIT = new int[]{1, 2, 3, 4};
    private static final int[] MOCK_LOCATION_APP_OPS = new int[]{58};
    private static final int[] OVERLAY_DISPLAY_DEVICES = new int[]{480, 480, 720, 720, 1080, 1080, 4, 4, 4, 4, 720, 1080};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        private boolean isShowingDeveloperOptions(Context context) {
            if (!context.getSharedPreferences("development", 0).getBoolean("show", Build.TYPE.equals("eng")) || ParentControl.isChildModeOn(context)) {
                return false;
            }
            return true;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!isShowingDeveloperOptions(context)) {
                return null;
            }
            new SearchIndexableResource(context).xmlResId = 2131230767;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            if (!isShowingDeveloperOptions(context)) {
                return null;
            }
            List<String> keys = new ArrayList();
            if (Utils.isWifiOnly(context)) {
                keys.add("wifi_aggressive_handover");
                keys.add("mobile_data_always_on");
            }
            if (!DevelopmentSettings.showEnableOemUnlockPreference()) {
                keys.add("oem_unlock_enable");
            }
            if (!context.getResources().getBoolean(17956985) || Utils.atLestOneSharingAppExist(context)) {
                keys.add("wifi_display_certification");
            }
            if (!SystemProperties.getBoolean("ro.adb.secure", false)) {
                keys.add("clear_adb_keys");
            }
            if (!DevelopmentSettings.isPackageInstalled(context, "com.android.terminal")) {
                keys.add("enable_terminal");
            }
            if (!DevelopmentSettings.isPackageInstalled(context, "com.android.packageinstaller")) {
                keys.add("adb_install_need_confirm");
            }
            if (Global.getInt(context.getContentResolver(), "verifier_setting_visible", 1) < 1) {
                keys.add("verify_apps_over_usb");
            }
            if ("user".equals(Build.TYPE)) {
                keys.add("hdcp_checking");
            }
            PrivacyModeManager pmm = new PrivacyModeManager(context);
            boolean privacyProtectionOn = pmm.isPrivacyModeEnabled();
            boolean isGuestModeOn = pmm.isGuestModeOn();
            if ((privacyProtectionOn && isGuestModeOn) || !MdppUtils.isCcModeDisabled()) {
                keys.add("screen_lock_switch");
            }
            if (!context.getResources().getBoolean(2131492919)) {
                keys.add("color_temperature");
            }
            if (SystemProperties.getInt("ro.config.hw_smart_backlight", 1) == 0) {
                keys.add("smart_backlight");
            }
            return keys;
        }
    };
    private Dialog mAdbDialog;
    private Dialog mAdbKeysDialog;
    private Dialog mAllowChargingAdbDialog;
    private ListPreference mAnimatorDurationScale;
    private ListPreference mAppProcessLimit;
    private IBackupManager mBackupManager;
    private SwitchPreference mBluetoothDisableAbsVolume;
    private SwitchPreference mBtHciSnoopLog;
    private Preference mBugreport;
    private Preference mClearAdbKeys;
    private ColorModePreference mColorModePreference;
    private SwitchPreference mColorTemperaturePreference;
    private Preference mDebugAppPref;
    private ListPreference mDebugHwOverdraw;
    private SwitchPreference mDebugLayout;
    private boolean mDialogClicked;
    private SwitchPreference mDisableOverlays;
    private final HashSet<Preference> mDisabledPrefs = new HashSet();
    private boolean mDontPokeProperties;
    private DevicePolicyManager mDpm;
    private Dialog mEnableDialog;
    private SwitchPreference mEnableOemUnlock;
    private SwitchPreference mEnableTerminal;
    private SwitchPreference mForceAllowOnExternal;
    private SwitchPreference mForceHardwareUi;
    private SwitchPreference mForceMsaa;
    private SwitchPreference mForceResizable;
    private SwitchPreference mForceRtlLayout;
    private boolean mHaveDebugSettings;
    private HwCustDevelopmentSettings mHwCustDevelopmentSettings;
    private SwitchPreference mImmediatelyDestroyActivities;
    private RestrictedSwitchPreference mKeepScreenOn;
    private ListPreference mLogdSize;
    private SwitchPreference mMobileDataAlwaysOn;
    private String mMockLocationApp;
    private Preference mMockLocationAppPref;
    private PreferenceGroup mNetworkingCategory;
    private PersistentDataBlockManager mOemUnlockManager;
    private SwitchPreference mOtaDisableAutomaticUpdate;
    private ListPreference mOverlayDisplayDevices;
    private PreferenceScreen mPassword;
    private SwitchPreference mPointerLocation;
    private SwitchPreference mShowAllANRs;
    private SwitchPreference mShowCpuUsage;
    private SwitchPreference mShowHwLayersUpdates;
    private SwitchPreference mShowHwScreenUpdates;
    private ListPreference mShowNonRectClip;
    private SwitchPreference mShowScreenUpdates;
    private SwitchPreference mShowTouches;
    private ListPreference mSimulateColorSpace;
    private SwitchPreference mStrictMode;
    private ListPreference mTrackFrameTime;
    private ListPreference mTransitionAnimationScale;
    private SwitchPreference mUSBAudio;
    private UserManager mUm;
    private boolean mUnavailable;
    private ListPreference mUsbConfiguration;
    private UsbManager mUsbManager;
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DevelopmentSettings.this.updateUsbConfigurationValues();
        }
    };
    private SwitchPreference mVerifyAppsOverUsb;
    private SwitchPreference mWaitForDebugger;
    private SwitchPreference mWebViewMultiprocess;
    private ListPreference mWebViewProvider;
    private IWebViewUpdateService mWebViewUpdateService;
    private SwitchPreference mWifiAggressiveHandover;
    private SwitchPreference mWifiAllowScansWithTraffic;
    private WifiManager mWifiManager;
    private SwitchPreference mWifiVerboseLogging;
    private ListPreference mWindowAnimationScale;
    private IWindowManager mWindowManager;

    public static class SystemPropPoker extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            String[] services = ServiceManager.listServices();
            if (services == null) {
                return null;
            }
            for (String service : services) {
                IBinder obj = ServiceManager.checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(1599295570, data, null, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (Exception e2) {
                        Log.i("DevelopmentSettings", "Someone wrote a bad service '" + service + "' that doesn't like to be poked: " + e2);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }

    protected int getMetricsCategory() {
        return 39;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUsbManager = (UsbManager) getSystemService("usb");
        this.mHwCustDevelopmentSettings = (HwCustDevelopmentSettings) HwCustUtils.createObj(HwCustDevelopmentSettings.class, new Object[]{this});
        this.mWindowManager = Stub.asInterface(ServiceManager.getService("window"));
        this.mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
        this.mWebViewUpdateService = IWebViewUpdateService.Stub.asInterface(ServiceManager.getService("webviewupdate"));
        this.mOemUnlockManager = (PersistentDataBlockManager) getActivity().getSystemService("persistent_data_block");
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        this.mUm = (UserManager) getSystemService("user");
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        setIfOnlyAvailableForAdmins(true);
        if (isUiRestricted() || !Utils.isDeviceProvisioned(getActivity())) {
            this.mUnavailable = true;
            getActivity().finish();
        }
        addPreferencesFromResource(2131230767);
        PreferenceGroup debugDebuggingCategory = (PreferenceGroup) findPreference("debug_debugging_category");
        this.mEnableAdb = findAndInitSwitchPref("enable_adb");
        this.mAllowCharingAdb = findAndInitSwitchPref("allow_charging_adb");
        this.mClearAdbKeys = findPreference("clear_adb_keys");
        if (!(SystemProperties.getBoolean("ro.adb.secure", false) || debugDebuggingCategory == null)) {
            debugDebuggingCategory.removePreference(this.mClearAdbKeys);
        }
        this.mAllPrefs.add(this.mClearAdbKeys);
        this.mUsbConnPrompt = findAndInitSwitchPref("usb_connect_prompt");
        this.mEnableTerminal = findAndInitSwitchPref("enable_terminal");
        if (!isPackageInstalled(getActivity(), "com.android.terminal")) {
            debugDebuggingCategory.removePreference(this.mEnableTerminal);
            this.mEnableTerminal = null;
        }
        this.mBugreport = findPreference("bugreport");
        this.mAllPrefs.add(this.mBugreport);
        this.mKeepScreenOn = (RestrictedSwitchPreference) findAndInitSwitchPref("keep_screen_on");
        this.mBtHciSnoopLog = findAndInitSwitchPref("bt_hci_snoop_log");
        this.mEnableOemUnlock = findAndInitSwitchPref("oem_unlock_enable");
        if (!showEnableOemUnlockPreference()) {
            removePreference(this.mEnableOemUnlock);
            this.mEnableOemUnlock = null;
        }
        this.mDebugViewAttributes = findAndInitSwitchPref("debug_view_attributes");
        this.mForceAllowOnExternal = findAndInitSwitchPref("force_allow_on_external");
        this.mPassword = (PreferenceScreen) findPreference("local_backup_password");
        this.mAllPrefs.add(this.mPassword);
        if (!this.mUm.isAdminUser()) {
            disableForUser(this.mEnableAdb);
            disableForUser(this.mClearAdbKeys);
            disableForUser(this.mEnableTerminal);
            disableForUser(this.mPassword);
        }
        this.mDebugAppPref = findPreference("debug_app");
        this.mAllPrefs.add(this.mDebugAppPref);
        this.mWaitForDebugger = findAndInitSwitchPref("wait_for_debugger");
        this.mMockLocationAppPref = findPreference("mock_location_app");
        this.mAllPrefs.add(this.mMockLocationAppPref);
        this.mVerifyAppsOverUsb = findAndInitSwitchPref("verify_apps_over_usb");
        if (!showVerifierSetting()) {
            if (debugDebuggingCategory != null) {
                debugDebuggingCategory.removePreference(this.mVerifyAppsOverUsb);
            } else {
                this.mVerifyAppsOverUsb.setEnabled(false);
            }
        }
        this.mStrictMode = findAndInitSwitchPref("strict_mode");
        this.mPointerLocation = findAndInitSwitchPref("pointer_location");
        this.mShowTouches = findAndInitSwitchPref("show_touches");
        this.mShowScreenUpdates = findAndInitSwitchPref("show_screen_updates");
        this.mDisableOverlays = findAndInitSwitchPref("disable_overlays");
        this.mShowCpuUsage = findAndInitSwitchPref("show_cpu_usage");
        this.mForceHardwareUi = findAndInitSwitchPref("force_hw_ui");
        this.mForceMsaa = findAndInitSwitchPref("force_msaa");
        this.mTrackFrameTime = addListPreference("track_frame_time");
        this.mShowNonRectClip = addListPreference("show_non_rect_clip");
        this.mShowHwScreenUpdates = findAndInitSwitchPref("show_hw_screen_udpates");
        this.mShowHwLayersUpdates = findAndInitSwitchPref("show_hw_layers_udpates");
        this.mDebugLayout = findAndInitSwitchPref("debug_layout");
        this.mForceRtlLayout = findAndInitSwitchPref("force_rtl_layout_all_locales");
        this.mDebugHwOverdraw = addListPreference("debug_hw_overdraw");
        this.mWifiDisplayCertification = findAndInitSwitchPref("wifi_display_certification");
        this.mWifiVerboseLogging = findAndInitSwitchPref("wifi_verbose_logging");
        this.mWifiAggressiveHandover = findAndInitSwitchPref("wifi_aggressive_handover");
        this.mWifiAllowScansWithTraffic = findAndInitSwitchPref("wifi_allow_scan_with_traffic");
        this.mMobileDataAlwaysOn = findAndInitSwitchPref("mobile_data_always_on");
        this.mLogdSize = addListPreference("select_logd_size");
        this.mUsbConfiguration = addListPreference("select_usb_configuration");
        this.mWebViewProvider = addListPreference("select_webview_provider");
        this.mWebViewMultiprocess = findAndInitSwitchPref("enable_webview_multiprocess");
        this.mBluetoothDisableAbsVolume = findAndInitSwitchPref("bluetooth_disable_absolute_volume");
        this.mWindowAnimationScale = addListPreference("window_animation_scale");
        this.mTransitionAnimationScale = addListPreference("transition_animation_scale");
        this.mAnimatorDurationScale = addListPreference("animator_duration_scale");
        this.mOverlayDisplayDevices = addListPreference("overlay_display_devices");
        this.mSimulateColorSpace = addListPreference("simulate_color_space");
        this.mUSBAudio = findAndInitSwitchPref("usb_audio");
        this.mForceResizable = findAndInitSwitchPref("force_resizable_activities");
        this.mAdbInstallNeedConfirm = findAndInitSwitchPref("adb_install_need_confirm");
        this.mAdbInstallNeedConfirm.setChecked(Secure.getInt(getContentResolver(), "adb_install_need_confirm", 1) == 1);
        if (!isPackageInstalled(getActivity(), "com.android.packageinstaller")) {
            debugDebuggingCategory.removePreference(this.mAdbInstallNeedConfirm);
        }
        this.mEnabledSwitch = findAndInitSwitchPref("development_enable_switch");
        this.mImmediatelyDestroyActivities = findAndInitSwitchPref("immediately_destroy_activities");
        this.mAppProcessLimit = addListPreference("app_process_limit");
        this.mAppProcessLimit.setEntries(buildLimitEntries(getActivity()));
        this.mShowAllANRs = findAndInitSwitchPref("show_all_anrs");
        if (SystemProperties.getInt("ro.config.hw_smart_backlight", 1) == 1) {
            initSmartBackLightPreference();
            findAndInitSwitchPref("smart_backlight");
        } else {
            removePreference("debug_debugging_category", "smart_backlight");
        }
        Preference hdcpChecking = findPreference("hdcp_checking");
        if (hdcpChecking != null) {
            this.mAllPrefs.add(hdcpChecking);
            removePreferenceForProduction(hdcpChecking);
        }
        PreferenceScreen convertFbePreference = (PreferenceScreen) findPreference("convert_to_file_encryption");
        try {
            if (!IMountService.Stub.asInterface(ServiceManager.getService("mount")).isConvertibleToFBE()) {
                removePreference("convert_to_file_encryption");
            } else if ("file".equals(SystemProperties.get("ro.crypto.type", "none"))) {
                convertFbePreference.setEnabled(false);
                convertFbePreference.setSummary(getResources().getString(2131624196));
            }
        } catch (RemoteException e) {
            removePreference("convert_to_file_encryption");
        }
        this.mOtaDisableAutomaticUpdate = findAndInitSwitchPref("ota_disable_automatic_update");
        this.mColorModePreference = (ColorModePreference) findPreference("color_mode");
        this.mColorModePreference.updateCurrentAndSupported();
        if (this.mColorModePreference.getTransformsCount() < 2) {
            removePreference("color_mode");
            this.mColorModePreference = null;
        }
        updateWebViewProviderOptions();
        this.mColorTemperaturePreference = (SwitchPreference) findPreference("color_temperature");
        if (getResources().getBoolean(2131492919)) {
            this.mAllPrefs.add(this.mColorTemperaturePreference);
            this.mResetSwitchPrefs.add(this.mColorTemperaturePreference);
        } else {
            removePreference("color_temperature");
            this.mColorTemperaturePreference = null;
        }
        initPreferences(icicle, this.mForceRtlLayout);
        if (this.mHwCustDevelopmentSettings != null) {
            this.mHwCustDevelopmentSettings.updateCustPreference(getActivity());
        }
        setHasOptionsMenu(true);
        this.mNetworkingCategory = (PreferenceGroup) findPreference("debug_networking_category");
    }

    private CharSequence[] buildLimitEntries(Context context) {
        timeoutEntries = new CharSequence[6];
        timeoutEntries[2] = String.format(getResources().getString(2131628311, new Object[]{Integer.valueOf(APP_PROCESS_LIMIT[0])}), new Object[0]);
        timeoutEntries[3] = String.format(getResources().getString(2131628312, new Object[]{Integer.valueOf(APP_PROCESS_LIMIT[1])}), new Object[0]);
        timeoutEntries[4] = String.format(getResources().getString(2131628313, new Object[]{Integer.valueOf(APP_PROCESS_LIMIT[2])}), new Object[0]);
        timeoutEntries[5] = String.format(getResources().getString(2131628314, new Object[]{Integer.valueOf(APP_PROCESS_LIMIT[3])}), new Object[0]);
        return timeoutEntries;
    }

    private ListPreference addListPreference(String prefKey) {
        ListPreference pref = (ListPreference) findPreference(prefKey);
        this.mAllPrefs.add(pref);
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    private void disableForUser(Preference pref) {
        if (pref != null) {
            pref.setEnabled(false);
            this.mDisabledPrefs.add(pref);
        }
    }

    private SwitchPreference findAndInitSwitchPref(String key) {
        SwitchPreference pref = (SwitchPreference) findPreference(key);
        if (pref == null) {
            throw new IllegalArgumentException("Cannot find preference with key = " + key);
        }
        if (!"development_enable_switch".equals(key)) {
            this.mAllPrefs.add(pref);
            this.mResetSwitchPrefs.add(pref);
        }
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mUnavailable) {
            this.mEnabledSwitch.setEnabled(false);
        }
    }

    private boolean removePreferenceForProduction(Preference preference) {
        if (!"user".equals(Build.TYPE)) {
            return false;
        }
        removePreference(preference);
        return true;
    }

    private void removePreference(Preference preference) {
        getPreferenceScreen().removePreference(preference);
        this.mAllPrefs.remove(preference);
        this.mResetSwitchPrefs.remove(preference);
    }

    public void setPrefsEnabledState(boolean enabled) {
        for (int i = 0; i < this.mAllPrefs.size(); i++) {
            Preference pref = (Preference) this.mAllPrefs.get(i);
            if (pref != null) {
                boolean z = enabled && !this.mDisabledPrefs.contains(pref);
                pref.setEnabled(z);
            }
        }
        updateAllOptions();
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        if (this.mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(2131624063);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        boolean z2;
        EnforcedAdmin admin = RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(getActivity());
        this.mKeepScreenOn.setDisabledByAdmin(admin);
        if (admin == null) {
            this.mDisabledPrefs.remove(this.mKeepScreenOn);
        } else {
            this.mDisabledPrefs.add(this.mKeepScreenOn);
        }
        if (Global.getInt(getActivity().getContentResolver(), "development_settings_enabled", 0) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mLastEnabledState = z2;
        this.mEnabledSwitch.setChecked(this.mLastEnabledState);
        setPrefsEnabledState(this.mLastEnabledState);
        if (this.mHaveDebugSettings && !this.mLastEnabledState) {
            Global.putInt(getActivity().getContentResolver(), "development_settings_enabled", 1);
            this.mLastEnabledState = true;
            this.mEnabledSwitch.setChecked(this.mLastEnabledState);
            setPrefsEnabledState(this.mLastEnabledState);
        }
        if (this.mColorModePreference != null) {
            this.mColorModePreference.startListening();
            this.mColorModePreference.updateCurrentAndSupported();
        }
        setStatusOfSomeMeber(this.mEnabledSwitch, this.mEnableDialog, this.mAdbDialog, this.mEnableAdb);
        if (this.mHwCustDevelopmentSettings != null) {
            this.mHwCustDevelopmentSettings.disableAllDeveloperSettings();
        }
        if (this.mUsbManager != null) {
            SwitchPreference switchPreference = this.mEnableAdb;
            if (!this.mUsbManager.isFunctionEnabled("manufacture,adb")) {
                z = true;
            }
            switchPreference.setEnabled(z);
        } else {
            Log.w("DevelopmentSettings", "USB Service is null!");
        }
        updateScreenLockSwitch();
    }

    public void onPause() {
        super.onPause();
        if (this.mColorModePreference != null) {
            this.mColorModePreference.stopListening();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        if (getActivity().registerReceiver(this.mUsbReceiver, filter) == null) {
            updateUsbConfigurationValues();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (!this.mUnavailable) {
            getActivity().unregisterReceiver(this.mUsbReceiver);
        }
    }

    void updateSwitchPreference(TwoStatePreference checkBox, boolean value) {
        checkBox.setChecked(value);
        this.mHaveDebugSettings |= value;
    }

    protected void updateAllOptions() {
        boolean z = true;
        Context context = getActivity();
        if (context != null) {
            boolean z2;
            ContentResolver cr = context.getContentResolver();
            this.mHaveDebugSettings = false;
            TwoStatePreference twoStatePreference = this.mEnableAdb;
            if (Global.getInt(cr, "adb_enabled", 0) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            twoStatePreference = this.mUsbConnPrompt;
            if (Secure.getInt(cr, "usb_conn_prompt", 1) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            twoStatePreference = this.mAllowCharingAdb;
            if (Global.getInt(cr, "allow_charging_adb", 0) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            if (this.mEnableTerminal != null) {
                twoStatePreference = this.mEnableTerminal;
                if (context.getPackageManager().getApplicationEnabledSetting("com.android.terminal") == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                updateSwitchPreference(twoStatePreference, z2);
            }
            twoStatePreference = this.mKeepScreenOn;
            if (Global.getInt(cr, "stay_on_while_plugged_in", 0) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            twoStatePreference = this.mBtHciSnoopLog;
            if (Secure.getInt(cr, "bluetooth_hci_log", 0) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            if (this.mEnableOemUnlock != null) {
                updateSwitchPreference(this.mEnableOemUnlock, Utils.isOemUnlockEnabled(getActivity()));
            }
            twoStatePreference = this.mDebugViewAttributes;
            if (Global.getInt(cr, "debug_view_attributes", 0) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            updateSwitchPreference(twoStatePreference, z2);
            TwoStatePreference twoStatePreference2 = this.mForceAllowOnExternal;
            if (Global.getInt(cr, "force_allow_on_external", 0) == 0) {
                z = false;
            }
            updateSwitchPreference(twoStatePreference2, z);
            updateHdcpValues();
            updatePasswordSummary();
            updateDebuggerOptions();
            updateMockLocation();
            updateStrictModeVisualOptions();
            updatePointerLocationOptions();
            updateShowTouchesOptions();
            updateFlingerOptions();
            updateCpuUsageOptions();
            updateHardwareUiOptions();
            updateMsaaOptions();
            updateTrackFrameTimeOptions();
            updateShowNonRectClipOptions();
            updateShowHwScreenUpdatesOptions();
            updateShowHwLayersUpdatesOptions();
            updateDebugHwOverdrawOptions();
            updateDebugLayoutOptions();
            updateAnimationScaleOptions();
            updateOverlayDisplayDevicesOptions();
            updateImmediatelyDestroyActivitiesOptions();
            updateAppProcessLimitOptions();
            updateShowAllANRsOptions();
            updateVerifyAppsOverUsbOptions();
            updateOtaDisableAutomaticUpdateOptions();
            updateBugreportOptions();
            updateLogdSizeValues();
            updateWifiDisplayCertificationOptions();
            updateWifiVerboseLoggingOptions();
            if (!Utils.isWifiOnly(context) || this.mNetworkingCategory == null) {
                updateWifiAggressiveHandoverOptions();
                updateMobileDataAlwaysOnOptions();
            } else {
                if (this.mWifiAggressiveHandover != null) {
                    this.mNetworkingCategory.removePreference(this.mWifiAggressiveHandover);
                }
                if (this.mMobileDataAlwaysOn != null) {
                    this.mNetworkingCategory.removePreference(this.mMobileDataAlwaysOn);
                }
            }
            updateWifiAllowScansWithTrafficOptions();
            updateSimulateColorSpace();
            updateUSBAudioOptions();
            updateForceResizableOptions();
            updateWebViewMultiprocessOptions();
            updateWebViewProviderOptions();
            updateOemUnlockOptions();
            if (this.mColorTemperaturePreference != null) {
                updateColorTemperature();
            }
            updateBluetoothDisableAbsVolumeOptions();
        }
    }

    public void resetDangerousOptions() {
        this.mDontPokeProperties = true;
        for (int i = 0; i < this.mResetSwitchPrefs.size(); i++) {
            TwoStatePreference cb = (TwoStatePreference) this.mResetSwitchPrefs.get(i);
            if (cb.isChecked()) {
                cb.setChecked(false);
                onPreferenceChange(cb, Boolean.valueOf(false));
            }
        }
        resetDebuggerOptions();
        writeLogdSizeOption(null);
        writeAnimationScaleOption(0, this.mWindowAnimationScale, null);
        writeAnimationScaleOption(1, this.mTransitionAnimationScale, null);
        writeAnimationScaleOption(2, this.mAnimatorDurationScale, null);
        if (usingDevelopmentColorSpace()) {
            writeSimulateColorSpace(Integer.valueOf(-1));
        }
        writeOverlayDisplayDevicesOptions(null);
        writeAppProcessLimitOptions(null);
        this.mHaveDebugSettings = false;
        updateAllOptions();
        this.mDontPokeProperties = false;
        pokeSystemProperties();
        Global.putInt(getActivity().getContentResolver(), "verifier_verify_adb_installs", 0);
    }

    private void updateWebViewProviderOptions() {
        try {
            WebViewProviderInfo[] providers = this.mWebViewUpdateService.getValidWebViewPackages();
            if (providers == null) {
                Log.e("DevelopmentSettings", "No WebView providers available");
                return;
            }
            ArrayList<String> options = new ArrayList();
            ArrayList<String> values = new ArrayList();
            for (int n = 0; n < providers.length; n++) {
                if (Utils.isPackageEnabled(getActivity(), providers[n].packageName)) {
                    options.add(providers[n].description);
                    values.add(providers[n].packageName);
                }
            }
            this.mWebViewProvider.setEntries((CharSequence[]) options.toArray(new String[options.size()]));
            this.mWebViewProvider.setEntryValues((CharSequence[]) values.toArray(new String[values.size()]));
            String value = this.mWebViewUpdateService.getCurrentWebViewPackageName();
            if (value == null) {
                value = "";
            }
            for (int i = 0; i < values.size(); i++) {
                if (value.contentEquals((CharSequence) values.get(i))) {
                    this.mWebViewProvider.setValueIndex(i);
                    return;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateWebViewMultiprocessOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mWebViewMultiprocess;
        if (Global.getInt(getActivity().getContentResolver(), "webview_multiprocess", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeWebViewMultiprocessOptions(boolean isChecked) {
        Global.putInt(getActivity().getContentResolver(), "webview_multiprocess", isChecked ? 1 : 0);
        try {
            ActivityManagerNative.getDefault().killPackageDependents(this.mWebViewUpdateService.getCurrentWebViewPackageName(), -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHdcpValues() {
        ListPreference hdcpChecking = (ListPreference) findPreference("hdcp_checking");
        if (hdcpChecking != null) {
            String currentValue = SystemProperties.get("persist.sys.hdcp_checking");
            String[] values = getResources().getStringArray(2131361795);
            String[] summaries = getResources().getStringArray(2131361796);
            int index = 1;
            for (int i = 0; i < values.length; i++) {
                if (currentValue.equals(values[i])) {
                    index = i;
                    break;
                }
            }
            hdcpChecking.setValue(values[index]);
            hdcpChecking.setSummary(summaries[index]);
            hdcpChecking.setOnPreferenceChangeListener(this);
        }
    }

    private void updatePasswordSummary() {
        try {
            if (this.mBackupManager.hasBackupPassword()) {
                this.mPassword.setSummary(2131624180);
            } else {
                this.mPassword.setSummary(2131624179);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void writeBtHciSnoopLogOptions(boolean isChecked) {
        BluetoothAdapter.getDefaultAdapter().configHciSnoopLog(isChecked);
        Secure.putInt(getActivity().getContentResolver(), "bluetooth_hci_log", isChecked ? 1 : 0);
    }

    private boolean writeWebViewProviderOptions(Object newValue) {
        boolean z = false;
        try {
            String updatedProvider = this.mWebViewUpdateService.changeProviderAndSetting(newValue == null ? "" : newValue.toString());
            updateWebViewProviderOptions();
            if (newValue != null) {
                z = newValue.equals(updatedProvider);
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void writeDebuggerOptions(boolean isChecked) {
        try {
            ActivityManagerNative.getDefault().setDebugApp(this.mDebugApp, isChecked, true);
        } catch (RemoteException ex) {
            MLog.e("DevelopmentSettings", "RemoteException e: " + ex);
        }
    }

    private void writeMockLocation() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService("appops");
        List<PackageOps> packageOps = appOpsManager.getPackagesForOps(MOCK_LOCATION_APP_OPS);
        if (packageOps != null) {
            for (PackageOps packageOp : packageOps) {
                if (((OpEntry) packageOp.getOps().get(0)).getMode() != 2) {
                    String oldMockLocationApp = packageOp.getPackageName();
                    try {
                        appOpsManager.setMode(58, getActivity().getPackageManager().getApplicationInfo(oldMockLocationApp, 512).uid, oldMockLocationApp, 2);
                    } catch (NameNotFoundException e) {
                        Log.w("DevelopmentSettings", "writeMockLocation PackageName not fount:", e);
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(this.mMockLocationApp)) {
            try {
                appOpsManager.setMode(58, getActivity().getPackageManager().getApplicationInfo(this.mMockLocationApp, 512).uid, this.mMockLocationApp, 0);
            } catch (NameNotFoundException e2) {
                Log.w("DevelopmentSettings", "writeMockLocation packageName not found:", e2);
            }
        }
    }

    private static void resetDebuggerOptions() {
        try {
            ActivityManagerNative.getDefault().setDebugApp(null, false, true);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void updateDebuggerOptions() {
        boolean z;
        this.mDebugApp = Global.getString(getActivity().getContentResolver(), "debug_app");
        TwoStatePreference twoStatePreference = this.mWaitForDebugger;
        if (Global.getInt(getActivity().getContentResolver(), "wait_for_debugger", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        updateSwitchPreference(twoStatePreference, z);
        if (this.mDebugApp == null || this.mDebugApp.length() <= 0) {
            this.mDebugAppPref.setSummary(getResources().getString(2131624120));
            this.mWaitForDebugger.setEnabled(false);
        } else {
            String label;
            try {
                CharSequence lab = getActivity().getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(this.mDebugApp, 512));
                label = lab != null ? lab.toString() : this.mDebugApp;
            } catch (NameNotFoundException e) {
                label = this.mDebugApp;
            }
            this.mDebugAppPref.setSummary(getResources().getString(2131624121, new Object[]{label}));
            this.mWaitForDebugger.setEnabled(true);
            this.mHaveDebugSettings = true;
        }
        setStatusOfDebugAppPref(this.mDebugAppPref);
    }

    protected void updateMockLocation() {
        this.mMockLocationApp = "";
        List<PackageOps> packageOps = ((AppOpsManager) getSystemService("appops")).getPackagesForOps(MOCK_LOCATION_APP_OPS);
        if (packageOps != null) {
            for (PackageOps packageOp : packageOps) {
                if (((OpEntry) packageOp.getOps().get(0)).getMode() == 0) {
                    this.mMockLocationApp = ((PackageOps) packageOps.get(0)).getPackageName();
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(this.mMockLocationApp)) {
            this.mMockLocationAppPref.setSummary(getString(2131624081));
            return;
        }
        String label = this.mMockLocationApp;
        try {
            CharSequence appLabel = getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(this.mMockLocationApp, 512));
            if (appLabel != null) {
                label = appLabel.toString();
            }
        } catch (NameNotFoundException e) {
            Log.w("DevelopmentSettings", "updateMockLocation packageName not not found:", e);
        }
        this.mMockLocationAppPref.setSummary(getString(2131624082, new Object[]{label}));
        this.mHaveDebugSettings = true;
    }

    private void updateVerifyAppsOverUsbOptions() {
        boolean z = true;
        TwoStatePreference twoStatePreference = this.mVerifyAppsOverUsb;
        if (Global.getInt(getActivity().getContentResolver(), "verifier_verify_adb_installs", 1) == 0) {
            z = false;
        }
        updateSwitchPreference(twoStatePreference, z);
        this.mVerifyAppsOverUsb.setEnabled(enableVerifierSetting());
    }

    private void writeVerifyAppsOverUsbOptions(boolean isChecked) {
        Global.putInt(getActivity().getContentResolver(), "verifier_verify_adb_installs", isChecked ? 1 : 0);
    }

    private void updateOtaDisableAutomaticUpdateOptions() {
        boolean z = true;
        TwoStatePreference twoStatePreference = this.mOtaDisableAutomaticUpdate;
        if (Global.getInt(getActivity().getContentResolver(), "ota_disable_automatic_update", 0) == 1) {
            z = false;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeOtaDisableAutomaticUpdateOptions(boolean isChecked) {
        Global.putInt(getActivity().getContentResolver(), "ota_disable_automatic_update", isChecked ? 0 : 1);
    }

    private boolean enableVerifierSetting() {
        ContentResolver cr = getActivity().getContentResolver();
        if (Global.getInt(cr, "adb_enabled", 0) == 0 || Global.getInt(cr, "package_verifier_enable", 1) == 0) {
            return false;
        }
        PackageManager pm = getActivity().getPackageManager();
        Intent verification = new Intent("android.intent.action.PACKAGE_NEEDS_VERIFICATION");
        verification.setType("application/vnd.android.package-archive");
        verification.addFlags(1);
        return pm.queryBroadcastReceivers(verification, 0).size() != 0;
    }

    private boolean showVerifierSetting() {
        return Global.getInt(getActivity().getContentResolver(), "verifier_setting_visible", 1) > 0;
    }

    private static boolean showEnableOemUnlockPreference() {
        return !SystemProperties.get("ro.frp.pst").equals("");
    }

    private boolean enableOemUnlockPreference() {
        int flashLockState = -1;
        if (this.mOemUnlockManager != null) {
            flashLockState = this.mOemUnlockManager.getFlashLockState();
        }
        if (flashLockState != 0) {
            return true;
        }
        return false;
    }

    private void updateOemUnlockOptions() {
        if (this.mEnableOemUnlock != null) {
            this.mEnableOemUnlock.setEnabled(enableOemUnlockPreference());
        }
    }

    private void updateBugreportOptions() {
        this.mBugreport.setEnabled(true);
    }

    private static int currentStrictModeActiveIndex() {
        if (TextUtils.isEmpty(SystemProperties.get("persist.sys.strictmode.visual"))) {
            return 0;
        }
        return SystemProperties.getBoolean("persist.sys.strictmode.visual", false) ? 1 : 2;
    }

    private void writeStrictModeVisualOptions(boolean isChecked) {
        try {
            this.mWindowManager.setStrictModeVisualIndicatorPreference(isChecked ? "1" : "");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateStrictModeVisualOptions() {
        boolean z = true;
        TwoStatePreference twoStatePreference = this.mStrictMode;
        if (currentStrictModeActiveIndex() != 1) {
            z = false;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writePointerLocationOptions(boolean isChecked) {
        System.putInt(getActivity().getContentResolver(), "pointer_location", isChecked ? 1 : 0);
    }

    private void updatePointerLocationOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mPointerLocation;
        if (System.getInt(getActivity().getContentResolver(), "pointer_location", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeShowTouchesOptions(boolean isChecked) {
        System.putInt(getActivity().getContentResolver(), "show_touches", isChecked ? 1 : 0);
    }

    private void updateShowTouchesOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mShowTouches;
        if (System.getInt(getActivity().getContentResolver(), "show_touches", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void updateFlingerOptions() {
        boolean z = true;
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                boolean z2;
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(1010, data, reply, 0);
                int showCpu = reply.readInt();
                int enableGL = reply.readInt();
                int showUpdates = reply.readInt();
                TwoStatePreference twoStatePreference = this.mShowScreenUpdates;
                if (showUpdates != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                updateSwitchPreference(twoStatePreference, z2);
                int showBackground = reply.readInt();
                int disableOverlays = reply.readInt();
                TwoStatePreference twoStatePreference2 = this.mDisableOverlays;
                if (disableOverlays == 0) {
                    z = false;
                }
                updateSwitchPreference(twoStatePreference2, z);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void writeShowUpdatesOption(boolean isChecked) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(isChecked ? 1 : 0);
                flinger.transact(1002, data, null, 0);
                data.recycle();
                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void writeDisableOverlaysOption(boolean isChecked) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(isChecked ? 1 : 0);
                flinger.transact(1008, data, null, 0);
                data.recycle();
                updateFlingerOptions();
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void updateHardwareUiOptions() {
        updateSwitchPreference(this.mForceHardwareUi, SystemProperties.getBoolean("persist.sys.ui.hw", false));
    }

    private void writeHardwareUiOptions(boolean isChecked) {
        SystemProperties.set("persist.sys.ui.hw", isChecked ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateMsaaOptions() {
        updateSwitchPreference(this.mForceMsaa, SystemProperties.getBoolean("debug.egl.force_msaa", false));
    }

    private void writeMsaaOptions(boolean isChecked) {
        SystemProperties.set("debug.egl.force_msaa", isChecked ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateTrackFrameTimeOptions() {
        String value = SystemProperties.get("debug.hwui.profile");
        if (value == null) {
            value = "";
        }
        CharSequence[] values = this.mTrackFrameTime.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                this.mTrackFrameTime.setValueIndex(i);
                this.mTrackFrameTime.setSummary(this.mTrackFrameTime.getEntries()[i]);
                return;
            }
        }
        this.mTrackFrameTime.setValueIndex(0);
        this.mTrackFrameTime.setSummary(this.mTrackFrameTime.getEntries()[0]);
    }

    private void writeTrackFrameTimeOptions(Object newValue) {
        SystemProperties.set("debug.hwui.profile", newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateTrackFrameTimeOptions();
    }

    private void updateShowNonRectClipOptions() {
        String value = SystemProperties.get("debug.hwui.show_non_rect_clip");
        if (value == null) {
            value = "hide";
        }
        CharSequence[] values = this.mShowNonRectClip.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                this.mShowNonRectClip.setValueIndex(i);
                this.mShowNonRectClip.setSummary(this.mShowNonRectClip.getEntries()[i]);
                return;
            }
        }
        this.mShowNonRectClip.setValueIndex(0);
        this.mShowNonRectClip.setSummary(this.mShowNonRectClip.getEntries()[0]);
    }

    private void writeShowNonRectClipOptions(Object newValue) {
        SystemProperties.set("debug.hwui.show_non_rect_clip", newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateShowNonRectClipOptions();
    }

    private void updateShowHwScreenUpdatesOptions() {
        updateSwitchPreference(this.mShowHwScreenUpdates, SystemProperties.getBoolean("debug.hwui.show_dirty_regions", false));
    }

    private void writeShowHwScreenUpdatesOptions(boolean isChecked) {
        SystemProperties.set("debug.hwui.show_dirty_regions", isChecked ? "true" : null);
        pokeSystemProperties();
    }

    private void updateShowHwLayersUpdatesOptions() {
        updateSwitchPreference(this.mShowHwLayersUpdates, SystemProperties.getBoolean("debug.hwui.show_layers_updates", false));
    }

    private void writeShowHwLayersUpdatesOptions(boolean isChecked) {
        SystemProperties.set("debug.hwui.show_layers_updates", isChecked ? "true" : null);
        pokeSystemProperties();
    }

    private void updateDebugHwOverdrawOptions() {
        String value = SystemProperties.get("debug.hwui.overdraw");
        if (value == null) {
            value = "";
        }
        CharSequence[] values = this.mDebugHwOverdraw.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                this.mDebugHwOverdraw.setValueIndex(i);
                this.mDebugHwOverdraw.setSummary(this.mDebugHwOverdraw.getEntries()[i]);
                return;
            }
        }
        this.mDebugHwOverdraw.setValueIndex(0);
        this.mDebugHwOverdraw.setSummary(this.mDebugHwOverdraw.getEntries()[0]);
    }

    private void writeDebugHwOverdrawOptions(Object newValue) {
        SystemProperties.set("debug.hwui.overdraw", newValue == null ? "" : newValue.toString());
        pokeSystemProperties();
        updateDebugHwOverdrawOptions();
    }

    private void updateDebugLayoutOptions() {
        updateSwitchPreference(this.mDebugLayout, SystemProperties.getBoolean("debug.layout", false));
    }

    private void writeDebugLayoutOptions(boolean isChecked) {
        SystemProperties.set("debug.layout", isChecked ? "true" : "false");
        pokeSystemProperties();
    }

    private void updateSimulateColorSpace() {
        boolean enabled;
        ContentResolver cr = getContentResolver();
        if (Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0) != 0) {
            enabled = true;
        } else {
            enabled = false;
        }
        if (enabled) {
            String mode = Integer.toString(Secure.getInt(cr, "accessibility_display_daltonizer", -1));
            this.mSimulateColorSpace.setValue(mode);
            if (this.mSimulateColorSpace.findIndexOfValue(mode) < 0) {
                this.mSimulateColorSpace.setSummary(getString(2131624209, new Object[]{getString(2131624207)}));
                return;
            }
            this.mSimulateColorSpace.setSummary("%s");
            return;
        }
        this.mSimulateColorSpace.setValue(Integer.toString(-1));
        this.mSimulateColorSpace.setSummary("%s");
    }

    private boolean usingDevelopmentColorSpace() {
        boolean enabled;
        ContentResolver cr = getContentResolver();
        if (Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0) != 0) {
            enabled = true;
        } else {
            enabled = false;
        }
        if (enabled) {
            if (this.mSimulateColorSpace.findIndexOfValue(Integer.toString(Secure.getInt(cr, "accessibility_display_daltonizer", -1))) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void writeSimulateColorSpace(Object value) {
        ContentResolver cr = getContentResolver();
        int newMode = Integer.parseInt(value.toString());
        if (newMode < 0) {
            Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 0);
            Secure.putInt(cr, "accessibility_display_daltonizer", -1);
            return;
        }
        Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1);
        Secure.putInt(cr, "accessibility_display_daltonizer", newMode);
    }

    private void updateColorTemperature() {
        updateSwitchPreference(this.mColorTemperaturePreference, SystemProperties.getBoolean("persist.sys.debug.color_temp", false));
    }

    private void writeColorTemperature() {
        SystemProperties.set("persist.sys.debug.color_temp", this.mColorTemperaturePreference.isChecked() ? "1" : "0");
        pokeSystemProperties();
        Toast.makeText(getActivity(), 2131627133, 1).show();
    }

    private void updateUSBAudioOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mUSBAudio;
        if (Secure.getInt(getContentResolver(), "usb_audio_automatic_routing_disabled", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeUSBAudioOptions(boolean isChecked) {
        Secure.putInt(getContentResolver(), "usb_audio_automatic_routing_disabled", isChecked ? 1 : 0);
    }

    private void updateForceResizableOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mForceResizable;
        if (Global.getInt(getContentResolver(), "force_resizable_activities", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeForceResizableOptions(boolean isChecked) {
        Global.putInt(getContentResolver(), "force_resizable_activities", isChecked ? 1 : 0);
    }

    private void updateWifiDisplayCertificationOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mWifiDisplayCertification;
        if (Global.getInt(getActivity().getContentResolver(), "wifi_display_certification_on", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeWifiDisplayCertificationOptions(boolean isChecked) {
        Global.putInt(getActivity().getContentResolver(), "wifi_display_certification_on", isChecked ? 1 : 0);
    }

    private void updateWifiVerboseLoggingOptions() {
        updateSwitchPreference(this.mWifiVerboseLogging, this.mWifiManager.getVerboseLoggingLevel() > 0);
    }

    private void writeWifiVerboseLoggingOptions(boolean isChecked) {
        this.mWifiManager.enableVerboseLogging(isChecked ? 1 : 0);
    }

    private void updateWifiAggressiveHandoverOptions() {
        updateSwitchPreference(this.mWifiAggressiveHandover, this.mWifiManager.getAggressiveHandover() > 0);
    }

    private void writeWifiAggressiveHandoverOptions(boolean isChecked) {
        this.mWifiManager.enableAggressiveHandover(isChecked ? 1 : 0);
    }

    private void updateWifiAllowScansWithTrafficOptions() {
        updateSwitchPreference(this.mWifiAllowScansWithTraffic, this.mWifiManager.getAllowScansWithTraffic() > 0);
    }

    private void writeWifiAllowScansWithTrafficOptions(boolean isChecked) {
        this.mWifiManager.setAllowScansWithTraffic(isChecked ? 1 : 0);
    }

    private void updateBluetoothDisableAbsVolumeOptions() {
        updateSwitchPreference(this.mBluetoothDisableAbsVolume, SystemProperties.getBoolean("persist.bluetooth.disableabsvol", false));
    }

    private void writeBluetoothDisableAbsVolumeOptions(boolean isChecked) {
        SystemProperties.set("persist.bluetooth.disableabsvol", isChecked ? "true" : "false");
    }

    private void updateMobileDataAlwaysOnOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mMobileDataAlwaysOn;
        if (Global.getInt(getActivity().getContentResolver(), "mobile_data_always_on", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeMobileDataAlwaysOnOptions() {
        Global.putInt(getActivity().getContentResolver(), "mobile_data_always_on", this.mMobileDataAlwaysOn.isChecked() ? 1 : 0);
    }

    private String defaultLogdSizeValue() {
        String defaultValue = SystemProperties.get("ro.logd.size");
        if (defaultValue != null && defaultValue.length() != 0) {
            return defaultValue;
        }
        if (SystemProperties.get("ro.config.low_ram").equals("true")) {
            return "65536";
        }
        return "262144";
    }

    private void updateLogdSizeValues() {
        if (this.mLogdSize != null) {
            String currentTag = SystemProperties.get("persist.log.tag");
            String currentValue = SystemProperties.get("persist.logd.size");
            if (currentTag != null && currentTag.startsWith("Settings")) {
                currentValue = "32768";
            }
            if (currentValue == null || currentValue.length() == 0) {
                currentValue = defaultLogdSizeValue();
            }
            String[] values = getResources().getStringArray(2131361799);
            CharSequence[] titles = buildSelectLogdSizeTitles(getActivity());
            int index = 2;
            if (SystemProperties.get("ro.config.low_ram").equals("true")) {
                titles = buildSelectLogdSizeLowramTitles(getActivity());
                index = 1;
            }
            this.mLogdSize.setEntries(titles);
            String[] summaries = buildSelectLogdSizeSummaries(getActivity());
            int i = 0;
            while (i < titles.length) {
                if (currentValue.equals(values[i]) || currentValue.equals(titles[i])) {
                    index = i;
                    break;
                }
                i++;
            }
            this.mLogdSize.setValue(values[index]);
            this.mLogdSize.setSummary(summaries[index]);
            this.mLogdSize.setOnPreferenceChangeListener(this);
        }
    }

    protected void writeLogdSizeOption(Object newValue) {
        boolean equals;
        if (newValue != null) {
            equals = newValue.toString().equals("32768");
        } else {
            equals = false;
        }
        String currentTag = SystemProperties.get("persist.log.tag");
        if (currentTag == null) {
            currentTag = "";
        }
        String newTag = currentTag.replaceAll(",+Settings", "").replaceFirst("^Settings,*", "").replaceAll(",+", ",").replaceFirst(",+$", "");
        if (equals) {
            newValue = "65536";
            String snetValue = SystemProperties.get("persist.log.tag.snet_event_log");
            if (snetValue == null || snetValue.length() == 0) {
                snetValue = SystemProperties.get("log.tag.snet_event_log");
                if (snetValue == null || snetValue.length() == 0) {
                    SystemProperties.set("persist.log.tag.snet_event_log", "I");
                }
            }
            if (newTag.length() != 0) {
                newTag = "," + newTag;
            }
            newTag = "Settings" + newTag;
        }
        if (!newTag.equals(currentTag)) {
            SystemProperties.set("persist.log.tag", newTag);
        }
        String defaultValue = defaultLogdSizeValue();
        String size = (newValue == null || newValue.toString().length() == 0) ? defaultValue : newValue.toString();
        String str = "persist.logd.size";
        if (defaultValue.equals(size)) {
            size = "";
        }
        SystemProperties.set(str, size);
        SystemProperties.set("ctl.start", "logd-reinit");
        pokeSystemProperties();
        updateLogdSizeValues();
    }

    private void updateUsbConfigurationValues() {
        if (this.mUsbConfiguration != null) {
            UsbManager manager = (UsbManager) getSystemService("usb");
            SystemClock.sleep(100);
            String[] values = getResources().getStringArray(2131361820);
            String[] titles = getResources().getStringArray(2131361819);
            int index = 0;
            for (int i = 0; i < titles.length; i++) {
                if (manager.isFunctionEnabled(values[i])) {
                    index = i;
                    break;
                }
            }
            this.mUsbConfiguration.setValue(values[index]);
            this.mUsbConfiguration.setSummary(titles[index]);
            this.mUsbConfiguration.setOnPreferenceChangeListener(this);
        }
    }

    private void writeUsbConfigurationOption(Object newValue) {
        UsbManager manager = (UsbManager) getActivity().getSystemService("usb");
        String function = newValue.toString();
        manager.setCurrentFunction(function);
        if (function.equals("none")) {
            manager.setUsbDataUnlocked(false);
        } else {
            manager.setUsbDataUnlocked(true);
        }
    }

    private void updateCpuUsageOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mShowCpuUsage;
        if (Global.getInt(getActivity().getContentResolver(), "show_processes", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void writeCpuUsageOptions(boolean isChecked) {
        Global.putInt(getActivity().getContentResolver(), "show_processes", isChecked ? 1 : 0);
        Intent service = new Intent().setClassName("com.android.systemui", "com.android.systemui.LoadAverageService");
        if (isChecked) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
    }

    private void writeImmediatelyDestroyActivitiesOptions(boolean isChecked) {
        try {
            ActivityManagerNative.getDefault().setAlwaysFinish(isChecked);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private void updateImmediatelyDestroyActivitiesOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mImmediatelyDestroyActivities;
        if (Global.getInt(getActivity().getContentResolver(), "always_finish_activities", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void updateAnimationScaleValue(int which, ListPreference pref) {
        try {
            float scale = this.mWindowManager.getAnimationScale(which);
            if (scale != 1.0f) {
                this.mHaveDebugSettings = true;
            }
            CharSequence[] values = pref.getEntryValues();
            for (int i = 0; i < values.length; i++) {
                if (scale <= Float.parseFloat(values[i].toString())) {
                    pref.setValueIndex(i);
                    pref.setSummary(pref.getEntries()[i]);
                    return;
                }
            }
            pref.setValueIndex(values.length - 1);
            pref.setSummary(pref.getEntries()[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateAnimationScaleOptions() {
        updateAnimationScaleValue(0, this.mWindowAnimationScale);
        updateAnimationScaleValue(1, this.mTransitionAnimationScale);
        updateAnimationScaleValue(2, this.mAnimatorDurationScale);
    }

    private void writeAnimationScaleOption(int which, ListPreference pref, Object newValue) {
        float scale;
        if (newValue != null) {
            try {
                scale = Float.parseFloat(newValue.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        scale = 1.0f;
        this.mWindowManager.setAnimationScale(which, scale);
        updateAnimationScaleValue(which, pref);
    }

    private void updateOverlayDisplayDevicesOptions() {
        String value = Global.getString(getActivity().getContentResolver(), "overlay_display_devices");
        if (value == null) {
            value = "";
        }
        CharSequence[] values = this.mOverlayDisplayDevices.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (value.contentEquals(values[i])) {
                this.mOverlayDisplayDevices.setValueIndex(i);
                this.mOverlayDisplayDevices.setSummary(this.mOverlayDisplayDevices.getEntries()[i]);
                return;
            }
        }
        this.mOverlayDisplayDevices.setValueIndex(0);
        this.mOverlayDisplayDevices.setSummary(this.mOverlayDisplayDevices.getEntries()[0]);
    }

    private void writeOverlayDisplayDevicesOptions(Object newValue) {
        Global.putString(getActivity().getContentResolver(), "overlay_display_devices", (String) newValue);
        updateOverlayDisplayDevicesOptions();
    }

    private void updateAppProcessLimitOptions() {
        try {
            int limit = ActivityManagerNative.getDefault().getProcessLimit();
            CharSequence[] values = this.mAppProcessLimit.getEntryValues();
            for (int i = 0; i < values.length; i++) {
                if (Integer.parseInt(values[i].toString()) >= limit) {
                    if (i != 0) {
                        this.mHaveDebugSettings = true;
                    }
                    this.mAppProcessLimit.setValueIndex(i);
                    this.mAppProcessLimit.setSummary(this.mAppProcessLimit.getEntries()[i]);
                    return;
                }
            }
            this.mAppProcessLimit.setValueIndex(0);
            this.mAppProcessLimit.setSummary(this.mAppProcessLimit.getEntries()[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void writeAppProcessLimitOptions(Object newValue) {
        int limit;
        if (newValue != null) {
            try {
                limit = Integer.parseInt(newValue.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        limit = -1;
        ActivityManagerNative.getDefault().setProcessLimit(limit);
        updateAppProcessLimitOptions();
    }

    private void writeShowAllANRsOptions(boolean isChecked) {
        Secure.putInt(getActivity().getContentResolver(), "anr_show_background", isChecked ? 1 : 0);
    }

    private void updateShowAllANRsOptions() {
        boolean z = false;
        TwoStatePreference twoStatePreference = this.mShowAllANRs;
        if (Secure.getInt(getActivity().getContentResolver(), "anr_show_background", 0) != 0) {
            z = true;
        }
        updateSwitchPreference(twoStatePreference, z);
    }

    private void confirmEnableOemUnlock() {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    Utils.setOemUnlockEnabled(DevelopmentSettings.this.getActivity(), true);
                }
            }
        };
        new Builder(getActivity()).setTitle(2131624078).setMessage(2131624079).setPositiveButton(2131625620, onClickListener).setNegativeButton(17039360, null).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (DevelopmentSettings.this.getActivity() != null) {
                    DevelopmentSettings.this.updateAllOptions();
                }
            }
        }).create().show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == -1) {
                this.mDebugApp = data.getAction();
                writeDebuggerOptions(this.mWaitForDebugger.isChecked());
                updateDebuggerOptions();
            }
        } else if (requestCode == 1001) {
            if (resultCode == -1) {
                this.mMockLocationApp = data.getAction();
                writeMockLocation();
                updateMockLocation();
            }
        } else if (requestCode != 0) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode != -1) {
        } else {
            if (this.mEnableOemUnlock.isChecked()) {
                confirmEnableOemUnlock();
            } else {
                Utils.setOemUnlockEnabled(getActivity(), false);
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (Utils.isMonkeyRunning()) {
            Log.e("DevelopmentSettings", "onPreferenceTreeClick()-->is in Monkey, return");
            return false;
        }
        if (preference != this.mBugreport) {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        }
        if (preference == this.mClearAdbKeys) {
            if (this.mAdbKeysDialog != null) {
                dismissDialogs();
            }
            this.mAdbKeysDialog = new Builder(getActivity()).setTitle(2131624069).setMessage(2131624108).setPositiveButton(17039370, this).setNegativeButton(17039360, null).show();
        } else if (preference == this.mDebugAppPref) {
            intent = new Intent(getActivity(), AppPicker.class);
            intent.putExtra("com.android.settings.extra.DEBUGGABLE", true);
            startActivityForResult(intent, 1000);
        } else if ("inactive_apps".equals(preference.getKey())) {
            startInactiveAppsFragment();
        } else if (preference != this.mMockLocationAppPref) {
            return super.onPreferenceTreeClick(preference);
        } else {
            intent = new Intent(getActivity(), AppPicker.class);
            intent.putExtra("com.android.settings.extra.REQUESTIING_PERMISSION", "android.permission.ACCESS_MOCK_LOCATION");
            startActivityForResult(intent, 1001);
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        int i2 = 1;
        if (Utils.isMonkeyRunning()) {
            Log.e("DevelopmentSettings", "onPreferenceChange()-->is in Monkey, return");
            return false;
        }
        boolean z = false;
        if (preference instanceof TwoStatePreference) {
            z = ((Boolean) newValue).booleanValue();
            if (!(preference == this.mEnableAdb || preference == this.mEnabledSwitch)) {
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            }
        }
        if (preference == this.mEnabledSwitch) {
            if (z != this.mLastEnabledState) {
                if (z) {
                    this.mDialogClicked = false;
                    if (this.mEnableDialog != null) {
                        dismissDialogs();
                    }
                    this.mEnableDialog = new Builder(getActivity()).setMessage(getActivity().getResources().getString(2131624110)).setTitle(2131624109).setIconAttribute(16843605).setPositiveButton(17039379, this).setNegativeButton(17039369, this).show();
                    this.mEnableDialog.setOnDismissListener(this);
                } else {
                    resetDangerousOptions();
                    Global.putInt(getActivity().getContentResolver(), "development_settings_enabled", 0);
                    this.mLastEnabledState = z;
                    setPrefsEnabledState(this.mLastEnabledState);
                    ItemUseStat.getInstance().handleClick(getActivity(), 3, this.mEnabledSwitch.getKey(), "off");
                }
            }
            return true;
        } else if (preference == this.mWebViewProvider) {
            if (newValue == null) {
                Log.e("DevelopmentSettings", "Tried to set a null WebView provider");
                return false;
            } else if (writeWebViewProviderOptions(newValue)) {
                return true;
            } else {
                Toast.makeText(getActivity(), 2131624193, 0).show();
                return false;
            }
        } else if (preference == this.mEnableAdb) {
            if (!z) {
                Global.putInt(getActivity().getContentResolver(), "adb_enabled", 0);
                this.mVerifyAppsOverUsb.setEnabled(false);
                this.mVerifyAppsOverUsb.setChecked(false);
                updateBugreportOptions();
                ItemUseStat.getInstance().handleClick(getActivity(), 3, this.mEnableAdb.getKey(), "off");
            } else if (isSupportUsbLimit()) {
                return startSimUsbLimitActivity(getActivity());
            } else {
                createAdbDialog();
            }
            return true;
        } else if (preference == this.mAllowCharingAdb) {
            if (z) {
                createAllowChargingAdbDialog();
            } else {
                Global.putInt(getActivity().getContentResolver(), "allow_charging_adb", 0);
            }
            return true;
        } else if (preference == this.mUsbConnPrompt) {
            ContentResolver contentResolver = getActivity().getContentResolver();
            r7 = "usb_conn_prompt";
            if (z) {
                r3 = 1;
            } else {
                r3 = 0;
            }
            Secure.putInt(contentResolver, r7, r3);
            return true;
        } else if (preference == this.mEnableTerminal) {
            PackageManager pm = getActivity().getPackageManager();
            r6 = "com.android.terminal";
            if (z) {
                r3 = 1;
            } else {
                r3 = 0;
            }
            pm.setApplicationEnabledSetting(r6, r3, 0);
            return true;
        } else if (preference == this.mKeepScreenOn) {
            r3 = getActivity().getContentResolver();
            r7 = "stay_on_while_plugged_in";
            if (z) {
                i = 3;
            }
            Global.putInt(r3, r7, i);
            return true;
        } else if (preference == this.mBtHciSnoopLog) {
            writeBtHciSnoopLogOptions(z);
            return true;
        } else if (preference == this.mEnableOemUnlock && this.mEnableOemUnlock.isEnabled()) {
            if (!z) {
                Utils.setOemUnlockEnabled(getActivity(), false);
            } else if (!showKeyguardConfirmation(getResources(), 0)) {
                confirmEnableOemUnlock();
            }
            return true;
        } else {
            if (preference == this.mDebugViewAttributes) {
                r3 = getActivity().getContentResolver();
                r6 = "debug_view_attributes";
                if (!z) {
                    i2 = 0;
                }
                Global.putInt(r3, r6, i2);
            } else if (preference == this.mForceAllowOnExternal) {
                r3 = getActivity().getContentResolver();
                r6 = "force_allow_on_external";
                if (!this.mForceAllowOnExternal.isChecked()) {
                    i2 = 0;
                }
                Global.putInt(r3, r6, i2);
            } else if (preference == this.mWaitForDebugger) {
                writeDebuggerOptions(z);
                return true;
            } else if (preference == this.mVerifyAppsOverUsb) {
                writeVerifyAppsOverUsbOptions(z);
                return true;
            } else if (preference == this.mOtaDisableAutomaticUpdate) {
                writeOtaDisableAutomaticUpdateOptions(z);
                return true;
            } else if (preference == this.mStrictMode) {
                writeStrictModeVisualOptions(z);
                return true;
            } else if (preference == this.mPointerLocation) {
                writePointerLocationOptions(z);
                return true;
            } else if (preference == this.mShowTouches) {
                writeShowTouchesOptions(z);
                return true;
            } else if (preference == this.mShowScreenUpdates) {
                writeShowUpdatesOption(z);
                return true;
            } else if (preference == this.mDisableOverlays) {
                writeDisableOverlaysOption(z);
                return true;
            } else if (preference == this.mShowCpuUsage) {
                writeCpuUsageOptions(z);
                return true;
            } else if (preference == this.mImmediatelyDestroyActivities) {
                writeImmediatelyDestroyActivitiesOptions(z);
                return true;
            } else if (preference == this.mShowAllANRs) {
                writeShowAllANRsOptions(z);
                return true;
            } else if (preference == this.mForceHardwareUi) {
                writeHardwareUiOptions(z);
                return true;
            } else if (preference == this.mForceMsaa) {
                writeMsaaOptions(z);
                return true;
            } else if (preference == this.mShowHwScreenUpdates) {
                writeShowHwScreenUpdatesOptions(z);
                return true;
            } else if (preference == this.mShowHwLayersUpdates) {
                writeShowHwLayersUpdatesOptions(z);
                return true;
            } else if (preference == this.mDebugLayout) {
                writeDebugLayoutOptions(z);
                return true;
            } else if (preference == this.mWifiDisplayCertification) {
                writeWifiDisplayCertificationOptions(z);
                return true;
            } else if (preference == this.mWifiVerboseLogging) {
                writeWifiVerboseLoggingOptions(z);
                return true;
            } else if (preference == this.mWifiAggressiveHandover) {
                writeWifiAggressiveHandoverOptions(z);
                return true;
            } else if (preference == this.mWifiAllowScansWithTraffic) {
                writeWifiAllowScansWithTrafficOptions(z);
                return true;
            } else if (preference == this.mMobileDataAlwaysOn) {
                this.mMobileDataAlwaysOn.setChecked(z);
                writeMobileDataAlwaysOnOptions();
            } else if (preference == this.mColorTemperaturePreference) {
                writeColorTemperature();
            } else if (preference == this.mUSBAudio) {
                writeUSBAudioOptions(z);
                return true;
            } else if (preference == this.mForceResizable) {
                writeForceResizableOptions(z);
                return true;
            } else if ("background_check".equals(preference.getKey())) {
                startBackgroundCheckFragment();
                return true;
            } else if (preference == this.mBluetoothDisableAbsVolume) {
                writeBluetoothDisableAbsVolumeOptions(z);
                return true;
            } else if (preference == this.mWebViewMultiprocess) {
                writeWebViewMultiprocessOptions(z);
                return true;
            } else if ("reset_shortcut_manager_throttling".equals(preference.getKey())) {
                confirmResetShortcutManagerThrottling();
                return true;
            } else if ("hdcp_checking".equals(preference.getKey())) {
                SystemProperties.set("persist.sys.hdcp_checking", newValue.toString());
                updateHdcpValues();
                pokeSystemProperties();
                return true;
            } else if (preference == this.mLogdSize) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mLogdSize, ItemUseStat.KEY_LOGGER_BUFFER_SIZE, (String) newValue);
                writeLogdSizeOption(newValue);
                return true;
            } else if (preference == this.mUsbConfiguration) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mUsbConfiguration, ItemUseStat.KEY_SELECT_USB_CONFIGURATION, (String) newValue);
                writeUsbConfigurationOption(newValue);
                return true;
            } else if (preference == this.mWindowAnimationScale) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mWindowAnimationScale, ItemUseStat.KEY_WINDOW_ANIMATION_SCALE, (String) newValue);
                writeAnimationScaleOption(0, this.mWindowAnimationScale, newValue);
                return true;
            } else if (preference == this.mTransitionAnimationScale) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mTransitionAnimationScale, ItemUseStat.KEY_TRANSLATION_ANIMATION_SCALE, (String) newValue);
                writeAnimationScaleOption(1, this.mTransitionAnimationScale, newValue);
                return true;
            } else if (preference == this.mAnimatorDurationScale) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mAnimatorDurationScale, ItemUseStat.KEY_ANIMATOR_DURATION_SCALE, (String) newValue);
                writeAnimationScaleOption(2, this.mAnimatorDurationScale, newValue);
                return true;
            } else if (preference == this.mOverlayDisplayDevices) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mOverlayDisplayDevices, ItemUseStat.KEY_SIMULATE_SECONDARY_DISPLAYS, (String) newValue);
                writeOverlayDisplayDevicesOptions(newValue);
                return true;
            } else if (preference == this.mTrackFrameTime) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mTrackFrameTime, ItemUseStat.KEY_PROFILE_GPU_RENDORING, (String) newValue);
                writeTrackFrameTimeOptions(newValue);
                return true;
            } else if (preference == this.mDebugHwOverdraw) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mDebugHwOverdraw, ItemUseStat.KEY_DEBUG_GPU, (String) newValue);
                writeDebugHwOverdrawOptions(newValue);
                return true;
            } else if (preference == this.mShowNonRectClip) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mShowNonRectClip, ItemUseStat.KEY_DEBUG_NON_RECTANGULAR_CLIP, (String) newValue);
                writeShowNonRectClipOptions(newValue);
                return true;
            } else if (preference == this.mAppProcessLimit) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mAppProcessLimit, ItemUseStat.KEY_BACKGROUND_PROCESS_LIMIT, (String) newValue);
                writeAppProcessLimitOptions(newValue);
                return true;
            } else if (preference == this.mSimulateColorSpace) {
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mSimulateColorSpace, ItemUseStat.KEY_SIMULATE_COLOR_SPACE, (String) newValue);
                writeSimulateColorSpace(newValue);
                updateSimulateColorSpace();
                return true;
            } else if (preference == this.mAdbInstallNeedConfirm) {
                r3 = getContentResolver();
                r6 = "adb_install_need_confirm";
                if (z) {
                    i = 1;
                }
                Secure.putInt(r3, r6, i);
                return true;
            } else if ("smart_backlight".equals(preference.getKey())) {
                r3 = getContentResolver();
                r6 = "smart_backlight_enable";
                if (z) {
                    i = 1;
                }
                System.putIntForUser(r3, r6, i, -2);
                return true;
            }
            return super.onPreferenceChange(preference, newValue);
        }
    }

    private void startBackgroundCheckFragment() {
        ((SettingsActivity) getActivity()).startPreferencePanel(BackgroundCheckSummary.class.getName(), null, 2131627006, null, null, 0);
    }

    private boolean showKeyguardConfirmation(Resources resources, int requestCode) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(requestCode, resources.getString(2131624076));
    }

    private void startInactiveAppsFragment() {
        ((SettingsActivity) getActivity()).startPreferencePanel(InactiveApps.class.getName(), null, 2131624184, null, null, 0);
    }

    private void dismissDialogs() {
        if (this.mAdbDialog != null) {
            this.mAdbDialog.dismiss();
            this.mAdbDialog = null;
        }
        if (this.mAdbKeysDialog != null) {
            this.mAdbKeysDialog.dismiss();
            this.mAdbKeysDialog = null;
        }
        if (this.mEnableDialog != null) {
            this.mEnableDialog.dismiss();
            this.mEnableDialog = null;
        }
        if (this.mAllowChargingAdbDialog != null) {
            this.mAllowChargingAdbDialog.dismiss();
            this.mAllowChargingAdbDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == this.mAdbDialog) {
            if (which == -1) {
                this.mDialogClicked = true;
                Global.putInt(getActivity().getContentResolver(), "adb_enabled", 1);
                this.mVerifyAppsOverUsb.setEnabled(true);
                updateVerifyAppsOverUsbOptions();
                updateBugreportOptions();
                ItemUseStat.getInstance().handleClick(getActivity(), 3, this.mEnableAdb.getKey(), "on");
                return;
            }
            this.mEnableAdb.setChecked(false);
        } else if (dialog == this.mAdbKeysDialog) {
            if (which == -1) {
                try {
                    IUsbManager.Stub.asInterface(ServiceManager.getService("usb")).clearUsbDebuggingKeys();
                } catch (RemoteException e) {
                    Log.e("DevelopmentSettings", "Unable to clear adb keys", e);
                }
            }
        } else if (dialog == this.mEnableDialog) {
            if (which == -1) {
                this.mDialogClicked = true;
                Global.putInt(getActivity().getContentResolver(), "development_settings_enabled", 1);
                this.mLastEnabledState = true;
                setPrefsEnabledState(this.mLastEnabledState);
                ItemUseStat.getInstance().handleClick(getActivity(), 3, this.mEnabledSwitch.getKey(), "on");
                return;
            }
            this.mEnabledSwitch.setChecked(false);
        } else if (dialog != this.mAllowChargingAdbDialog) {
        } else {
            if (which == -1) {
                Global.putInt(getActivity().getContentResolver(), "allow_charging_adb", 1);
                this.mAllowCharingAdb.setChecked(true);
                return;
            }
            Global.putInt(getActivity().getContentResolver(), "allow_charging_adb", 0);
            this.mAllowCharingAdb.setChecked(false);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (dialog == this.mAdbDialog) {
            if (!this.mDialogClicked) {
                this.mEnableAdb.setChecked(false);
            }
            this.mAdbDialog = null;
        } else if (dialog == this.mEnableDialog) {
            if (!this.mDialogClicked) {
                this.mEnabledSwitch.setChecked(false);
            }
            this.mEnableDialog = null;
        }
    }

    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();
    }

    void pokeSystemProperties() {
        if (!this.mDontPokeProperties) {
            new SystemPropPoker().execute(new Void[0]);
        }
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        boolean z = false;
        try {
            if (context.getPackageManager().getPackageInfo(packageName, 0) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.i("DevelopmentSettings", "isPackageInstalled packageName not found:", e);
            return false;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mEnableDialog != null && this.mEnableDialog.isShowing()) {
            outState.putBoolean("com.android.settings.Enable_Dialog", true);
        }
        if (this.mAdbDialog != null && this.mAdbDialog.isShowing()) {
            outState.putBoolean("com.android.settings.Adb_Dialog", true);
        }
    }

    protected void createAdbDialog() {
        this.mDialogClicked = false;
        if (this.mAdbDialog != null) {
            dismissDialogs();
        }
        this.mAdbDialog = new Builder(getActivity()).setMessage(getActivity().getResources().getString(2131624107)).setTitle(2131624106).setIconAttribute(16843605).setPositiveButton(17039379, this).setNegativeButton(17039369, this).show();
        this.mAdbDialog.setOnDismissListener(this);
    }

    protected void createAllowChargingAdbDialog() {
        this.mDialogClicked = false;
        if (this.mAllowChargingAdbDialog != null) {
            dismissDialogs();
        }
        this.mAllowChargingAdbDialog = new Builder(getActivity()).setMessage(getActivity().getResources().getString(2131628671)).setTitle(2131628672).setIconAttribute(16843605).setPositiveButton(17039379, this).setNegativeButton(17039369, this).show();
    }

    public void setLastEnabledState(boolean lastEnabledState) {
        this.mLastEnabledState = lastEnabledState;
    }

    public void uncheckEnabledSwitch() {
        this.mEnabledSwitch.setChecked(false);
    }

    private void confirmResetShortcutManagerThrottling() {
        final IShortcutService service = IShortcutService.Stub.asInterface(ServiceManager.getService("shortcut"));
        new Builder(getActivity()).setTitle(2131627214).setMessage(2131627215).setPositiveButton(2131624573, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    try {
                        service.resetThrottling();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).setNegativeButton(17039360, null).create().show();
    }
}
