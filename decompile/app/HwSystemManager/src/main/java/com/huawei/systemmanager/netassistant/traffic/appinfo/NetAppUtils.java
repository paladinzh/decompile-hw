package com.huawei.systemmanager.netassistant.traffic.appinfo;

import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NetAppUtils {
    public static final String NET_PERMISSION = "android.permission.INTERNET";

    public static boolean isNetApp(String pkgName) {
        if (!TextUtils.isEmpty(pkgName) && GlobalContext.getContext().getPackageManager().checkPermission(NET_PERMISSION, pkgName) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isRemovableApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        return HsmPackageManager.getInstance().isRemovable(pkgName);
    }

    public static boolean isRemovableUid(int uid) {
        String[] apps = GlobalContext.getContext().getPackageManager().getPackagesForUid(uid);
        if (apps == null || apps.length <= 0) {
            return false;
        }
        for (String isRemovableApp : apps) {
            if (!isRemovableApp(isRemovableApp)) {
                return false;
            }
        }
        return true;
    }

    public static SparseIntArray getAllNetRemovableUid() {
        SparseIntArray intArray = new SparseIntArray();
        for (HsmPkgInfo applicationInfo : HsmPackageManager.getInstance().getAllPackages()) {
            if (isNetApp(applicationInfo.mPkgName) && isRemovableUid(applicationInfo.mUid)) {
                intArray.put(applicationInfo.mUid, applicationInfo.mUid);
            }
        }
        return intArray;
    }

    public static SparseIntArray getAllNetUnRemovableUid() {
        SparseIntArray intArray = new SparseIntArray();
        for (HsmPkgInfo applicationInfo : HsmPackageManager.getInstance().getAllPackages()) {
            if (isNetApp(applicationInfo.mPkgName) && !isRemovableUid(applicationInfo.mUid)) {
                intArray.put(applicationInfo.mUid, applicationInfo.mUid);
            }
        }
        return intArray;
    }

    public static List<String> getAllNetRemovableUidPkg() {
        SparseIntArray sparseIntArray = getAllNetRemovableUid();
        List<String> list = new ArrayList();
        int size = sparseIntArray.size();
        for (int i = 0; i < size; i++) {
            String[] apps = GlobalContext.getContext().getPackageManager().getPackagesForUid(sparseIntArray.keyAt(i));
            if (apps != null && apps.length > 0) {
                list.addAll(Arrays.asList(apps));
            }
        }
        return list;
    }

    public static List<HsmPkgInfo> getAllNetRemovableUidHwPkgInfo() {
        List<String> list = getAllNetRemovableUidPkg();
        List<HsmPkgInfo> result = new ArrayList();
        PackageManager pm = GlobalContext.getContext().getPackageManager();
        for (String name : list) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(name);
            if (info != null && NetAppManager.packageCanAccessInternet(pm, info.getPackageName())) {
                if (UserHandle.isApp(info.getUid())) {
                    result.add(info);
                } else if (info.getUid() == 1000) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    private static List<String> getAllNetUnRemovableUidPkg() {
        SparseIntArray sparseIntArray = getAllNetUnRemovableUid();
        List<String> list = new ArrayList();
        int size = sparseIntArray.size();
        for (int i = 0; i < size; i++) {
            String[] apps = GlobalContext.getContext().getPackageManager().getPackagesForUid(sparseIntArray.keyAt(i));
            if (apps != null && apps.length > 0) {
                list.addAll(Arrays.asList(apps));
            }
        }
        return list;
    }

    public static List<HsmPkgInfo> getAllNetUnRemovableUidHwPkgInfo() {
        List<String> list = getAllNetUnRemovableUidPkg();
        List<HsmPkgInfo> result = new ArrayList();
        PackageManager pm = GlobalContext.getContext().getPackageManager();
        for (String name : list) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(name);
            if (info != null && NetAppManager.packageCanAccessInternet(pm, info.getPackageName())) {
                if (UserHandle.isApp(info.getUid())) {
                    result.add(info);
                } else if (info.getUid() == 1000) {
                    result.add(info);
                }
            }
        }
        return result;
    }
}
