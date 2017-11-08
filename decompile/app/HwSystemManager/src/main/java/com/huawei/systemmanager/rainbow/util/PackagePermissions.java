package com.huawei.systemmanager.rainbow.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;

public class PackagePermissions {
    private static ArrayList<HwPermissionInfo> mPermissonList = ShareLib.getControlPermissions();

    public static int getComparePermissionCode(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        int compareCode = 0;
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        }
        try {
            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, packageName, 12288);
            if (!(packageInfo == null || packageInfo.requestedPermissions == null)) {
                String[] permissions = packageInfo.requestedPermissions;
                for (HwPermissionInfo info : mPermissonList) {
                    if (getPermission(info, permissions, info.misUnit)) {
                        compareCode += ((Integer) ShareLib.getPermissionTypeMaps().get(info)).intValue();
                    }
                }
            }
            compareCode = (compareCode | 67108864) | 33554432;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return compareCode;
    }

    private static boolean getPermission(HwPermissionInfo info, String[] permissions, boolean isUnit) {
        int length = permissions.length;
        for (String perStr : info.mPermissionStr) {
            int i = 0;
            while (i < length) {
                if (perStr.equals(permissions[i])) {
                    if (!isUnit) {
                        return true;
                    }
                } else if (isUnit && i == length - 1) {
                    return false;
                } else {
                    i++;
                }
            }
        }
        return isUnit;
    }
}
