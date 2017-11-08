package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import com.android.internal.widget.LockPatternUtils;

public class ScreenPinningSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private LockPatternUtils mLockPatternUtils;
    private SwitchPreference mPinningSwitch;
    private SwitchPreference mUseScreenLock;

    protected int getMetricsCategory() {
        return 86;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mLockPatternUtils = new LockPatternUtils((SettingsActivity) getActivity());
        updateDisplay();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    private static boolean isLockToAppEnabled(Context context) {
        return System.getInt(context.getContentResolver(), "lock_to_app_enabled", 0) != 0;
    }

    private void setLockToAppEnabled(boolean isEnabled) {
        System.putInt(getContentResolver(), "lock_to_app_enabled", isEnabled ? 1 : 0);
        if (isEnabled) {
            setScreenLockUsedSetting(isScreenLockUsed());
        }
    }

    private boolean isScreenLockUsed() {
        if (Secure.getInt(getContentResolver(), "lock_to_app_exit_locked", getCurrentSecurityTitle() != 2131626858 ? 1 : 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean setScreenLockUsed(boolean isEnabled) {
        if (isEnabled && new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) == 0) {
            Intent chooseLockIntent = new Intent("android.app.action.SET_NEW_PASSWORD");
            chooseLockIntent.putExtra("minimum_quality", 65536);
            startActivityForResult(chooseLockIntent, 43);
            return false;
        }
        setScreenLockUsedSetting(isEnabled);
        return true;
    }

    private void setScreenLockUsedSetting(boolean isEnabled) {
        Secure.putInt(getContentResolver(), "lock_to_app_exit_locked", isEnabled ? 1 : 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean validPassQuality = false;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 43) {
            if (new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) != 0) {
                validPassQuality = true;
            }
            setScreenLockUsed(validPassQuality);
            this.mUseScreenLock.setChecked(validPassQuality);
        }
    }

    private int getCurrentSecurityTitle() {
        switch (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId())) {
            case 65536:
                if (this.mLockPatternUtils.isLockPatternEnabled(UserHandle.myUserId())) {
                    return 2131626855;
                }
                break;
            case 131072:
            case 196608:
                return 2131626856;
            case 262144:
            case 327680:
            case 393216:
            case 524288:
                return 2131626857;
        }
        return 2131626858;
    }

    public void updateDisplay() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230852);
        root = getPreferenceScreen();
        this.mPinningSwitch = (SwitchPreference) root.findPreference("pinning_switch");
        this.mPinningSwitch.setOnPreferenceChangeListener(this);
        this.mPinningSwitch.setChecked(isLockToAppEnabled(getActivity()));
        root.findPreference("help_summary").setSummary(String.format(getResources().getString(2131628887, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}), new Object[0]));
        if (isLockToAppEnabled(getActivity())) {
            this.mUseScreenLock = (SwitchPreference) root.findPreference("use_screen_lock");
            this.mUseScreenLock.setOnPreferenceChangeListener(this);
            this.mUseScreenLock.setChecked(isScreenLockUsed());
            removePreference("pinning_introduction");
            return;
        }
        removePreference("use_screen_lock");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mPinningSwitch == preference) {
            setLockToAppEnabled(((Boolean) newValue).booleanValue());
            updateDisplay();
        } else if (this.mUseScreenLock == preference) {
            setScreenLockUsed(((Boolean) newValue).booleanValue());
        }
        return true;
    }
}
