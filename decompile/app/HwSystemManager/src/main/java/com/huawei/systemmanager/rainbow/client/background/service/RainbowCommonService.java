package com.huawei.systemmanager.rainbow.client.background.service;

import android.app.IntentService;
import android.content.Intent;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.CloudNotificationServHandle;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.HandleCloudChangeEvent;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.InitCloudDBServHandle;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.RecommendMultiApk;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.RecommendSingleApk;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.UpdateAllDataServHandle;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.tipsmanager.NotificationUtilConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Map.Entry;

public class RainbowCommonService extends IntentService {
    private static final String TAG = "RainbowCommonService";
    private static Map<String, IIntentHandler> mIntentHandlers = Maps.newHashMap();

    static {
        mIntentHandlers.put(CloudActions.INTENT_INIT_CLOUDDB, new InitCloudDBServHandle());
        IIntentHandler notificationHandler = new CloudNotificationServHandle();
        mIntentHandlers.put(NotificationUtilConst.CLOUD_NOTIFICATION_REFUSE, notificationHandler);
        mIntentHandlers.put(NotificationUtilConst.CLOUD_NOTIFICATION_ALLOW, notificationHandler);
        mIntentHandlers.put(CloudActions.INTENT_UPDATE_ALL_DATA, new UpdateAllDataServHandle());
        mIntentHandlers.put(CloudActions.INTENT_CLOUD_RECOMMEND_SINGLE_APK, new RecommendSingleApk());
        mIntentHandlers.put(CloudActions.INTENT_CLOUD_RECOMMEND_MULTI_APK, new RecommendMultiApk());
        mIntentHandlers.put("android.net.conn.CONNECTIVITY_CHANGE", new HandleCloudChangeEvent());
    }

    public RainbowCommonService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            HwLog.e(TAG, "onHandleIntent get null intent");
            return;
        }
        String action = intent.getAction();
        for (Entry<String, IIntentHandler> entry : mIntentHandlers.entrySet()) {
            if (((String) entry.getKey()).equals(action)) {
                HwLog.v(TAG, "onHandleIntent handle action: " + action);
                ((IIntentHandler) entry.getValue()).handleIntent(getApplicationContext(), intent);
                return;
            }
        }
        HwLog.w(TAG, "No handler exist for specified action:" + action);
    }
}
