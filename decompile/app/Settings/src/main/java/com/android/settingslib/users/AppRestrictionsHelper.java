package com.android.settingslib.users;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AppRestrictionsHelper {
    private final Context mContext;
    private final IPackageManager mIPm;
    private boolean mLeanback;
    private final PackageManager mPackageManager;
    private final boolean mRestrictedProfile;
    HashMap<String, Boolean> mSelectedPackages = new HashMap();
    private final UserHandle mUser;
    private final UserManager mUserManager;
    private List<SelectableAppInfo> mVisibleApps;

    public interface OnDisableUiForPackageListener {
        void onDisableUiForPackage(String str);
    }

    private static class AppLabelComparator implements Comparator<SelectableAppInfo> {
        private AppLabelComparator() {
        }

        public int compare(SelectableAppInfo lhs, SelectableAppInfo rhs) {
            return lhs.activityName.toString().toLowerCase().compareTo(rhs.activityName.toString().toLowerCase());
        }
    }

    public static class SelectableAppInfo {
        public CharSequence activityName;
        public CharSequence appName;
        public Drawable icon;
        public SelectableAppInfo masterEntry;
        public String packageName;

        public String toString() {
            return this.packageName + ": appName=" + this.appName + "; activityName=" + this.activityName + "; icon=" + this.icon + "; masterEntry=" + this.masterEntry;
        }
    }

    public AppRestrictionsHelper(Context context, UserHandle user) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mIPm = AppGlobals.getPackageManager();
        this.mUser = user;
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mRestrictedProfile = this.mUserManager.getUserInfo(this.mUser.getIdentifier()).isRestricted();
    }

    public void setPackageSelected(String packageName, boolean selected) {
        this.mSelectedPackages.put(packageName, Boolean.valueOf(selected));
    }

    public boolean isPackageSelected(String packageName) {
        return ((Boolean) this.mSelectedPackages.get(packageName)).booleanValue();
    }

    public List<SelectableAppInfo> getVisibleApps() {
        return this.mVisibleApps;
    }

    public void applyUserAppsStates(OnDisableUiForPackageListener listener) {
        int userId = this.mUser.getIdentifier();
        if (this.mUserManager.getUserInfo(userId).isRestricted() || userId == UserHandle.myUserId()) {
            for (Entry<String, Boolean> entry : this.mSelectedPackages.entrySet()) {
                applyUserAppState((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue(), listener);
            }
            return;
        }
        Log.e("AppRestrictionsHelper", "Cannot apply application restrictions on another user!");
    }

    public void applyUserAppState(String packageName, boolean enabled, OnDisableUiForPackageListener listener) {
        int userId = this.mUser.getIdentifier();
        if (enabled) {
            try {
                ApplicationInfo info = this.mIPm.getApplicationInfo(packageName, 8192, userId);
                if (info == null || !info.enabled || (info.flags & 8388608) == 0) {
                    this.mIPm.installExistingPackageAsUser(packageName, this.mUser.getIdentifier());
                }
                if (info != null && (info.privateFlags & 1) != 0 && (info.flags & 8388608) != 0) {
                    listener.onDisableUiForPackage(packageName);
                    this.mIPm.setApplicationHiddenSettingAsUser(packageName, false, userId);
                    return;
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        try {
            if (this.mIPm.getApplicationInfo(packageName, 0, userId) == null) {
                return;
            }
            if (this.mRestrictedProfile) {
                this.mIPm.deletePackageAsUser(packageName, null, this.mUser.getIdentifier(), 4);
                return;
            }
            listener.onDisableUiForPackage(packageName);
            this.mIPm.setApplicationHiddenSettingAsUser(packageName, true, userId);
        } catch (RemoteException e2) {
        }
    }

    public void fetchAndMergeApps() {
        this.mVisibleApps = new ArrayList();
        PackageManager pm = this.mPackageManager;
        IPackageManager ipm = this.mIPm;
        HashSet<String> excludePackages = new HashSet();
        addSystemImes(excludePackages);
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        if (this.mLeanback) {
            launcherIntent.addCategory("android.intent.category.LEANBACK_LAUNCHER");
        } else {
            launcherIntent.addCategory("android.intent.category.LAUNCHER");
        }
        addSystemApps(this.mVisibleApps, launcherIntent, excludePackages);
        Intent widgetIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        addSystemApps(this.mVisibleApps, widgetIntent, excludePackages);
        for (ApplicationInfo app : pm.getInstalledApplications(8192)) {
            if ((app.flags & 8388608) != 0) {
                if ((app.flags & 1) == 0 && (app.flags & 128) == 0) {
                    SelectableAppInfo info = new SelectableAppInfo();
                    info.packageName = app.packageName;
                    info.appName = app.loadLabel(pm);
                    info.activityName = info.appName;
                    info.icon = app.loadIcon(pm);
                    this.mVisibleApps.add(info);
                } else {
                    try {
                        PackageInfo pi = pm.getPackageInfo(app.packageName, 0);
                        if (this.mRestrictedProfile && pi.requiredAccountType != null && pi.restrictedAccountType == null) {
                            this.mSelectedPackages.put(app.packageName, Boolean.valueOf(false));
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
            }
        }
        Iterable userApps = null;
        try {
            ParceledListSlice<ApplicationInfo> listSlice = ipm.getInstalledApplications(8192, this.mUser.getIdentifier());
            if (listSlice != null) {
                userApps = listSlice.getList();
            }
        } catch (RemoteException e2) {
        }
        if (r20 != null) {
            for (ApplicationInfo app2 : r20) {
                if ((app2.flags & 8388608) != 0 && (app2.flags & 1) == 0 && (app2.flags & 128) == 0) {
                    info = new SelectableAppInfo();
                    info.packageName = app2.packageName;
                    info.appName = app2.loadLabel(pm);
                    info.activityName = info.appName;
                    info.icon = app2.loadIcon(pm);
                    this.mVisibleApps.add(info);
                }
            }
        }
        Collections.sort(this.mVisibleApps, new AppLabelComparator());
        Set<String> dedupPackageSet = new HashSet();
        for (int i = this.mVisibleApps.size() - 1; i >= 0; i--) {
            info = (SelectableAppInfo) this.mVisibleApps.get(i);
            String both = info.packageName + "+" + info.activityName;
            if (TextUtils.isEmpty(info.packageName) || TextUtils.isEmpty(info.activityName) || !dedupPackageSet.contains(both)) {
                dedupPackageSet.add(both);
            } else {
                this.mVisibleApps.remove(i);
            }
        }
        HashMap<String, SelectableAppInfo> packageMap = new HashMap();
        for (SelectableAppInfo info2 : this.mVisibleApps) {
            if (packageMap.containsKey(info2.packageName)) {
                info2.masterEntry = (SelectableAppInfo) packageMap.get(info2.packageName);
            } else {
                packageMap.put(info2.packageName, info2);
            }
        }
    }

    private void addSystemImes(Set<String> excludePackages) {
        for (InputMethodInfo imi : ((InputMethodManager) this.mContext.getSystemService("input_method")).getInputMethodList()) {
            try {
                if (imi.isDefault(this.mContext) && isSystemPackage(imi.getPackageName())) {
                    excludePackages.add(imi.getPackageName());
                }
            } catch (NotFoundException e) {
            }
        }
    }

    private void addSystemApps(List<SelectableAppInfo> visibleApps, Intent intent, Set<String> excludePackages) {
        PackageManager pm = this.mPackageManager;
        for (ResolveInfo app : pm.queryIntentActivities(intent, 8704)) {
            if (!(app.activityInfo == null || app.activityInfo.applicationInfo == null)) {
                String packageName = app.activityInfo.packageName;
                int flags = app.activityInfo.applicationInfo.flags;
                if (!(((flags & 1) == 0 && (flags & 128) == 0) || excludePackages.contains(packageName))) {
                    int enabled = pm.getApplicationEnabledSetting(packageName);
                    if (enabled == 4 || enabled == 2) {
                        ApplicationInfo targetUserAppInfo = getAppInfoForUser(packageName, 0, this.mUser);
                        if (targetUserAppInfo != null) {
                            if ((targetUserAppInfo.flags & 8388608) == 0) {
                            }
                        }
                    }
                    SelectableAppInfo info = new SelectableAppInfo();
                    info.packageName = app.activityInfo.packageName;
                    info.appName = app.activityInfo.applicationInfo.loadLabel(pm);
                    info.icon = app.activityInfo.loadIcon(pm);
                    info.activityName = app.activityInfo.loadLabel(pm);
                    if (info.activityName == null) {
                        info.activityName = info.appName;
                    }
                    visibleApps.add(info);
                }
            }
        }
    }

    private boolean isSystemPackage(String packageName) {
        try {
            PackageInfo pi = this.mPackageManager.getPackageInfo(packageName, 0);
            if (pi.applicationInfo == null) {
                return false;
            }
            int flags = pi.applicationInfo.flags;
            if (!((flags & 1) == 0 && (flags & 128) == 0)) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
        }
    }

    private ApplicationInfo getAppInfoForUser(String packageName, int flags, UserHandle user) {
        try {
            return this.mIPm.getApplicationInfo(packageName, flags, user.getIdentifier());
        } catch (RemoteException e) {
            return null;
        }
    }
}
