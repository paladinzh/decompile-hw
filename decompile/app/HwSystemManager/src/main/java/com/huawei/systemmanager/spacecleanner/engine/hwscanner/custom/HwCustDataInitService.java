package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.app.IntentService;
import android.content.Intent;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.util.HwLog;

public class HwCustDataInitService extends IntentService {
    private static final String TAG = "HwCustDataInitService";

    public HwCustDataInitService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            HwLog.e(TAG, "intent is null");
            return;
        }
        String action = intent.getAction();
        HwLog.i(TAG, "recevie action =" + action);
        if (Const.ACTION_INITIAL_HWCUST_TRASH.equals(action)) {
            ScanManager.getAllHwCustData();
        }
    }
}
