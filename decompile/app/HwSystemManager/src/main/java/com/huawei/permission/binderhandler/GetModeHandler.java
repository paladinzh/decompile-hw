package com.huawei.permission.binderhandler;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DBPermissionItem;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager.UidDetail;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class GetModeHandler extends HoldServiceBinderHandler {
    private static final String LOG_TAG = "GetModeHandler";
    public static final int TYPE_NET = Integer.MIN_VALUE;
    private Context mContext;

    public GetModeHandler(Context cxt) {
        this.mContext = cxt;
    }

    public Bundle handleTransact(Bundle params) {
        return handleGetMode(params);
    }

    protected boolean ignorePermissionCheck() {
        return true;
    }

    private Bundle handleGetMode(Bundle params) {
        this.mContext.enforceCallingOrSelfPermission(Utility.SDK_API_PERMISSION, null);
        if (!CustomizeWrapper.isPermissionEnabled()) {
            return getResult(3);
        }
        if (params == null) {
            HwLog.w(LOG_TAG, "handleGetMode, params is null.");
            return getResult(3);
        }
        int typeCode = params.getInt(HoldServiceConst.EXTRA_CODE);
        String pkg = params.getString("packageName");
        if (TextUtils.isEmpty(pkg)) {
            return getResult(2);
        }
        if (Integer.MIN_VALUE == typeCode) {
            long identity = Binder.clearCallingIdentity();
            try {
                Bundle netStatus = getNetStatus(pkg);
                return netStatus;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkg)) {
            DBPermissionItem item = DBAdapter.getInstance(this.mContext).getDBItemByPackage(pkg);
            if (item == null) {
                HwLog.w(LOG_TAG, "zzz handleGetMode, no data info for " + pkg);
                return getResult(1);
            }
            int operation = ((Integer) ShareCfg.value2UserOperation.get(item.getValueByType(typeCode))).intValue();
            if (Log.HWINFO) {
                HwLog.i(LOG_TAG, "zzz handleGetMode pkg:" + pkg + ", type:" + typeCode + ", caller:" + Binder.getCallingUid() + ",pid:" + Binder.getCallingPid() + ", result:" + operation);
            }
            return getResult(operation);
        } else {
            HwLog.w(LOG_TAG, "zzz handleGetMode, system fixed app " + pkg);
            return getResult(1);
        }
    }

    private Bundle getNetStatus(String pkg) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkg);
        if (info == null) {
            HwLog.w(LOG_TAG, "zzz null info for " + pkg);
            return getResult(2);
        }
        int mode;
        int uid = info.mUid;
        if (UidDetail.create(uid).isMobileAccess()) {
            mode = 1;
        } else {
            mode = 2;
        }
        HwLog.i(LOG_TAG, "zzz getNetStatus, pkg:" + pkg + ", uid:" + uid + ", mode:" + mode);
        return getResult(mode);
    }

    private Bundle getResult(int resultCode) {
        Bundle res = new Bundle();
        res.putInt("result", resultCode);
        return res;
    }
}
