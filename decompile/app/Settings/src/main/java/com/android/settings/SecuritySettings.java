package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.security.KeyStore;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.TrustAgentUtils.TrustAgentComponentInfo;
import com.android.settings.fingerprint.FingerprintSettings;
import com.android.settings.sdencryption.SdEncryptionSettingsActivity;
import com.android.settings.sdencryption.SdEncryptionUtils;
import com.android.settings.sdencryption.SdLog;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.ArrayList;
import java.util.List;

public class SecuritySettings extends SecuritySettingsHwBase implements OnPreferenceChangeListener, OnClickListener, Indexable, OnDismissListener {
    private static final int MY_USER_ID = UserHandle.myUserId();
    private static final int[] SCREEN_TIMEOUT = new int[]{5, 15, 30, 1, 2, 5, 10, 30};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new SecuritySearchIndexProvider();
    private static final String[] SWITCH_PREFERENCE_KEYS = new String[]{"show_password", "toggle_install_applications", "unification", "visiblepattern_profile"};
    private static final Intent TRUST_AGENT_INTENT = new Intent("android.service.trust.TrustAgentService");
    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private AlertDialog mDisallowDecryptDialog;
    private boolean mIsAdmin;
    private KeyStore mKeyStore;
    private ManagedLockPasswordProvider mManagedPasswordProvider;
    private RestrictedPreference mResetCredentials;
    private SwitchPreference mShowPassword;
    private SubscriptionManager mSubscriptionManager;
    private RestrictedSwitchPreference mToggleAppInstallation;
    private Intent mTrustAgentClickIntent;
    private UserManager mUm;
    private DialogInterface mWarnInstallApps;

    private static class SecuritySearchIndexProvider extends BaseSearchIndexProvider {
        private SecuritySearchIndexProvider() {
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> index = new ArrayList();
            if (UserManager.get(context).isAdminUser() && SecuritySettingsHwBase.isShowEncryption(context) && !LockPatternUtils.isDeviceEncryptionEnabled()) {
                index.add(getSearchResource(context, 2131230884));
            }
            index.add(getSearchResource(context, 2131230866));
            if (CallEncryptionSettings.shouldDisplay() && (Utils.isCdmaLteNetwork(context, 13, 0) || Utils.isCdmaLteNetwork(context, 13, 1))) {
                index.add(getSearchResource(context, 2131230857));
            }
            return index;
        }

