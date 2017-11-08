package com.android.settings;

import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.search.Indexable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;
import java.util.HashMap;

public class SecureLockSettings extends SettingsPreferenceFragment implements Indexable, OnPreferenceChangeListener {
    private static final int MY_USER_ID = UserHandle.myUserId();
    private static final int[] SCREEN_TIMEOUT = new int[]{5, 15, 30, 1, 2, 5, 10, 30};
    private static final String[] lockafter_values = new String[]{"lockafter_right_now", "lockafter_5_seconds", "lockafter_15_seconds", "lockafter_30_seconds", "lockafter_1_minutes", "lockafter_2_minutes", "lockafter_5_minutes", "lockafter_10_minutes", "lockafter_30_minutes"};
    private DevicePolicyManager mDPM;
    protected TimeoutListPreference mLockAfter;
    protected LockPatternUtils mLockPatternUtils;
    protected CustomSwitchPreference mPowerButtonInstantlyLocks;
    private HashMap<CharSequence, CharSequence> mTimeoutMap = new HashMap();
    protected CustomSwitchPreference mVisiblePattern;

    protected int getMetricsCategory() {
        return 100000;
    }

    protected PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230854);
        root = getPreferenceScreen();
        if (this.mLockPatternUtils.isSecure(MY_USER_ID) && 65536 == this.mLockPatternUtils.getKeyguardStoredPasswordQuality(MY_USER_ID)) {
            this.mVisiblePattern = (CustomSwitchPreference) findPreference("visiblepattern");
            if (this.mVisiblePattern != null) {
                this.mVisiblePattern.setOnPreferenceChangeListener(this);
            }
        } else {
            removePreference("visiblepattern");
        }
        this.mLockAfter = (TimeoutListPreference) root.findPreference("lock_after_timeout");
        if (!this.mLockPatternUtils.isSecure(MY_USER_ID)) {
            removePreference("lock_after_timeout");
        } else if (this.mLockAfter != null) {
            setupLockAfterPreference();
            buildHwTimeoutKVPairs();
            updateLockAfterPreferenceSummary();
        }
        this.mPowerButtonInstantlyLocks = (CustomSwitchPreference) root.findPreference("power_button_instantly_locks");
        if (!this.mLockPatternUtils.isSecure(MY_USER_ID)) {
            removePreference("power_button_instantly_locks");
        } else if (this.mPowerButtonInstantlyLocks != null) {
            this.mPowerButtonInstantlyLocks.setOnPreferenceChangeListener(this);
        }
        return root;
    }

    private void buildHwTimeoutKVPairs() {
        CharSequence[] keys = this.mLockAfter.getEntryValues();
        CharSequence[] values = buildTimeoutEntries();
        this.mTimeoutMap.clear();
        for (int i = 0; i < keys.length; i++) {
            this.mTimeoutMap.put(keys[i], values[i]);
        }
    }

    protected void refreshUi() {
        if (this.mVisiblePattern != null) {
            this.mVisiblePattern.setChecked(this.mLockPatternUtils.isVisiblePatternEnabled(MY_USER_ID));
        }
        if (this.mPowerButtonInstantlyLocks != null) {
            this.mPowerButtonInstantlyLocks.setChecked(this.mLockPatternUtils.getPowerButtonInstantlyLocks(MY_USER_ID));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230854);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        setHasOptionsMenu(true);
        setOnLockAfterOnPreferenceChangeListener();
    }

    private void setOnLockAfterOnPreferenceChangeListener() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            this.mLockAfter = (TimeoutListPreference) root.findPreference("lock_after_timeout");
            if (this.mLockAfter != null) {
                this.mLockAfter.setOnPreferenceChangeListener(this);
            }
        }
    }

    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
        refreshUi();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        String key = preference.getKey();
        if ("visiblepattern".equals(key)) {
            this.mLockPatternUtils.setVisiblePatternEnabled(((Boolean) value).booleanValue(), MY_USER_ID);
        } else if ("power_button_instantly_locks".equals(key)) {
            this.mLockPatternUtils.setPowerButtonInstantlyLocks(((Boolean) value).booleanValue(), UserHandle.myUserId());
        } else if ("lock_after_timeout".equals(key)) {
            int timeout = Integer.parseInt((String) value);
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mLockAfter, lockafter_values, (String) value);
            try {
                Secure.putInt(getContentResolver(), "lock_screen_lock_after_timeout", timeout);
            } catch (NumberFormatException e) {
                Log.e("SecuritySettings", "could not persist lockAfter timeout setting", e);
            }
            setupLockAfterPreference();
            updateLockAfterPreferenceSummary();
        }
        return true;
    }

    private void setupLockAfterPreference() {
        this.mLockAfter.setValue(String.valueOf(Secure.getLong(getContentResolver(), "lock_screen_lock_after_timeout", 5000)));
        this.mLockAfter.setOnPreferenceChangeListener(this);
        if (this.mDPM != null) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(getActivity());
            this.mLockAfter.removeUnusableTimeouts(Math.max(0, this.mDPM.getMaximumTimeToLockForUserAndProfiles(UserHandle.myUserId()) - ((long) Math.max(0, System.getInt(getContentResolver(), "screen_off_timeout", 0)))), admin);
        }
    }

    private CharSequence[] buildTimeoutEntries() {
        timeoutEntries = new CharSequence[9];
        timeoutEntries[1] = String.format(getResources().getString(2131628301, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[0])}), new Object[0]);
        timeoutEntries[2] = String.format(getResources().getString(2131628302, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[1])}), new Object[0]);
        timeoutEntries[3] = String.format(getResources().getString(2131628303, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[2])}), new Object[0]);
        timeoutEntries[4] = String.format(getResources().getString(2131628304, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[3])}), new Object[0]);
        timeoutEntries[5] = String.format(getResources().getString(2131628305, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[4])}), new Object[0]);
        timeoutEntries[6] = String.format(getResources().getString(2131628306, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[5])}), new Object[0]);
        timeoutEntries[7] = String.format(getResources().getString(2131628307, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[6])}), new Object[0]);
        timeoutEntries[8] = String.format(getResources().getString(2131628308, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[7])}), new Object[0]);
        return timeoutEntries;
    }

    private void updateLockAfterPreferenceSummary() {
        if (this.mLockAfter != null) {
            String summary;
            if (this.mLockAfter.isDisabledByAdmin()) {
                summary = getString(2131627106);
            } else {
                long currentTimeout = Secure.getLong(getContentResolver(), "lock_screen_lock_after_timeout", 5000);
                this.mLockAfter.setEntries(buildEntriesByValues());
                CharSequence[] entries = this.mLockAfter.getEntries();
                CharSequence[] values = this.mLockAfter.getEntryValues();
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    if (currentTimeout >= Long.valueOf(values[i].toString()).longValue()) {
                        best = i;
                    }
                }
                if (best == 0) {
                    summary = getString(2131627481);
                } else {
                    summary = getString(2131624621, new Object[]{entries[best]});
                }
            }
            this.mLockAfter.setSummary(summary);
        }
    }

    private CharSequence[] buildEntriesByValues() {
        CharSequence[] entryValues = this.mLockAfter.getEntryValues();
        ArrayList<CharSequence> entriesList = new ArrayList();
        for (Object obj : entryValues) {
            entriesList.add((CharSequence) this.mTimeoutMap.get(obj));
        }
        return (CharSequence[]) entriesList.toArray(new CharSequence[0]);
    }
}
