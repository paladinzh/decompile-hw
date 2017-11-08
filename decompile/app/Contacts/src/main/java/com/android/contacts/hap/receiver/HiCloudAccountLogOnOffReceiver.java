package com.android.contacts.hap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;

public class HiCloudAccountLogOnOffReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.i("HiCloudAccountLogOnOffReceiver", "no context or intent");
            return;
        }
        String action = intent.getAction();
        if ("com.huawei.hicloud.intent.action.HICLOUD_LOGON_ACTION".equals(action)) {
            if (HwLog.HWDBG) {
                HwLog.d("HiCloudAccountLogOnOffReceiver", "HiCloud LogOn action received in HiCloudAccountLogOnOffReceiver");
            }
            String accountName = intent.getStringExtra("accountName");
            if (!TextUtils.isEmpty(accountName)) {
                HiCloudUtil.setHicloudAccount(accountName);
                HiCloudUtil.setHuaWeiCloudAccountLogOn(true);
            }
        } else if ("com.huawei.hicloud.intent.action.HICLOUD_LOGOFF_ACTION".equals(action)) {
            if (HwLog.HWDBG) {
                HwLog.d("HiCloudAccountLogOnOffReceiver", "HiCloud LogOff action received in HiCloudAccountLogOnOffReceiver");
            }
            HiCloudUtil.setHuaWeiCloudAccountLogOn(false);
            HiCloudUtil.setHicloudAccount("");
        }
    }
}