        private SearchIndexableResource getSearchResource(Context context, int xmlResId) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = xmlResId;
            return sir;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131628917);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            result.add(data);
            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            if (lockPatternUtils.isSecure(SecuritySettings.MY_USER_ID)) {
                ArrayList<TrustAgentComponentInfo> agents = SecuritySettings.getActiveTrustAgents(context, lockPatternUtils, (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class));
                for (int i = 0; i < agents.size(); i++) {
                    TrustAgentComponentInfo agent = (TrustAgentComponentInfo) agents.get(i);
                    data = new SearchIndexableRaw(context);
                    data.title = agent.title;
                    data.screenTitle = screenTitle;
                    result.add(data);
                }
            }
            if (Utils.isChinaArea()) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131628526);
                data.summaryOn = res.getString(2131628527);
                data.summaryOff = res.getString(2131628527);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
            UserManager um = UserManager.get(context);
            TelephonyManager tm = TelephonyManager.from(context);
            if (!(um.isAdminUser() && tm.hasIccCard())) {
                keys.add("sim_lock");
                keys.add("sim_lock_settings");
            }
            if (um.hasUserRestriction("no_config_credentials")) {
                keys.add("credentials_management");
            }
            if (!lockPatternUtils.isSecure(SecuritySettings.MY_USER_ID)) {
                keys.add("trust_agent");
                keys.add("manage_trust_agents");
            }
            if (!(Utils.isChinaArea() && um.isAdminUser())) {
                keys.add("toggle_download_application");
            }
            if (Utils.isChinaArea()) {
                keys.add("toggle_install_applications");
            }
            if (SystemProperties.get("persist.service.hdb.enable", "false").equals("false")) {
                keys.add("hisuite_category");
                keys.add("hdb_allowed");
                keys.add("hdb_authorization_cancel");
            }
            boolean isFeatureAvailable = SdEncryptionUtils.isFeatureAvailable();
            if (!(um.isPrimaryUser() && isFeatureAvailable)) {
                keys.add("security_category_encryption_sd");
            }
            return keys;
        }
    }

    public static class SecuritySubSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
        private static final String[] SWITCH_PREFERENCE_KEYS = new String[]{"lock_after_timeout", "visiblepattern", "power_button_instantly_locks"};
        private DevicePolicyManager mDPM;
        private TimeoutListPreference mLockAfter;
        private LockPatternUtils mLockPatternUtils;
        private RestrictedPreference mOwnerInfoPref;
        private SwitchPreference mPowerButtonInstantlyLocks;
        private SwitchPreference mVisiblePattern;

        protected int getMetricsCategory() {
            return 87;
        }

        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            this.mLockPatternUtils = new LockPatternUtils(getContext());
            this.mDPM = (DevicePolicyManager) getContext().getSystemService(DevicePolicyManager.class);
            createPreferenceHierarchy();
        }

        public void onResume() {
            super.onResume();
            createPreferenceHierarchy();
            if (this.mVisiblePattern != null) {
                this.mVisiblePattern.setChecked(this.mLockPatternUtils.isVisiblePatternEnabled(SecuritySettings.MY_USER_ID));
            }
            if (this.mPowerButtonInstantlyLocks != null) {
                this.mPowerButtonInstantlyLocks.setChecked(this.mLockPatternUtils.getPowerButtonInstantlyLocks(SecuritySettings.MY_USER_ID));
            }
            updateOwnerInfo();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            createPreferenceHierarchy();
        }

        private void createPreferenceHierarchy() {
            PreferenceScreen root = getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            addPreferencesFromResource(getResIdForLockUnlockSubScreen(getActivity(), new LockPatternUtils(getContext()), ManagedLockPasswordProvider.get(getContext(), SecuritySettings.MY_USER_ID)));
            this.mLockAfter = (TimeoutListPreference) findPreference("lock_after_timeout");
            if (this.mLockAfter != null) {
                setupLockAfterPreference();
                updateLockAfterPreferenceSummary();
            }
            this.mVisiblePattern = (SwitchPreference) findPreference("visiblepattern");
            this.mPowerButtonInstantlyLocks = (SwitchPreference) findPreference("power_button_instantly_locks");
            Preference trustAgentPreference = findPreference("trust_agent");
            if (!(this.mPowerButtonInstantlyLocks == null || trustAgentPreference == null || trustAgentPreference.getTitle().length() <= 0)) {
                this.mPowerButtonInstantlyLocks.setSummary(getString(2131625571, new Object[]{trustAgentPreference.getTitle()}));
            }
            this.mOwnerInfoPref = (RestrictedPreference) findPreference("owner_info_settings");
            if (this.mOwnerInfoPref != null) {
                if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
                    this.mOwnerInfoPref.setDisabledByAdmin(RestrictedLockUtils.getDeviceOwner(getActivity()));
                } else {
                    boolean z;
                    this.mOwnerInfoPref.setDisabledByAdmin(null);
                    RestrictedPreference restrictedPreference = this.mOwnerInfoPref;
                    if (this.mLockPatternUtils.isLockScreenDisabled(SecuritySettings.MY_USER_ID)) {
                        z = false;
                    } else {
                        z = true;
                    }
                    restrictedPreference.setEnabled(z);
                    if (this.mOwnerInfoPref.isEnabled()) {
                        this.mOwnerInfoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            public boolean onPreferenceClick(Preference preference) {
                                OwnerInfoSettings.show(SecuritySubSettings.this);
                                return true;
                            }
                        });
                    }
                }
            }
            for (CharSequence findPreference : SWITCH_PREFERENCE_KEYS) {
                Preference pref = findPreference(findPreference);
                if (pref != null) {
                    pref.setOnPreferenceChangeListener(this);
                }
            }
        }

        private void setupLockAfterPreference() {
            this.mLockAfter.setValue(String.valueOf(Secure.getLong(getContentResolver(), "lock_screen_lock_after_timeout", 5000)));
            this.mLockAfter.setOnPreferenceChangeListener(this);
            if (this.mDPM != null) {
                EnforcedAdmin admin = RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(getActivity());
                this.mLockAfter.removeUnusableTimeouts(Math.max(0, this.mDPM.getMaximumTimeToLockForUserAndProfiles(UserHandle.myUserId()) - ((long) Math.max(0, System.getInt(getContentResolver(), "screen_off_timeout", 0)))), admin);
            }
        }

        private void updateLockAfterPreferenceSummary() {
            String summary;
            if (this.mLockAfter.isDisabledByAdmin()) {
                summary = getString(2131627106);
            } else {
                long currentTimeout = Secure.getLong(getContentResolver(), "lock_screen_lock_after_timeout", 5000);
                this.mLockAfter.setEntries(buildTimeoutEntries(getActivity()));
                CharSequence[] entries = this.mLockAfter.getEntries();
                CharSequence[] values = this.mLockAfter.getEntryValues();
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    if (currentTimeout >= Long.valueOf(values[i].toString()).longValue()) {
                        best = i;
                    }
                }
                Preference preference = findPreference("trust_agent");
                if (preference == null || preference.getTitle().length() <= 0) {
                    summary = getString(2131624621, new Object[]{entries[best]});
                } else if (Long.valueOf(values[best].toString()).longValue() == 0) {
                    summary = getString(2131624622, new Object[]{preference.getTitle()});
                } else {
                    summary = getString(2131624623, new Object[]{entries[best], preference.getTitle()});
                }
            }
            this.mLockAfter.setSummary(summary);
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
            if (this.mLockPatternUtils.isOwnerInfoEnabled(SecuritySettings.MY_USER_ID)) {
                ownerInfo = this.mLockPatternUtils.getOwnerInfo(SecuritySettings.MY_USER_ID);
            } else {
                ownerInfo = getString(2131624628);
            }
            restrictedPreference.setSummary(ownerInfo);
        }

        private static int getResIdForLockUnlockSubScreen(Context context, LockPatternUtils lockPatternUtils, ManagedLockPasswordProvider managedPasswordProvider) {
            if (lockPatternUtils.isSecure(SecuritySettings.MY_USER_ID)) {
                switch (lockPatternUtils.getKeyguardStoredPasswordQuality(SecuritySettings.MY_USER_ID)) {
                    case 65536:
                        return 2131230873;
                    case 131072:
                    case 196608:
                        return 2131230879;
                    case 262144:
                    case 327680:
                    case 393216:
                        return 2131230870;
                    case 524288:
                        return managedPasswordProvider.getResIdForLockUnlockSubScreen();
                }
            } else if (!lockPatternUtils.isLockScreenDisabled(SecuritySettings.MY_USER_ID)) {
                return 2131230883;
            }
            return 0;
        }

        private CharSequence[] buildTimeoutEntries(Context context) {
            timeoutEntries = new CharSequence[9];
            timeoutEntries[1] = String.format(getResources().getString(2131628301, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[0])}), new Object[0]);
            timeoutEntries[2] = String.format(getResources().getString(2131628302, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[1])}), new Object[0]);
            timeoutEntries[3] = String.format(getResources().getString(2131628303, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[2])}), new Object[0]);
            timeoutEntries[4] = String.format(getResources().getString(2131628304, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[3])}), new Object[0]);
            timeoutEntries[5] = String.format(getResources().getString(2131628305, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[4])}), new Object[0]);
            timeoutEntries[6] = String.format(getResources().getString(2131628306, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[5])}), new Object[0]);
            timeoutEntries[7] = String.format(getResources().getString(2131628307, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[6])}), new Object[0]);
            timeoutEntries[8] = String.format(getResources().getString(2131628308, new Object[]{Integer.valueOf(SecuritySettings.SCREEN_TIMEOUT[7])}), new Object[0]);
            return timeoutEntries;
        }

        public boolean onPreferenceChange(Preference preference, Object value) {
            String key = preference.getKey();
            if ("power_button_instantly_locks".equals(key)) {
                this.mLockPatternUtils.setPowerButtonInstantlyLocks(((Boolean) value).booleanValue(), SecuritySettings.MY_USER_ID);
            } else if ("lock_after_timeout".equals(key)) {
                try {
                    Secure.putInt(getContentResolver(), "lock_screen_lock_after_timeout", Integer.parseInt((String) value));
                } catch (NumberFormatException e) {
                    Log.e("SecuritySettings", "could not persist lockAfter timeout setting", e);
                }
                setupLockAfterPreference();
                updateLockAfterPreferenceSummary();
            } else if ("visiblepattern".equals(key)) {
                this.mLockPatternUtils.setVisiblePatternEnabled(((Boolean) value).booleanValue(), SecuritySettings.MY_USER_ID);
            }
            return true;
        }
    }

    protected int getMetricsCategory() {
        return 87;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSubscriptionManager = SubscriptionManager.from(getActivity());
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        this.mManagedPasswordProvider = ManagedLockPasswordProvider.get(getActivity(), MY_USER_ID);
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mUm = UserManager.get(getActivity());
        this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        if (savedInstanceState != null && savedInstanceState.containsKey("trust_agent_click_intent")) {
            this.mTrustAgentClickIntent = (Intent) savedInstanceState.getParcelable("trust_agent_click_intent");
        }
    }

    public static int getResIdForLockUnlockScreen(Context context, LockPatternUtils lockPatternUtils, ManagedLockPasswordProvider managedPasswordProvider, int userId) {
        boolean isMyUser = userId == MY_USER_ID;
        if (lockPatternUtils.isSecure(userId)) {
            switch (lockPatternUtils.getKeyguardStoredPasswordQuality(userId)) {
                case 65536:
                    if (isMyUser) {
                        return 2131230871;
                    }
                    return 2131230872;
                case 131072:
                case 196608:
                    if (isMyUser) {
                        return 2131230877;
                    }
                    return 2131230878;
                case 262144:
                case 327680:
                case 393216:
                    if (isMyUser) {
                        return 2131230868;
                    }
                    return 2131230869;
                case 524288:
                    return managedPasswordProvider.getResIdForLockUnlockScreen(!isMyUser);
                default:
                    return 0;
            }
        } else if (!isMyUser) {
            return 2131230865;
        } else {
            if (lockPatternUtils.isLockScreenDisabled(userId)) {
                return 2131230863;
            }
            return 2131230858;
        }
    }

    protected PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (getActivity() == null) {
            return root;
        }
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230856);
        root = getPreferenceScreen();
        addPrivacyProtection();
        if (CallEncryptionSettings.shouldDisplay()) {
            addPreferencesFromResource(2131230857);
            Preference callEncryptionPrefs = findPreference("call_encryption");
            if (callEncryptionPrefs != null) {
                if (Secure.getInt(getContentResolver(), "encrypt_version", 0) == 1) {
                    callEncryptionPrefs.setSummary(2131627698);
                } else {
                    callEncryptionPrefs.setSummary(2131627699);
                }
            }
        }
        this.mIsAdmin = this.mUm.isAdminUser();
        if (this.mIsAdmin && SecuritySettingsHwBase.isShowEncryption(getActivity()) && !LockPatternUtils.isDeviceEncryptionEnabled()) {
            addPreferencesFromResource(2131230884);
            updateEncryptionPreference(root);
        }
        PreferenceGroup securityCategory = (PreferenceGroup) root.findPreference("security_category");
        if (securityCategory != null) {
            maybeAddFingerprintPreference(securityCategory, UserHandle.myUserId());
            addTrustAgentSettings(securityCategory);
        }
        addPreferencesFromResource(2131230866);
        hideSdEncryptionIfNeeded(root);
        if (Utils.isWifiOnly(getActivity())) {
            Preference preference = root.findPreference("sim_lock");
            if (preference != null) {
                root.removePreference(preference);
            }
        } else {
            setSimLockPreference(root);
        }
        Preference screenPining = root.findPreference("screen_pinning_settings");
        if (screenPining != null) {
            String string;
            if (System.getInt(getContentResolver(), "lock_to_app_enabled", 0) != 0) {
                string = getResources().getString(2131626851);
            } else {
                string = getResources().getString(2131626852);
            }
            screenPining.setSummary((CharSequence) string);
        }
        this.mShowPassword = (SwitchPreference) root.findPreference("show_password");
        this.mResetCredentials = (RestrictedPreference) root.findPreference("credentials_reset");
        UserManager um = (UserManager) getActivity().getSystemService("user");
        this.mKeyStore = KeyStore.getInstance();
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_config_credentials", MY_USER_ID)) {
            PreferenceGroup credentialsManager = (PreferenceGroup) root.findPreference("credentials_management");
            credentialsManager.removePreference(root.findPreference("credentials_reset"));
            credentialsManager.removePreference(root.findPreference("credentials_install"));
            credentialsManager.removePreference(root.findPreference("credential_storage_type"));
            credentialsManager.removePreference(root.findPreference("user_credentials"));
        } else {
            int storageSummaryRes;
            ((RestrictedPreference) root.findPreference("user_credentials")).checkRestrictionAndSetDisabled("no_config_credentials");
            RestrictedPreference credentialStorageType = (RestrictedPreference) root.findPreference("credential_storage_type");
            credentialStorageType.checkRestrictionAndSetDisabled("no_config_credentials");
            ((RestrictedPreference) root.findPreference("credentials_install")).checkRestrictionAndSetDisabled("no_config_credentials");
            this.mResetCredentials.checkRestrictionAndSetDisabled("no_config_credentials");
            if (this.mKeyStore.isHardwareBacked()) {
                storageSummaryRes = 2131626128;
            } else {
                storageSummaryRes = 2131626129;
            }
            credentialStorageType.setSummary(storageSummaryRes);
        }
        PreferenceGroup deviceAdminCategory = (PreferenceGroup) root.findPreference("device_admin_category");
        this.mToggleAppInstallation = (RestrictedSwitchPreference) findPreference("toggle_install_applications");
        if (Utils.isChinaArea()) {
            this.mToggleAppInstallation.setTitle(2131628526);
            this.mToggleAppInstallation.setSummary(2131628527);
        }
        this.mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
        this.mToggleAppInstallation.setEnabled(!um.getUserInfo(MY_USER_ID).isRestricted());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_install_unknown_sources", MY_USER_ID) || RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_install_apps", MY_USER_ID)) {
            this.mToggleAppInstallation.setEnabled(false);
        }
        if (this.mToggleAppInstallation.isEnabled()) {
            this.mToggleAppInstallation.checkRestrictionAndSetDisabled("no_install_unknown_sources");
            if (!this.mToggleAppInstallation.isDisabledByAdmin()) {
                this.mToggleAppInstallation.checkRestrictionAndSetDisabled("no_install_apps");
            }
        }
        initOtherDeviceAdminSettings(deviceAdminCategory);
        this.mHdbAllowed = (SwitchPreference) findPreference("hdb_allowed");
        this.mHdbAllowed.setOnPreferenceChangeListener(this);
        this.mHdbAllowed.setChecked(1 == System.getInt(getContentResolver(), "hdb_enabled", 0));
        if (SystemProperties.get("persist.service.hdb.enable", "false").equals("false")) {
            root.removePreference(root.findPreference("hisuite_category"));
        }
        PreferenceGroup advancedCategory = (PreferenceGroup) root.findPreference("advanced_security");
        if (advancedCategory != null) {
            Preference manageAgents = advancedCategory.findPreference("manage_trust_agents");
            if (!(manageAgents == null || this.mLockPatternUtils == null || this.mLockPatternUtils.isSecure(MY_USER_ID))) {
                manageAgents.setEnabled(false);
                manageAgents.setSummary(2131624803);
            }
        }
        Index.getInstance(getActivity()).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
        Index.getInstance(getActivity()).updateFromClassNameResource(ScreenLockSettings.class.getName(), true, true);
        for (String string2 : SWITCH_PREFERENCE_KEYS) {
            Preference pref = findPreference(string2);
            if (pref != null) {
                pref.setOnPreferenceChangeListener(this);
            }
        }
        updateCustPreference();
        return root;
    }

    private void hideSdEncryptionIfNeeded(PreferenceScreen root) {
        Preference tmpPref = root.findPreference("security_category_encryption_sd");
        if (tmpPref == null || !(tmpPref instanceof PreferenceCategory)) {
            SdLog.e("SecuritySettings", "encryptionCategory == null");
            return;
        }
        PreferenceCategory encryptionCategory = (PreferenceCategory) tmpPref;
        boolean isPrimaryUser = this.mUm.isPrimaryUser();
        SdLog.i("SecuritySettings", "User isPrimary = " + String.valueOf(isPrimaryUser));
        if (isPrimaryUser && this.mSdCryptFeatureAvailable) {
            Preference tmpTwoSummaryPref = root.findPreference("sdcardencryption");
            if (tmpTwoSummaryPref == null || !(tmpTwoSummaryPref instanceof TwoSummaryPreference)) {
                SdLog.e("SecuritySettings", "encryption preference == null");
                return;
            }
            TwoSummaryPreference sdEncryptionPref = (TwoSummaryPreference) tmpTwoSummaryPref;
            String state = SdEncryptionUtils.getSdCryptionState(sdEncryptionPref.getContext());
            if ("no_card".equals(state)) {
                sdEncryptionPref.setNetherSummary(getString(2131628782));
                sdEncryptionPref.setEnabled(false);
            } else if (state.equals("disable") || state.equals("encrypting")) {
                sdEncryptionPref.setTitle(2131628778);
                sdEncryptionPref.setNetherSummary(getString(2131628779));
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
                } else {
                    sdEncryptionPref.setEnabled(false);
                }
            } else if (state.equals("decrypting")) {
                sdEncryptionPref.setTitle(2131628780);
                sdEncryptionPref.setNetherSummary(getString(2131628781));
                sdEncryptionPref.setEnabled(true);
            } else {
                sdEncryptionPref.setEnabled(false);
            }
            setSdCryptionClickListener(sdEncryptionPref);
        } else {
            root.removePreference(encryptionCategory);
        }
    }

    private void setSdCryptionClickListener(Preference pref) {
        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                String state = SdEncryptionUtils.getSdCryptionState(SecuritySettings.this.getActivity());
                if ("no_card".equals(state)) {
                    return true;
                }
                if ("enable".equals(state)) {
                    SecuritySettings.this.mDisallowDecryptDialog = SdEncryptionUtils.getDisallowDialog(SecuritySettings.this.getActivity());
                    if (SecuritySettings.this.mDisallowDecryptDialog != null) {
                        SecuritySettings.this.mDisallowDecryptDialog.show();
                        return true;
                    }
                }
                SecuritySettings.this.startActivity(new Intent(SecuritySettings.this.getContext(), SdEncryptionSettingsActivity.class));
                return true;
            }
        });
    }

    private void maybeAddFingerprintPreference(PreferenceGroup securityCategory, int userId) {
        Preference fingerprintPreference = FingerprintSettings.getFingerprintPreferenceForUser(securityCategory.getContext(), userId);
        if (fingerprintPreference != null) {
            securityCategory.addPreference(fingerprintPreference);
        }
    }

    private void addTrustAgentSettings(PreferenceGroup securityCategory) {
        boolean isSecure = this.mLockPatternUtils != null ? this.mLockPatternUtils.isSecure(MY_USER_ID) : false;
        ArrayList<TrustAgentComponentInfo> agents = getActiveTrustAgents(getActivity(), this.mLockPatternUtils, this.mDPM);
        for (int i = 0; i < agents.size(); i++) {
            TrustAgentComponentInfo agent = (TrustAgentComponentInfo) agents.get(i);
            RestrictedPreference trustAgentPreference = new RestrictedPreference(securityCategory.getContext());
            trustAgentPreference.setKey("trust_agent");
            trustAgentPreference.setTitle(agent.title);
            trustAgentPreference.setSummary(agent.summary);
            Intent intent = new Intent();
            intent.setComponent(agent.componentName);
            intent.setAction("android.intent.action.MAIN");
            trustAgentPreference.setIntent(intent);
            securityCategory.addPreference(trustAgentPreference);
            trustAgentPreference.setDisabledByAdmin(agent.admin);
            if (!(trustAgentPreference.isDisabledByAdmin() || isSecure)) {
                trustAgentPreference.setEnabled(false);
                trustAgentPreference.setSummary(2131624803);
            }
        }
    }

    public static ArrayList<TrustAgentComponentInfo> getActiveTrustAgents(Context context, LockPatternUtils utils, DevicePolicyManager dpm) {
        PackageManager pm = context.getPackageManager();
        ArrayList<TrustAgentComponentInfo> result = new ArrayList();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(TRUST_AGENT_INTENT, 128);
        if (utils == null) {
            return result;
        }
        List<ComponentName> enabledTrustAgents = utils.getEnabledTrustAgents(MY_USER_ID);
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(context, 16, UserHandle.myUserId());
        if (!(enabledTrustAgents == null || enabledTrustAgents.isEmpty())) {
            for (int i = 0; i < resolveInfos.size(); i++) {
                ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
                if (resolveInfo.serviceInfo != null && TrustAgentUtils.checkProvidePermission(resolveInfo, pm)) {
                    TrustAgentComponentInfo trustAgentComponentInfo = TrustAgentUtils.getSettingsComponent(pm, resolveInfo);
                    if (!(trustAgentComponentInfo.componentName == null || !enabledTrustAgents.contains(TrustAgentUtils.getComponentName(resolveInfo)) || TextUtils.isEmpty(trustAgentComponentInfo.title))) {
                        if (admin != null && dpm.getTrustAgentConfiguration(null, TrustAgentUtils.getComponentName(resolveInfo)) == null) {
                            trustAgentComponentInfo.admin = admin;
                        }
                        result.add(trustAgentComponentInfo);
                    }
                }
            }
        }
        return result;
    }

    private boolean isNonMarketAppsAllowed() {
        return Global.getInt(getContentResolver(), "install_non_market_apps", 0) > 0;
    }

    protected void setNonMarketAppsAllowed(boolean enabled) {
        if (!((UserManager) getActivity().getSystemService("user")).hasUserRestriction("no_install_unknown_sources")) {
            Global.putInt(getContentResolver(), "install_non_market_apps", enabled ? 1 : 0);
        }
    }

    protected void warnAppInstallation() {
        if (Utils.isChinaArea()) {
            View view = LayoutInflater.from(getActivity()).inflate(2130968748, null);
            this.mAppVerification = (CheckBox) view.findViewById(2131886515);
            this.mCheckboxLayout = (LinearLayout) view.findViewById(2131886514);
            this.mCheckboxLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (SecuritySettings.this.mAppVerification != null) {
                        SecuritySettings.this.mAppVerification.setChecked(!SecuritySettings.this.mAppVerification.isChecked());
                    }
                }
            });
            this.mWarnInstallApps = new Builder(getActivity()).setTitle(getResources().getString(2131625399)).setIcon(17301543).setView(view).setPositiveButton(17039379, this).setNegativeButton(17039369, this).setOnDismissListener(this).show();
            return;
        }
        this.mWarnInstallApps = new Builder(getActivity()).setTitle(getResources().getString(2131625399)).setIcon(17301543).setMessage(getResources().getString(2131625596)).setPositiveButton(17039379, this).setNegativeButton(17039369, this).setOnDismissListener(this).show();
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean turnOn = true;
        if (dialog == this.mWarnInstallApps) {
            if (which != -1) {
                turnOn = false;
            }
            setNonMarketAppsAllowed(turnOn);
            if (this.mToggleAppInstallation != null) {
                this.mToggleAppInstallation.setChecked(turnOn);
            }
            handlemWarnInstallAppsBtn(which);
            if (turnOn && this.mToggleAppInstallation != null) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, this.mToggleAppInstallation.getKey(), "on");
            }
        } else if (dialog == this.mWarnAppDownload) {
            if (which != -1) {
                turnOn = false;
            }
            setAppDownloadAllowed(turnOn);
            if (this.mToggleDownloadApplication != null) {
                this.mToggleDownloadApplication.setChecked(turnOn);
            }
            if (turnOn && this.mToggleDownloadApplication != null) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, this.mToggleDownloadApplication.getKey(), "on");
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWarnInstallApps != null) {
            this.mWarnInstallApps.dismiss();
        }
        if (this.mWarnAppDownload != null) {
            this.mWarnAppDownload.dismiss();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mTrustAgentClickIntent != null) {
            outState.putParcelable("trust_agent_click_intent", this.mTrustAgentClickIntent);
        }
    }

    public void onResume() {
        boolean z = false;
        super.onResume();
        createPreferenceHierarchy();
        refreshUi();
        if (this.mShowPassword != null) {
            this.mShowPassword.setChecked(System.getInt(getContentResolver(), "show_password", 1) != 0);
        }
        if (this.mResetCredentials != null && !this.mResetCredentials.isDisabledByAdmin()) {
            RestrictedPreference restrictedPreference = this.mResetCredentials;
            if (!this.mKeyStore.isEmpty()) {
                z = true;
            }
            restrictedPreference.setEnabled(z);
            if (!Utils.isWifiOnly(getActivity())) {
                registerSimStateReceiver();
            }
        }
    }

    public void onPause() {
        if (this.mDisallowDecryptDialog != null && this.mDisallowDecryptDialog.isShowing()) {
            this.mDisallowDecryptDialog.dismiss();
            this.mDisallowDecryptDialog = null;
        }
        super.onPause();
        if (this.mResetCredentials != null && !Utils.isWifiOnly(getActivity())) {
            unRegisterSimStateReceiver();
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!"trust_agent".equals(preference.getKey())) {
            return super.onPreferenceTreeClick(preference);
        }
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);
        this.mTrustAgentClickIntent = preference.getIntent();
        if (!(helper.launchConfirmationActivity(126, preference.getTitle()) || this.mTrustAgentClickIntent == null)) {
            startActivity(this.mTrustAgentClickIntent);
            this.mTrustAgentClickIntent = null;
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 126 && resultCode == -1) {
            if (this.mTrustAgentClickIntent != null) {
                startActivity(this.mTrustAgentClickIntent);
                this.mTrustAgentClickIntent = null;
            }
            return;
        }
        createPreferenceHierarchy();
        refreshUi();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        LockPatternUtils lockPatternUtils = this.mChooseLockSettingsHelper.utils();
        if (!"show_password".equals(key)) {
            return super.onPreferenceChange(preference, value);
        }
        System.putInt(getContentResolver(), "show_password", ((Boolean) value).booleanValue() ? 1 : 0);
        lockPatternUtils.setVisiblePasswordEnabled(((Boolean) value).booleanValue(), MY_USER_ID);
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        return true;
    }

    protected int getHelpResource() {
        return 2131626551;
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mToggleAppInstallation != null) {
            this.mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());
        }
        if (this.mToggleDownloadApplication != null) {
            this.mToggleDownloadApplication.setChecked(isAppDownloadAllowed());
        }
    }
}
