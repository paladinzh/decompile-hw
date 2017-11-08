package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.settings.ConfirmLockPassword.InternalActivity;
import com.android.settings.fingerprint.enrollment.FingerprintEnrollActivity;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.huawei.android.os.UserManagerEx;
import java.util.List;

public final class PrivacySpaceSettingsHelper {
    private Activity mActivity;
    private Fragment mFragment;
    private LockPatternUtils mLockPatternUtils;
    private PasswordCheckListener mPasswordCheckListener;

    public interface PasswordCheckListener {
        void onCheckFinished(byte[] bArr);
    }

    public PrivacySpaceSettingsHelper(Activity activity) {
        this.mActivity = activity;
        this.mLockPatternUtils = new LockPatternUtils(this.mActivity);
    }

    public PrivacySpaceSettingsHelper(Fragment fragment) {
        this.mFragment = fragment;
        this.mActivity = this.mFragment.getActivity();
        this.mLockPatternUtils = new LockPatternUtils(this.mActivity);
    }

    public void setPasswordCheckListener(PasswordCheckListener listener) {
        this.mPasswordCheckListener = listener;
    }

    public static boolean isPrivacySpaceSupported() {
        return SystemProperties.getBoolean("ro.config.hw_privacySpace", true);
    }

    public static boolean isPrivacyUser(Context context, int userId) {
        if (context == null) {
            return false;
        }
        return isPrivacyUser(((UserManager) context.getSystemService("user")).getUserInfo(userId));
    }

    public static boolean isPrivacyUser(UserInfo userInfo) {
        return UserManagerEx.isHwHiddenSpace(userInfo);
    }

    public boolean isPrivacyUserAdded() {
        return getPrivacyUser() != null;
    }

    public UserInfo getPrivacyUser() {
        if (this.mActivity == null) {
            return null;
        }
        for (UserInfo user : ((UserManager) this.mActivity.getSystemService("user")).getUsers()) {
            if (isPrivacyUser(user)) {
                return user;
            }
        }
        return null;
    }

    public boolean canAddPrivacyUser() {
        boolean z = false;
        if (this.mActivity == null) {
            return false;
        }
        UserManager manager = (UserManager) this.mActivity.getSystemService("user");
        if (isPrivacySpaceSupported() && !isPrivacyUserAdded()) {
            z = manager.canAddMoreUsers();
        }
        return z;
    }

