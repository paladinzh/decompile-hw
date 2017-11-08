package com.huawei.systemmanager.adblock.ui.model;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.connect.request.AdBlockRequest;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public class AdIntentService extends IntentService {
    private static final String SERVICE_TAG = "AdBlock_AdIntentService";
    private static final String TAG = "AdBlock_AdIntentService";

    public AdIntentService() {
        super("AdBlock_AdIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            HwLog.e("AdBlock_AdIntentService", "onHandleIntent get null intent");
        } else if (Utility.isOwner()) {
            String action = intent.getAction();
            HwLog.i("AdBlock_AdIntentService", "onHandleIntent action=" + action);
            if (AdConst.ACTION_AD_UPDATE.equals(action)) {
                update(intent);
            }
        } else {
            HwLog.i("AdBlock_AdIntentService", "onHandleIntent is not owner user, just return");
        }
    }

    private void update(Intent intent) {
        Context context = getApplicationContext();
        if (AdUtils.isCloudEnable(context)) {
            int updateType = intent.getIntExtra(AdConst.BUNDLE_KEY_UPDATE_TYPE, 2);
            AdUtils.sendUpdateResult(this, updateType, new AdBlockRequest(updateType).processRequest(context));
            return;
        }
        HwLog.i("AdBlock_AdIntentService", "update it is not allow access network, just return");
    }
}
