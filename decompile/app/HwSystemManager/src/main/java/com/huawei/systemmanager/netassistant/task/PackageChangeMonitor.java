package com.huawei.systemmanager.netassistant.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.HwNetworkPolicyManager;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.systemmanager.Task;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class PackageChangeMonitor extends Task {
    private static final String TAG = "PackageChangeMonitor";
    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private static BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && TextUtils.equals(intent.getAction(), "android.intent.action.PACKAGE_REMOVED")) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    HwLog.v(PackageChangeMonitor.TAG, "ACTION_PACKAGE_REMOVED for uid = " + uid);
                    HwNetworkPolicyManager.from(context).removeHwUidPolicy(uid, -1);
                }
            }
        }
    };

    public String getName() {
        return TAG;
    }

    public void registerListener() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        GlobalContext.getContext().registerReceiver(mPackageReceiver, packageFilter, null, null);
    }

    public void unRegisterListener() {
        GlobalContext.getContext().unregisterReceiver(mPackageReceiver);
    }

    public void onHandleMessage(Message msg) {
    }
}
