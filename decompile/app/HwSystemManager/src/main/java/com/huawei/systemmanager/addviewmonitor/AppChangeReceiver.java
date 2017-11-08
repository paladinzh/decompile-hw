package com.huawei.systemmanager.addviewmonitor;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.addviewmonitor.cloud.CloudAddViewControlChange;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class AppChangeReceiver extends HsmBroadcastReceiver {
    private String LOG_TAG = "AppChangeReceiver";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            HwLog.d(this.LOG_TAG, "AppChangeReceiver:  onReceive(), action = " + intent.getAction());
            sendToBackground(context, intent);
        }
    }

    boolean shouldIgnoreIntent(Context context, Intent intent) {
        return !GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_DROPZONE, intent.getData().getSchemeSpecificPart());
    }

    boolean isReplaceOperation(Intent intent) {
        return intent.getBooleanExtra("android.intent.extra.REPLACING", false);
    }

    private void handlePackageAdd(Context context, Intent intent) {
        AddViewAppManager.getInstance(context).installApp(intent.getData().getSchemeSpecificPart());
    }

    private void handlePackageRemove(Context context, Intent intent) {
        AddViewAppManager.getInstance(context).uninstallApp(intent.getData().getSchemeSpecificPart());
    }

    public void doInBackground(Context context, Intent intent) {
        String action = intent.getAction();
        if (ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION.equals(action)) {
            new CloudAddViewControlChange(context).handleWhiteListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if ("com.rainbow.blacklist.change".equals(action)) {
            new CloudAddViewControlChange(context).handleBlackListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        } else if (intent.getData() == null) {
            HwLog.w(this.LOG_TAG, "AppChangeReceiver:  onReceive(), intent.getData() == null");
        } else if (isReplaceOperation(intent)) {
            HwLog.d(this.LOG_TAG, "AppChangeReceiver:  onReceive(), isReplaceOperation operation!");
        } else if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                handlePackageRemove(context, intent);
            }
        } else if (shouldIgnoreIntent(context, intent)) {
            HwLog.d(this.LOG_TAG, "AppChangeReceiver:  onReceive(), shouldIgnoreIntent operation!");
        } else {
            handlePackageAdd(context, intent);
        }
    }
}
