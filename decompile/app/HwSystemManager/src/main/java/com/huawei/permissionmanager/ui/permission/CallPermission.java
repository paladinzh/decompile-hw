package com.huawei.permissionmanager.ui.permission;

import android.content.Context;
import com.huawei.permissionmanager.ui.CheckPackagePermissionInterface;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.systemmanager.R;

public class CallPermission extends Permission {
    public CallPermission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(context, permName, permDescriptions, permissionType, permissionPopupInfoCode, permissionCategoryId, permissionNoneAppTipsCode, interfaceCheckPackagePermission);
    }

    public int getHistoryStringId() {
        return R.string.permission_history_make_call;
    }

    public int getPermissionCode() {
        return 64;
    }

    public String getName(Context ctx) {
        return ctx.getString(R.string.CallPhonePermission);
    }

    public boolean donotAskAgain() {
        return false;
    }

    public boolean showBillingWarning() {
        return true;
    }
}
