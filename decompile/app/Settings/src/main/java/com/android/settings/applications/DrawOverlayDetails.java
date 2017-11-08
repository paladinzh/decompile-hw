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
import com.android.settings.applications.AppStateOverlayBridge.OverlayState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class DrawOverlayDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = new int[]{24};
    private AppOpsManager mAppOpsManager;
    private AppStateOverlayBridge mOverlayBridge;
    private Preference mOverlayDesc;
    private Preference mOverlayPrefs;
    private OverlayState mOverlayState;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mOverlayBridge = new AppStateOverlayBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(2131230739);
        this.mSwitchPref = (SwitchPreference) findPreference("app_ops_settings_switch");
        this.mOverlayPrefs = findPreference("app_ops_settings_preference");
        this.mOverlayDesc = findPreference("app_ops_settings_description");
        getPreferenceScreen().setTitle(2131627043);
        this.mSwitchPref.setTitle(2131627047);
        this.mOverlayPrefs.setTitle(2131627048);
        this.mOverlayDesc.setSummary(2131627049);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mOverlayPrefs.setOnPreferenceClickListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").setAction("android.settings.action.MANAGE_OVERLAY_PERMISSION");
    }

    public void onDestroy() {
        if (!(this.mOverlayBridge == null || this.mState == null)) {
            this.mOverlayBridge.release();
        }
        super.onDestroy();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mOverlayPrefs) {
            return false;
        }
        if (this.mSettingsIntent != null) {
            try {
                getActivity().startActivityAsUser(this.mSettingsIntent, new UserHandle(this.mUserId));
            } catch (ActivityNotFoundException e) {
                Log.w("DrawOverlayDetails", "Unable to launch app draw overlay settings " + this.mSettingsIntent, e);
            }
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean z = false;
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mOverlayState == null || ((Boolean) newValue).booleanValue() == this.mOverlayState.isPermissible())) {
            if (!this.mOverlayState.isPermissible()) {
                z = true;
            }
            setCanDrawOverlay(z);
            refreshUi();
        }
        return true;
    }

    private void setCanDrawOverlay(boolean newState) {
        this.mAppOpsManager.setMode(24, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 2);
    }

    protected boolean refreshUi() {
        if (this.mPackageInfo == null || !ApplicationExtUtils.isPackageInfoExist("DrawOverlayDetails", this.mPackageName, this.mPackageInfo.applicationInfo.uid)) {
            return false;
        }
        this.mOverlayState = this.mOverlayBridge.getOverlayInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        boolean isAllowed = this.mOverlayState.isPermissible();
        this.mSwitchPref.setChecked(isAllowed);
        this.mSwitchPref.setEnabled(this.mOverlayState.permissionDeclared);
        this.mOverlayPrefs.setEnabled(isAllowed);
        checkParentControl(this.mPackageName, isAllowed);
        getPreferenceScreen().removePreference(this.mOverlayPrefs);
        return true;
    }

    private void checkParentControl(String pkgName, boolean isAllowed) {
        if ("com.huawei.parentcontrol".equals(pkgName) && isAllowed) {
            Log.i("DrawOverlayDetails", "com.huawei.parentcontrol is on will can't change it");
            this.mSwitchPref.setEnabled(false);
        }
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    protected int getMetricsCategory() {
        return 221;
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        OverlayState state;
        if (entry.extraInfo instanceof OverlayState) {
            state = (OverlayState) entry.extraInfo;
        } else if (entry.extraInfo instanceof PermissionState) {
            state = new OverlayState((PermissionState) entry.extraInfo);
        } else {
            state = new AppStateOverlayBridge(context, null, null).getOverlayInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, OverlayState overlayState) {
        return context.getString(overlayState.isPermissible() ? 2131627055 : 2131627056);
    }
}
