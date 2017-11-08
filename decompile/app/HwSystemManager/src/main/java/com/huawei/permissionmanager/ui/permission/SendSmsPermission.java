package com.huawei.permissionmanager.ui.permission;

import android.content.Context;
import com.huawei.permissionmanager.ui.CheckPackagePermissionInterface;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.systemmanager.R;

public class SendSmsPermission extends Permission {
    public SendSmsPermission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(context, permName, permDescriptions, permissionType, permissionPopupInfoCode, permissionCategoryId, permissionNoneAppTipsCode, interfaceCheckPackagePermission);
    }

    public int getHistoryStringId() {
        return R.string.permission_history_send_sms;
    }

    public int getPermissionCode() {
        return 32;
    }

    public String getName(Context ctx) {
        return ctx.getString(R.string.PayProtectPermission_gongxin);
    }

    public boolean donotAskAgain() {
        return false;
    }

    public boolean showBillingWarning() {
        return true;
    }
}
