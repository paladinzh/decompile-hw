package com.huawei.permission.cloud;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.CloudControlChange;
import java.util.List;

public class CloudPermissionControlChange extends CloudControlChange {
    private Context mContext = null;

    public CloudPermissionControlChange(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    protected void processBlackToWhiteList(List<String> pkgNameList) {
        DBAdapter.getInstance(this.mContext).deleteRecords(pkgNameList);
    }

    protected void processWhiteToBlackList(List<String> pkgNameList) {
        DBAdapter.getInstance(this.mContext).addRecords(pkgNameList);
    }

    protected String getCurrentScenario() {
        return MonitorScenario.SCENARIO_PERMISSION;
    }

    protected boolean isCurrentPkgNotInited(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean initedStatus = false;
        if (CursorHelper.checkCursorValidAndClose(this.mContext.getContentResolver().query(DBHelper.BLOCK_TABLE_NAME_URI, null, "packageName = ? ", new String[]{pkgName}, null))) {
            initedStatus = true;
        }
        return !initedStatus;
    }
}
