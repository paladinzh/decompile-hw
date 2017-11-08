package com.huawei.systemmanager.securitythreats.ui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;
import java.util.List;

public class SecurityThreatsBroadcast extends HsmBroadcastReceiver {
    private static final String TAG = "SecurityThreatsBroadcast";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            HwLog.v(TAG, "onReceive action=" + intent.getAction());
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        super.doInBackground(context, intent);
        if (intent != null) {
            if (SecurityThreatsConst.ACTION_VIRUS_PKG_UPDATE.equals(intent.getAction())) {
                HwLog.v(TAG, "doInBackground ACTION_VIRUS_PKG_UPDATE");
                notify(context, check(context));
            }
        }
    }

    private List<ScanResultEntity> check(Context context) {
        return VirusPkgChecker.getInstance(context, true, true).checkAll();
    }

    private void notify(Context context, List<ScanResultEntity> virusList) {
        for (ScanResultEntity entity : virusList) {
            SecurityThreatsUtil.notifyVirusByPushToUI(context, entity.packageName);
        }
    }
}
