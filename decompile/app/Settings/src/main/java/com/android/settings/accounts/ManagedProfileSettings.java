package com.android.settings.accounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;

public class ManagedProfileSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private RestrictedSwitchPreference mContactPrefrence;
    private Context mContext;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver;
    private UserHandle mManagedUser;
    private UserManager mUserManager;
    private SwitchPreference mWorkModePreference;

    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        private ManagedProfileBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("ManagedProfileSettings", "Received broadcast: " + action);
            if ("android.intent.action.MANAGED_PROFILE_REMOVED".equals(action)) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.getActivity().finish();
                }
            } else if ("android.intent.action.MANAGED_PROFILE_AVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNAVAILABLE".equals(action)) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.mWorkModePreference.setChecked(!ManagedProfileSettings.this.mUserManager.isQuietModeEnabled(ManagedProfileSettings.this.mManagedUser));
                }
            } else {
                Log.w("ManagedProfileSettings", "Cannot handle received broadcast: " + intent.getAction());
            }
        }

        public void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
            context.registerReceiver(this, intentFilter);
        }

        public void unregister(Context context) {
            context.unregisterReceiver(this);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230814);
        this.mWorkModePreference = (SwitchPreference) findPreference("work_mode");
        this.mWorkModePreference.setOnPreferenceChangeListener(this);
        this.mContactPrefrence = (RestrictedSwitchPreference) findPreference("contacts_search");
        this.mContactPrefrence.setOnPreferenceChangeListener(this);
        this.mContext = getActivity().getApplicationContext();
        this.mUserManager = (UserManager) getSystemService("user");
        this.mManagedUser = getManagedUserFromArgument();
        if (this.mManagedUser == null) {
            getActivity().finish();
        }
        this.mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver();
        this.mManagedProfileBroadcastReceiver.register(getActivity());
    }

    public void onResume() {
        super.onResume();
        loadDataAndPopulateUi();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mManagedProfileBroadcastReceiver.unregister(getActivity());
    }

    private UserHandle getManagedUserFromArgument() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            UserHandle userHandle = (UserHandle) arguments.getParcelable("android.intent.extra.USER");
            if (userHandle == null || !this.mUserManager.isManagedProfile(userHandle.getIdentifier())) {
                return null;
            }
            return userHandle;
        }
        return null;
    }

    private void loadDataAndPopulateUi() {
        boolean z = true;
        if (this.mWorkModePreference != null) {
            boolean z2;
            SwitchPreference switchPreference = this.mWorkModePreference;
            if (this.mUserManager.isQuietModeEnabled(this.mManagedUser)) {
                z2 = false;
            } else {
                z2 = true;
            }
            switchPreference.setChecked(z2);
        }
        if (this.mContactPrefrence != null) {
            int value = Secure.getIntForUser(getContentResolver(), "managed_profile_contact_remote_search", 0, this.mManagedUser.getIdentifier());
            RestrictedSwitchPreference restrictedSwitchPreference = this.mContactPrefrence;
            if (value == 0) {
                z = false;
            }
            restrictedSwitchPreference.setChecked(z);
            this.mContactPrefrence.setDisabledByAdmin(RestrictedLockUtils.checkIfRemoteContactSearchDisallowed(this.mContext, this.mManagedUser.getIdentifier()));
        }
    }

    protected int getMetricsCategory() {
        return 401;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mWorkModePreference) {
            if (((Boolean) newValue).booleanValue()) {
                this.mUserManager.trySetQuietModeDisabled(this.mManagedUser.getIdentifier(), null);
            } else {
                this.mUserManager.setQuietModeEnabled(this.mManagedUser.getIdentifier(), true);
            }
            return true;
        } else if (preference != this.mContactPrefrence) {
            return false;
        } else {
            Secure.putIntForUser(getContentResolver(), "managed_profile_contact_remote_search", ((Boolean) newValue).booleanValue() ? 1 : 0, this.mManagedUser.getIdentifier());
            return true;
        }
    }
}
