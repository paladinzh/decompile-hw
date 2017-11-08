package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import com.huawei.systemmanager.antimal.AntiMalUtils;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.power.notification.PowerNotificationMgr;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class HandlePGWastePower implements IBroadcastHandler {
    private static final String TAG = HandlePGWastePower.class.getSimpleName();
    ArrayList<String> recpkgList;
    ArrayList<Integer> rectypeList;

    public void handleBroadcast(Context ctx, Intent intent) {
        handleTakingWakeLockAPPs(ctx, intent.getExtras());
    }

    private void handleTakingWakeLockAPPs(Context ctx, Bundle bundle) {
        if (bundle == null) {
            HwLog.w(TAG, "handleTakingWakeLockAPPs bundle is null");
            return;
        }
        this.recpkgList = bundle.getStringArrayList("pkgList");
        this.rectypeList = bundle.getIntegerArrayList("reasonList");
        HwLog.i(TAG, "PG_FOUND_WASTE_POWER_APP received,pkgList= " + this.recpkgList + " tepeList= " + this.rectypeList);
        if (this.recpkgList == null || this.rectypeList == null) {
            HwLog.i(TAG, "Invalid Intent,recpkgList or rectypeList is null");
        } else if (this.recpkgList.size() != this.rectypeList.size()) {
            HwLog.i(TAG, "Invalid Intent,pkgList size= " + this.recpkgList.size() + " typeList size= " + this.rectypeList.size());
        } else {
            ArrayList<Integer> uidList = new ArrayList();
            ArrayList<String> pkgNotifList = new ArrayList();
            int i = 0;
            while (i < this.recpkgList.size()) {
                String pkgName = (String) this.recpkgList.get(i);
                int reasonType = ((Integer) this.rectypeList.get(i)).intValue();
                HwLog.i(TAG, "handle power high app, pkgName= " + pkgName + " resonType= " + reasonType);
                ApplicationInfo app = null;
                int i2 = -1;
                try {
                    i2 = SysCoreUtils.getGoogleSharedUid(ctx);
                    app = ctx.getPackageManager().getApplicationInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    HwLog.e(TAG, "NameNotFoundException!");
                }
                if (app != null) {
                    if (-1 == i2 || app.uid != i2) {
                        String[] packageName = ctx.getPackageManager().getPackagesForUid(app.uid);
                        if (packageName != null && packageName.length > 1) {
                            pkgName = packageName[0];
                        }
                        if (doValidPGNotificationJudge(ctx, pkgName, app.uid, reasonType)) {
                            uidList.add(Integer.valueOf(app.uid));
                            pkgNotifList.add(pkgName);
                        }
                        i++;
                    } else {
                        HwLog.i(TAG, "it is googleUid apk, not notify!");
                        return;
                    }
                }
                return;
            }
            if (pkgNotifList.size() > 0 && uidList.size() > 0) {
                HandleComm.notifyConsume(ctx, pkgNotifList, uidList);
                HwLog.i(TAG, "HSM send high power consume notification, pkgList= " + pkgNotifList);
                AntiMalUtils.sendNotification(ctx);
            }
        }
    }

    public boolean doValidPGNotificationJudge(Context ctx, String pkgName, int uid, int reasonType) {
        if (!SysCoreUtils.checkIsBackground(ctx, pkgName)) {
            HwLog.w(TAG, "handleTakingWakeLockAPPs not running in background: " + pkgName);
            return false;
        } else if (!ProtectAppControl.getInstance(ctx).getAllControlledAppFromDb().contains(pkgName)) {
            HwLog.w(TAG, "package " + pkgName + " not in protect list.");
            return false;
        } else if (SysCoreUtils.getRunningUids(ctx).contains(Integer.valueOf(uid))) {
            boolean isValidReason = true;
            ArrayList<String> rogueApp = SavingSettingUtil.rogueDBQuery(ctx);
            switch (reasonType) {
                case 0:
                    SavingSettingUtil.recordRogue(ctx, pkgName, rogueApp, 1);
                    break;
                case 1:
                    SavingSettingUtil.recordRogue(ctx, pkgName, rogueApp, 2);
                    break;
                case 2:
                    SavingSettingUtil.recordRogue(ctx, pkgName, rogueApp, 3);
                    break;
                default:
                    isValidReason = false;
                    break;
            }
            if (!isValidReason) {
                HwLog.i(TAG, "Invalid reasonType,  pkgName= " + pkgName + " type= " + reasonType);
                return false;
            } else if (PowerNotificationMgr.isAppNotified(ctx, pkgName)) {
                HwLog.i(TAG, "handleTakingWakeLockAPPs pkg has notified: " + pkgName);
                return false;
            } else if (!PowerNotificationMgr.isAppIgnore(ctx, pkgName)) {
                return true;
            } else {
                HwLog.i(TAG, "handleTakingWakeLockAPPs pkg  is ignored: " + pkgName);
                return false;
            }
        } else {
            HwLog.e(TAG, "the app is not running!");
            return false;
        }
    }
}
