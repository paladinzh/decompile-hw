package com.huawei.permissionmanager.ui.permission;

import android.content.Context;
import com.huawei.permissionmanager.ui.CheckPackagePermissionInterface;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;

public class CameraPermission extends Permission {
    public CameraPermission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(context, permName, permDescriptions, permissionType, permissionPopupInfoCode, permissionCategoryId, permissionNoneAppTipsCode, interfaceCheckPackagePermission);
    }

    public int getHistoryStringId() {
        return R.string.permission_history_use_camera;
    }

    public int getPermissionCode() {
        return 1024;
    }

    public String getName(Context ctx) {
        return ctx.getString(R.string.permgrouplab_use_camera);
    }

    public String getmPermissionForbitTips(Context mContext, String pkgName) {
        super.getmPermissionForbitTips(mContext, pkgName);
        if (DualAppUtil.isPackageCloned(mContext, pkgName)) {
            return mContext.getString(R.string.super_permission_camera_text_for_dual_app);
        }
        return mContext.getString(R.string.super_app_permission_camera_text);
    }
}
