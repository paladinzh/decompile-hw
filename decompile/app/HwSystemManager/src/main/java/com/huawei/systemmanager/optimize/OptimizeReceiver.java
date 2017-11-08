package com.huawei.systemmanager.optimize;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.optimize.bootstart.BootStartManager;
import com.huawei.systemmanager.optimize.cloud.CloudBootstartupControlChange;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.rainbow.client.base.GetAppListBasic.CloudUpdateAction;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class OptimizeReceiver extends HsmBroadcastReceiver {
    private static final String TAG = OptimizeReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (Utility.checkBroadcast(context, intent)) {
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        String action = intent.getAction();
        HwLog.i("OptimizeReceiver", "OptimizeReceiver received action:" + action);
        if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
            handlePackageAdded(context, intent);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
            handlePackageRemoved(context, intent);
        } else if ("com.huawei.install.permission".equals(action)) {
            setProtect(context, intent);
        } else if (IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            handleExternalAppAvailable(context, intent);
        } else if (ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION.equals(intent.getAction())) {
            new CloudBootstartupControlChange(context).handleWhiteListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if ("com.rainbow.blacklist.change".equals(intent.getAction())) {
            new CloudBootstartupControlChange(context).handleBlackListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if (CloudUpdateAction.BACKGROUND_DATA_UPDATE_ACTION.equals(action)) {
            handleCloudBackgroundConfigChange(context);
        }
    }

    private void handlePackageAdded(Context context, Intent intent) {
        boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
        String packageName = intent.getData().getSchemeSpecificPart();
        if (CustomizeWrapper.isBootstartupEnabled()) {
            BootStartManager.getInstance(context).installApp(packageName);
        }
        if (TextUtils.isEmpty(packageName)) {
            HwLog.i(TAG, "package name is mepty");
            return;
        }
        ProtectAppControl.getInstance(context).installApp(packageName);
        if (replacing && CustomizeWrapper.isBootstartupEnabled()) {
            BootStartManager.getInstance(context).checkConsisteny();
        }
    }

    private void handlePackageRemoved(Context context, Intent intent) {
        if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            ProtectAppControl.getInstance(context).uninstallApp(pkgName);
            if (CustomizeWrapper.isBootstartupEnabled()) {
                BootStartManager.getInstance(context).updateStartupDBWhenRemove(pkgName);
            }
        }
    }

    private void handleExternalAppAvailable(Context context, Intent intent) {
        String[] pkgArray = intent.getStringArrayExtra(IntentCompat.EXTRA_CHANGED_PACKAGE_LIST);
        if (pkgArray != null) {
            ProtectAppControl protectControl = ProtectAppControl.getInstance(context);
            for (String pkgName : pkgArray) {
                if (!TextUtils.isEmpty(pkgName)) {
                    if (protectControl.checkExsist(pkgName)) {
                        HwLog.i("OptimizeReceiver", "sdcard packge:" + pkgName + "already exist");
                    } else {
                        HwLog.i("OptimizeReceiver", "add sdcard packge:" + pkgName);
                        protectControl.installApp(pkgName);
                    }
                }
            }
        }
    }

    private void setProtect(Context context, Intent intent) {
        ProtectAppControl protectControl = ProtectAppControl.getInstance(context);
        int powerState = intent.getIntExtra("POWER_SET", 1);
        String packageName1 = intent.getStringExtra("packageName");
        if (!protectControl.isFriendApp(packageName1)) {
            if (powerState == 1) {
                protectControl.setProtect(packageName1);
            } else if (powerState == 0) {
                protectControl.setNoProtect(packageName1);
            }
        }
    }

    private void handleCloudBackgroundConfigChange(Context context) {
        ProtectAppControl.getInstance(context).updateLocalBkgConfigFromCloud(context);
    }
}
