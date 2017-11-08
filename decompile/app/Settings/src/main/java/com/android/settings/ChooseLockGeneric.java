package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import java.util.List;

public class ChooseLockGeneric extends SettingsActivity {
    private ChooseLockWindowListener mWindowListener = new ChooseLockWindowListener();

    public static class ChooseLockGenericFragment extends ChooseLockGenericFragmentHwBase {
        private long mChallenge;
        private DevicePolicyManager mDPM;
        private boolean mEncryptionRequestDisabled;
        private int mEncryptionRequestQuality;
        private FingerprintManager mFingerprintManager;
        private boolean mForChangeCredRequiredForBoot = false;
        protected boolean mForFingerprint = false;
        private boolean mHasChallenge = false;
        private boolean mHideDrawer = false;
        private KeyStore mKeyStore;
        private LockPatternUtils mLockPatternUtils;
        private ManagedLockPasswordProvider mManagedPasswordProvider;
        private boolean mPasswordConfirmed = false;
        private boolean mRequirePassword;
        private int mUserId;
        private String mUserPassword;
        private boolean mWaitingForConfirmation = false;

        public static class FactoryResetProtectionWarningDialog extends DialogFragment {
            public static FactoryResetProtectionWarningDialog newInstance(int titleRes, int messageRes, String unlockMethodToSet) {
                FactoryResetProtectionWarningDialog frag = new FactoryResetProtectionWarningDialog();
                Bundle args = new Bundle();
                args.putInt("titleRes", titleRes);
                args.putInt("messageRes", messageRes);
                args.putString("unlockMethodToSet", unlockMethodToSet);
                frag.setArguments(args);
                return frag;
            }

