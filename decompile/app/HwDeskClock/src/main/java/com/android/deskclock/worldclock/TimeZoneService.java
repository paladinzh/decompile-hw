package com.android.deskclock.worldclock;

import android.app.IntentService;
import android.content.Intent;
import java.util.ArrayList;

public class TimeZoneService extends IntentService {
    public static final String TAG = TimeZoneService.class.getSimpleName();

    public TimeZoneService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED".equals(action)) {
                ArrayList<String> worldCityList;
                if (intent.getBooleanExtra("need_init_tz_pref", false)) {
                    worldCityList = TimeZoneUtils.getCityIndexList(this);
                } else {
                    worldCityList = TimeZoneUtils.getExitCities(this);
                }
                TimeZoneUtils.queryRemoteTimeZone(this, worldCityList);
            } else if ("com.android.desk.syncData".equals(action)) {
                TimeZoneUtils.updateWidgets(this);
                TimeZoneUtils.worldPageUpdate(this);
            }
        }
    }
}
