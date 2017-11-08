package com.huawei.permissionmanager.ui.permission;

import android.content.Context;
import com.huawei.permissionmanager.ui.CheckPackagePermissionInterface;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;

public class WriteCalendarPermission extends Permission {
    public WriteCalendarPermission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(context, permName, permDescriptions, permissionType, permissionPopupInfoCode, permissionCategoryId, permissionNoneAppTipsCode, interfaceCheckPackagePermission);
    }

    public int getHistoryStringId() {
        return R.string.permission_modify_calendar_history;
    }

    public int getPermissionCode() {
        return ShareCfg.PERMISSION_MODIFY_CALENDAR;
    }

    public String getName(Context ctx) {
        return ctx.getString(R.string.write_calendar_permission_name);
    }
}
