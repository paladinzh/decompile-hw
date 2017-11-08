package com.android.settings.users;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.RestrictedLockUtils;

public class UserDetailsSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String TAG = UserDetailsSettings.class.getSimpleName();
    private Bundle mDefaultGuestRestrictions;
    private boolean mGuestUser;
    private SwitchPreference mPhonePref;
    private Preference mRemoveUserPref;
    private UserInfo mUserInfo;
    private UserManager mUserManager;

    protected int getMetricsCategory() {
        return 98;
    }

    public void onCreate(Bundle icicle) {
        boolean z = false;
        super.onCreate(icicle);
        Context context = getActivity();
        this.mUserManager = (UserManager) context.getSystemService("user");
        addPreferencesFromResource(2131230921);
        this.mPhonePref = (SwitchPreference) findPreference("enable_calling");
        this.mRemoveUserPref = findPreference("remove_user");
        this.mGuestUser = getArguments().getBoolean("guest_user", false);
        if (this.mGuestUser) {
            removePreference("remove_user");
            this.mPhonePref.setTitle(2131626486);
            this.mDefaultGuestRestrictions = this.mUserManager.getDefaultGuestRestrictions();
            if (this.mDefaultGuestRestrictions != null) {
                SwitchPreference switchPreference = this.mPhonePref;
                if (!this.mDefaultGuestRestrictions.getBoolean("no_outgoing_calls")) {
                    z = true;
                }
                switchPreference.setChecked(z);
            }
        } else {
            int userId = getArguments().getInt("user_id", -1);
            if (userId == -1) {
                throw new RuntimeException("Arguments to this fragment must contain the user id");
            }
            boolean z2;
            this.mUserInfo = this.mUserManager.getUserInfo(userId);
            SwitchPreference switchPreference2 = this.mPhonePref;
            if (this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(userId))) {
                z2 = false;
            } else {
                z2 = true;
            }
            switchPreference2.setChecked(z2);
            this.mRemoveUserPref.setOnPreferenceClickListener(this);
        }
        if (RestrictedLockUtils.hasBaseUserRestriction(context, "no_remove_user", UserHandle.myUserId())) {
            removePreference("remove_user");
        }
        this.mPhonePref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mRemoveUserPref) {
            return false;
        }
        if (this.mUserManager.isAdminUser()) {
            showDialog(1);
            return true;
        }
        throw new RuntimeException("Only admins can remove a user");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Boolean.TRUE.equals(newValue)) {
            int i;
            if (this.mGuestUser) {
                i = 2;
            } else {
                i = 3;
            }
            showDialog(i);
            return false;
        }
        enableCallsAndSms(false);
        return true;
    }

    void enableCallsAndSms(boolean enabled) {
        boolean z = false;
        this.mPhonePref.setChecked(enabled);
        UserHandle userHandle;
        if (this.mGuestUser) {
            Bundle bundle = this.mDefaultGuestRestrictions;
            String str = "no_outgoing_calls";
            if (!enabled) {
                z = true;
            }
            bundle.putBoolean(str, z);
            this.mDefaultGuestRestrictions.putBoolean("no_sms", true);
            this.mUserManager.setDefaultGuestRestrictions(this.mDefaultGuestRestrictions);
            for (UserInfo user : this.mUserManager.getUsers(true)) {
                if (user.isGuest()) {
                    userHandle = UserHandle.of(user.id);
                    for (String key : this.mDefaultGuestRestrictions.keySet()) {
                        this.mUserManager.setUserRestriction(key, this.mDefaultGuestRestrictions.getBoolean(key), userHandle);
                    }
                }
            }
            return;
        }
        boolean z2;
        userHandle = UserHandle.of(this.mUserInfo.id);
        UserManager userManager = this.mUserManager;
        String str2 = "no_outgoing_calls";
        if (enabled) {
            z2 = false;
        } else {
            z2 = true;
        }
        userManager.setUserRestriction(str2, z2, userHandle);
        UserManager userManager2 = this.mUserManager;
        str = "no_sms";
        if (!enabled) {
            z = true;
        }
        userManager2.setUserRestriction(str, z, userHandle);
    }

    public Dialog onCreateDialog(int dialogId) {
        if (getActivity() == null) {
            return null;
        }
        switch (dialogId) {
            case 1:
                return UserDialogs.createRemoveDialog(getActivity(), this.mUserInfo.id, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.removeUser();
                    }
                });
            case 2:
                return UserDialogs.createEnablePhoneCallsDialog(getActivity(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.enableCallsAndSms(true);
                    }
                });
            case 3:
                return UserDialogs.createEnablePhoneCallsAndSmsDialog(getActivity(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.enableCallsAndSms(true);
                    }
                });
            default:
                throw new IllegalArgumentException("Unsupported dialogId " + dialogId);
        }
    }

    void removeUser() {
        this.mUserManager.removeUser(this.mUserInfo.id);
        finishFragment();
    }
}