            public void show(FragmentManager manager, String tag) {
                if (manager.findFragmentByTag(tag) == null) {
                    super.show(manager, tag);
                }
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final Bundle args = getArguments();
                return new Builder(getActivity()).setTitle(args.getInt("titleRes")).setMessage(args.getInt("messageRes")).setPositiveButton(2131624770, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ((ChooseLockGenericFragment) FactoryResetProtectionWarningDialog.this.getParentFragment()).setUnlockMethod(args.getString("unlockMethodToSet"));
                    }
                }).setNegativeButton(2131624572, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        FactoryResetProtectionWarningDialog.this.dismiss();
                    }
                }).create();
            }
        }

        protected int getMetricsCategory() {
            return 27;
        }

        public void onCreate(Bundle savedInstanceState) {
            boolean z;
            super.onCreate(savedInstanceState);
            this.mUserId = getActivity().getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
            this.mFingerprintManager = (FingerprintManager) getActivity().getSystemService("fingerprint");
            this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
            this.mKeyStore = KeyStore.getInstance();
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            boolean confirmCredentials = getActivity().getIntent().getBooleanExtra("confirm_credentials", true);
            if (getActivity() instanceof InternalActivity) {
                if (confirmCredentials) {
                    z = false;
                } else {
                    z = true;
                }
                this.mPasswordConfirmed = z;
            }
            this.mHideDrawer = getActivity().getIntent().getBooleanExtra(":settings:hide_drawer", false);
            this.mHasChallenge = getActivity().getIntent().getBooleanExtra("has_challenge", false);
            this.mChallenge = getActivity().getIntent().getLongExtra("challenge", 0);
            this.mForFingerprint = getActivity().getIntent().getBooleanExtra("for_fingerprint", false);
            if (getArguments() != null) {
                z = getArguments().getBoolean("for_cred_req_boot");
            } else {
                z = false;
            }
            this.mForChangeCredRequiredForBoot = z;
            if (savedInstanceState != null) {
                this.mPasswordConfirmed = savedInstanceState.getBoolean("password_confirmed");
                this.mWaitingForConfirmation = savedInstanceState.getBoolean("waiting_for_confirmation");
                this.mEncryptionRequestQuality = savedInstanceState.getInt("encrypt_requested_quality");
                this.mEncryptionRequestDisabled = savedInstanceState.getBoolean("encrypt_requested_disabled");
            }
            int targetUser = Utils.getSecureTargetUser(getActivity().getActivityToken(), UserManager.get(getActivity()), null, getActivity().getIntent().getExtras()).getIdentifier();
            if (!"android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(getActivity().getIntent().getAction()) && this.mLockPatternUtils.isSeparateProfileChallengeAllowed(targetUser)) {
                this.mUserId = targetUser;
            }
            if ("android.app.action.SET_NEW_PASSWORD".equals(getActivity().getIntent().getAction()) && Utils.isManagedProfile(UserManager.get(getActivity()), this.mUserId) && this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId)) {
                getActivity().setTitle(2131624719);
            }
            this.mManagedPasswordProvider = ManagedLockPasswordProvider.get(getActivity(), this.mUserId);
            if (this.mPasswordConfirmed) {
                updatePreferencesOrFinish();
                if (this.mForChangeCredRequiredForBoot) {
                    maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                }
            } else if (!this.mWaitingForConfirmation) {
                ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);
                boolean managedProfileWithUnifiedLock = Utils.isManagedProfile(UserManager.get(getActivity()), this.mUserId) ? !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId) : false;
                if (managedProfileWithUnifiedLock || !helper.launchConfirmationActivity(100, getString(2131625526), true, this.mUserId)) {
                    this.mPasswordConfirmed = true;
                    updatePreferencesOrFinish();
                    return;
                }
                this.mWaitingForConfirmation = true;
            }
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if ("unlock_set_none".equals(key)) {
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            }
            if (needConfirm(preference, key)) {
                return true;
            }
            if (isUnlockMethodSecure(key) || !this.mLockPatternUtils.isSecure(this.mUserId) || !Utils.isFRPEnabled()) {
                return setUnlockMethod(key);
            }
            showFactoryResetProtectionWarningDialog(key);
            return true;
        }

        protected boolean needConfirm(Preference preference, String key) {
            if (PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), this.mUserId)) {
                showQuickSwitchUnavailableDialog(key);
                return true;
            } else if (!BiometricManager.isFingerprintSupported(getActivity()) || !this.mIsLastLockTypePinOrPasswd || "unlock_set_pin".equals(key) || "unlock_set_password".equals(key)) {
                return false;
            } else {
                if (!isUnlockMethodSecure(key) && this.mLockPatternUtils.isSecure(UserHandle.myUserId()) && Utils.isFRPEnabled()) {
                    showFactoryResetProtectionWarningDialog(key);
                    return true;
                }
                showSwitchLockDlg(key, preference.getTitle());
                return true;
            }
        }

        protected boolean respondPreferenceClick(String key) {
            return setUnlockMethod(key);
        }

        private void maybeEnableEncryption(int quality, boolean disabled) {
            boolean z = false;
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService("device_policy");
            if (UserManager.get(getActivity()).isAdminUser() && this.mUserId == UserHandle.myUserId() && LockPatternUtils.isDeviceEncryptionEnabled() && !LockPatternUtils.isFileEncryptionEnabled() && !dpm.getDoNotAskCredentialsOnBoot()) {
                if (SystemProperties.getBoolean("ro.config.decrypt_require_pwd", false)) {
                    this.mEncryptionRequestQuality = quality;
                    this.mEncryptionRequestDisabled = disabled;
                    Intent unlockMethodIntent = getIntentForUnlockMethod(quality, disabled);
                    unlockMethodIntent.putExtra("for_cred_req_boot", this.mForChangeCredRequiredForBoot);
                    Context context = getActivity();
                    boolean accEn = AccessibilityManager.getInstance(context).isEnabled();
                    LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
                    if (!accEn) {
                        z = true;
                    }
                    Intent intent = getEncryptionInterstitialIntent(context, quality, lockPatternUtils.isCredentialRequiredToDecrypt(z), unlockMethodIntent);
                    intent.putExtra("for_fingerprint", this.mForFingerprint);
                    intent.putExtra(":settings:hide_drawer", this.mHideDrawer);
                    startActivityForResult(intent, 101);
                } else if (this.mForChangeCredRequiredForBoot) {
                    finish();
                } else {
                    this.mRequirePassword = false;
                    updateUnlockMethodAndFinish(quality, disabled);
                }
            } else if (this.mForChangeCredRequiredForBoot) {
                finish();
            } else {
                this.mRequirePassword = false;
                updateUnlockMethodAndFinish(quality, disabled);
            }
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            this.mWaitingForConfirmation = false;
            if (requestCode == 100 && resultCode == -1) {
                this.mPasswordConfirmed = true;
                if (data != null) {
                    this.mUserPassword = data.getStringExtra("password");
                }
                updatePreferencesOrFinish();
                if (this.mForChangeCredRequiredForBoot) {
                    if (TextUtils.isEmpty(this.mUserPassword)) {
                        finish();
                    } else {
                        maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                    }
                }
            } else if (requestCode == 101 && resultCode == -1) {
                if (this.mForChangeCredRequiredForBoot) {
                    getActivity().setResult(resultCode, data);
                    finish();
                }
            } else if (requestCode == 102) {
                int quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId);
                if (quality == 65536 && resultCode == 1) {
                    Log.i("ChooseLockGenericFragment", "Remove all fingerprints due to Pattern Lock switch.");
                    removeAllFingerprintForUserAndFinish(this.mUserId);
                }
                int userType = getActivity().getIntent().getIntExtra("hidden_space_main_user_type", -1);
                if (1 == userType && resultCode == 1 && quality >= 131072 && BiometricManager.isFingerprintSupported(getActivity())) {
                    updatePreferencesOrFinish();
                    new PrivacySpaceSettingsHelper((Fragment) this).launchFingerPrintEnrollConfirmDialog(data.getByteArrayExtra("hw_auth_token"), quality, this.mUserId, userType);
                    return;
                } else if (resultCode != 0 || this.mForChangeCredRequiredForBoot) {
                    getActivity().setResult(resultCode, data);
                    finish();
                }
            } else if (requestCode == 401 || requestCode == 201) {
                getActivity().setResult(-1, data);
                finish();
            } else {
                getActivity().setResult(0);
                finish();
            }
            if (requestCode == 0 && this.mForChangeCredRequiredForBoot) {
                finish();
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("password_confirmed", this.mPasswordConfirmed);
            outState.putBoolean("waiting_for_confirmation", this.mWaitingForConfirmation);
            outState.putInt("encrypt_requested_quality", this.mEncryptionRequestQuality);
            outState.putBoolean("encrypt_requested_disabled", this.mEncryptionRequestDisabled);
        }

        private void updatePreferencesOrFinish() {
            if (new PrivacyModeManager(getActivity()).isPrivacyModeEnabled()) {
                getActivity().finish();
                Intent intent = new Intent("com.huawei.privacymode.action.CHANGE_PASSWORD");
                intent.setPackage("com.huawei.privacymode");
                intent.putExtra("confirm_credentials", false);
                startActivity(intent);
                return;
            }
            intent = getActivity().getIntent();
            int quality = intent.getIntExtra("lockscreen.password_type", -1);
            if (quality == -1) {
                quality = upgradeQuality(intent.getIntExtra("minimum_quality", -1));
                boolean hideDisabledPrefs = intent.getBooleanExtra("hide_disabled_prefs", false);
                PreferenceScreen prefScreen = getPreferenceScreen();
                if (prefScreen != null) {
                    prefScreen.removeAll();
                }
                addPreferences();
                disableUnusablePreferences(quality, hideDisabledPrefs);
                updatePreference();
                updateCurrentPreference();
            } else {
                updateUnlockMethodAndFinish(quality, false);
            }
            setDividerStyle();
        }

        private void setDividerStyle() {
            if (SettingsExtUtils.isStartupGuideMode(getActivity().getContentResolver())) {
                PreferenceScreen root = getPreferenceScreen();
                int count = root.getPreferenceCount();
                if (count > 0) {
                    for (int index = 0; index < count; index++) {
                        Preference origPref = root.getPreference(index);
                        if (origPref != null && (origPref instanceof CustomDividerPreference)) {
                            CustomDividerPreference pref = (CustomDividerPreference) origPref;
                            pref.setAllowAbove(true);
                            pref.setAllowBelow(true);
                        }
                    }
                }
            }
        }

        protected void addPreferences() {
            boolean isHuaweiUnlockEnabled = false;
            addPreferencesFromResource(SettingsExtUtils.getScreenlockSettingsId(getActivity()));
            if (System.getInt(getActivity().getContentResolver(), "hw_unlock_enabled", 0) > 0) {
                isHuaweiUnlockEnabled = true;
            }
            if (isHuaweiUnlockEnabled) {
                findPreference("unlock_set_huawei").setViewId(2131886091);
            } else {
                findPreference("unlock_set_none").setViewId(2131886091);
            }
            findPreference("unlock_set_pin").setViewId(2131886092);
            findPreference("unlock_set_password").setViewId(2131886093);
        }

        private void updatePreference() {
            if (this.mManagedPasswordProvider.isSettingManagedPasswordSupported()) {
                findPreference("unlock_set_managed").setTitle(this.mManagedPasswordProvider.getPickerOptionTitle(this.mForFingerprint));
            } else {
                removePreference("unlock_set_managed");
            }
        }

        private void updateCurrentPreference() {
            Preference preference = findPreference(getKeyForCurrent());
            if (preference != null) {
                preference.setSummary(2131624740);
            }
        }

        private String getKeyForCurrent() {
            int credentialOwner = UserManager.get(getContext()).getCredentialOwnerProfile(this.mUserId);
            if (this.mLockPatternUtils.isLockScreenDisabled(credentialOwner)) {
                return "unlock_set_off";
            }
            switch (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(credentialOwner)) {
                case 0:
                    return "unlock_set_none";
                case 65536:
                    return "unlock_set_pattern";
                case 131072:
                case 196608:
                    return "unlock_set_pin";
                case 262144:
                case 327680:
                case 393216:
                    return "unlock_set_password";
                case 524288:
                    return "unlock_set_managed";
                default:
                    return null;
            }
        }

        private int upgradeQuality(int quality) {
            return upgradeQualityForDPM(quality);
        }

        private int upgradeQualityForDPM(int quality) {
            int minQuality = this.mDPM.getPasswordQuality(null, this.mUserId);
            if (quality < minQuality) {
                return minQuality;
            }
            return quality;
        }

        protected void disableUnusablePreferences(int quality, boolean hideDisabledPrefs) {
            disableUnusablePreferencesImpl(quality, hideDisabledPrefs);
        }

        protected void disableUnusablePreferencesImpl(int quality, boolean hideDisabled) {
            PreferenceScreen entries = getPreferenceScreen();
            boolean isHidePatternAndPassword = getActivity().getIntent().getBooleanExtra("hide_pattern_and_password", false);
            int adminEnforcedQuality = this.mDPM.getPasswordQuality(null, this.mUserId);
            EnforcedAdmin enforcedAdmin = RestrictedLockUtils.checkIfPasswordQualityIsSet(getActivity(), this.mUserId);
            boolean isUnlockMethodSecure = this.mLockPatternUtils.isSecure(this.mUserId);
            for (int i = entries.getPreferenceCount() - 1; i >= 0; i--) {
                Preference pref = entries.getPreference(i);
                if (pref instanceof RestrictedPreference) {
                    String key = pref.getKey();
                    boolean enabled = true;
                    boolean z = true;
                    boolean disabledByAdmin = false;
                    if ("unlock_set_off".equals(key)) {
                        enabled = quality <= 0;
                        if (getResources().getBoolean(2131492877)) {
                            enabled = false;
                            z = false;
                        }
                        disabledByAdmin = adminEnforcedQuality > 0;
                    } else if ("unlock_set_none".equals(key) || "unlock_set_huawei".equals(key)) {
                        if (this.mUserId != UserHandle.myUserId()) {
                            z = false;
                        }
                        enabled = quality <= 0;
                        disabledByAdmin = adminEnforcedQuality > 0;
                        if (getActivity().getIntent().getBooleanExtra("hide_no_password", false) || !MdppUtils.isCcModeDisabled()) {
                            z = false;
                        }
                        if (PrivacySpaceSettingsHelper.isPrivacyUser(getActivity(), UserHandle.myUserId())) {
                            z = false;
                        }
                        if (isHidePatternAndPassword) {
                            z = false;
                        }
                    } else if ("unlock_set_pattern".equals(key)) {
                        enabled = quality <= 65536;
                        if (!isUnlockMethodSecure && Utils.isDemoVersion()) {
                            enabled = false;
                        }
                        disabledByAdmin = adminEnforcedQuality > 65536;
                        if (isHidePatternAndPassword) {
                            z = false;
                        }
                    } else if ("unlock_set_pin".equals(key)) {
                        enabled = quality <= 196608;
                        if (!isUnlockMethodSecure && Utils.isDemoVersion()) {
                            enabled = false;
                        }
                        disabledByAdmin = adminEnforcedQuality > 196608;
                        if (this.mIsFromFp) {
                            if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
                                pref.setLayoutResource(2130968920);
                            }
                            pref.setWidgetLayoutResource(2130968998);
                        }
                    } else if ("unlock_set_password".equals(key)) {
                        enabled = quality <= 393216;
                        if (!isUnlockMethodSecure && Utils.isDemoVersion()) {
                            enabled = false;
                        }
                        disabledByAdmin = adminEnforcedQuality > 393216;
                        if (isHidePatternAndPassword) {
                            z = false;
                        }
                        if (this.mIsFromFp) {
                            if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
                                pref.setLayoutResource(2130968920);
                            }
                            pref.setWidgetLayoutResource(2130968998);
                        }
                    } else if ("unlock_set_managed".equals(key)) {
                        if (quality <= 524288) {
                            enabled = this.mManagedPasswordProvider.isManagedPasswordChoosable();
                        } else {
                            enabled = false;
                        }
                        disabledByAdmin = adminEnforcedQuality > 524288;
                    }
                    if (hideDisabled && !Utils.isDemoVersion()) {
                        z = enabled;
                    }
                    if (!("unlock_set_pin".equals(key) || "unlock_set_password".equals(key))) {
                        enabled = enabled && !this.mIsFromFp;
                        z = z && !this.mIsFromFp;
                    }
                    if (!z) {
                        entries.removePreference(pref);
                    } else if (disabledByAdmin && enforcedAdmin != null) {
                        ((RestrictedPreference) pref).setDisabledByAdmin(enforcedAdmin);
                        if (!this.mIsFromFp) {
                            pref.setSummary(2131624744);
                        }
                    } else if (enabled) {
                        ((RestrictedPreference) pref).setDisabledByAdmin(null);
                    } else {
                        ((RestrictedPreference) pref).setDisabledByAdmin(null);
                        if (!this.mIsFromFp) {
                            pref.setSummary(2131624744);
                        }
                        pref.setEnabled(false);
                    }
                }
            }
        }

        protected Intent getLockManagedPasswordIntent(boolean requirePassword, String password) {
            return this.mManagedPasswordProvider.createIntent(requirePassword, password);
        }

        protected Intent getLockPasswordIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, long challenge, int userId) {
            return ChooseLockPassword.createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, challenge, userId);
        }

        protected Intent getLockPasswordIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, String password, int userId) {
            return ChooseLockPassword.createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, password, userId);
        }

        protected Intent getLockPatternIntent(Context context, boolean requirePassword, long challenge, int userId) {
            return ChooseLockPattern.createIntent(context, requirePassword, challenge, userId);
        }

        protected Intent getLockPatternIntent(Context context, boolean requirePassword, String pattern, int userId) {
            return ChooseLockPattern.createIntent(context, requirePassword, pattern, userId);
        }

        protected Intent getEncryptionInterstitialIntent(Context context, int quality, boolean required, Intent unlockMethodIntent) {
            return EncryptionInterstitial.createStartIntent(context, quality, required, unlockMethodIntent);
        }

        void updateUnlockMethodAndFinish(int quality, boolean disabled) {
            if (this.mPasswordConfirmed) {
                quality = upgradeQuality(quality);
                Intent intent = getIntentForUnlockMethod(quality, disabled);
                if (intent != null) {
                    startActivityForResult(intent, 102);
                    return;
                }
                if (quality == 0) {
                    this.mLockPatternUtils.setSeparateProfileChallengeEnabled(this.mUserId, true, this.mUserPassword);
                    this.mChooseLockSettingsHelper.utils().clearLock(this.mUserId);
                    this.mChooseLockSettingsHelper.utils().setLockScreenDisabled(disabled, this.mUserId);
                    if (BiometricManager.isFingerprintSupported(getPrefContext())) {
                        removeAllFingerprintForUserAndFinish(this.mUserId);
                    } else {
                        finish();
                    }
                    getActivity().setResult(-1);
                } else {
                    removeAllFingerprintForUserAndFinish(this.mUserId);
                }
                return;
            }
            throw new IllegalStateException("Tried to update password without confirming it");
        }

        private Intent getIntentForUnlockMethod(int quality, boolean disabled) {
            Intent intent = null;
            Context context = getActivity();
            if (quality >= 524288) {
                intent = getLockManagedPasswordIntent(this.mRequirePassword, this.mUserPassword);
            } else if (quality >= 131072) {
                int minLength = this.mDPM.getPasswordMinimumLength(null, this.mUserId);
                if (minLength < 4) {
                    minLength = 4;
                }
                int maxLength = this.mDPM.getPasswordMaximumLength(quality);
                if (this.mHasChallenge) {
                    intent = getLockPasswordIntent(context, quality, minLength, maxLength, this.mRequirePassword, this.mChallenge, this.mUserId);
                    if (this.mUserPassword != null) {
                        intent.putExtra("password", this.mUserPassword);
                    }
                } else {
                    intent = getLockPasswordIntent(context, quality, minLength, maxLength, this.mRequirePassword, this.mUserPassword, this.mUserId);
                }
            } else if (quality == 65536) {
                if (this.mHasChallenge) {
                    intent = getLockPatternIntent(context, this.mRequirePassword, this.mChallenge, this.mUserId);
                    if (this.mUserPassword != null) {
                        intent.putExtra("password", this.mUserPassword);
                    }
                } else {
                    intent = getLockPatternIntent(context, this.mRequirePassword, this.mUserPassword, this.mUserId);
                }
            }
            if (intent != null) {
                intent.putExtra(":settings:hide_drawer", this.mHideDrawer);
            }
            return intent;
        }

        private void removeAllFingerprintForUserAndFinish(final int userId) {
            if (UserHandle.myUserId() == userId) {
                FingerprintUtils.removeAllFingerprintTemplates(getActivity(), new RemovalCallback() {
                    public void onRemovalSucceeded(Fingerprint fingerprint) {
                        Log.v("ChooseLockGenericFragment", "Fingerprint removed: " + fingerprint.getFingerId());
                        if (ChooseLockGenericFragment.this.getActivity() != null) {
                            if (System.getIntForUser(ChooseLockGenericFragment.this.getActivity().getContentResolver(), "fingerprint_alipay_dialog", 1, userId) != 1) {
                                System.putIntForUser(ChooseLockGenericFragment.this.getActivity().getContentResolver(), "fingerprint_alipay_dialog", 1, userId);
                            }
                            FingerprintUtils.onFingerprintNumChanged(ChooseLockGenericFragment.this.getActivity(), userId);
                        }
                        if (ChooseLockGenericFragment.this.mFingerprintManager.getEnrolledFingerprints().size() == 0) {
                            ChooseLockGenericFragment.this.finish();
                        }
                    }

                    public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                        if (ChooseLockGenericFragment.this.getActivity() != null) {
                            Toast.makeText(ChooseLockGenericFragment.this.getActivity(), errString, 0);
                        }
                        ChooseLockGenericFragment.this.finish();
                    }
                }, this.mFingerprintManager);
            } else if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
                finish();
            } else if (this.mFingerprintManager.hasEnrolledFingerprints(userId)) {
                this.mFingerprintManager.setActiveUser(userId);
                int groupId = userId;
                this.mFingerprintManager.remove(new Fingerprint(null, userId, 0, 0), userId, new RemovalCallback() {
                    public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                        Log.v("ChooseLockGenericFragment", "Fingerprint removed: " + fp.getFingerId());
                        if (fp.getFingerId() == 0) {
                            ChooseLockGenericFragment.this.removeManagedProfileFingerprintsAndFinishIfNecessary(userId);
                        }
                    }

                    public void onRemovalSucceeded(Fingerprint fingerprint) {
                        if (fingerprint.getFingerId() == 0) {
                            ChooseLockGenericFragment.this.removeManagedProfileFingerprintsAndFinishIfNecessary(userId);
                        }
                    }
                });
            } else {
                removeManagedProfileFingerprintsAndFinishIfNecessary(userId);
            }
        }

        private void removeManagedProfileFingerprintsAndFinishIfNecessary(int parentUserId) {
            this.mFingerprintManager.setActiveUser(UserHandle.myUserId());
            Context context = getActivity();
            if (context != null) {
                UserManager um = UserManager.get(context);
                boolean hasChildProfile = false;
                if (um != null) {
                    try {
                        if (!um.getUserInfo(parentUserId).isManagedProfile()) {
                            List<UserInfo> profiles = um.getProfiles(parentUserId);
                            int profilesSize = profiles.size();
                            for (int i = 0; i < profilesSize; i++) {
                                UserInfo userInfo = (UserInfo) profiles.get(i);
                                if (userInfo.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userInfo.id)) {
                                    removeAllFingerprintForUserAndFinish(userInfo.id);
                                    hasChildProfile = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("ChooseLockGenericFragment", "removeManagedProfileFingerprintsAndFinishIfNecessary()-->ex : " + ex);
                    }
                }
                if (!hasChildProfile) {
                    finish();
                }
            }
        }

        public void onDestroy() {
            super.onDestroy();
        }

        protected int getHelpResource() {
            return 2131626545;
        }

        private int getResIdForFactoryResetProtectionWarningTitle() {
            if (Utils.isManagedProfile(UserManager.get(getActivity()), this.mUserId)) {
                return 2131624753;
            }
            return 2131624752;
        }

        private int getResIdForFactoryResetProtectionWarningMessage() {
            boolean hasFingerprints = this.mFingerprintManager.hasEnrolledFingerprints(this.mUserId);
            boolean isProfile = Utils.isManagedProfile(UserManager.get(getActivity()), this.mUserId);
            switch (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId)) {
                case 65536:
                    if (hasFingerprints && isProfile) {
                        return 2131624763;
                    }
                    if (hasFingerprints && !isProfile) {
                        return 2131624755;
                    }
                    if (isProfile) {
                        return 2131624762;
                    }
                    return 2131624754;
                case 131072:
                case 196608:
                    if (hasFingerprints && isProfile) {
                        return 2131624765;
                    }
                    if (hasFingerprints && !isProfile) {
                        return 2131624757;
                    }
                    if (isProfile) {
                        return 2131624764;
                    }
                    return 2131624756;
                case 262144:
                case 327680:
                case 393216:
                case 524288:
                    if (hasFingerprints && isProfile) {
                        return 2131624767;
                    }
                    if (hasFingerprints && !isProfile) {
                        return 2131624759;
                    }
                    if (isProfile) {
                        return 2131624766;
                    }
                    return 2131624758;
                default:
                    if (hasFingerprints && isProfile) {
                        return 2131624769;
                    }
                    if (hasFingerprints && !isProfile) {
                        return 2131624761;
                    }
                    if (isProfile) {
                        return 2131624768;
                    }
                    return 2131624760;
            }
        }

        private boolean isUnlockMethodSecure(String unlockMethod) {
            return ("unlock_set_off".equals(unlockMethod) || "unlock_set_none".equals(unlockMethod) || "unlock_set_huawei".equals(unlockMethod)) ? false : true;
        }

        protected boolean setUnlockMethod(String unlockMethod) {
            boolean handled = true;
            EventLog.writeEvent(90200, unlockMethod);
            if ("unlock_set_off".equals(unlockMethod)) {
                updateUnlockMethodAndFinish(0, true);
            } else if ("unlock_set_none".equals(unlockMethod)) {
                updateUnlockMethodAndFinish(0, false);
            } else if ("unlock_set_huawei".equals(unlockMethod)) {
                updateUnlockMethodAndFinish(0, false);
                Secure.putString(getContentResolver(), "lockscreen_package", "hw_unlock");
            } else if ("unlock_set_managed".equals(unlockMethod)) {
                maybeEnableEncryption(524288, false);
            } else if ("unlock_set_pattern".equals(unlockMethod)) {
                maybeEnableEncryption(65536, false);
            } else if ("unlock_set_pin".equals(unlockMethod)) {
                maybeEnableEncryption(131072, false);
            } else if ("unlock_set_password".equals(unlockMethod)) {
                maybeEnableEncryption(262144, false);
            } else {
                Log.e("ChooseLockGenericFragment", "Encountered unknown unlock method to set: " + unlockMethod);
                handled = false;
            }
            updateFingerprintEnableStatus();
            return handled;
        }

        private void showFactoryResetProtectionWarningDialog(String unlockMethodToSet) {
            FactoryResetProtectionWarningDialog.newInstance(getResIdForFactoryResetProtectionWarningTitle(), getResIdForFactoryResetProtectionWarningMessage(), unlockMethodToSet).show(getChildFragmentManager(), "frp_warning_dialog");
        }
    }

    private static class ChooseLockWindowListener implements OnApplyWindowInsetsListener {
        int statusBarHeight;
        View windowView;

        private ChooseLockWindowListener() {
            this.windowView = null;
            this.statusBarHeight = 0;
        }

        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            if (this.windowView != null) {
                this.windowView.setPadding(0, this.statusBarHeight, 0, 0);
            }
            return insets;
        }
    }

    public static class InternalActivity extends ChooseLockGeneric {
    }

    protected boolean shouldHideActionBarInStartupGuide() {
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int statusBarHeight = Utils.getStatusBarHeight(this);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (shouldHideActionBarInStartupGuide()) {
                SettingsExtUtils.hideActionBarInStartupGuide(this);
                Utils.hideNavigationBar(getWindow(), 5890);
                LinearLayout parentView = (LinearLayout) findViewById(2131887012);
                parentView.setFitsSystemWindows(true);
                this.mWindowListener.statusBarHeight = statusBarHeight;
                this.mWindowListener.windowView = parentView;
                parentView.setOnApplyWindowInsetsListener(this.mWindowListener);
            }
        } else if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", getFragmentClass().getName());
        String action = modIntent.getAction();
        if ("android.app.action.SET_NEW_PASSWORD".equals(action) || "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(action)) {
            modIntent.putExtra(":settings:hide_drawer", true);
        }
        return modIntent;
    }

    protected void onResume() {
        super.onResume();
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            Utils.hideNavigationBar(getWindow(), 5890);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            Utils.hideNavigationBar(getWindow(), 5890);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockGenericFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    Class<? extends Fragment> getFragmentClass() {
        return ChooseLockGenericFragment.class;
    }
}
