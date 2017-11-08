package com.android.gallery3d.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.gallery3d.settings.HicloudAccountManager;
import java.util.HashMap;

public class GalleryBusinessMonitorService extends IntentService {
    private static HwLogExceptionWrapper sReporter = new HwLogExceptionWrapper();

    public GalleryBusinessMonitorService() {
        super("GalleryBusinessMonitorService");
    }

    public static void startActionReportEvent(Context context, HashMap<Short, Object> eventInfoMap, int eventId) {
        Intent intent = new Intent(context, GalleryBusinessMonitorService.class);
        intent.setAction("com.huawei.gallery3d.radarservice.action.REPORT_EVENT");
        intent.putExtra("event_info", eventInfoMap);
        intent.putExtra("event_id", eventId);
        context.startService(intent);
    }

    public static HashMap<Short, Object> packageData(Object... params) {
        HashMap<Short, Object> eventInfoMap = new HashMap();
        eventInfoMap.put(Short.valueOf((short) 0), getCurrentVersionCode(GalleryUtils.getContext()));
        short order = (short) 1;
        for (Object obj : params) {
            if (obj != null) {
                eventInfoMap.put(Short.valueOf(order), obj);
                order = (short) (order + 1);
            }
        }
        return eventInfoMap;
    }

    private static String getCurrentVersionCode(Context context) {
        int currentVersionCode = -1;
        try {
            currentVersionCode = context.getPackageManager().getPackageInfo(HicloudAccountManager.PACKAGE_NAME, 0).versionCode;
        } catch (NameNotFoundException e) {
            GalleryLog.d("GalleryBusinessMonitorService", "Not found com.android.gallery3d versionCode");
        }
        return String.valueOf(currentVersionCode);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if ("com.huawei.gallery3d.radarservice.action.REPORT_EVENT".equals(intent.getAction())) {
                HashMap<Short, Object> eventInfoMap = (HashMap) intent.getSerializableExtra("event_info");
                int eventId = intent.getIntExtra("event_id", -1);
                if (eventInfoMap == null || eventInfoMap.isEmpty() || eventId == -1) {
                    GalleryLog.w("GalleryBusinessMonitorService", "report msg is empty");
                } else {
                    handleActionReportEvent(eventInfoMap, eventId);
                }
            }
        }
    }

    private void handleActionReportEvent(HashMap<Short, Object> eventInfoMap, int eventId) {
        sReporter.reportEvent(eventInfoMap, eventId);
    }
}
