package com.huawei.notificationmanager.cloud;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.notificationmanager.db.DBAdapter;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.notificationmanager.util.NmCenterDefValueXmlHelper;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.CloudControlChange;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.List;

public class CloudNotificationControlChange extends CloudControlChange {
    private Context mContext = null;

    public CloudNotificationControlChange(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    protected void processBlackToWhiteList(List<String> processList) {
        if (processList != null && !processList.isEmpty()) {
            DBAdapter dbAdapter = new DBAdapter(this.mContext);
            for (String pkgName : processList) {
                int uid = HsmPkgUtils.getPackageUid(pkgName);
                if (-1 != uid) {
                    Helper.setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(true));
                }
                dbAdapter.deleteCfg(pkgName);
                dbAdapter.deleteLog(pkgName);
            }
            Helper.setCfgChangeFlag(this.mContext, true);
            Helper.setLogChangeFlag(this.mContext, true);
        }
    }

    protected void processWhiteToBlackList(List<String> processList) {
        if (processList != null && !processList.isEmpty()) {
            NmCenterDefValueXmlHelper parser = new NmCenterDefValueXmlHelper();
            for (String pkgName : processList) {
                new DBAdapter(this.mContext).initNewApp(pkgName, Helper.getDefaultValue(parser, this.mContext, pkgName, false));
            }
            Helper.setCfgChangeFlag(this.mContext, true);
        }
    }

    protected String getCurrentScenario() {
        return "notification";
    }

    protected boolean isCurrentPkgNotInited(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean initedStatus = false;
        if (CursorHelper.checkCursorValidAndClose(this.mContext.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, "packageName = ?", new String[]{pkgName}, null))) {
            initedStatus = true;
        }
        return !initedStatus;
    }
}
