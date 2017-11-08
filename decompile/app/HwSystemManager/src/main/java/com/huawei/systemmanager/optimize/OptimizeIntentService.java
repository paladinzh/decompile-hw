package com.huawei.systemmanager.optimize;

import android.app.IntentService;
import android.content.Intent;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.util.HwLog;

public class OptimizeIntentService extends IntentService {
    private static final String OPTIMIZE_SERVICE_TAG = OptimizeIntentService.class.getSimpleName();

    public OptimizeIntentService() {
        super(OPTIMIZE_SERVICE_TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            HwLog.i(OPTIMIZE_SERVICE_TAG, "recevie action =" + action);
            if (Const.ACTION_INITIAL_PROTECT_DATA.equals(action)) {
                ProtectAppControl.getInstance(this).checkPackageFullInner();
            }
        }
    }
}
