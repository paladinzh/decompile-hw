package com.android.settings.applications;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateWriteSettingsBridge.WriteSettingsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class WriteSettingsDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = new int[]{23};
    private AppStateWriteSettingsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;
    private Preference mWriteSettingsDesc;
    private Preference mWriteSettingsPrefs;
    private WriteSettingsState mWriteSettingsState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mAppBridge = new AppStateWriteSettingsBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(2131230739);
        this.mSwitchPref = (SwitchPreference) findPreference("app_ops_settings_switch");
        this.mWriteSettingsPrefs = findPreference("app_ops_settings_preference");
        this.mWriteSettingsDesc = findPreference("app_ops_settings_description");
        getPreferenceScreen().setTitle(2131627057);
        this.mSwitchPref.setTitle(2131627064);
        this.mWriteSettingsPrefs.setTitle(2131627063);
        this.mWriteSettingsDesc.setSummary(2131627065);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mWriteSettingsPrefs.setOnPreferenceClickListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.USAGE_ACCESS_CONFIG").setPackage(this.mPackageName);
    }

    public void onDestroy() {
        if (!(this.mAppBridge == null || this.mState == null)) {
            this.mAppBridge.release();
        }
        super.onDestroy();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mWriteSettingsPrefs) {
            return false;
        }
        if (this.mSettingsIntent != null) {
            try {
                getActivity().startActivityAsUser(this.mSettingsIntent, new UserHandle(this.mUserId));
            } catch (ActivityNotFoundException e) {
                Log.w("WriteSettingsDetails", "Unable to launch write system settings " + this.mSettingsIntent, e);
            }
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean z = false;
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mWriteSettingsState == null || ((Boolean) newValue).booleanValue() == this.mWriteSettingsState.isPermissible())) {
            if (!this.mWriteSettingsState.isPermissible()) {
                z = true;
            }
            setCanWriteSettings(z);
            refreshUi();
        }
        return true;
    }

    private void setCanWriteSettings(boolean newState) {
        this.mAppOpsManager.setMode(23, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 2);
    }

    protected boolean refreshUi() {
        if (this.mPackageInfo == null || !ApplicationExtUtils.isPackageInfoExist("WriteSettingsDetails", this.mPackageName, this.mPackageInfo.applicationInfo.uid)) {
            return false;
        }
        this.mWriteSettingsState = this.mAppBridge.getWriteSettingsInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        boolean canWrite = this.mWriteSettingsState.isPermissible();
        this.mSwitchPref.setChecked(canWrite);
        this.mSwitchPref.setEnabled(this.mWriteSettingsState.permissionDeclared);
        this.mWriteSettingsPrefs.setEnabled(canWrite);
        getPreferenceScreen().removePreference(this.mWriteSettingsPrefs);
        return true;
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    protected int getMetricsCategory() {
        return 221;
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        WriteSettingsState state;
        if (entry.extraInfo instanceof WriteSettingsState) {
            state = (WriteSettingsState) entry.extraInfo;
        } else if (entry.extraInfo instanceof PermissionState) {
            state = new WriteSettingsState((PermissionState) entry.extraInfo);
        } else {
            state = new AppStateWriteSettingsBridge(context, null, null).getWriteSettingsInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, WriteSettingsState writeSettingsState) {
        int i;
        if (writeSettingsState.isPermissible()) {
            i = 2131627066;
        } else {
            i = 2131627067;
        }
        return context.getString(i);
    }
}
