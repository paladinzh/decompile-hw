package com.huawei.systemmanager.addviewmonitor.cloud;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.rainbow.CloudControlChange;
import java.util.ArrayList;
import java.util.List;

public class CloudAddViewControlChange extends CloudControlChange {
    private Context mContext = null;

    public CloudAddViewControlChange(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    protected void processBlackToWhiteList(List<String> processList) {
        AddViewAppManager.getInstance(this.mContext).deleteRecords(processList);
    }

    protected void processWhiteToBlackList(List<String> processList) {
        AddViewAppManager.getInstance(this.mContext).addRecords(processList);
    }

    protected String getCurrentScenario() {
        return MonitorScenario.SCENARIO_DROPZONE;
    }

    protected boolean isCurrentPkgNotInited(String pkgName) {
        boolean z = false;
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        ArrayList<String> pkgList = AddViewAppManager.getInstance(this.mContext).getInitedPkgInFile();
        if (pkgList.isEmpty()) {
            return true;
        }
        boolean initedStatus = false;
        if (pkgList.contains(pkgName)) {
            initedStatus = true;
        }
        if (!initedStatus) {
            z = true;
        }
        return z;
    }
}
