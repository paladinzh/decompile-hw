package com.huawei.permissionmanager.model;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import com.android.internal.util.ArrayUtils;
import com.huawei.permissionmanager.utils.LocationUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public final class AppPermissionGroup {
    private static final String KILL_REASON_APP_OP_CHANGE = "Permission related app op changed";
    private static final String PLATFORM_PACKAGE_NAME = "android";
    private static final String TAG = "AppPermissionGroup";
    private final AppOpsManager mAppOps;
    private final boolean mAppSupportsRuntimePermissions;
    private final Context mContext;
    private final String mDeclaringPackage;
    private final int mIconResId;
    private final String mName;
    private final PackageInfo mPackageInfo;
    private final PackageManager mPackageManager;
    private final ArrayMap<String, Permission> mPermissions = new ArrayMap();
    private final UserHandle mUserHandle;

    public static AppPermissionGroup create(Context context, PackageInfo packageInfo, String permissionName) {
        try {
            PackageItemInfo permissionInfo = context.getPackageManager().getPermissionInfo(permissionName, 0);
            if (permissionInfo.protectionLevel != 1 || (permissionInfo.flags & 1073741824) == 0 || (permissionInfo.flags & 2) != 0) {
                return null;
            }
            PackageItemInfo groupInfo = permissionInfo;
            if (permissionInfo.group != null) {
                try {
                    groupInfo = context.getPackageManager().getPermissionGroupInfo(permissionInfo.group, 0);
                } catch (NameNotFoundException e) {
                }
            }
            List permissionInfos = null;
            if (groupInfo instanceof PermissionGroupInfo) {
                try {
                    permissionInfos = context.getPackageManager().queryPermissionsByGroup(groupInfo.name, 0);
                } catch (NameNotFoundException e2) {
                }
            }
            return create(context, packageInfo, groupInfo, permissionInfos, Process.myUserHandle());
        } catch (NameNotFoundException e3) {
            return null;
        }
    }

    public static AppPermissionGroup create(Context context, PackageInfo packageInfo, PackageItemInfo groupInfo, List<PermissionInfo> permissionInfos, UserHandle userHandle) {
        AppPermissionGroup group = new AppPermissionGroup(context, packageInfo, groupInfo.name, groupInfo.packageName, null, null, groupInfo.packageName, groupInfo.icon, userHandle);
        if (groupInfo instanceof PermissionInfo) {
            permissionInfos = new ArrayList();
            permissionInfos.add((PermissionInfo) groupInfo);
        }
        if (permissionInfos == null || permissionInfos.isEmpty()) {
            return null;
        }
        int permissionCount = packageInfo.requestedPermissions.length;
        for (int i = 0; i < permissionCount; i++) {
            String requestedPermission = packageInfo.requestedPermissions[i];
            PermissionInfo requestedPermissionInfo = null;
            for (PermissionInfo permissionInfo : permissionInfos) {
                if (requestedPermission.equals(permissionInfo.name)) {
                    requestedPermissionInfo = permissionInfo;
                    break;
                }
            }
            if (requestedPermissionInfo != null && requestedPermissionInfo.protectionLevel == 1 && (packageInfo.applicationInfo.targetSdkVersion > 22 || "android".equals(groupInfo.packageName))) {
                boolean granted = (packageInfo.requestedPermissionsFlags[i] & 2) != 0;
                String permissionToOp = "android".equals(requestedPermissionInfo.packageName) ? AppOpsManager.permissionToOp(requestedPermissionInfo.name) : null;
                AppOpsManager aom = (AppOpsManager) context.getSystemService(AppOpsManager.class);
                boolean appOpAllowed = (permissionToOp == null || aom == null) ? false : aom.checkOpNoThrow(permissionToOp, packageInfo.applicationInfo.uid, packageInfo.packageName) == 0;
                int flags = 1;
                if (userHandle != null) {
                    try {
                        flags = context.getPackageManager().getPermissionFlags(requestedPermission, packageInfo.packageName, userHandle);
                    } catch (IllegalArgumentException e) {
                        HwLog.e(TAG, "unknown packageName : " + packageInfo.packageName + e.toString());
                    }
                }
                group.addPermission(new Permission(requestedPermission, granted, permissionToOp, appOpAllowed, flags));
            }
        }
        return group;
    }

    public static CharSequence loadGroupDescription(Context context, PackageItemInfo group) {
        CharSequence description = null;
        if (group instanceof PermissionGroupInfo) {
            description = ((PermissionGroupInfo) group).loadDescription(context.getPackageManager());
        } else if (group instanceof PermissionInfo) {
            description = ((PermissionInfo) group).loadDescription(context.getPackageManager());
        }
        if (description == null || description.length() <= 0) {
            return context.getString(R.string.GeneralPermissionType);
        }
        return description;
    }

    private AppPermissionGroup(Context context, PackageInfo packageInfo, String name, String declaringPackage, CharSequence label, CharSequence description, String iconPkg, int iconResId, UserHandle userHandle) {
        boolean z = false;
        this.mContext = context;
        this.mUserHandle = userHandle;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPackageInfo = packageInfo;
        if (packageInfo.applicationInfo.targetSdkVersion > 22) {
            z = true;
        }
        this.mAppSupportsRuntimePermissions = z;
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mDeclaringPackage = declaringPackage;
        this.mName = name;
        if (iconResId != 0) {
            this.mIconResId = iconResId;
        } else {
            this.mIconResId = R.drawable.ic_launcher_permission;
        }
    }

    public boolean hasRuntimePermission() {
        return this.mAppSupportsRuntimePermissions;
    }

    public boolean hasGrantedByDefaultPermission() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (((Permission) this.mPermissions.valueAt(i)).isGrantedByDefault()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAppOpPermission() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (((Permission) this.mPermissions.valueAt(i)).getAppOp() != null) {
                return true;
            }
        }
        return false;
    }

    public PackageInfo getApp() {
        return this.mPackageInfo;
    }

    public String getName() {
        return this.mName;
    }

    public String getDeclaringPackage() {
        return this.mDeclaringPackage;
    }

    public int getIconResId() {
        return this.mIconResId;
    }

    public int getUserId() {
        return this.mUserHandle.getIdentifier();
    }

    public boolean hasPermission(String permission) {
        return this.mPermissions.get(permission) != null;
    }

    public boolean areRuntimePermissionsGranted() {
        return areRuntimePermissionsGranted(null);
    }

    public boolean areRuntimePermissionsGranted(String[] filterPermissions) {
        if (LocationUtils.isLocationGroupAndProvider(this.mName, this.mPackageInfo.packageName)) {
            return LocationUtils.isLocationEnabled(this.mContext);
        }
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            Permission permission = (Permission) this.mPermissions.valueAt(i);
            if (filterPermissions == null || ArrayUtils.contains(filterPermissions, permission.getName())) {
                if (this.mAppSupportsRuntimePermissions) {
                    if (permission.isGranted()) {
                        return true;
                    }
                } else if (permission.isGranted() && (permission.getAppOp() == null || permission.isAppOpAllowed())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean grantRuntimePermissions(boolean fixedByTheUser) {
        return grantRuntimePermissions(fixedByTheUser, null);
    }

    public boolean grantRuntimePermissions(boolean fixedByTheUser, String[] filterPermissions) {
        int uid = this.mPackageInfo.applicationInfo.uid;
        for (Permission permission : this.mPermissions.values()) {
            if (filterPermissions == null || ArrayUtils.contains(filterPermissions, permission.getName())) {
                if (this.mAppSupportsRuntimePermissions) {
                    if (permission.isSystemFixed()) {
                        return false;
                    }
                    if (permission.hasAppOp() && !permission.isAppOpAllowed()) {
                        permission.setAppOpAllowed(true);
                        this.mAppOps.setUidMode(permission.getAppOp(), uid, 0);
                    }
                    if (!permission.isGranted()) {
                        permission.setGranted(true);
                        this.mPackageManager.grantRuntimePermission(this.mPackageInfo.packageName, permission.getName(), this.mUserHandle);
                    }
                    if (!fixedByTheUser && (permission.isUserFixed() || permission.isUserSet())) {
                        permission.setUserFixed(false);
                        permission.setUserSet(true);
                        this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, 3, 0, this.mUserHandle);
                    }
                } else if (permission.isGranted()) {
                    int mask = 0;
                    if (permission.hasAppOp()) {
                        if (!permission.isAppOpAllowed()) {
                            permission.setAppOpAllowed(true);
                            this.mAppOps.setUidMode(permission.getAppOp(), uid, 0);
                        }
                        if (permission.shouldRevokeOnUpgrade()) {
                            permission.setRevokeOnUpgrade(false);
                            mask = 8;
                        }
                    }
                    if (mask != 0) {
                        this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, mask, 0, this.mUserHandle);
                    }
                }
            }
        }
        return true;
    }

    public boolean revokeRuntimePermissions(boolean fixedByTheUser) {
        return revokeRuntimePermissions(fixedByTheUser, null);
    }

    public boolean revokeRuntimePermissions(boolean fixedByTheUser, String[] filterPermissions) {
        int uid = this.mPackageInfo.applicationInfo.uid;
        for (Permission permission : this.mPermissions.values()) {
            if (filterPermissions == null || ArrayUtils.contains(filterPermissions, permission.getName())) {
                if (this.mAppSupportsRuntimePermissions) {
                    if (permission.isSystemFixed()) {
                        return false;
                    }
                    if (permission.isGranted()) {
                        permission.setGranted(false);
                        this.mPackageManager.revokeRuntimePermission(this.mPackageInfo.packageName, permission.getName(), this.mUserHandle);
                    }
                    if (fixedByTheUser) {
                        if (permission.isUserSet() || !permission.isUserFixed()) {
                            permission.setUserSet(false);
                            permission.setUserFixed(true);
                            this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, 3, 2, this.mUserHandle);
                        }
                    } else if (!permission.isUserSet()) {
                        permission.setUserSet(true);
                        this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, 1, 1, this.mUserHandle);
                    }
                } else if (permission.isGranted()) {
                    int mask = 0;
                    int flags = 0;
                    if (permission.hasAppOp()) {
                        if (permission.isAppOpAllowed()) {
                            permission.setAppOpAllowed(false);
                            this.mAppOps.setUidMode(permission.getAppOp(), uid, 1);
                        }
                        if (!permission.shouldRevokeOnUpgrade()) {
                            permission.setRevokeOnUpgrade(true);
                            mask = 8;
                            flags = 8;
                        }
                    }
                    if (mask != 0) {
                        this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, mask, flags, this.mUserHandle);
                    }
                }
            }
        }
        return true;
    }

    public void setPolicyFixed() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            Permission permission = (Permission) this.mPermissions.valueAt(i);
            permission.setPolicyFixed(true);
            this.mPackageManager.updatePermissionFlags(permission.getName(), this.mPackageInfo.packageName, 4, 4, this.mUserHandle);
        }
    }

    public List<Permission> getPermissions() {
        return new ArrayList(this.mPermissions.values());
    }

    public int getFlags() {
        int flags = 0;
        for (int i = 0; i < this.mPermissions.size(); i++) {
            flags |= ((Permission) this.mPermissions.valueAt(i)).getFlags();
        }
        return flags;
    }

    public boolean isUserFixed() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (!((Permission) this.mPermissions.valueAt(i)).isUserFixed()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPolicyFixed() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (((Permission) this.mPermissions.valueAt(i)).isPolicyFixed()) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserSet() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (!((Permission) this.mPermissions.valueAt(i)).isUserSet()) {
                return false;
            }
        }
        return true;
    }

    public boolean isSystemFixed() {
        int permissionCount = this.mPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            if (((Permission) this.mPermissions.valueAt(i)).isSystemFixed()) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AppPermissionGroup other = (AppPermissionGroup) obj;
        if (this.mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!this.mName.equals(other.mName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mName != null ? this.mName.hashCode() : 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{name=").append(this.mName);
        if (this.mPermissions.isEmpty()) {
            builder.append('}');
        } else {
            builder.append(", <has permissions>}");
        }
        return builder.toString();
    }

    private void addPermission(Permission permission) {
        this.mPermissions.put(permission.getName(), permission);
    }
}
