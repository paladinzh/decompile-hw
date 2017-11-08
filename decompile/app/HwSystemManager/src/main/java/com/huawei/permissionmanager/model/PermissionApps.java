package com.huawei.permissionmanager.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.hsm.pm.M2NAdapter;
import com.huawei.permissionmanager.utils.Utils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PermissionApps {
    private static final String LOG_TAG = "PermissionApps";
    private ArrayMap<String, PermissionApp> mAppLookup;
    private final PmCache mCache;
    private final Callback mCallback;
    private final Context mContext;
    private final String mGroupName;
    private Drawable mIcon;
    private CharSequence mLabel;
    private List<PermissionApp> mPermApps;
    private final PackageManager mPm;
    private boolean mRefreshing;
    private boolean mSkipUi;

    public interface Callback {
        void onPermissionsLoaded(PermissionApps permissionApps);
    }

    public static class PermissionApp implements Comparable<PermissionApp> {
        private final AppPermissionGroup mAppPermissionGroup;
        private final Drawable mIcon;
        private final ApplicationInfo mInfo;
        private final String mLabel;
        private final String mPackageName;

        public PermissionApp(String packageName, AppPermissionGroup appPermissionGroup, String label, Drawable icon, ApplicationInfo info) {
            this.mPackageName = packageName;
            this.mAppPermissionGroup = appPermissionGroup;
            this.mLabel = label;
            this.mIcon = icon;
            this.mInfo = info;
        }

        public ApplicationInfo getAppInfo() {
            return this.mInfo;
        }

        public String getKey() {
            return Integer.toString(getUid());
        }

        public String getLabel() {
            return this.mLabel;
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public boolean areRuntimePermissionsGranted() {
            return this.mAppPermissionGroup.areRuntimePermissionsGranted();
        }

        public boolean areRuntimePermissionsGranted(String permission) {
            return this.mAppPermissionGroup.areRuntimePermissionsGranted(new String[]{permission});
        }

        public void grantRuntimePermissions() {
            this.mAppPermissionGroup.grantRuntimePermissions(false);
        }

        public void grantRuntimePermissions(String permission) {
            this.mAppPermissionGroup.grantRuntimePermissions(false, new String[]{permission});
        }

        public void revokeRuntimePermissions() {
            this.mAppPermissionGroup.revokeRuntimePermissions(false);
        }

        public boolean isPolicyFixed() {
            return this.mAppPermissionGroup.isPolicyFixed();
        }

        public boolean isSystemFixed() {
            return this.mAppPermissionGroup.isSystemFixed();
        }

        public boolean hasGrantedByDefaultPermissions() {
            return this.mAppPermissionGroup.hasGrantedByDefaultPermission();
        }

        public boolean hasRuntimePermissions() {
            return this.mAppPermissionGroup.hasRuntimePermission();
        }

        public boolean hasAppOpPermissions() {
            return this.mAppPermissionGroup.hasAppOpPermission();
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public AppPermissionGroup getPermissionGroup() {
            return this.mAppPermissionGroup;
        }

        public int compareTo(PermissionApp another) {
            int result = this.mLabel.compareTo(another.mLabel);
            if (result == 0) {
                return getUid() - another.getUid();
            }
            return result;
        }

        public int getUid() {
            return this.mAppPermissionGroup.getApp().applicationInfo.uid;
        }
    }

    private class PermissionAppsLoader extends AsyncTask<Void, Void, List<PermissionApp>> {
        private PermissionAppsLoader() {
        }

        protected List<PermissionApp> doInBackground(Void... args) {
            return PermissionApps.this.loadPermissionApps();
        }

        protected void onPostExecute(List<PermissionApp> result) {
            PermissionApps.this.mRefreshing = false;
            PermissionApps.this.createMap(result);
            if (PermissionApps.this.mCallback != null) {
                PermissionApps.this.mCallback.onPermissionsLoaded(PermissionApps.this);
            }
        }
    }

    public static class PmCache {
        private final SparseArray<List<PackageInfo>> mPackageInfoCache = new SparseArray();
        private final PackageManager mPm;

        public PmCache(PackageManager pm) {
            this.mPm = pm;
        }

        public synchronized List<PackageInfo> getPackages(int userId) {
            List<PackageInfo> ret;
            ret = (List) this.mPackageInfoCache.get(userId);
            if (ret == null) {
                ret = M2NAdapter.getInstalledPackagesAsUser(this.mPm, 4096, userId);
                this.mPackageInfoCache.put(userId, ret);
            }
            return ret;
        }
    }

    public static PermissionApps create(Context context, String groupName) {
        HwLog.i(LOG_TAG, "create pas for " + groupName);
        PermissionApps pa = new PermissionApps(context, groupName, null);
        pa.loadNowWithoutUi();
        HwLog.i(LOG_TAG, "create finished for " + groupName);
        return pa;
    }

    public PermissionApps(Context context, String groupName, Callback callback) {
        this(context, groupName, callback, null);
    }

    public PermissionApps(Context context, String groupName, Callback callback, PmCache cache) {
        this.mCache = cache;
        this.mContext = context;
        this.mPm = this.mContext.getPackageManager();
        this.mGroupName = groupName;
        this.mCallback = callback;
        loadGroupInfo();
    }

    public String getGroupName() {
        return this.mGroupName;
    }

    public void loadNowWithoutUi() {
        this.mSkipUi = true;
        createMap(loadPermissionApps());
    }

    public void refresh(boolean getUiInfo) {
        boolean z = true;
        if (!this.mRefreshing) {
            this.mRefreshing = true;
            if (getUiInfo) {
                z = false;
            }
            this.mSkipUi = z;
            new PermissionAppsLoader().execute(new Void[0]);
        }
    }

    public int getGrantedCount(ArraySet<String> launcherPkgs) {
        int count = 0;
        for (PermissionApp app : this.mPermApps) {
            if (Utils.shouldShowPermission(app) && !Utils.isSystem(app, launcherPkgs) && app.areRuntimePermissionsGranted()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount(ArraySet<String> launcherPkgs) {
        int count = 0;
        for (PermissionApp app : this.mPermApps) {
            if (Utils.shouldShowPermission(app) && !Utils.isSystem(app, launcherPkgs)) {
                count++;
            }
        }
        return count;
    }

    public Collection<PermissionApp> getApps() {
        return this.mPermApps;
    }

    public PermissionApp getApp(String key) {
        return (PermissionApp) this.mAppLookup.get(key);
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    private List<PermissionApp> loadPermissionApps() {
        PackageItemInfo groupInfo = getGroupInfo(this.mGroupName);
        if (groupInfo == null) {
            return Collections.emptyList();
        }
        List<PermissionInfo> groupPermInfos = getGroupPermissionInfos(this.mGroupName);
        if (groupPermInfos == null) {
            return Collections.emptyList();
        }
        ArrayList<PermissionApp> permApps = new ArrayList();
        for (UserHandle user : UserManager.get(this.mContext).getUserProfiles()) {
            List<PackageInfo> apps;
            if (this.mCache != null) {
                apps = this.mCache.getPackages(user.getIdentifier());
            } else {
                apps = M2NAdapter.getInstalledPackagesAsUser(this.mPm, 4096, user.getIdentifier());
            }
            int N = apps.size();
            for (int i = 0; i < N; i++) {
                PackageInfo app = (PackageInfo) apps.get(i);
                if (app.requestedPermissions != null) {
                    int j = 0;
                    while (j < app.requestedPermissions.length) {
                        String requestedPerm = app.requestedPermissions[j];
                        PermissionInfo requestedPermissionInfo = null;
                        for (PermissionInfo groupPermInfo : groupPermInfos) {
                            if (requestedPerm.equals(groupPermInfo.name)) {
                                requestedPermissionInfo = groupPermInfo;
                                break;
                            }
                        }
                        if (requestedPermissionInfo == null || requestedPermissionInfo.protectionLevel != 1 || (requestedPermissionInfo.flags & 1073741824) == 0) {
                            j++;
                        } else {
                            String label;
                            AppPermissionGroup group = AppPermissionGroup.create(this.mContext, app, groupInfo, groupPermInfos, user);
                            if (this.mSkipUi) {
                                label = app.packageName;
                            } else {
                                label = app.applicationInfo.loadLabel(this.mPm).toString();
                            }
                            permApps.add(new PermissionApp(app.packageName, group, label, getBadgedIcon(app.applicationInfo), app.applicationInfo));
                        }
                    }
                }
            }
        }
        return permApps;
    }

    private void createMap(List<PermissionApp> result) {
        this.mAppLookup = new ArrayMap();
        for (PermissionApp app : result) {
            this.mAppLookup.put(app.getKey(), app);
        }
        this.mPermApps = result;
    }

    private PackageItemInfo getGroupInfo(String groupName) {
        try {
            return this.mContext.getPackageManager().getPermissionGroupInfo(groupName, 0);
        } catch (NameNotFoundException e) {
            try {
                return this.mContext.getPackageManager().getPermissionInfo(groupName, 0);
            } catch (NameNotFoundException e2) {
                return null;
            }
        }
    }

    private List<PermissionInfo> getGroupPermissionInfos(String groupName) {
        try {
            return this.mContext.getPackageManager().queryPermissionsByGroup(groupName, 0);
        } catch (NameNotFoundException e) {
            try {
                PermissionInfo permissionInfo = this.mContext.getPackageManager().getPermissionInfo(groupName, 0);
                List<PermissionInfo> permissions = new ArrayList();
                permissions.add(permissionInfo);
                return permissions;
            } catch (NameNotFoundException e2) {
                return null;
            }
        }
    }

    private Drawable getBadgedIcon(ApplicationInfo appInfo) {
        if (this.mSkipUi) {
            return null;
        }
        return this.mPm.getUserBadgedIcon(appInfo.loadUnbadgedIcon(this.mPm), new UserHandle(UserHandle.getUserId(appInfo.uid)));
    }

    private void loadGroupInfo() {
        try {
            PackageItemInfo info = this.mPm.getPermissionGroupInfo(this.mGroupName, 0);
        } catch (NameNotFoundException e) {
            try {
                PermissionInfo permInfo = this.mPm.getPermissionInfo(this.mGroupName, 0);
                if (permInfo.protectionLevel != 1) {
                    Log.w(LOG_TAG, this.mGroupName + " is not a runtime permission");
                    return;
                }
                PermissionInfo permissionInfo = permInfo;
            } catch (NameNotFoundException reallyNotFound) {
                Log.w(LOG_TAG, "Can't find permission: " + this.mGroupName, reallyNotFound);
            }
        }
    }
}
