package com.android.settings.applications;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.applications.AppStateUsageBridge.UsageState;

public class UsageAccessDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private AppOpsManager mAppOpsManager;
    private DevicePolicyManager mDpm;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;
    private AppStateUsageBridge mUsageBridge;
    private Preference mUsageDesc;
    private Preference mUsagePrefs;
    private UsageState mUsageState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mUsageBridge = new AppStateUsageBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mDpm = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        addPreferencesFromResource(2131230739);
        this.mSwitchPref = (SwitchPreference) findPreference("app_ops_settings_switch");
        this.mUsagePrefs = findPreference("app_ops_settings_preference");
        this.mUsageDesc = findPreference("app_ops_settings_description");
        getPreferenceScreen().setTitle(2131626959);
        this.mSwitchPref.setTitle(2131626960);
        this.mUsagePrefs.setTitle(2131626961);
        this.mUsageDesc.setSummary(2131626962);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mUsagePrefs.setOnPreferenceClickListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.USAGE_ACCESS_CONFIG").setPackage(this.mPackageName);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mUsagePrefs) {
            return false;
        }
        if (this.mSettingsIntent != null) {
            try {
                getActivity().startActivityAsUser(this.mSettingsIntent, new UserHandle(this.mUserId));
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Unable to launch app usage access settings " + this.mSettingsIntent, e);
            }
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean z = false;
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mUsageState == null || ((Boolean) newValue).booleanValue() == this.mUsageState.isPermissible())) {
            if (this.mUsageState.isPermissible() && this.mDpm.isProfileOwnerApp(this.mPackageName)) {
                new Builder(getContext()).setIcon(17302317).setTitle(17039380).setMessage(2131627040).setPositiveButton(2131624573, null).show();
            }
            if (!this.mUsageState.isPermissible()) {
                z = true;
            }
            setHasAccess(z);
            refreshUi();
        }
        return true;
    }

    private void setHasAccess(boolean newState) {
        this.mAppOpsManager.setMode(43, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 1);
    }

    protected boolean refreshUi() {
        if (this.mPackageInfo == null) {
            Log.e(TAG, "mPackageInfo is null");
            return false;
        }
        this.mUsageState = this.mUsageBridge.getUsageInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        boolean hasAccess = this.mUsageState.isPermissible();
        this.mSwitchPref.setChecked(hasAccess);
        this.mSwitchPref.setEnabled(this.mUsageState.permissionDeclared);
        this.mUsagePrefs.setEnabled(hasAccess);
        ResolveInfo resolveInfo = this.mPm.resolveActivityAsUser(this.mSettingsIntent, 128, this.mUserId);
        if (resolveInfo != null) {
            if (findPreference("app_ops_settings_preference") == null) {
                getPreferenceScreen().addPreference(this.mUsagePrefs);
            }
            Bundle metaData = resolveInfo.activityInfo.metaData;
            this.mSettingsIntent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            if (metaData != null && metaData.containsKey("android.settings.metadata.USAGE_ACCESS_REASON")) {
                this.mSwitchPref.setSummary(metaData.getString("android.settings.metadata.USAGE_ACCESS_REASON"));
            }
        } else if (findPreference("app_ops_settings_preference") != null) {
            getPreferenceScreen().removePreference(this.mUsagePrefs);
        }
        return true;
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    protected int getMetricsCategory() {
        return 183;
    }
}
