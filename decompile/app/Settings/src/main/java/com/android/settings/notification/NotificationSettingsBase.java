package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.ImportanceSeekBarPreference.Callback;
import com.android.settings.notification.RestrictedDropDownPreference.RestrictedItem;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.ArrayList;

public abstract class NotificationSettingsBase extends SettingsPreferenceFragment {
    private static final boolean DEBUG = Log.isLoggable("NotifiSettingsBase", 3);
    protected final NotificationBackend mBackend = new NotificationBackend();
    protected RestrictedSwitchPreference mBlock;
    protected Context mContext;
    protected boolean mCreated;
    protected ImportanceSeekBarPreference mImportance;
    protected String mPkg;
    protected PackageInfo mPkgInfo;
    protected PackageManager mPm;
    protected RestrictedSwitchPreference mPriority;
    protected boolean mShowSlider = false;
    protected RestrictedSwitchPreference mSilent;
    protected EnforcedAdmin mSuspendedAppsAdmin;
    protected int mUid;
    protected UserManager mUm;
    protected int mUserId;
    protected RestrictedDropDownPreference mVisibilityOverride;

    abstract void updateDependents(int i);

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) {
            Log.d("NotifiSettingsBase", "onActivityCreated mCreated=" + this.mCreated);
        }
        if (this.mCreated) {
            Log.w("NotifiSettingsBase", "onActivityCreated: ignoring duplicate call");
        } else {
            this.mCreated = true;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        Intent intent = getActivity().getIntent();
        Bundle args = getArguments();
        if (DEBUG) {
            Log.d("NotifiSettingsBase", "onCreate getIntent()=" + intent);
        }
        if (intent == null && args == null) {
            Log.w("NotifiSettingsBase", "No intent");
            toastAndFinish();
            return;
        }
        String stringExtra;
        int intExtra;
        this.mPm = getPackageManager();
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        if (args == null || !args.containsKey("package")) {
            stringExtra = intent.getStringExtra("app_package");
        } else {
            stringExtra = args.getString("package");
        }
        this.mPkg = stringExtra;
        if (args == null || !args.containsKey("uid")) {
            intExtra = intent.getIntExtra("app_uid", -1);
        } else {
            intExtra = args.getInt("uid");
        }
        this.mUid = intExtra;
        if (this.mUid == -1 || TextUtils.isEmpty(this.mPkg)) {
            Log.w("NotifiSettingsBase", "Missing extras: app_package was " + this.mPkg + ", " + "app_uid" + " was " + this.mUid);
            toastAndFinish();
            return;
        }
        this.mUserId = UserHandle.getUserId(this.mUid);
        if (DEBUG) {
            Log.d("NotifiSettingsBase", "Load details for pkg=" + this.mPkg + " uid=" + this.mUid);
        }
        this.mPkgInfo = findPackageInfo(this.mPkg, this.mUid);
        if (this.mPkgInfo == null) {
            Log.w("NotifiSettingsBase", "Failed to find package info: app_package was " + this.mPkg + ", " + "app_uid" + " was " + this.mUid);
            toastAndFinish();
            return;
        }
        boolean z;
        this.mSuspendedAppsAdmin = RestrictedLockUtils.checkIfApplicationIsSuspended(this.mContext, this.mPkg, this.mUserId);
        if (Secure.getInt(getContentResolver(), "show_importance_slider", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mShowSlider = z;
    }

    public void onResume() {
        super.onResume();
        if (this.mUid == -1 || getPackageManager().getPackagesForUid(this.mUid) != null) {
            this.mSuspendedAppsAdmin = RestrictedLockUtils.checkIfApplicationIsSuspended(this.mContext, this.mPkg, this.mUserId);
            if (this.mImportance != null) {
                this.mImportance.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            }
            if (this.mPriority != null) {
                this.mPriority.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            }
            if (this.mBlock != null) {
                this.mBlock.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            }
            if (this.mSilent != null) {
                this.mSilent.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            }
            if (this.mVisibilityOverride != null) {
                this.mVisibilityOverride.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            }
            return;
        }
        finish();
    }

    protected void setupImportancePrefs(boolean isSystemApp, int importance, boolean banned) {
        boolean z = true;
        int i = 0;
        if (this.mShowSlider) {
            int i2;
            setVisible(this.mBlock, false);
            setVisible(this.mSilent, false);
            this.mImportance.setDisabledByAdmin(this.mSuspendedAppsAdmin);
            ImportanceSeekBarPreference importanceSeekBarPreference = this.mImportance;
            if (isSystemApp) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            importanceSeekBarPreference.setMinimumProgress(i2);
            this.mImportance.setMax(5);
            this.mImportance.setProgress(importance);
            ImportanceSeekBarPreference importanceSeekBarPreference2 = this.mImportance;
            if (importance != -1000) {
                z = false;
            }
            importanceSeekBarPreference2.setAutoOn(z);
            this.mImportance.setCallback(new Callback() {
                public void onImportanceChanged(int progress, boolean fromUser) {
                    if (fromUser) {
                        NotificationSettingsBase.this.mBackend.setImportance(NotificationSettingsBase.this.mPkg, NotificationSettingsBase.this.mUid, progress);
                    }
                    NotificationSettingsBase.this.updateDependents(progress);
                }
            });
            return;
        }
        setVisible(this.mImportance, false);
        this.mBlock.setChecked(importance != 0 ? banned : true);
        this.mBlock.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int importance = ((Boolean) newValue).booleanValue() ? 0 : -1000;
                NotificationSettingsBase.this.mBackend.setImportance(NotificationSettingsBase.this.mPkgInfo.packageName, NotificationSettingsBase.this.mUid, importance);
                NotificationSettingsBase.this.updateDependents(importance);
                return true;
            }
        });
        this.mSilent.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int importance = ((Boolean) newValue).booleanValue() ? 2 : -1000;
                NotificationSettingsBase.this.mBackend.setImportance(NotificationSettingsBase.this.mPkgInfo.packageName, NotificationSettingsBase.this.mUid, importance);
                NotificationSettingsBase.this.updateDependents(importance);
                return true;
            }
        });
        if (!banned) {
            i = importance;
        }
        updateDependents(i);
    }

    protected void setupPriorityPref(boolean priority) {
        this.mPriority.setDisabledByAdmin(this.mSuspendedAppsAdmin);
        this.mPriority.setChecked(priority);
        this.mPriority.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return NotificationSettingsBase.this.mBackend.setBypassZenMode(NotificationSettingsBase.this.mPkgInfo.packageName, NotificationSettingsBase.this.mUid, ((Boolean) newValue).booleanValue());
            }
        });
    }

    protected void setupVisOverridePref(int sensitive) {
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        this.mVisibilityOverride.clearRestrictedItems();
        if (getLockscreenNotificationsEnabled() && getLockscreenAllowPrivateNotifications()) {
            String summaryShowEntry = getString(2131626728);
            String summaryShowEntryValue = Integer.toString(-1000);
            entries.add(summaryShowEntry);
            values.add(summaryShowEntryValue);
            setRestrictedIfNotificationFeaturesDisabled(summaryShowEntry, summaryShowEntryValue, 12);
        }
        String summaryHideEntry = getString(2131626729);
        String summaryHideEntryValue = Integer.toString(0);
        entries.add(summaryHideEntry);
        values.add(summaryHideEntryValue);
        setRestrictedIfNotificationFeaturesDisabled(summaryHideEntry, summaryHideEntryValue, 4);
        entries.add(getString(2131626730));
        values.add(Integer.toString(-1));
        this.mVisibilityOverride.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mVisibilityOverride.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        if (sensitive == -1000) {
            this.mVisibilityOverride.setValue(Integer.toString(getGlobalVisibility()));
        } else {
            this.mVisibilityOverride.setValue(Integer.toString(sensitive));
        }
        this.mVisibilityOverride.setSummary("%s");
        this.mVisibilityOverride.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int sensitive = Integer.parseInt((String) newValue);
                if (sensitive == NotificationSettingsBase.this.getGlobalVisibility()) {
                    sensitive = -1000;
                }
                NotificationSettingsBase.this.mBackend.setVisibilityOverride(NotificationSettingsBase.this.mPkgInfo.packageName, NotificationSettingsBase.this.mUid, sensitive);
                return true;
            }
        });
    }

    private void setRestrictedIfNotificationFeaturesDisabled(CharSequence entry, CharSequence entryValue, int keyguardNotificationFeatures) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, this.mUserId);
        if (admin != null) {
            this.mVisibilityOverride.addRestrictedItem(new RestrictedItem(entry, entryValue, admin));
        }
    }

    private int getGlobalVisibility() {
        if (!getLockscreenNotificationsEnabled()) {
            return -1;
        }
        if (getLockscreenAllowPrivateNotifications()) {
            return -1000;
        }
        return 0;
    }

    protected boolean getLockscreenNotificationsEnabled() {
        return Secure.getInt(getContentResolver(), "lock_screen_show_notifications", 0) != 0;
    }

    protected boolean getLockscreenAllowPrivateNotifications() {
        return Secure.getInt(getContentResolver(), "lock_screen_allow_private_notifications", 0) != 0;
    }

    protected void setVisible(Preference p, boolean visible) {
        if ((getPreferenceScreen().findPreference(p.getKey()) != null) != visible) {
            if (visible) {
                getPreferenceScreen().addPreference(p);
            } else {
                getPreferenceScreen().removePreference(p);
            }
        }
    }

    protected void toastAndFinish() {
        Toast.makeText(this.mContext, 2131625659, 0).show();
        getActivity().finish();
    }

    private PackageInfo findPackageInfo(String pkg, int uid) {
        String[] packages = this.mPm.getPackagesForUid(uid);
        if (!(packages == null || pkg == null)) {
            int N = packages.length;
            int i = 0;
            while (i < N) {
                if (pkg.equals(packages[i])) {
                    try {
                        return this.mPm.getPackageInfo(pkg, 64);
                    } catch (NameNotFoundException e) {
                        Log.w("NotifiSettingsBase", "Failed to load package " + pkg, e);
                    }
                } else {
                    i++;
                }
            }
        }
        return null;
    }
}
