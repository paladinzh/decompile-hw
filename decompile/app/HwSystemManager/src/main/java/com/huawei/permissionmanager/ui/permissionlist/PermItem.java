package com.huawei.permissionmanager.ui.permissionlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.ui.PermissionSettingActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.util.HwLog;

public class PermItem implements ListItem, ISearchKey {
    private static final String TAG = "PermItem";
    private final Permission mPermission;

    public PermItem(Permission p) {
        this.mPermission = p;
    }

    public String getTitle(Context ctx) {
        if (this.mPermission == null) {
            return "";
        }
        return this.mPermission.getName(ctx);
    }

    public String getDescription(Context ctx) {
        if (this.mPermission == null) {
            return "";
        }
        int applicationCount = this.mPermission.getPermissionCount();
        return ctx.getResources().getQuantityString(R.plurals.app_cnt_suffix, applicationCount, new Object[]{Integer.valueOf(applicationCount)});
    }

    public Intent getIntent(Context ctx) {
        if (this.mPermission == null) {
            return null;
        }
        HwLog.d(TAG, "getIntent, permission name:" + this.mPermission.getmPermissionNames());
        Bundle bundle = new Bundle();
        bundle.putInt("permissionType", this.mPermission.getPermissionCode());
        Intent intent = new Intent();
        intent.setPackage(ctx.getPackageName());
        intent.putExtras(bundle);
        intent.setClass(ctx, PermissionSettingActivity.class);
        return intent;
    }

    public String getKey() {
        if (this.mPermission == null) {
            return "";
        }
        return String.valueOf(this.mPermission.getPermissionCode());
    }
}
