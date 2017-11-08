package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.AppHeader;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.List;

public class AppNotificationSettings extends NotificationSettingsBase {
    private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES");
    private static final boolean DEBUG = Log.isLoggable("AppNotificationSettings", 3);
    private AppRow mAppRow;
    private boolean mDndVisualEffectsSuppressed;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mAppRow != null) {
            AppHeader.createAppHeader((SettingsPreferenceFragment) this, this.mAppRow.icon, this.mAppRow.label, this.mAppRow.pkg, this.mAppRow.uid, this.mAppRow.settingsIntent);
        }
    }

    protected int getMetricsCategory() {
        return 72;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230738);
        this.mImportance = (ImportanceSeekBarPreference) findPreference("importance");
        this.mPriority = (RestrictedSwitchPreference) getPreferenceScreen().findPreference("bypass_dnd");
        this.mVisibilityOverride = (RestrictedDropDownPreference) getPreferenceScreen().findPreference("visibility_override");
        this.mBlock = (RestrictedSwitchPreference) getPreferenceScreen().findPreference("block");
        this.mSilent = (RestrictedSwitchPreference) getPreferenceScreen().findPreference("silent");
        if (this.mPkgInfo != null) {
            this.mAppRow = this.mBackend.loadAppRow(this.mContext, this.mPm, this.mPkgInfo);
            Policy policy = NotificationManager.from(this.mContext).getNotificationPolicy();
            boolean z = (policy == null || policy.suppressedVisualEffects == 0) ? false : true;
            this.mDndVisualEffectsSuppressed = z;
            ArrayMap<String, AppRow> rows = new ArrayMap();
            rows.put(this.mAppRow.pkg, this.mAppRow);
            collectConfigActivities(rows);
            setupImportancePrefs(this.mAppRow.systemApp, this.mAppRow.appImportance, this.mAppRow.banned);
            setupPriorityPref(this.mAppRow.appBypassDnd);
            setupVisOverridePref(this.mAppRow.appVisOverride);
            updateDependents(this.mAppRow.appImportance);
        }
    }

    protected void updateDependents(int importance) {
        boolean z;
        LockPatternUtils utils = new LockPatternUtils(getActivity());
        boolean lockscreenSecure = utils.isSecure(UserHandle.myUserId());
        UserInfo parentUser = this.mUm.getProfileParent(UserHandle.myUserId());
        if (parentUser != null) {
            lockscreenSecure |= utils.isSecure(parentUser.id);
        }
        if (getPreferenceScreen().findPreference(this.mBlock.getKey()) != null) {
            setVisible(this.mSilent, checkCanBeVisible(1, importance));
            RestrictedSwitchPreference restrictedSwitchPreference = this.mSilent;
            if (importance == 2) {
                z = true;
            } else {
                z = false;
            }
            restrictedSwitchPreference.setChecked(z);
        }
        Preference preference = this.mPriority;
        z = checkCanBeVisible(3, importance) ? !this.mDndVisualEffectsSuppressed : false;
        setVisible(preference, z);
        Preference preference2 = this.mVisibilityOverride;
        if (!checkCanBeVisible(1, importance)) {
            lockscreenSecure = false;
        }
        setVisible(preference2, lockscreenSecure);
    }

    protected boolean checkCanBeVisible(int minImportanceVisible, int importance) {
        boolean z = true;
        if (importance == -1000) {
            return true;
        }
        if (importance < minImportanceVisible) {
            z = false;
        }
        return z;
    }

    private List<ResolveInfo> queryNotificationConfigActivities() {
        if (DEBUG) {
            Log.d("AppNotificationSettings", "APP_NOTIFICATION_PREFS_CATEGORY_INTENT is " + APP_NOTIFICATION_PREFS_CATEGORY_INTENT);
        }
        return this.mPm.queryIntentActivities(APP_NOTIFICATION_PREFS_CATEGORY_INTENT, 0);
    }

    private void collectConfigActivities(ArrayMap<String, AppRow> rows) {
        applyConfigActivities(rows, queryNotificationConfigActivities());
    }

    private void applyConfigActivities(ArrayMap<String, AppRow> rows, List<ResolveInfo> resolveInfos) {
        if (DEBUG) {
            Log.d("AppNotificationSettings", "Found " + resolveInfos.size() + " preference activities" + (resolveInfos.size() == 0 ? " ;_;" : ""));
        }
        for (ResolveInfo ri : resolveInfos) {
            ActivityInfo activityInfo = ri.activityInfo;
            AppRow row = (AppRow) rows.get(activityInfo.applicationInfo.packageName);
            if (row == null) {
                if (DEBUG) {
                    Log.v("AppNotificationSettings", "Ignoring notification preference activity (" + activityInfo.name + ") for unknown package " + activityInfo.packageName);
                }
            } else if (row.settingsIntent == null) {
                row.settingsIntent = new Intent(APP_NOTIFICATION_PREFS_CATEGORY_INTENT).setClassName(activityInfo.packageName, activityInfo.name);
            } else if (DEBUG) {
                Log.v("AppNotificationSettings", "Ignoring duplicate notification preference activity (" + activityInfo.name + ") for package " + activityInfo.packageName);
            }
        }
    }
}
