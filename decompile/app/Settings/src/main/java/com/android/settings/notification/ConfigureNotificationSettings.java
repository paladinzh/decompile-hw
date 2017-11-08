package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.RestrictedListPreference.RestrictedItem;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;

public class ConfigureNotificationSettings extends SettingsPreferenceFragment {
    private Context mContext;
    private NotificationLockscreenPreference mLockscreen;
    private NotificationLockscreenPreference mLockscreenProfile;
    private int mLockscreenSelectedValue;
    private int mLockscreenSelectedValueProfile;
    private TwoStatePreference mNotificationPulse;
    private int mProfileChallengeUserId;
    private boolean mSecure;
    private boolean mSecureProfile;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private final class SettingsObserver extends ContentObserver {
        private final Uri LOCK_SCREEN_PRIVATE_URI = Secure.getUriFor("lock_screen_allow_private_notifications");
        private final Uri LOCK_SCREEN_SHOW_URI = Secure.getUriFor("lock_screen_show_notifications");
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");

        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = ConfigureNotificationSettings.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this);
                cr.registerContentObserver(this.LOCK_SCREEN_PRIVATE_URI, false, this);
                cr.registerContentObserver(this.LOCK_SCREEN_SHOW_URI, false, this);
                return;
            }
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                ConfigureNotificationSettings.this.updatePulse();
            }
            if (this.LOCK_SCREEN_PRIVATE_URI.equals(uri) || this.LOCK_SCREEN_SHOW_URI.equals(uri)) {
                ConfigureNotificationSettings.this.updateLockscreenNotifications();
                if (ConfigureNotificationSettings.this.mProfileChallengeUserId != -10000) {
                    ConfigureNotificationSettings.this.updateLockscreenNotificationsForProfile();
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 337;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        this.mProfileChallengeUserId = Utils.getManagedProfileId(UserManager.get(this.mContext), UserHandle.myUserId());
        LockPatternUtils utils = new LockPatternUtils(getActivity());
        boolean isUnified = !utils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId);
        this.mSecure = utils.isSecure(UserHandle.myUserId());
        if (this.mProfileChallengeUserId != -10000) {
            if (utils.isSecure(this.mProfileChallengeUserId)) {
                z = true;
            } else if (isUnified) {
                z = this.mSecure;
            }
        }
        this.mSecureProfile = z;
        addPreferencesFromResource(2131230754);
        initPulse();
        initLockscreenNotifications();
        if (this.mProfileChallengeUserId != -10000) {
            addPreferencesFromResource(2131230755);
            initLockscreenNotificationsForProfile();
        }
    }

    public void onResume() {
        super.onResume();
        this.mSettingsObserver.register(true);
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
    }

    private void initPulse() {
        this.mNotificationPulse = (TwoStatePreference) getPreferenceScreen().findPreference("notification_pulse");
        if (this.mNotificationPulse == null) {
            Log.i("ConfigNotiSettings", "Preference not found: notification_pulse");
            return;
        }
        if (getResources().getBoolean(17956932)) {
            updatePulse();
            this.mNotificationPulse.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return System.putInt(ConfigureNotificationSettings.this.getContentResolver(), "notification_light_pulse", ((Boolean) newValue).booleanValue() ? 1 : 0);
                }
            });
        } else {
            getPreferenceScreen().removePreference(this.mNotificationPulse);
        }
    }

    private void updatePulse() {
        boolean z = true;
        if (this.mNotificationPulse != null) {
            try {
                TwoStatePreference twoStatePreference = this.mNotificationPulse;
                if (System.getInt(getContentResolver(), "notification_light_pulse") != 1) {
                    z = false;
                }
                twoStatePreference.setChecked(z);
            } catch (SettingNotFoundException e) {
                Log.e("ConfigNotiSettings", "notification_light_pulse not found");
            }
        }
    }

    private void initLockscreenNotifications() {
        this.mLockscreen = (NotificationLockscreenPreference) getPreferenceScreen().findPreference("lock_screen_notifications");
        if (this.mLockscreen == null) {
            Log.i("ConfigNotiSettings", "Preference not found: lock_screen_notifications");
            return;
        }
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        entries.add(getString(2131626730));
        values.add(Integer.toString(2131626730));
        String summaryShowEntry = getString(2131626728);
        String summaryShowEntryValue = Integer.toString(2131626728);
        entries.add(summaryShowEntry);
        values.add(summaryShowEntryValue);
        setRestrictedIfNotificationFeaturesDisabled(summaryShowEntry, summaryShowEntryValue, 12);
        if (this.mSecure) {
            String summaryHideEntry = getString(2131626729);
            String summaryHideEntryValue = Integer.toString(2131626729);
            entries.add(summaryHideEntry);
            values.add(summaryHideEntryValue);
            setRestrictedIfNotificationFeaturesDisabled(summaryHideEntry, summaryHideEntryValue, 4);
        }
        this.mLockscreen.setRemoteInputRestricted(RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, 64, UserHandle.myUserId()));
        this.mLockscreen.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mLockscreen.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        updateLockscreenNotifications();
        if (this.mLockscreen.getEntries().length > 1) {
            this.mLockscreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int i = 0;
                    int val = Integer.parseInt((String) newValue);
                    if (val == ConfigureNotificationSettings.this.mLockscreenSelectedValue) {
                        return false;
                    }
                    int i2;
                    boolean enabled = val != 2131626730;
                    boolean show = val == 2131626728;
                    ContentResolver -wrap0 = ConfigureNotificationSettings.this.getContentResolver();
                    String str = "lock_screen_allow_private_notifications";
                    if (show) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    Secure.putInt(-wrap0, str, i2);
                    ContentResolver -wrap02 = ConfigureNotificationSettings.this.getContentResolver();
                    String str2 = "lock_screen_show_notifications";
                    if (enabled) {
                        i = 1;
                    }
                    Secure.putInt(-wrap02, str2, i);
                    ConfigureNotificationSettings.this.mLockscreenSelectedValue = val;
                    return true;
                }
            });
        } else {
            this.mLockscreen.setEnabled(false);
        }
    }

    private void initLockscreenNotificationsForProfile() {
        this.mLockscreenProfile = (NotificationLockscreenPreference) getPreferenceScreen().findPreference("lock_screen_notifications_profile");
        if (this.mLockscreenProfile == null) {
            Log.i("ConfigNotiSettings", "Preference not found: lock_screen_notifications_profile");
            return;
        }
        this.mLockscreenProfile.setUserId(this.mProfileChallengeUserId);
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        entries.add(getString(2131626735));
        values.add(Integer.toString(2131626735));
        String summaryShowEntry = getString(2131626733);
        String summaryShowEntryValue = Integer.toString(2131626733);
        entries.add(summaryShowEntry);
        values.add(summaryShowEntryValue);
        setRestrictedIfNotificationFeaturesDisabled(summaryShowEntry, summaryShowEntryValue, 12);
        if (this.mSecureProfile) {
            String summaryHideEntry = getString(2131626734);
            String summaryHideEntryValue = Integer.toString(2131626734);
            entries.add(summaryHideEntry);
            values.add(summaryHideEntryValue);
            setRestrictedIfNotificationFeaturesDisabled(summaryHideEntry, summaryHideEntryValue, 4);
        }
        this.mLockscreen.setRemoteInputRestricted(RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, 64, this.mProfileChallengeUserId));
        this.mLockscreenProfile.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mLockscreenProfile.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        this.mLockscreenProfile.setRemoteInputCheckBoxEnabled(false);
        updateLockscreenNotificationsForProfile();
        if (this.mLockscreenProfile.getEntries().length > 1) {
            this.mLockscreenProfile.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int i = 0;
                    int val = Integer.parseInt((String) newValue);
                    if (val == ConfigureNotificationSettings.this.mLockscreenSelectedValueProfile) {
                        return false;
                    }
                    int i2;
                    boolean enabled = val != 2131626735;
                    boolean show = val == 2131626733;
                    ContentResolver -wrap0 = ConfigureNotificationSettings.this.getContentResolver();
                    String str = "lock_screen_allow_private_notifications";
                    if (show) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    Secure.putIntForUser(-wrap0, str, i2, ConfigureNotificationSettings.this.mProfileChallengeUserId);
                    ContentResolver -wrap02 = ConfigureNotificationSettings.this.getContentResolver();
                    String str2 = "lock_screen_show_notifications";
                    if (enabled) {
                        i = 1;
                    }
                    Secure.putIntForUser(-wrap02, str2, i, ConfigureNotificationSettings.this.mProfileChallengeUserId);
                    ConfigureNotificationSettings.this.mLockscreenSelectedValueProfile = val;
                    return true;
                }
            });
        } else {
            this.mLockscreenProfile.setEnabled(false);
        }
    }

    private void setRestrictedIfNotificationFeaturesDisabled(CharSequence entry, CharSequence entryValue, int keyguardNotificationFeatures) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, UserHandle.myUserId());
        if (admin != null) {
            this.mLockscreen.addRestrictedItem(new RestrictedItem(entry, entryValue, admin));
        }
        if (this.mProfileChallengeUserId != -10000) {
            EnforcedAdmin profileAdmin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, this.mProfileChallengeUserId);
            if (profileAdmin != null) {
                this.mLockscreenProfile.addRestrictedItem(new RestrictedItem(entry, entryValue, profileAdmin));
            }
        }
    }

    private void updateLockscreenNotifications() {
        if (this.mLockscreen != null) {
            boolean allowPrivate;
            int i;
            boolean enabled = getLockscreenNotificationsEnabled(UserHandle.myUserId());
            if (this.mSecure) {
                allowPrivate = getLockscreenAllowPrivateNotifications(UserHandle.myUserId());
            } else {
                allowPrivate = true;
            }
            if (!enabled) {
                i = 2131626730;
            } else if (allowPrivate) {
                i = 2131626728;
            } else {
                i = 2131626729;
            }
            this.mLockscreenSelectedValue = i;
            this.mLockscreen.setValue(Integer.toString(this.mLockscreenSelectedValue));
        }
    }

    private void updateLockscreenNotificationsForProfile() {
        if (this.mProfileChallengeUserId != -10000 && this.mLockscreenProfile != null) {
            boolean allowPrivate;
            int i;
            boolean enabled = getLockscreenNotificationsEnabled(this.mProfileChallengeUserId);
            if (this.mSecureProfile) {
                allowPrivate = getLockscreenAllowPrivateNotifications(this.mProfileChallengeUserId);
            } else {
                allowPrivate = true;
            }
            if (!enabled) {
                i = 2131626735;
            } else if (allowPrivate) {
                i = 2131626733;
            } else {
                i = 2131626734;
            }
            this.mLockscreenSelectedValueProfile = i;
            this.mLockscreenProfile.setValue(Integer.toString(this.mLockscreenSelectedValueProfile));
        }
    }

    private boolean getLockscreenNotificationsEnabled(int userId) {
        return Secure.getIntForUser(getContentResolver(), "lock_screen_show_notifications", 0, userId) != 0;
    }

    private boolean getLockscreenAllowPrivateNotifications(int userId) {
        return Secure.getIntForUser(getContentResolver(), "lock_screen_allow_private_notifications", 0, userId) != 0;
    }
}
