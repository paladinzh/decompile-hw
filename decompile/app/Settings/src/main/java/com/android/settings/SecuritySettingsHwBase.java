package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.deviceinfo.DefaultStorageLocation;
import com.android.settings.sdencryption.SdEncryptionUtils;
import com.android.settings.sdencryption.SdLog;
import com.android.settings.search.Index;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;
import java.util.Locale;

public abstract class SecuritySettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnClickListener, OnDismissListener {
    protected CheckBox mAppVerification;
    protected LinearLayout mCheckboxLayout;
    private HwCustSecuritySettingsHwBase mCustSecuritySettingsHwBase = null;
    protected DevicePolicyManager mDPM;
    protected SwitchPreference mHdbAllowed;
    protected boolean mIsGuestModeOn = false;
    protected LockPatternUtils mLockPatternUtils;
    protected PreferenceScreen mPrefLock;
    protected ContentObserver mPrivacyModeObserver;
    protected boolean mPrivacyProtectionOn = false;
    protected final boolean mSdCryptFeatureAvailable = SdEncryptionUtils.isFeatureAvailable();
    protected BroadcastReceiver mSdcardReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED".equals(action)) {
                SecuritySettingsHwBase.this.removeDialog(10002);
                SecuritySettingsHwBase.this.updateSdCardEncryptionCategory();
            } else if ("android.intent.action.HWSDLOCK_LOCKED_SD_ADDED".equals(action)) {
                SecuritySettingsHwBase.this.createPreferenceHierarchy();
                SecuritySettingsHwBase.this.refreshUi();
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                SecuritySettingsHwBase.this.updateSdCardEncryptedState(" ");
            }
        }
    };
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            String action = intent.getAction();
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                SecuritySettingsHwBase.this.enableOrDisableCardLock();
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                boolean enabled = intent.getBooleanExtra("state", false);
                if (SecuritySettingsHwBase.this.mPrefLock != null) {
                    PreferenceScreen preferenceScreen = SecuritySettingsHwBase.this.mPrefLock;
                    if (!enabled) {
                        z = true;
                    }
                    preferenceScreen.setEnabled(z);
                }
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                SecuritySettingsHwBase.this.handleServiceStateChanged();
            }
        }
    };
    protected boolean mSimStateRegistered;
    protected StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            SecuritySettingsHwBase.this.updateSdCardEncryptedState(newState);
            if ("unmounted".equals(newState)) {
                SecuritySettingsHwBase.this.removeDialog(10001);
                SecuritySettingsHwBase.this.updateSdCardEncryptionCategory();
            } else if ("mounted".equals(newState) || "mounted_ro".equals(newState)) {
                SecuritySettingsHwBase.this.createPreferenceHierarchy();
                SecuritySettingsHwBase.this.refreshUi();
            }
        }
    };
    protected StorageManager mStorageManager;
    protected SwitchPreference mToggleAppInstallation;
    protected CustomSwitchPreference mToggleDownloadApplication;
    protected DialogInterface mWarnAppDownload;

    protected abstract PreferenceScreen createPreferenceHierarchy();

    private void updateSdCardEncryptedState(String mountState) {
        if (mountState != null) {
            Preference tmpTwoSummaryPref = findPreference("sdcardencryption");
            if (tmpTwoSummaryPref == null || !(tmpTwoSummaryPref instanceof TwoSummaryPreference)) {
                SdLog.e("SecuritySettingsHwBase", "encryption preference == null");
                return;
            }
            TwoSummaryPreference sdEncryptionPref = (TwoSummaryPreference) tmpTwoSummaryPref;
            String state = SdEncryptionUtils.getSdCryptionState(sdEncryptionPref.getContext());
            if ("no_card".equals(state)) {
                sdEncryptionPref.setTitle(2131628778);
                sdEncryptionPref.setNetherSummary(getString(2131628782));
                sdEncryptionPref.setSummary((CharSequence) "");
                sdEncryptionPref.setEnabled(false);
            } else if (state.equals("disable") || state.equals("encrypting")) {
                sdEncryptionPref.setTitle(2131628778);
                sdEncryptionPref.setNetherSummary(getString(2131628779));
                sdEncryptionPref.setSummary((CharSequence) "");
                sdEncryptionPref.setEnabled(true);
            } else if (state.equals("enable")) {
                EnforcedAdmin admin = SdEncryptionUtils.checkIfDecryptSdDisallowed(getContext());
                if (admin != null) {
                    sdEncryptionPref.setSummary(2131627106);
                } else {
                    sdEncryptionPref.setSummary((CharSequence) "");
                }
                sdEncryptionPref.setTitle(2131628780);
                sdEncryptionPref.setNetherSummary(getString(2131628781));
                if (admin == null || admin.component == null || !this.mDPM.isRemovingAdmin(admin.component, UserHandle.myUserId())) {
                    sdEncryptionPref.setEnabled(true);
                    SdLog.i("SecuritySettingsHwBase", "active admin is null or was removed!");
                } else {
                    sdEncryptionPref.setEnabled(false);
                    SdLog.i("SecuritySettingsHwBase", "active admin is being removing!");
                }
            } else if (state.equals("decrypting")) {
                sdEncryptionPref.setTitle(2131628780);
                sdEncryptionPref.setNetherSummary(getString(2131628781));
                sdEncryptionPref.setSummary((CharSequence) "");
                sdEncryptionPref.setEnabled(true);
            } else {
                sdEncryptionPref.setEnabled(false);
            }
        }
    }

    protected void setSimLockPreference(PreferenceScreen root) {
        boolean isOwnerUser = false;
        this.mPrefLock = (PreferenceScreen) root.findPreference("sim_lock_settings");
        if (ActivityManager.getCurrentUser() == 0) {
            isOwnerUser = true;
        }
        if (!isOwnerUser) {
            removePreference("sim_lock_settings");
        }
        enableOrDisableCardLock();
        try {
            if (Utils.isMultiSimEnabled() && this.mPrefLock != null) {
                Intent lIntent = this.mPrefLock.getIntent();
                if (lIntent != null) {
                    lIntent.setClassName("com.android.settings", "com.android.settings.MSimIccLockSettings");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static boolean isShowEncryption(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        if (1 != System.getInt(context.getContentResolver(), "has_encryption", 1)) {
            z = false;
        }
        return z;
    }

    protected void registerSimStateReceiver() {
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.SERVICE_STATE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        if (!this.mSimStateRegistered) {
            getActivity().registerReceiver(this.mSimStateReceiver, filter);
            this.mSimStateRegistered = true;
        }
    }

    protected void unRegisterSimStateReceiver() {
        if (this.mSimStateRegistered) {
            getActivity().unregisterReceiver(this.mSimStateReceiver);
            this.mSimStateRegistered = false;
        }
    }

    private static boolean cardPinChangable() {
        if (Utils.isSimCardLockStateChangeAble(0)) {
            return true;
        }
        return Utils.isSimCardLockStateChangeAble(1);
    }

    public static String getSimLockTitleForMultiSim(Context context) {
        if (context == null) {
            return null;
        }
        String formatedString;
        boolean isCard1PinChangeAble = Utils.isSimCardLockStateChangeAble(0);
        boolean isCard2PinChangeAble = Utils.isSimCardLockStateChangeAble(1);
        if (isCard1PinChangeAble && isCard2PinChangeAble) {
            formatedString = context.getResources().getString(2131627386) + "/" + context.getResources().getString(2131627387);
        } else if (isCard1PinChangeAble) {
            formatedString = context.getResources().getString(2131627386);
        } else if (isCard2PinChangeAble) {
            formatedString = context.getResources().getString(2131627387);
        } else {
            formatedString = context.getResources().getString(2131627386) + "/" + context.getResources().getString(2131627387);
        }
        return context.getResources().getString(2131627394, new Object[]{formatedString});
    }

    private void enableOrDisableCardLock() {
        if (getPreferenceScreen() != null && this.mPrefLock != null) {
            boolean isOwnerUser = ActivityManager.getCurrentUser() == 0;
            try {
                PreferenceCategory simLockCat;
                if (Utils.isMultiSimEnabled()) {
                    if (isAirplaneModeOn()) {
                        this.mPrefLock.setEnabled(false);
                    } else {
                        this.mPrefLock.setEnabled(cardPinChangable());
                    }
                    if ("true".equals(System.getString(getContentResolver(), "isDisableSimLock"))) {
                        this.mPrefLock.setEnabled(false);
                    }
                    simLockCat = (PreferenceCategory) getPreferenceScreen().findPreference("sim_lock");
                    if (simLockCat != null) {
                        simLockCat.setTitle(2131627398);
                    }
                    if (!isOwnerUser) {
                        removePreference("sim_lock");
                    }
                    this.mPrefLock.setTitle(getSimLockTitleForMultiSim(getActivity()));
                } else {
                    Locale locale = Locale.getDefault();
                    String country = locale.getCountry();
                    String lan = locale.getLanguage();
                    int isWestRurope = System.getInt(getContentResolver(), "region_west_europe", 0);
                    if ("PT".equals(country) && "pt".equals(lan) && isWestRurope == 1) {
                        this.mPrefLock.setTitle(2131627413);
                    }
                    TelephonyManager tm = TelephonyManager.getDefault();
                    int simState = tm.getSimState();
                    simLockCat = (PreferenceCategory) getPreferenceScreen().findPreference("sim_lock");
                    Preference simLockPreferences = simLockCat.getPreference(0);
                    if (2 == tm.getCurrentPhoneType() && tm.getLteOnCdmaMode() != 1) {
                        this.mPrefLock.setTitle(2131627316);
                        simLockCat.setTitle(2131627317);
                    }
                    if (simState == 1 || simState == 0 || simState == 6) {
                        simLockPreferences.setEnabled(false);
                    } else {
                        simLockPreferences.setEnabled(true);
                    }
                    if (this.mCustSecuritySettingsHwBase != null) {
                        this.mCustSecuritySettingsHwBase.enableOrDisableSimLock(getActivity(), simLockPreferences);
                    }
                    if (isAirplaneModeOn()) {
                        this.mPrefLock.setEnabled(false);
                    }
                    if (!isOwnerUser) {
                        removePreference("sim_lock");
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        int i = 0;
        String key = preference.getKey();
        if ("toggle_install_applications".equals(key)) {
            if (((Boolean) value).booleanValue()) {
                if (this.mToggleAppInstallation != null) {
                    this.mToggleAppInstallation.setChecked(false);
                }
                warnAppInstallation();
            } else {
                Global.putInt(getContentResolver(), "app_check_risk", 0);
                setNonMarketAppsAllowed(false);
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
            }
            return true;
        } else if ("toggle_download_application".equals(key)) {
            if (((Boolean) value).booleanValue()) {
                if (this.mToggleDownloadApplication != null) {
                    this.mToggleDownloadApplication.setChecked(false);
                }
                warnAppDownload();
            } else {
                setAppDownloadAllowed(false);
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
            }
            return true;
        } else {
            if (!"hdb_allowed".equals(key)) {
                return this.mCustSecuritySettingsHwBase == null || this.mCustSecuritySettingsHwBase.handlePreferenceChange(preference, value);
            } else {
                Boolean valueObj = (Boolean) value;
                ContentResolver contentResolver = getContentResolver();
                String str = "hdb_enabled";
                if (valueObj.booleanValue()) {
                    i = 1;
                }
                System.putInt(contentResolver, str, i);
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
            }
        }
    }

    protected void warnAppInstallation() {
    }

    protected void setNonMarketAppsAllowed(boolean enabled) {
    }

    private boolean isAirplaneModeOn() {
        return System.getInt(getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    protected void warnAppDownload() {
        this.mWarnAppDownload = new Builder(getActivity()).setTitle(2131625399).setMessage(2131625596).setPositiveButton(17039379, this).setNegativeButton(17039360, this).setOnDismissListener(this).show();
    }

    protected boolean isAppDownloadAllowed() {
        return Global.getInt(getContentResolver(), "hw_download_non_market_apps", 0) == 1;
    }

    protected void setAppDownloadAllowed(boolean enabled) {
        Global.putInt(getContentResolver(), "hw_download_non_market_apps", enabled ? 1 : 0);
    }

    protected void updateEncryptionPreference(PreferenceScreen root) {
        PreferenceCategory encryptionCategory = (PreferenceCategory) root.findPreference("security_category_encryption");
        if (encryptionCategory != null) {
            Preference encryptionPref = encryptionCategory.findPreference("encryption");
            if (encryptionPref != null) {
                boolean isSdcardDefault = DefaultStorageLocation.isSdcard();
                encryptionPref.setEnabled(!isSdcardDefault);
                if (isSdcardDefault) {
                    encryptionPref.setSummary(2131627762);
                }
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (!("toggle_install_applications".equals(preference.getKey()) || "toggle_download_application".equals(preference.getKey()) || "hdb_allowed".equals(preference.getKey()) || "show_password".equals(preference.getKey()))) {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        }
        if ("pirvacy_protection".equals(preference.getKey())) {
            try {
                if (preference.getIntent() != null) {
                    preference.getIntent().setPackage("com.huawei.privacymode");
                }
                getActivity().startActivity(preference.getIntent());
            } catch (Exception e) {
                MLog.e("ScreenLockBaseFragment", "pivacy mode entrance action not supported!");
                e.printStackTrace();
            }
            new HwAnimationReflection(getActivity()).overrideTransition(1);
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            return true;
        }
        if ("screen_pinning_settings".equals(key)) {
            Bundle extras = preference.getExtras();
            extras.putString("title", getString(2131626853));
            extras.putCharSequence("summary", getActivity().getResources().getText(2131626854));
        } else if ("set_sdcard_pin".equals(key)) {
            showDialog(10003);
        } else if ("clear_sdcard_pin".equals(key)) {
            showDialog(10001);
            return true;
        } else if ("force_clear_sdcard".equals(key)) {
            showDialog(10002);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mCustSecuritySettingsHwBase = (HwCustSecuritySettingsHwBase) HwCustUtils.createObj(HwCustSecuritySettingsHwBase.class, new Object[]{this});
        this.mStorageManager = (StorageManager) getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
        registerPrivacyModeObserver();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void updateCustPreference() {
        if (this.mCustSecuritySettingsHwBase != null) {
            this.mCustSecuritySettingsHwBase.updateCustPreference(getActivity());
        }
    }

    protected void addPrivacyProtection() {
        PrivacyModeManager pmm = new PrivacyModeManager(getActivity());
        boolean privacyProtectionOn = pmm.isPrivacyModeEnabled();
        boolean isGuestModeOn = pmm.isGuestModeOn();
        if (!PrivacyModeManager.isFeatrueSupported()) {
            return;
        }
        if (!privacyProtectionOn || !isGuestModeOn) {
            addPreferencesFromResource(2131230880);
        }
    }

    protected void handlemWarnInstallAppsBtn(int which) {
        if (Utils.isChinaArea() && -1 == which && this.mAppVerification != null) {
            Global.putInt(getContentResolver(), "app_check_risk", this.mAppVerification.isChecked() ? 1 : 0);
        }
    }

    protected void refreshUi() {
        updateSdCardEncryptionCategory();
        if (this.mToggleDownloadApplication != null) {
            this.mToggleDownloadApplication.setChecked(isAppDownloadAllowed());
        }
    }

    protected void initOtherDeviceAdminSettings(PreferenceGroup root) {
        if (Utils.isChinaArea() && UserManager.get(getActivity()).isAdminUser()) {
            this.mToggleDownloadApplication = (CustomSwitchPreference) root.findPreference("toggle_download_application");
            if (this.mToggleDownloadApplication != null) {
                this.mToggleDownloadApplication.setOnPreferenceChangeListener(this);
                return;
            }
            return;
        }
        removePreference(root, "toggle_download_application");
        this.mToggleDownloadApplication = null;
    }

    protected void updateSdCardEncryptionCategory() {
        boolean isOwnerUser = true;
        Context context = getActivity();
        if (context != null) {
            Index.getInstance(context).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
            PrivacyModeManager pmm = new PrivacyModeManager(context);
            this.mPrivacyProtectionOn = pmm.isPrivacyModeEnabled();
            this.mIsGuestModeOn = pmm.isGuestModeOn();
            if (ActivityManager.getCurrentUser() != 0) {
                isOwnerUser = false;
            }
            if (!isOwnerUser) {
                removePreference("lock_sdcard_category");
            } else if (this.mPrivacyProtectionOn && this.mIsGuestModeOn) {
                removePreference("lock_sdcard_category");
            } else if (!SdCardLockUtils.isSdCardPresent(context)) {
                removePreference("lock_sdcard_category");
            } else if (DefaultStorageLocation.isSdcard()) {
                removePreference("lock_sdcard_category");
            } else {
                if (SdCardLockUtils.isPasswordProtected(context)) {
                    removePreference("lock_sdcard_category", "set_sdcard_pin");
                    if (SdCardLockUtils.isSdCardUnlocked(context)) {
                        removePreference("lock_sdcard_category", "unlock_sdcard");
                        removePreference("lock_sdcard_category", "force_clear_sdcard");
                    } else {
                        removePreference("lock_sdcard_category", "change_sdcard_pin");
                        removePreference("lock_sdcard_category", "clear_sdcard_pin");
                    }
                } else {
                    removePreference("lock_sdcard_category", "change_sdcard_pin");
                    removePreference("lock_sdcard_category", "unlock_sdcard");
                    removePreference("lock_sdcard_category", "clear_sdcard_pin");
                    removePreference("lock_sdcard_category", "force_clear_sdcard");
                }
                if (SdCardLockUtils.isSdCardBusy(context)) {
                    disableLockSdcardCategory();
                }
                PreferenceCategory lockSdcardCategory = (PreferenceCategory) findPreference("lock_sdcard_category");
                if (lockSdcardCategory != null && lockSdcardCategory.getPreferenceCount() == 0) {
                    removePreference("lock_sdcard_category");
                }
            }
        }
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 10001:
                return new Builder(getActivity()).setTitle(getResources().getString(2131628095)).setMessage(getResources().getString(2131628096)).setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SdCardLockUtils.clearSDLockPassword(SecuritySettingsHwBase.this.getActivity());
                        SecuritySettingsHwBase.this.createPreferenceHierarchy();
                        SecuritySettingsHwBase.this.refreshUi();
                    }
                }).setNegativeButton(17039369, null).create();
            case 10002:
                String forceClearSdcardButtonText = getResources().getString(2131628104);
                SpannableString forceClearSpanText = new SpannableString(forceClearSdcardButtonText);
                forceClearSpanText.setSpan(new ForegroundColorSpan(-65536), 0, forceClearSdcardButtonText.length(), 18);
                return new Builder(getActivity()).setTitle(getResources().getString(2131628099)).setMessage(getResources().getString(2131628101)).setPositiveButton(forceClearSpanText, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SdCardLockUtils.eraseSDLock(SecuritySettingsHwBase.this.getActivity());
                        SecuritySettingsHwBase.this.disableLockSdcardCategory();
                    }
                }).setNegativeButton(17039369, null).create();
            case 10003:
                return new Builder(getActivity()).setTitle(getResources().getString(2131628180)).setMessage(getResources().getString(2131628227)).setPositiveButton(2131625563, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.ChooseLockSdCardPin");
                        try {
                            SecuritySettingsHwBase.this.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(17039360, null).create();
            default:
                return super.onCreateDialog(id);
        }
    }

    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HWSDLOCK_LOCKED_SD_ADDED");
        intentFilter.addAction("android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED");
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getActivity().registerReceiver(this.mSdcardReceiver, intentFilter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mSdcardReceiver);
    }

    public void onDestroy() {
        if (!(this.mStorageManager == null || this.mStorageListener == null)) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
        }
        unregisterPrivacyModeObserver();
        super.onDestroy();
    }

    protected void disableLockSdcardCategory() {
        PreferenceCategory lockSdcardCategory = (PreferenceCategory) findPreference("lock_sdcard_category");
        if (lockSdcardCategory != null) {
            int COUNT = lockSdcardCategory.getPreferenceCount();
            for (int i = 0; i < COUNT; i++) {
                lockSdcardCategory.getPreference(i).setEnabled(false);
            }
        }
    }

    protected int getMetricsCategory() {
        return 87;
    }

    private void registerPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            this.mPrivacyModeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    SecuritySettingsHwBase.this.createPreferenceHierarchy();
                    SecuritySettingsHwBase.this.refreshUi();
                }
            };
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_state"), true, this.mPrivacyModeObserver);
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_on"), true, this.mPrivacyModeObserver);
        }
    }

    private void unregisterPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().unregisterContentObserver(this.mPrivacyModeObserver);
        }
    }

    protected boolean isCdmaNetworkExist() {
        return !Utils.isCdmaNetwork(getContext(), 0) ? Utils.isCdmaNetwork(getContext(), 1) : true;
    }

    private void handleServiceStateChanged() {
        if (!isCdmaNetworkExist()) {
            removePreference("call_encryption");
        } else if (findPreference("call_encryption") == null && CallEncryptionSettings.shouldDisplay()) {
            createPreferenceHierarchy();
        }
    }
}
