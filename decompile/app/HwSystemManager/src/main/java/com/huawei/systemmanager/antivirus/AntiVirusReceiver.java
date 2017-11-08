package com.huawei.systemmanager.antivirus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.antivirus.utils.AntivirusTipUtil;
import com.huawei.systemmanager.util.HwLog;

public class AntiVirusReceiver extends BroadcastReceiver {
    private String TAG = "AntiVirusReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            String action = intent.getAction();
            HwLog.d(this.TAG, "onReceive: " + intent.getAction());
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                AntiVirusTools.checkVirusForNewInstalledApk(context, intent);
            }
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                handlerPackageRemoved(context, intent);
            }
        }
    }

    private void handlerPackageRemoved(Context ctx, Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String pkg = uri.getSchemeSpecificPart();
            AntiVirusTools.deleteVirusApk(ctx, intent);
            AntivirusTipUtil.removeViewedCompetitor(ctx, pkg);
        }
    }
}
