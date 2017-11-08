package com.huawei.permissionmanager.ui.permission;

import android.content.Context;
import com.huawei.permissionmanager.ui.CheckPackagePermissionInterface;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.systemmanager.R;

public class WriteContactPermission extends Permission {
    public WriteContactPermission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(context, permName, permDescriptions, permissionType, permissionPopupInfoCode, permissionCategoryId, permissionNoneAppTipsCode, interfaceCheckPackagePermission);
    }

    public int getHistoryStringId() {
        return R.string.permission_history_write_contacts;
    }

    public int getPermissionCode() {
        return 16384;
    }

    public String getName(Context ctx) {
        return ctx.getString(R.string.WriteContactsPermissionName);
    }
}
