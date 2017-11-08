package com.huawei.permissionmanager.model;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.ArraySet;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class PermissionGroups implements LoaderCallbacks<List<PermissionGroup>> {
    private final PermissionsGroupsChangeCallback mCallback;
    private final Context mContext;
    private final ArrayList<PermissionGroup> mGroups = new ArrayList();
    private final LoaderManager mLoaderManager;

    public interface PermissionsGroupsChangeCallback {
        void onPermissionGroupsChanged();
    }

    private static final class PermissionsLoader extends AsyncTaskLoader<List<PermissionGroup>> {
        public PermissionsLoader(Context context) {
            super(context);
        }

        public List<PermissionGroup> loadInBackground() {
            List<PermissionGroup> groups = new ArrayList();
            Set<String> seenPermissions = new ArraySet();
            PackageManager packageManager = getContext().getPackageManager();
            for (PermissionGroupInfo groupInfo : packageManager.getAllPermissionGroups(0)) {
                if (isLoadInBackgroundCanceled()) {
                    return Collections.emptyList();
                }
                try {
                    boolean hasRuntimePermissions = false;
                    for (PermissionInfo groupPermission : packageManager.queryPermissionsByGroup(groupInfo.name, 0)) {
                        seenPermissions.add(groupPermission.name);
                        if (groupPermission.protectionLevel == 1 && (groupPermission.flags & 1073741824) != 0) {
                            hasRuntimePermissions = true;
                        }
                    }
                    if (hasRuntimePermissions) {
                        groups.add(new PermissionGroup(groupInfo.name, groupInfo.packageName, loadItemInfoLabel(groupInfo), null));
                    }
                } catch (NameNotFoundException e) {
                }
            }
            List<PackageInfo> installedPackages = PackageManagerWrapper.getInstalledPackages(getContext().getPackageManager(), 4096);
            Set<String> requestedPermissions = new ArraySet();
            for (PackageInfo installedPackage : installedPackages) {
                if (installedPackage.requestedPermissions != null) {
                    for (String requestedPermission : installedPackage.requestedPermissions) {
                        requestedPermissions.add(requestedPermission);
                    }
                }
            }
            for (PackageInfo installedPackage2 : installedPackages) {
                if (installedPackage2.permissions != null) {
                    for (PackageItemInfo permissionInfo : installedPackage2.permissions) {
                        if (seenPermissions.add(permissionInfo.name) && permissionInfo.protectionLevel == 1 && (permissionInfo.flags & 1073741824) != 0) {
                            if (requestedPermissions.contains(permissionInfo.name)) {
                                groups.add(new PermissionGroup(permissionInfo.name, permissionInfo.packageName, loadItemInfoLabel(permissionInfo), null));
                            }
                        }
                    }
                }
            }
            Collections.sort(groups);
            return groups;
        }

        private CharSequence loadItemInfoLabel(PackageItemInfo itemInfo) {
            CharSequence label = itemInfo.loadLabel(getContext().getPackageManager());
            if (label == null) {
                return itemInfo.name;
            }
            return label;
        }
    }

    public PermissionGroups(Context context, LoaderManager loaderManager, PermissionsGroupsChangeCallback callback) {
        this.mContext = context;
        this.mLoaderManager = loaderManager;
        this.mCallback = callback;
    }

    public Loader<List<PermissionGroup>> onCreateLoader(int id, Bundle args) {
        return new PermissionsLoader(this.mContext);
    }

    public void onLoadFinished(Loader<List<PermissionGroup>> loader, List<PermissionGroup> groups) {
        if (!this.mGroups.equals(groups)) {
            this.mGroups.clear();
            this.mGroups.addAll(groups);
            this.mCallback.onPermissionGroupsChanged();
        }
    }

    public void onLoaderReset(Loader<List<PermissionGroup>> loader) {
        this.mGroups.clear();
        this.mCallback.onPermissionGroupsChanged();
    }

    public void refresh() {
        this.mLoaderManager.restartLoader(0, null, this);
        Loader<List<PermissionGroup>> loader = this.mLoaderManager.getLoader(0);
        if (loader != null) {
            loader.forceLoad();
        }
    }

    public List<PermissionGroup> getGroups() {
        return this.mGroups;
    }

    public PermissionGroup getGroup(String name) {
        for (PermissionGroup group : this.mGroups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
}
