package com.huawei.permissionmanager.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.ArrayMap;
import com.android.internal.util.ArrayUtils;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public final class AppPermissions {
    private final Context mContext;
    private final String[] mFilterPermissions;
    private final ArrayList<AppPermissionGroup> mGroups = new ArrayList();
    private final ArrayMap<String, AppPermissionGroup> mNameToGroupMap = new ArrayMap();
    private final Runnable mOnErrorCallback;
    private PackageInfo mPackageInfo;

    public AppPermissions(Context context, PackageInfo packageInfo, String[] permissions, boolean sortGroups, Runnable onErrorCallback) {
        this.mContext = context;
        this.mPackageInfo = packageInfo;
        this.mFilterPermissions = permissions;
        this.mOnErrorCallback = onErrorCallback;
        loadPermissionGroups();
    }

    public PackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }

    public void refresh() {
        loadPackageInfo();
        loadPermissionGroups();
    }

    public AppPermissionGroup getPermissionGroup(String name) {
        return (AppPermissionGroup) this.mNameToGroupMap.get(name);
    }

    public List<AppPermissionGroup> getPermissionGroups() {
        return this.mGroups;
    }

    private void loadPackageInfo() {
        try {
            this.mPackageInfo = PackageManagerWrapper.getPackageInfo(this.mContext.getPackageManager(), this.mPackageInfo.packageName, 4096);
        } catch (NameNotFoundException e) {
            if (this.mOnErrorCallback != null) {
                this.mOnErrorCallback.run();
            }
        }
    }

    private void loadPermissionGroups() {
        this.mGroups.clear();
        if (this.mPackageInfo.requestedPermissions != null) {
            int i;
            AppPermissionGroup group;
            for (String requestedPerm : this.mPackageInfo.requestedPermissions) {
                if (!hasGroupForPermission(requestedPerm)) {
                    group = AppPermissionGroup.create(this.mContext, this.mPackageInfo, requestedPerm);
                    if (group != null) {
                        this.mGroups.add(group);
                    }
                }
            }
            if (!ArrayUtils.isEmpty(this.mFilterPermissions)) {
                for (i = this.mGroups.size() - 1; i >= 0; i--) {
                    group = (AppPermissionGroup) this.mGroups.get(i);
                    boolean groupHasPermission = false;
                    for (String filterPerm : this.mFilterPermissions) {
                        if (group.hasPermission(filterPerm)) {
                            groupHasPermission = true;
                            break;
                        }
                    }
                    if (!groupHasPermission) {
                        this.mGroups.remove(i);
                    }
                }
            }
            this.mNameToGroupMap.clear();
            for (AppPermissionGroup group2 : this.mGroups) {
                this.mNameToGroupMap.put(group2.getName(), group2);
            }
        }
    }

    private boolean hasGroupForPermission(String permission) {
        for (AppPermissionGroup group : this.mGroups) {
            if (group.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
