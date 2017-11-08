package com.huawei.permission.binderhandler;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.control.NetAppPermissionExcutor;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class SetModeHandler extends HoldServiceBinderHandler {
    private static final String LOG_TAG = "SetModeHandler";
    public static final int MODE_ALLOWED = 1;
    public static final int MODE_IGNORED = 2;
    public static final int TYPE_NET = Integer.MIN_VALUE;
    private Context mContext;

    public SetModeHandler(Context cxt) {
        this.mContext = cxt;
    }

    protected boolean ignorePermissionCheck() {
        return true;
    }

    public Bundle handleTransact(Bundle params) {
        return handleSetMode(params);
    }

    private Bundle handleSetMode(Bundle params) {
        this.mContext.enforceCallingOrSelfPermission(Utility.SDK_API_PERMISSION, null);
        long identity = Binder.clearCallingIdentity();
        try {
            Bundle result;
            if (!CustomizeWrapper.isPermissionEnabled()) {
                result = getResult(1);
                return result;
            } else if (params == null) {
                HwLog.w(LOG_TAG, "zzz handleSetMode, params is null.");
                result = getResult(2);
                Binder.restoreCallingIdentity(identity);
                return result;
            } else {
                int typeCode = params.getInt(HoldServiceConst.EXTRA_CODE);
                String pkg = params.getString("packageName");
                int mode = params.getInt(HoldServiceConst.EXTRA_MODE);
                if (TextUtils.isEmpty(pkg)) {
                    HwLog.w(LOG_TAG, "zzz handleSetMode, pkg is empty:" + pkg);
                    result = getResult(2);
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
                HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkg);
                if (info == null) {
                    result = getResult(2);
                    Binder.restoreCallingIdentity(identity);
                    return result;
                }
                int uid = info.mUid;
                if (typeCode == Integer.MIN_VALUE) {
                    result = handleNet(pkg, uid, mode);
                    Binder.restoreCallingIdentity(identity);
                    return result;
                } else if (!GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkg)) {
                    HwLog.w(LOG_TAG, "zzz handleSetMode, system fixed :" + pkg);
                    result = getResult(3);
                    Binder.restoreCallingIdentity(identity);
                    return result;
                } else if (mode < 0 || mode > 2) {
                    HwLog.w(LOG_TAG, "zzz handleSetMode, invalid mode:" + mode);
                    result = getResult(2);
                    Binder.restoreCallingIdentity(identity);
                    return result;
                } else {
                    HwAppPermissions aps = HwAppPermissions.create(this.mContext, pkg);
                    if (Log.HWINFO) {
                        HwLog.i(LOG_TAG, "zzz setMode pkg:" + pkg + ", type:" + typeCode + ", mode:" + mode + ", caller:" + Binder.getCallingUid() + ",pid:" + Binder.getCallingPid());
                    }
                    DBAdapter.setSinglePermissionAndSyncToSys(aps, this.mContext, uid, pkg, typeCode, mode, "api");
                    Binder.restoreCallingIdentity(identity);
                    return getResult(0);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private Bundle handleNet(String pkg, int uid, int mode) {
        int type = HsmPackageManager.getInstance().isSystem(pkg) ? 1 : 0;
        HwLog.i(LOG_TAG, "zzz handleNet pkg:" + pkg + ", uid:" + uid + ", mode:" + mode);
        if (1 == mode) {
            accessMobile(pkg, uid, type);
            return getResult(0);
        } else if (2 != mode) {
            return getResult(2);
        } else {
            denyMobile(pkg, uid, type);
            return getResult(0);
        }
    }

    public void accessMobile(String pkg, int uid, int type) {
        NetAppPermissionExcutor.execute(new AppPermissionController(0, 0, uid, type));
    }

    public void denyMobile(String pkg, int uid, int type) {
        NetAppPermissionExcutor.execute(new AppPermissionController(1, 0, uid, type));
    }

    private Bundle getResult(int resultCode) {
        Bundle res = new Bundle();
        res.putInt("result", resultCode);
        return res;
    }
}
