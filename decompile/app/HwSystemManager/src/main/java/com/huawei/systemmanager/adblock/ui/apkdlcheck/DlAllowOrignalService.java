package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.util.NotificationID;

public class DlAllowOrignalService extends IntentService {
    public DlAllowOrignalService() {
        super("DlAllowOrignalService");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int uid = intent.getIntExtra("uid", 0);
            if (uid != 0) {
                String pkg = intent.getStringExtra("pkg");
                ((NotificationManager) getApplicationContext().getSystemService("notification")).cancel(NotificationID.APK_DL_BLOCK);
                AdCheckUrlResult.updateOptPolicy(getApplicationContext(), uid + pkg, pkg, 3, true);
            }
        }
    }
}
