package com.huawei.systemmanager.optimize.cloud;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.bootstart.BootStartManager;
import com.huawei.systemmanager.rainbow.CloudControlChange;
import java.util.List;

public class CloudBootstartupControlChange extends CloudControlChange {
    private Context mContext = null;

    public CloudBootstartupControlChange(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void processBlackToWhiteList(List<String> processList) {
        if (processList != null && !processList.isEmpty() && CustomizeWrapper.isBootstartupEnabled()) {
            BootStartManager startupManager = BootStartManager.getInstance(this.mContext);
            for (String pkgName : processList) {
                startupManager.updateStartupDBWhenRemove(pkgName);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void processWhiteToBlackList(List<String> processList) {
        if (processList != null && !processList.isEmpty() && CustomizeWrapper.isBootstartupEnabled()) {
            BootStartManager startupManager = BootStartManager.getInstance(this.mContext);
            for (String pkgName : processList) {
                startupManager.installApp(pkgName);
            }
        }
    }

    protected String getCurrentScenario() {
        return MonitorScenario.SCENARIO_BOOTSTART;
    }

    protected boolean isCurrentPkgNotInited(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean initedStatus = false;
        Cursor allowCursor = this.mContext.getContentResolver().query(Const.STARTUP_ALLOW_APPS_URI, null, Const.START_UP_SELECTION_PACKAGE, new String[]{pkgName}, null);
        Cursor forbidCursor = this.mContext.getContentResolver().query(Const.START_UP_FORBIDDEN_APPS_URI, null, Const.START_UP_SELECTION_PACKAGE, new String[]{pkgName}, null);
        if (CursorHelper.checkCursorValid(allowCursor) || CursorHelper.checkCursorValid(forbidCursor)) {
            initedStatus = true;
        }
        CursorHelper.closeCursor(allowCursor);
        CursorHelper.closeCursor(forbidCursor);
        return !initedStatus;
    }
}
