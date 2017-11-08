package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.fingerprint.FingerprintSettingsActivity;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import java.util.ArrayList;
import java.util.List;

public class ScreenLockSettings extends SettingsPreferenceFragment implements Indexable, OnPreferenceChangeListener {
    private static final int MY_USER_ID = UserHandle.myUserId();
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            ManagedLockPasswordProvider managedPasswordProvider = ManagedLockPasswordProvider.get(context, ScreenLockSettings.MY_USER_ID);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
            int profileUserId = Utils.getManagedProfileId(UserManager.get(context), ScreenLockSettings.MY_USER_ID);
            result.add(getSearchResource(context, 2131230864));
            result.add(getSearchResource(context, SecuritySettings.getResIdForLockUnlockScreen(context, lockPatternUtils, managedPasswordProvider, ScreenLockSettings.MY_USER_ID)));
            if (!(profileUserId == -10000 || !lockPatternUtils.isSeparateProfileChallengeAllowed(profileUserId) || isPasswordManaged(profileUserId, context, dpm))) {
                result.add(getSearchResource(context, SecuritySettings.getResIdForLockUnlockScreen(context, lockPatternUtils, managedPasswordProvider, profileUserId)));
            }
            return result;
        }

        private SearchIndexableResource getSearchResource(Context context, int xmlResId) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = xmlResId;
            return sir;
        }

        private boolean isPasswordManaged(int userId, Context context, DevicePolicyManager dpm) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfPasswordQualityIsSet(context, userId);
            if (admin == null || dpm.getPasswordQuality(admin.component, userId) != 524288) {
                return false;
            }
            return true;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131627774);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            result.add(data);
            UserManager um = UserManager.get(context);
            if (!um.isAdminUser()) {
                int resId;
                data = new SearchIndexableRaw(context);
                if (um.isLinkedUser()) {
                    resId = 2131624633;
                } else {
                    resId = 2131624631;
                }
                data.title = res.getString(resId);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            int profileUserId = Utils.getManagedProfileId(um, ScreenLockSettings.MY_USER_ID);
            if (profileUserId != -10000 && lockPatternUtils.isSeparateProfileChallengeAllowed(profileUserId) && lockPatternUtils.getKeyguardStoredPasswordQuality(profileUserId) >= 65536 && lockPatternUtils.isSeparateProfileChallengeAllowedToUnify(profileUserId)) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131625579);
                data.screenTitle = screenTitle;
                result.add(data);
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627854);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            PrivacyModeManager pmm = new PrivacyModeManager(context);
            boolean privacyProtectionOn = pmm.isPrivacyModeEnabled();
            boolean isGuestModeOn = pmm.isGuestModeOn();
            if (privacyProtectionOn && isGuestModeOn) {
                keys.add("unlock_set_or_change");
            }
            if (!lockPatternUtils.isSecure(ScreenLockSettings.MY_USER_ID)) {
                keys.add("secure_lock_settings");
            }
            Intent magazineUnlockIntent = new Intent();
            magazineUnlockIntent.setClassName("com.android.keyguard", "com.huawei.keyguard.magazine.settings.MagazinePreferenceActivity");
            if (!(!LockScreenUtils.isMagazineUnlockForbidden(context) && UserManager.get(context).isAdminUser() && Utils.hasIntentActivity(context.getPackageManager(), magazineUnlockIntent))) {
                keys.add("magazine_unlock");
            }
            if (!(Utils.isHwHealthPackageExist(context) && UserManager.get(context).isAdminUser())) {
                keys.add("step_count_settings");
            }
            if (!(SystemProperties.getBoolean("ro.config.support_aod", false) && Utils.hasIntentActivity(context.getPackageManager(), new Intent("com.huawei.aodui.action.AOD_SETTINGS")))) {
                keys.add("always_on_display");
            }
            return keys;
        }
    };
    private Preference mAodPreference;
    private BiometricManager mBm;
    private long mChallenge = 0;
    protected ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private String mCurrentDevicePassword;
    private String mCurrentProfilePassword;
    private DevicePolicyManager mDPM;
    protected boolean mIsGuestModeOn = false;
    protected LockPatternUtils mLockPatternUtils;
    private ManagedLockPasswordProvider mManagedPasswordProvider;
    private boolean mNeedReloadLayout = false;
    private RestrictedPreference mOwnerInfoPref;
    private boolean mPreEnrolled = false;
    protected ContentObserver mPrivacyModeObserver;
    protected boolean mPrivacyProtectionOn = false;
    private int mProfileChallengeUserId;
    protected CustomSwitchPreference mStepCountSettings;
    private byte[] mToken;
    private UserManager mUm;
    private SwitchPreference mUnifyProfile;
    protected CustomSwitchPreference mVisiblePattern;
    private SwitchPreference mVisiblePatternProfile;

    public static class UnificationConfirmationDialog extends DialogFragment {

        private static class DialogOnclickListenerEx implements OnClickListener {
            boolean mCompliant;
            ScreenLockSettings mParentFragment;

            public DialogOnclickListenerEx(boolean compliant, ScreenLockSettings parentFragment) {
                this.mCompliant = compliant;
                this.mParentFragment = parentFragment;
            }

            public void onClick(DialogInterface dialog, int whichButton) {
                if (this.mCompliant) {
                    this.mParentFragment.launchConfirmDeviceLockForUnification();
                } else {
                    this.mParentFragment.unifyUncompliantLocks();
                }
            }
        }

        public static UnificationConfirmationDialog newIntance(boolean compliant) {
            UnificationConfirmationDialog dialog = new UnificationConfirmationDialog();
            Bundle args = new Bundle();
            args.putBoolean("compliant", compliant);
            dialog.setArguments(args);
            return dialog;
        }

        public void show(FragmentManager manager, String tag) {
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int i;
            ScreenLockSettings parentFragment = (ScreenLockSettings) getParentFragment();
            boolean compliant = getArguments().getBoolean("compliant");
            Builder title = new Builder(getActivity()).setTitle(2131625581);
            if (compliant) {
                i = 2131625582;
            } else {
                i = 2131625583;
            }
            title = title.setMessage(i);
            if (compliant) {
                i = 2131625584;
            } else {
                i = 2131625585;
            }
            return title.setPositiveButton(i, new DialogOnclickListenerEx(compliant, parentFragment)).setNegativeButton(2131624572, null).create();
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            ((ScreenLockSettings) getParentFragment()).updateUnificationPreference();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected PreferenceScreen createPreferenceHierarchy() {
        UserManager userManager;
        Preference magazineUnlock;
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230864);
        int resid = SecuritySettings.getResIdForLockUnlockScreen(getActivity(), this.mLockPatternUtils, this.mManagedPasswordProvider, MY_USER_ID);
        addPreferencesFromResource(resid);
        PreferenceGroup root2 = getPreferenceScreen();
        Preference huaweiUnlockStyle = findPreference("huawei_unlock_style");
        if (huaweiUnlockStyle != null) {
            Intent intent = huaweiUnlockStyle.getIntent();
            if (intent != null) {
                intent.setPackage("com.huawei.android.thememanager");
                huaweiUnlockStyle.setIntent(intent);
            }
            if (Utils.hasIntentActivity(getPackageManager(), intent)) {
                new ThemeQueryHandler(getContentResolver(), huaweiUnlockStyle).startQuery(1, null, ThemeQueryHandler.URI_MODULE_INFO, new String[]{"display_name_en", "display_name_zh"}, "module_name=?", new String[]{"unlock"}, null);
            } else {
                removePreference("other_category", "huawei_unlock_style");
            }
        }
        Preference unlocksetorchange = findPreference("unlock_set_or_change");
        if (unlocksetorchange != null) {
            if (resid == 2131230858) {
                unlocksetorchange.setSummary(2131627779);
            } else if (resid == 2131230877) {
                unlocksetorchange.setSummary(2131624735);
            } else if (resid == 2131230863) {
                unlocksetorchange.setSummary(2131627779);
            } else if (resid == 2131230871) {
                unlocksetorchange.setSummary(2131624733);
            } else if (resid == 2131230868) {
                unlocksetorchange.setSummary(2131624737);
            }
        }
        disableIfPasswordQualityManaged("unlock_set_or_change", MY_USER_ID);
        this.mProfileChallengeUserId = Utils.getManagedProfileId(this.mUm, MY_USER_ID);
        if (this.mProfileChallengeUserId != -10000 && this.mLockPatternUtils.isSeparateProfileChallengeAllowed(this.mProfileChallengeUserId)) {
            addPreferencesFromResource(2131230881);
            addPreferencesFromResource(2131230885);
            addPreferencesFromResource(SecuritySettings.getResIdForLockUnlockScreen(getActivity(), this.mLockPatternUtils, this.mManagedPasswordProvider, this.mProfileChallengeUserId));
            maybeAddFingerprintPreference(root2, this.mProfileChallengeUserId);
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId)) {
                disableIfPasswordQualityManaged("unlock_set_or_change_profile", this.mProfileChallengeUserId);
            } else {
                Preference lockPreference = findPreference("unlock_set_or_change_profile");
                lockPreference.setSummary((CharSequence) getResources().getString(2131625586));
                lockPreference.setEnabled(false);
                disableIfPasswordQualityManaged("unlock_set_or_change", this.mProfileChallengeUserId);
            }
        }
        this.mVisiblePatternProfile = (SwitchPreference) findPreference("visiblepattern_profile");
        if (this.mVisiblePatternProfile != null) {
            this.mVisiblePatternProfile.setOnPreferenceChangeListener(this);
        }
        this.mUnifyProfile = (SwitchPreference) findPreference("unification");
        if (this.mUnifyProfile != null) {
            this.mUnifyProfile.setOnPreferenceChangeListener(this);
        }
        if (resid == 2131230858) {
            String unlockPackage = Secure.getString(getContentResolver(), "lockscreen_package");
            if (unlockPackage == null) {
                unlockPackage = System.getString(getContentResolver(), "lockscreen_package");
            }
            boolean isHwUnlockEnabled = System.getInt(getContentResolver(), "hw_unlock_enabled", 0) > 0;
            boolean equals;
            if (unlockPackage != null) {
                equals = "hw_unlock".equals(unlockPackage);
            } else {
                equals = true;
            }
            if (isHwUnlockEnabled && r15 && unlocksetorchange != null) {
                unlocksetorchange.setSummary(getResources().getString(2131627779));
            }
        }
        if (!this.mLockPatternUtils.isSecure(MY_USER_ID)) {
            removePreference("security_category", "secure_lock_settings");
        }
        Preference ownerInfoPreference = findPreference("owner_info_settings");
        if (ownerInfoPreference != null && (ownerInfoPreference instanceof RestrictedPreference)) {
            this.mOwnerInfoPref = (RestrictedPreference) ownerInfoPreference;
        }
        if (this.mOwnerInfoPref != null) {
            if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
                this.mOwnerInfoPref.setDisabledByAdmin(RestrictedLockUtils.getDeviceOwner(getActivity()));
            } else {
                this.mOwnerInfoPref.setDisabledByAdmin(null);
                this.mOwnerInfoPref.setEnabled(!this.mLockPatternUtils.isLockScreenDisabled(MY_USER_ID));
                if (this.mOwnerInfoPref.isEnabled()) {
                    this.mOwnerInfoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            OwnerInfoSettings.show(ScreenLockSettings.this);
                            return true;
                        }
                    });
                }
            }
        }
        this.mVisiblePattern = (CustomSwitchPreference) findPreference("visiblepattern");
        if (this.mVisiblePattern != null) {
            this.mVisiblePattern.setOnPreferenceChangeListener(this);
        }
        PrivacyModeManager privacyModeManager = new PrivacyModeManager(getActivity());
        this.mPrivacyProtectionOn = privacyModeManager.isPrivacyModeEnabled();
        this.mIsGuestModeOn = privacyModeManager.isGuestModeOn();
        if (this.mPrivacyProtectionOn) {
            if (this.mIsGuestModeOn) {
                removePreference("security_category", "unlock_set_or_change");
            } else if (unlocksetorchange != null) {
                unlocksetorchange.setEnabled(false);
                unlocksetorchange.setSummary(2131627544);
            }
        } else if (unlocksetorchange != null) {
            unlocksetorchange.setLayoutResource(2130968977);
        }
        if (findPreference("huawei_smart_lock") != null) {
            if (!Utils.hasIntentActivity(getPackageManager(), new Intent("huawei.intent.action.TRUST_AGENT_SETTINGS"))) {
                removePreference("security_category", "huawei_smart_lock");
            }
        }
        if (Utils.isHwHealthPackageExist(getActivity())) {
            userManager = this.mUm;
            if (UserManager.get(getActivity()).isAdminUser()) {
                this.mStepCountSettings = (CustomSwitchPreference) findPreference("step_count_settings");
                if (this.mStepCountSettings != null) {
                    this.mStepCountSettings.setChecked(Global.getInt(getContentResolver(), "step_count_settings", 1) == 1);
                    this.mStepCountSettings.setOnPreferenceChangeListener(this);
                }
                magazineUnlock = findPreference("magazine_unlock");
                if (!LockScreenUtils.isMagazineUnlockForbidden(getActivity())) {
                    userManager = this.mUm;
                    if (UserManager.get(getActivity()).isAdminUser()) {
                        if (magazineUnlock != null) {
                        }
                        this.mAodPreference = findPreference("always_on_display");
                        if (!(this.mAodPreference == null || (SystemProperties.getBoolean("ro.config.support_aod", false) && Utils.hasIntentActivity(getPackageManager(), this.mAodPreference.getIntent())))) {
                            removePreference("other_category", "always_on_display");
                            this.mAodPreference = null;
                        }
                        Index.getInstance(getActivity()).updateFromClassNameResource(ScreenLockSettings.class.getName(), true, true);
                        Index.getInstance(getActivity()).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
                        return root2;
                    }
                }
                removePreference("other_category", "magazine_unlock");
                this.mAodPreference = findPreference("always_on_display");
                removePreference("other_category", "always_on_display");
                this.mAodPreference = null;
                Index.getInstance(getActivity()).updateFromClassNameResource(ScreenLockSettings.class.getName(), true, true);
                Index.getInstance(getActivity()).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
                return root2;
            }
        }
        removePreference("other_category", "step_count_settings");
        magazineUnlock = findPreference("magazine_unlock");
        if (LockScreenUtils.isMagazineUnlockForbidden(getActivity())) {
            userManager = this.mUm;
            if (UserManager.get(getActivity()).isAdminUser()) {
                if (magazineUnlock != null) {
                }
                this.mAodPreference = findPreference("always_on_display");
                removePreference("other_category", "always_on_display");
                this.mAodPreference = null;
                Index.getInstance(getActivity()).updateFromClassNameResource(ScreenLockSettings.class.getName(), true, true);
                Index.getInstance(getActivity()).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
                return root2;
            }
        }
        removePreference("other_category", "magazine_unlock");
        this.mAodPreference = findPreference("always_on_display");
        removePreference("other_category", "always_on_display");
        this.mAodPreference = null;
        Index.getInstance(getActivity()).updateFromClassNameResource(ScreenLockSettings.class.getName(), true, true);
        Index.getInstance(getActivity()).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
        return root2;
    }

    private void maybeAddFingerprintPreference(PreferenceGroup securityCategory, int userId) {
        if (BiometricManager.isFingerprintSupported(getContext())) {
            Preference fingerprintPreference = FingerprintSettingsActivity.getFingerprintPreferenceForUser(securityCategory.getContext(), userId);
            fingerprintPreference.setWidgetLayoutResource(2130968998);
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId)) {
                fingerprintPreference.setEnabled(true);
            } else {
                fingerprintPreference.setEnabled(false);
            }
            securityCategory.addPreference(fingerprintPreference);
            return;
        }
        Log.i("ScreenLockSettings", " current device not support fingerprint.");
    }

    private void disableIfPasswordQualityManaged(String preferenceKey, int userId) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfPasswordQualityIsSet(getActivity(), userId);
        if (admin != null && this.mDPM.getPasswordQuality(admin.component, userId) == 524288) {
            Preference pref = getPreferenceScreen().findPreference(preferenceKey);
            if (pref instanceof RestrictedPreference) {
                ((RestrictedPreference) pref).setDisabledByAdmin(admin);
            }
        }
    }

    public void updateUnificationPreference() {
        if (this.mUnifyProfile != null) {
            this.mUnifyProfile.setChecked(!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId));
        }
    }

    public void launchConfirmDeviceLockForUnification() {
        if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(128, getActivity().getString(2131624724), true, MY_USER_ID)) {
            launchConfirmProfileLockForUnification();
        }
    }

    private void launchConfirmProfileLockForUnification() {
        if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(129, getActivity().getString(2131624725), true, this.mProfileChallengeUserId)) {
            unifyLocks();
            createPreferenceHierarchy();
            refreshUi();
        }
    }

    private void unifyLocks() {
        int profileQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId);
        if (profileQuality == 65536) {
            this.mLockPatternUtils.saveLockPattern(LockPatternUtils.stringToPattern(this.mCurrentProfilePassword), this.mCurrentDevicePassword, MY_USER_ID);
        } else {
            this.mLockPatternUtils.saveLockPassword(this.mCurrentProfilePassword, this.mCurrentDevicePassword, profileQuality, MY_USER_ID);
        }
        this.mLockPatternUtils.setSeparateProfileChallengeEnabled(this.mProfileChallengeUserId, false, this.mCurrentProfilePassword);
        this.mLockPatternUtils.setVisiblePatternEnabled(this.mLockPatternUtils.isVisiblePatternEnabled(this.mProfileChallengeUserId), MY_USER_ID);
        this.mCurrentDevicePassword = null;
        this.mCurrentProfilePassword = null;
    }

    public void unifyUncompliantLocks() {
        this.mLockPatternUtils.setSeparateProfileChallengeEnabled(this.mProfileChallengeUserId, false, this.mCurrentProfilePassword);
        startFragment(this, "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment", 2131624718, 123, null);
    }

    private void ununifyLocks() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
        intent.putExtra("android.intent.extra.USER_ID", this.mProfileChallengeUserId);
        startActivityForResult(intent, 127);
    }

    protected void refreshUi() {
        LockPatternUtils lockPatternUtils = this.mChooseLockSettingsHelper.utils();
        if (this.mVisiblePattern != null) {
            this.mVisiblePattern.setChecked(lockPatternUtils.isVisiblePatternEnabled(MY_USER_ID));
        }
        if (this.mVisiblePatternProfile != null) {
            this.mVisiblePatternProfile.setChecked(this.mLockPatternUtils.isVisiblePatternEnabled(this.mProfileChallengeUserId));
        }
        if (this.mAodPreference != null) {
            int i;
            boolean aodEnabled = Secure.getInt(getContentResolver(), "aod_switch", 1) == 1;
            Preference preference = this.mAodPreference;
            if (aodEnabled) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
        updateUnificationPreference();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        this.mManagedPasswordProvider = ManagedLockPasswordProvider.get(getActivity(), MY_USER_ID);
        this.mUm = UserManager.get(getActivity());
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        registerPrivacyModeObserver();
        if (BiometricManager.isFingerprintSupported(getActivity())) {
            this.mBm = BiometricManager.open(getActivity());
        }
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
        refreshUi();
        this.mNeedReloadLayout = true;
        updateOwnerInfo();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterPrivacyModeObserver();
        if (!this.mPreEnrolled) {
            return;
        }
        if (this.mBm != null) {
            Log.d("ScreenLockSettings", "execute postEnroll when destroy activity");
            this.mBm.postEnrollSafe(this.mChallenge);
            return;
        }
        Log.w("ScreenLockSettings", "Failed to get BiometricManager, postEnroll ignored.");
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Intent intent;
        if ("huawei_unlock_style".equals(preference.getKey())) {
            Preference huaweiUnlockStyle = findPreference("huawei_unlock_style");
            if (huaweiUnlockStyle != null) {
                intent = huaweiUnlockStyle.getIntent();
                if (intent != null) {
                    Utils.cancelSplit(getActivity(), intent);
                }
            }
        }
        if (!("show_password".equals(preference.getKey()) || "credential_storage_type".equals(preference.getKey()))) {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        }
        if ("unlock_set_or_change".equals(preference.getKey())) {
            intent = Utils.onBuildStartFragmentIntent(getActivity(), "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment", null, null, 2131627773, null, false);
            if (BiometricManager.isFingerprintSupported(getActivity())) {
                if (this.mBm != null) {
                    Log.d("ScreenLockSettings", "Fingerprint arguments added.");
                    this.mChallenge = this.mBm.preEnrollSafe();
                    this.mPreEnrolled = true;
                    intent.putExtra("has_challenge", true);
                    intent.putExtra("challenge", this.mChallenge);
                } else {
                    Log.e("ScreenLockSettings", "Fingerprint supported but failed to initialize BiometricManager.");
                }
            }
            startActivityForResult(intent, 123);
        } else if ("unlock_set_or_change_profile".equals(preference.getKey())) {
            if (Utils.startQuietModeDialogIfNecessary(getActivity(), this.mUm, this.mProfileChallengeUserId)) {
                return false;
            }
            intent = new Intent();
            intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
            intent.putExtra("android.intent.extra.USER_ID", this.mProfileChallengeUserId);
            startActivityForResult(intent, 127);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123 && resultCode == 1) {
            this.mToken = null;
            int quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(MY_USER_ID);
            if (BiometricManager.isFingerprintSupported(getActivity()) && quality >= 131072) {
                if (data != null) {
                    this.mToken = data.getByteArrayExtra("hw_auth_token");
                }
                if (this.mToken == null) {
                    Log.e("ScreenLockSettings", "Failed to acquire token for fingerpint enrollment.");
                }
            }
            checkToPromptFingerPrintSettings();
        } else if (requestCode == 128 && resultCode == -1) {
            if (data != null) {
                this.mCurrentDevicePassword = data.getStringExtra("password");
            }
            launchConfirmProfileLockForUnification();
            return;
        } else if (requestCode == 129 && resultCode == -1) {
            if (data != null) {
                this.mCurrentProfilePassword = data.getStringExtra("password");
            }
            unifyLocks();
            return;
        } else if (requestCode == 130 && resultCode == -1) {
            ununifyLocks();
            return;
        }
        createPreferenceHierarchy();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        int compliantForDevice = 0;
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        LockPatternUtils lockPatternUtils = this.mChooseLockSettingsHelper.utils();
        String key = preference.getKey();
        if ("visiblepattern_profile".equals(key)) {
            if (Utils.startQuietModeDialogIfNecessary(getActivity(), this.mUm, this.mProfileChallengeUserId)) {
                return false;
            }
            lockPatternUtils.setVisiblePatternEnabled(((Boolean) value).booleanValue(), this.mProfileChallengeUserId);
        } else if ("unification".equals(key)) {
            if (Utils.startQuietModeDialogIfNecessary(getActivity(), this.mUm, this.mProfileChallengeUserId)) {
                return false;
            }
            if (((Boolean) value).booleanValue()) {
                boolean compliantForDevice2;
                if (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId) >= 65536) {
                    compliantForDevice2 = this.mLockPatternUtils.isSeparateProfileChallengeAllowedToUnify(this.mProfileChallengeUserId);
                }
                UnificationConfirmationDialog.newIntance(compliantForDevice2).show(getChildFragmentManager(), "unification_dialog");
            } else {
                if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(130, getActivity().getString(2131624724), true, MY_USER_ID)) {
                    ununifyLocks();
                }
            }
        } else if ("visiblepattern".equals(key)) {
            lockPatternUtils.setVisiblePatternEnabled(((Boolean) value).booleanValue(), MY_USER_ID);
        } else if ("step_count_settings".equals(key)) {
            Boolean newValue = (Boolean) value;
            ContentResolver contentResolver = getContentResolver();
            String str = "step_count_settings";
            if (newValue.booleanValue()) {
                compliantForDevice = 1;
            }
            Global.putInt(contentResolver, str, compliantForDevice);
        }
        return true;
    }

    public void updateOwnerInfo() {
        if (this.mOwnerInfoPref == null) {
            return;
        }
        if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
            this.mOwnerInfoPref.setSummary(this.mLockPatternUtils.getDeviceOwnerInfo());
            return;
        }
        CharSequence ownerInfo;
        RestrictedPreference restrictedPreference = this.mOwnerInfoPref;
        if (this.mLockPatternUtils.isOwnerInfoEnabled(MY_USER_ID)) {
            ownerInfo = this.mLockPatternUtils.getOwnerInfo(MY_USER_ID);
        } else {
            ownerInfo = getString(2131624628);
        }
        restrictedPreference.setSummary(ownerInfo);
    }

    private void checkToPromptFingerPrintSettings() {
        int quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(MY_USER_ID);
        if (!BiometricManager.isFingerprintSupported(getActivity())) {
            Log.d("ScreenLockSettings", "isFingerprintSupported = false");
        } else if (this.mToken == null) {
            Log.d("ScreenLockSettings", "pin or password not set, no need to prompt user.");
        } else {
            if (BiometricManager.hasFpTemplates(1, getActivity(), UserHandle.myUserId())) {
                if (BiometricManager.isKeyGuardNotBinded(getActivity(), UserHandle.myUserId())) {
                    if (quality == 131072 || quality == 196608) {
                        showSwitchLockDlg(getResources().getString(2131624735), false);
                    } else if (quality >= 262144) {
                        showSwitchLockDlg(getResources().getString(2131624737), false);
                    }
                }
            } else if (quality == 131072 || quality == 196608) {
                showSwitchLockDlg(getResources().getString(2131624735), true);
            } else if (quality > 131072) {
                showSwitchLockDlg(getResources().getString(2131624737), true);
            }
        }
    }

    private void showSwitchLockDlg(String curLockType, boolean hasNoFingerprints) {
        int title;
        int buttonOk;
        int buttonCancel;
        int content;
        if (hasNoFingerprints) {
            title = 2131627682;
            buttonOk = 2131627683;
            buttonCancel = 2131627684;
            content = 2131627685;
        } else {
            title = 2131627686;
            buttonOk = 2131627687;
            buttonCancel = 2131627688;
            content = 2131627689;
        }
        String dlgContent = getResources().getString(content, new Object[]{curLockType});
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(dlgContent);
        dialogBuilder.setPositiveButton(buttonOk, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ScreenLockSettings.this.toFpSettignsFragment();
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(buttonCancel, null);
        dialogBuilder.create().show();
    }

    private void toFpSettignsFragment() {
        Intent intent = new Intent(getActivity(), FingerprintSettingsActivity.class);
        intent.putExtra("fp_settings_start_mode_key", 1);
        if (this.mToken != null) {
            intent.putExtra("hw_auth_token", this.mToken);
        }
        startActivity(intent, null);
    }

    private void registerPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            this.mPrivacyModeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    ScreenLockSettings.this.createPreferenceHierarchy();
                    ScreenLockSettings.this.refreshUi();
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
}
