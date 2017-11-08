package com.android.settings.applications;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AppStateAppOpsBridge extends AppStateBaseBridge {
    private final AppOpsManager mAppOpsManager;
    private final int[] mAppOpsOpCodes;
    private final Context mContext;
    private final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    private final String[] mPermissions;
    private final List<UserHandle> mProfiles;
    private final UserManager mUserManager;

    public static class PermissionState {
        public int appOpMode = 3;
        public PackageInfo packageInfo;
        public final String packageName;
        public boolean permissionDeclared;
        public boolean staticPermissionGranted;
        public final UserHandle userHandle;

        public PermissionState(String packageName, UserHandle userHandle) {
            this.packageName = packageName;
            this.userHandle = userHandle;
        }

        public boolean isPermissible() {
            boolean z = false;
            if (this.appOpMode == 3) {
                return this.staticPermissionGranted;
            }
            if (this.appOpMode == 0) {
                z = true;
            }
            return z;
        }
    }

    protected abstract void updateExtraInfo(AppEntry appEntry, String str, int i);

    public AppStateAppOpsBridge(Context context, ApplicationsState appState, Callback callback, int appOpsOpCode, String[] permissions) {
        super(appState, callback);
        this.mContext = context;
        this.mUserManager = UserManager.get(context);
        this.mProfiles = this.mUserManager.getUserProfiles();
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mAppOpsOpCodes = new int[]{appOpsOpCode};
        this.mPermissions = permissions;
    }

    private boolean isThisUserAProfileOfCurrentUser(int userId) {
        int profilesMax = this.mProfiles.size();
        for (int i = 0; i < profilesMax; i++) {
            if (((UserHandle) this.mProfiles.get(i)).getIdentifier() == userId) {
                return true;
            }
        }
        return false;
    }

    private boolean doesAnyPermissionMatch(String permissionToMatch, String[] permissions) {
        for (String permission : permissions) {
            if (permissionToMatch.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    public PermissionState getPermissionInfo(String pkg, int uid) {
        PermissionState permissionState = new PermissionState(pkg, new UserHandle(UserHandle.getUserId(uid)));
        try {
            permissionState.packageInfo = this.mIPackageManager.getPackageInfo(pkg, 12288, permissionState.userHandle.getIdentifier());
            if (permissionState.packageInfo == null) {
                Log.e("AppStateAppOpsBridge", "permissionState.packageInfo == null");
                return permissionState;
            }
            String[] requestedPermissions = permissionState.packageInfo.requestedPermissions;
            int[] permissionFlags = permissionState.packageInfo.requestedPermissionsFlags;
            if (requestedPermissions != null) {
                for (int i = 0; i < requestedPermissions.length; i++) {
                    if (doesAnyPermissionMatch(requestedPermissions[i], this.mPermissions)) {
                        permissionState.permissionDeclared = true;
                        if ((permissionFlags[i] & 2) != 0) {
                            permissionState.staticPermissionGranted = true;
                            break;
                        }
                    }
                }
            }
            List<PackageOps> ops = this.mAppOpsManager.getOpsForPackage(uid, pkg, this.mAppOpsOpCodes);
            if (ops != null && ops.size() > 0 && ((PackageOps) ops.get(0)).getOps().size() > 0) {
                permissionState.appOpMode = ((OpEntry) ((PackageOps) ops.get(0)).getOps().get(0)).getMode();
            }
            return permissionState;
        } catch (RemoteException e) {
            Log.w("AppStateAppOpsBridge", "PackageManager is dead. Can't get package info " + pkg, e);
        }
    }

    protected void loadAllExtraInfo() {
        SparseArray<ArrayMap<String, PermissionState>> entries = getEntries();
        loadPermissionsStates(entries);
        loadAppOpsStates(entries);
        List<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            Object obj;
            AppEntry app = (AppEntry) apps.get(i);
            ArrayMap<String, PermissionState> userMap = (ArrayMap) entries.get(UserHandle.getUserId(app.info.uid));
            if (userMap != null) {
                obj = userMap.get(app.info.packageName);
            } else {
                obj = null;
            }
            app.extraInfo = obj;
        }
    }

    private SparseArray<ArrayMap<String, PermissionState>> getEntries() {
        try {
            Set<String> packagesSet = new HashSet();
            for (String permission : this.mPermissions) {
                String[] pkgs = this.mIPackageManager.getAppOpPermissionPackages(permission);
                if (pkgs != null) {
                    packagesSet.addAll(Arrays.asList(pkgs));
                }
            }
            if (packagesSet.isEmpty()) {
                return null;
            }
            SparseArray<ArrayMap<String, PermissionState>> entries = new SparseArray();
            for (UserHandle profile : this.mProfiles) {
                ArrayMap<String, PermissionState> entriesForProfile = new ArrayMap();
                int profileId = profile.getIdentifier();
                entries.put(profileId, entriesForProfile);
                for (String packageName : packagesSet) {
                    boolean isAvailable = this.mIPackageManager.isPackageAvailable(packageName, profileId);
                    if (!shouldIgnorePackage(packageName) && isAvailable) {
                        entriesForProfile.put(packageName, new PermissionState(packageName, profile));
                    }
                }
            }
            return entries;
        } catch (RemoteException e) {
            Log.w("AppStateAppOpsBridge", "PackageManager is dead. Can't get list of packages requesting " + this.mPermissions[0], e);
            return null;
        }
    }

    private void loadPermissionsStates(SparseArray<ArrayMap<String, PermissionState>> entries) {
        try {
            for (UserHandle profile : this.mProfiles) {
                int profileId = profile.getIdentifier();
                ArrayMap<String, PermissionState> entriesForProfile = (ArrayMap) entries.get(profileId);
                if (entriesForProfile != null) {
                    List<PackageInfo> packageInfos = this.mIPackageManager.getPackagesHoldingPermissions(this.mPermissions, 0, profileId).getList();
                    int packageInfoCount = packageInfos != null ? packageInfos.size() : 0;
                    for (int i = 0; i < packageInfoCount; i++) {
                        PackageInfo packageInfo = (PackageInfo) packageInfos.get(i);
                        PermissionState pe = (PermissionState) entriesForProfile.get(packageInfo.packageName);
                        if (pe != null) {
                            pe.packageInfo = packageInfo;
                            pe.staticPermissionGranted = true;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            Log.w("AppStateAppOpsBridge", "PackageManager is dead. Can't get list of packages granted " + this.mPermissions, e);
        }
    }

    private void loadAppOpsStates(SparseArray<ArrayMap<String, PermissionState>> entries) {
        List<PackageOps> packageOps = this.mAppOpsManager.getPackagesForOps(this.mAppOpsOpCodes);
        int packageOpsCount = packageOps != null ? packageOps.size() : 0;
        for (int i = 0; i < packageOpsCount; i++) {
            PackageOps packageOp = (PackageOps) packageOps.get(i);
            int userId = UserHandle.getUserId(packageOp.getUid());
            if (isThisUserAProfileOfCurrentUser(userId)) {
                ArrayMap<String, PermissionState> entriesForProfile = (ArrayMap) entries.get(userId);
                if (entriesForProfile != null) {
                    PermissionState pe = (PermissionState) entriesForProfile.get(packageOp.getPackageName());
                    if (pe == null) {
                        Log.w("AppStateAppOpsBridge", "AppOp permission exists for package " + packageOp.getPackageName() + " of user " + userId + " but package doesn't exist or did not request " + this.mPermissions + " access");
                    } else if (packageOp.getOps().size() < 1) {
                        Log.w("AppStateAppOpsBridge", "No AppOps permission exists for package " + packageOp.getPackageName());
                    } else {
                        pe.appOpMode = ((OpEntry) packageOp.getOps().get(0)).getMode();
                    }
                }
            }
        }
    }

    private boolean shouldIgnorePackage(String packageName) {
        return !packageName.equals("android") ? packageName.equals(this.mContext.getPackageName()) : true;
    }
}
