package com.huawei.systemmanager.securitythreats.ui;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.huawei.optimizer.utils.PackageUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.HashSet;

public class VirusNotifyService extends Service {
    private static final String ALLOW_NOT_SHOW_DIALOG_PACKAGE = "android.security.cts";
    private static final String TAG = "VirusNotifyService";
    private int mClientCount = 0;
    private HashSet<String> mPkgInfoSet = new HashSet();
    public VirusNotifyCallback mVirusNotifyCallback = new VirusNotifyCallback() {
        public void onResult(String pkg, boolean needCallback, boolean stat, boolean uninstall) {
            VirusNotifyService.this.mPkgInfoSet.remove(pkg);
            if (needCallback) {
                SecurityThreatsUtil.notifyFinishToService(VirusNotifyService.this.getApplicationContext(), pkg);
            }
            VirusNotifyService.this.stat(pkg, uninstall);
            if (uninstall) {
                VirusNotifyService.this.uninstallApk(pkg);
            }
            VirusNotifyService.this.stopSelfIfNeeded();
        }
    };

    public interface VirusNotifyCallback {
        void onResult(String str, boolean z, boolean z2, boolean z3);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this) {
            this.mClientCount++;
            HwLog.i(TAG, "Dialog service start, client count:" + this.mClientCount);
        }
        if (intent == null) {
            stopSelfIfNeeded();
            return 2;
        }
        if (SecurityThreatsConst.ACTION_VIRUS_NOTIFY.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return 2;
            }
            String pkg = bundle.getString("package_name", "");
            int level = bundle.getInt(SecurityThreatsConst.BUNDLE_KEY_VIRUS_LEVEL, 2);
            boolean needStat = bundle.getBoolean(SecurityThreatsConst.BUNDLE_KEY_NEED_STAT, false);
            boolean needCallback = bundle.getBoolean(SecurityThreatsConst.BUNDLE_KEY_NEED_CALLBACK, false);
            HwLog.i(TAG, "onStartCommand ACTION_VIRUS_NOTIFY pkg=" + pkg);
            if (this.mPkgInfoSet.contains(pkg)) {
                stopSelfIfNeeded();
            } else {
                HwLog.i(TAG, "onStartCommand pkg is not contained");
                showHoldDialog(pkg, needStat, needCallback, level);
                this.mPkgInfoSet.add(pkg);
            }
        } else {
            stopSelfIfNeeded();
        }
        return 2;
    }

    private void stopSelfIfNeeded() {
        synchronized (this) {
            this.mClientCount--;
            HwLog.i(TAG, "dialog service stop, client count:" + this.mClientCount);
            if (this.mClientCount <= 0) {
                stopSelf();
            }
        }
    }

    private void showHoldDialog(String pkg, boolean needStat, boolean needCallback, int level) {
        int themeResId = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkg);
        if (info == null) {
            HwLog.i(TAG, "showHoldDialog info is null");
            stopSelfIfNeeded();
        } else if (!ALLOW_NOT_SHOW_DIALOG_PACKAGE.equalsIgnoreCase(pkg)) {
            VirusNotifyDialog dialog = new VirusNotifyDialog(getApplicationContext(), themeResId, info, needStat, needCallback, level, this.mVirusNotifyCallback);
            dialog.getWindow().setType(2003);
            dialog.show();
        }
    }

    private void stat(String pkg, boolean uninstall) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkg);
        if (info == null) {
            HwLog.i(TAG, "stat info is null");
            return;
        }
        String label = info.label();
        String version = String.valueOf(info.getVersionCode());
        String op = uninstall ? "2" : "1";
        HwLog.i(TAG, "stat HsmStat:" + HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, pkg, HsmStatConst.PARAM_LABEL, label, HsmStatConst.PARAM_VERSION, version, HsmStatConst.PARAM_OP, op));
        HsmStat.statE((int) Events.E_VIRUS_USER_UNINSTALL, statParam);
    }

    private boolean uninstallApk(String pkg) {
        HwLog.i(TAG, "uninstallApk: try to uninstall, packageName = " + pkg);
        return PackageUtils.uninstallApp(getApplication(), pkg, true);
    }
}