    public int getUserPasswordQuality(int userId) {
        if (this.mLockPatternUtils == null) {
            return -1;
        }
        return this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userId);
    }

    public boolean shouldHidePrivacyUserEntry() {
        boolean z = true;
        if (this.mActivity == null) {
            return false;
        }
        if (Global.getInt(this.mActivity.getContentResolver(), "hide_privacy_user_entry", 0) != 1) {
            z = false;
        }
        return z;
    }

    public boolean setPrivacyUserEntryHidden(boolean hidden) {
        int i = 0;
        if (this.mActivity == null) {
            return false;
        }
        ContentResolver contentResolver = this.mActivity.getContentResolver();
        String str = "hide_privacy_user_entry";
        if (hidden) {
            i = 1;
        }
        Global.putInt(contentResolver, str, i);
        return true;
    }

    public boolean hasPasswordLock(int userId) {
        return getUserPasswordQuality(userId) >= 131072;
    }

    public boolean hasPatternLock(int userId) {
        int quality = getUserPasswordQuality(userId);
        if (quality >= 131072 || quality < 65536) {
            return false;
        }
        return true;
    }

    public int getOwnerPasswordQuality() {
        return getUserPasswordQuality(0);
    }

    public boolean isOwnerSecure() {
        return isSecure(0);
    }

    public boolean isSecure(int userId) {
        if (this.mLockPatternUtils == null) {
            return false;
        }
        return this.mLockPatternUtils.isSecure(userId);
    }

    public boolean startActivityForResult(Intent intent, int requestCode) {
        if (this.mActivity == null || intent == null) {
            return false;
        }
        if (this.mFragment != null) {
            this.mActivity.startActivityFromFragment(this.mFragment, intent, requestCode);
        } else {
            this.mActivity.startActivityForResult(intent, requestCode);
        }
        return true;
    }

    public boolean launchChooseLockSettingsForMainUser(Intent intent) {
        if (intent == null || this.mActivity == null) {
            return false;
        }
        return launchChooseLockSettingsForMainUser(intent.getBooleanExtra("has_challenge", false), intent.getLongExtra("challenge", 0));
    }

    public boolean launchChooseLockSettingsForMainUser(boolean hasChallenge, long challenge) {
        if (this.mActivity == null) {
            return false;
        }
        Intent intent = Utils.onBuildStartFragmentIntent(this.mActivity, "com.android.settings.ChooseLockGeneric$ChooseLockGenericFragment", null, null, 2131628750, null, false);
        if (BiometricManager.isFingerprintSupported(this.mActivity)) {
            intent.putExtra("has_challenge", hasChallenge);
            intent.putExtra("challenge", challenge);
        }
        intent.putExtra("hide_no_password", true);
        intent.putExtra("hidden_space_main_user_type", 1);
        intent.putExtra("android.intent.extra.USER_ID", 0);
        return startActivityForResult(intent, 203);
    }

    public boolean launchPrivacySpaceWizard(boolean hasChallenge, long challenge) {
        if (this.mActivity == null) {
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", PrivacySpaceWizardActivity.class.getName());
        intent.putExtra("android.intent.extra.USER_ID", 0);
        if (hasChallenge) {
            intent.putExtra("has_challenge", hasChallenge);
            intent.putExtra("challenge", challenge);
        }
        return startActivityForResult(intent, 30);
    }

    public boolean launchChooseLockSettingsForPrivacyUser(boolean hasChallenge, long challenge) {
        if (this.mActivity == null) {
            return false;
        }
        Intent intent = null;
        int quality = getOwnerPasswordQuality();
        switch (quality) {
            case 65536:
                if (!hasChallenge) {
                    intent = ChooseLockPattern.createIntent(this.mActivity, false, null, 0);
                    break;
                }
                intent = ChooseLockPattern.createIntent(this.mActivity, false, challenge, 0);
                break;
            case 131072:
            case 196608:
            case 262144:
            case 327680:
            case 393216:
                DevicePolicyManager dpm = (DevicePolicyManager) this.mActivity.getSystemService("device_policy");
                int minLength = dpm.getPasswordMinimumLength(null, 0);
                int maxLength = dpm.getPasswordMaximumLength(quality);
                if (!hasChallenge) {
                    intent = ChooseLockPassword.createIntent(this.mActivity, quality, minLength, maxLength, false, null, 0);
                    break;
                }
                intent = ChooseLockPassword.createIntent(this.mActivity, quality, minLength, maxLength, false, challenge, 0);
                break;
        }
        if (intent != null) {
            intent.putExtra("hidden_space_main_user_type", 4);
        }
        return startActivityForResult(intent, 31);
    }

    public boolean launchConfirmPasswordForRemovingPrivacyUser(int privacyUserId) {
        if (this.mActivity == null) {
            return false;
        }
        Intent intent = null;
        if (hasPasswordLock(0)) {
            intent = new Intent();
            intent.setClassName("com.android.settings", InternalActivity.class.getName());
        } else if (hasPatternLock(0)) {
            intent = new Intent();
            intent.setClassName("com.android.settings", ConfirmLockPattern.InternalActivity.class.getName());
        }
        if (intent != null) {
            UserInfo userInfo = UserManager.get(this.mActivity).getUserInfo(privacyUserId);
            if (userInfo != null) {
                intent.putExtra("privacy_user_name", userInfo.name);
            }
        }
        return startActivityForResult(intent, 204);
    }

    public boolean launchFingerPrintEnrollConfirmDialog(byte[] token, int quality, int userId, int userType) {
        if (token == null || this.mActivity == null) {
            Log.e("PrivacySpaceSettingsHelper", "Token is null, failed to start enroll!");
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", PrivacySpaceFingerPrintEnrollDialog.class.getName());
        intent.putExtra("hw_auth_token", token);
        intent.putExtra("hidden_space_main_user_type", userType);
        intent.putExtra("lockscreen.password_type", quality);
        intent.putExtra("android.intent.extra.USER", userId);
        return startActivityForResult(intent, 201);
    }

    public boolean launcheFingerEnrollActivity(byte[] token, int userId, int userType) {
        if (token == null || this.mActivity == null) {
            Log.e("PrivacySpaceSettingsHelper", "Token is null, failed to start enroll!");
            return false;
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        intent.setClassName("com.android.settings", FingerprintEnrollActivity.class.getName());
        intent.putExtra("fp_fragment_bundle", bundle);
        intent.putExtra("request_code", 401);
        intent.putExtra("hw_auth_token", token);
        intent.putExtra("android.intent.extra.USER", userId);
        intent.putExtra("hidden_space_main_user_type", userType);
        return startActivityForResult(intent, 401);
    }

    public boolean setPrivacyLockScreen(String password, int userId, boolean hasChallenge, long challenge, PasswordCheckListener listenr) {
        int quality = getOwnerPasswordQuality();
        switch (quality) {
            case 65536:
                setPasswordCheckListener(listenr);
                return setPrivacyUserPattern(password, userId, hasChallenge, challenge);
            case 131072:
            case 196608:
            case 262144:
            case 327680:
            case 393216:
                setPasswordCheckListener(listenr);
                return setPrivacyUserPassword(quality, password, userId, hasChallenge, challenge);
            default:
                return false;
        }
    }

    private boolean setPrivacyUserPassword(int quality, String password, int userId, boolean hasChallenge, long challenge) {
        if (this.mLockPatternUtils == null) {
            return false;
        }
        this.mLockPatternUtils.saveLockPassword(password, null, quality, userId);
        if (hasChallenge) {
            final String str = password;
            final long j = challenge;
            final int i = userId;
            new AsyncTask<Void, Void, byte[]>() {
                protected byte[] doInBackground(Void... params) {
                    try {
                        return PrivacySpaceSettingsHelper.this.mLockPatternUtils.verifyPassword(str, j, i);
                    } catch (RequestThrottledException e) {
                        Log.e("PrivacySpaceSettingsHelper", "critical: can not check password");
                        return new byte[0];
                    }
                }

                protected void onPostExecute(byte[] result) {
                    if (result == null) {
                        Log.e("PrivacySpaceSettingsHelper", "critical: no token returned for known good password");
                    }
                    if (PrivacySpaceSettingsHelper.this.mPasswordCheckListener != null) {
                        PrivacySpaceSettingsHelper.this.mPasswordCheckListener.onCheckFinished(result);
                    }
                }
            }.execute(new Void[0]);
        }
        return true;
    }

    private boolean setPrivacyUserPattern(String pattern, int userId, boolean hasChallenge, long challenge) {
        if (this.mLockPatternUtils == null) {
            return false;
        }
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        final List<Cell> patternCell = LockPatternUtils.stringToPattern(pattern);
        this.mLockPatternUtils.saveLockPattern(patternCell, null, userId);
        if (hasChallenge) {
            final long j = challenge;
            final int i = userId;
            new AsyncTask<Void, Void, byte[]>() {
                protected byte[] doInBackground(Void... params) {
                    try {
                        return PrivacySpaceSettingsHelper.this.mLockPatternUtils.verifyPattern(patternCell, j, i);
                    } catch (RequestThrottledException e) {
                        Log.e("PrivacySpaceSettingsHelper", "critical: can not check pattern");
                        return new byte[0];
                    }
                }

                protected void onPostExecute(byte[] result) {
                    if (result == null) {
                        Log.e("PrivacySpaceSettingsHelper", "critical: no token returned for known good pattern");
                    }
                    if (PrivacySpaceSettingsHelper.this.mPasswordCheckListener != null) {
                        PrivacySpaceSettingsHelper.this.mPasswordCheckListener.onCheckFinished(result);
                    }
                    PrivacySpaceSettingsHelper.this.mLockPatternUtils.setVisiblePatternEnabled(true, i);
                }
            }.execute(new Void[0]);
        }
        return true;
    }
}
