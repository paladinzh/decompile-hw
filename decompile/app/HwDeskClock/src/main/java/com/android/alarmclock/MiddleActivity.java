package com.android.alarmclock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MiddleActivity extends Activity {
    private static Long sBeginTime = Long.valueOf(-1);
    private final long MIN_TIME = 2500;
    private String mCityName;
    private String mIndex;
    private String mSecondConfigCityName;
    private String mSecondConfigIndex;
    private String mSecondConfigZone;
    private String mTimeZone;
    private int mWidgetID;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        int requestCode = 0;
        String intentString = intent.getAction();
        Bundle bundle = new Bundle();
        if ("android.appwidget.action.APPWIDGET_CONFIGURE".equals(intentString)) {
            synchronized (MiddleActivity.class) {
                if (sBeginTime.longValue() == -1 || Utils.getTimeNow() - sBeginTime.longValue() >= 2500) {
                    sBeginTime = Long.valueOf(Utils.getTimeNow());
                } else {
                    sBeginTime = Long.valueOf(Utils.getTimeNow());
                    Log.printf("onCreate  configing  do not finish", new Object[0]);
                    finish();
                    return;
                }
            }
        } else if ("com.android.desk.change_first_city".equals(intentString)) {
            changeFirstCity(intent, bundle);
            requestCode = 3;
        } else if ("com.android.desk.change_second_city".equals(intentString)) {
            changeSecondCity(intent, bundle);
            requestCode = 4;
        }
        if (!intent.getBooleanExtra("handle_action", false)) {
            intent.putExtra("handle_action", true);
            TimeZoneUtils.startPickZoneActivity((Activity) this, requestCode, bundle);
        }
    }

    private boolean isDataValid(String zone, String index, String city) {
        if (!TextUtils.isEmpty(zone) && !TextUtils.isEmpty(index) && !TextUtils.isEmpty(city)) {
            return true;
        }
        Log.printf("data from setting is invalid ", new Object[0]);
        return false;
    }

    private void changeFirstCity(Intent intent, Bundle bundle) {
        this.mWidgetID = intent.getIntExtra("widget_id", 0);
        String[] cityIndex = WidgetUtils.getCityIndex(this, this.mWidgetID);
        ArrayList<String> list = new ArrayList();
        if (TextUtils.isEmpty(cityIndex[0]) && TextUtils.isEmpty(cityIndex[1])) {
            Log.printf("left widget data clear", new Object[0]);
        } else if (!TextUtils.isEmpty(cityIndex[1])) {
            list.add(cityIndex[1]);
        }
        bundle.putStringArrayList("excluded_unique_ids", list);
        bundle.putInt("request_type", 5);
        bundle.putString("request_description", "HWDESKCLOCK_DUAL_CLOCKS_CHANGE_FIRST_CITY");
    }

    private void changeSecondCity(Intent intent, Bundle bundle) {
        this.mWidgetID = intent.getIntExtra("widget_id", 0);
        String[] cityIndex = WidgetUtils.getCityIndex(this, this.mWidgetID);
        ArrayList<String> list = new ArrayList();
        if (TextUtils.isEmpty(cityIndex[0]) && TextUtils.isEmpty(cityIndex[1])) {
            Log.printf("right widget data clear", new Object[0]);
        } else if (!TextUtils.isEmpty(cityIndex[0])) {
            list.add(cityIndex[0]);
        }
        bundle.putStringArrayList("excluded_unique_ids", list);
        bundle.putInt("request_type", 6);
        bundle.putString("request_description", "HWDESKCLOCK_DUAL_CLOCKS_CHANGE_SECOND_CITY");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.printf("MiddleActivity onActivityResult requestCode=%d, resultCode=%d", Integer.valueOf(requestCode), Integer.valueOf(resultCode));
        synchronized (MiddleActivity.class) {
            sBeginTime = Long.valueOf(-1);
        }
        if (resultCode == -1) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                finish();
                return;
            }
            this.mTimeZone = bundle.getString("id");
            this.mCityName = bundle.getString("name");
            this.mIndex = bundle.getString("unique_id");
            this.mCityName = TimeZoneUtils.getCityName(this.mCityName);
            if (!isDataValid(this.mTimeZone, this.mIndex, this.mCityName)) {
                finish();
            }
            if (requestCode == 1) {
                this.mSecondConfigZone = bundle.getString("second_id");
                this.mSecondConfigCityName = bundle.getString("second_name");
                this.mSecondConfigIndex = bundle.getString("second_unique_id");
                this.mSecondConfigCityName = TimeZoneUtils.getCityName(this.mSecondConfigCityName);
                if (!isDataValid(this.mSecondConfigZone, this.mSecondConfigIndex, this.mSecondConfigCityName)) {
                    finish();
                }
                Log.printf("mTimeZone= %s, mIndex= %s, mCityName= %s, mSecondConfigZone = %s, mSecondConfigIndex= %s, mSecondConfigCityName=%s", this.mTimeZone, this.mIndex, this.mCityName, this.mSecondConfigZone, this.mSecondConfigIndex, this.mSecondConfigCityName);
                Map<String, String> map = new HashMap();
                map.put(this.mIndex, this.mCityName);
                map.put(this.mSecondConfigIndex, this.mSecondConfigCityName);
                TimeZoneUtils.saveTimeZoneMap(this, map);
                configWorldClockWidget();
                finish();
            }
            if (requestCode == 3) {
                updateFirstWidget();
                finish();
            }
            if (requestCode == 4) {
                updateSecondWidget();
                finish();
            }
        } else if (resultCode == 0) {
            if (requestCode == 1) {
                finish();
            }
            if (requestCode == 3) {
                finish();
            }
            if (requestCode == 4) {
                finish();
            }
        }
    }

    private void updateWidget(int widgetId) {
        WorldClockAppWidgetProvider.updateAppWidget(DeskClockApplication.getDeskClockApplication(), AppWidgetManager.getInstance(DeskClockApplication.getDeskClockApplication()), widgetId);
        Intent resultValue = new Intent();
        resultValue.putExtra("appWidgetId", widgetId);
        setResult(-1, resultValue);
    }

    private Widget getWidgetInstance(String firstTimeZone, String secondTimeZone, String firstIndex, String secondIndex) {
        return new Widget(this.mWidgetID, firstTimeZone, secondTimeZone, firstIndex, secondIndex);
    }

    public void configWorldClockWidget() {
        WidgetUtils.addWidget(DeskClockApplication.getDeskClockApplication(), getWidgetInstance(this.mTimeZone, this.mSecondConfigZone, this.mIndex, this.mSecondConfigIndex));
        updateWidget(this.mWidgetID);
    }

    public void updateFirstWidget() {
        Log.printf("updateFirstWidget mTimeZone= %s, mIndex= %s, mCityName= %s", this.mTimeZone, this.mIndex, this.mCityName);
        String[] cities = WidgetUtils.getCityIndex(DeskClockApplication.getDeskClockApplication(), this.mWidgetID);
        if (!(cities.length == 0 || TextUtils.isEmpty(cities[0]))) {
            TimeZoneUtils.updatePreFromWidget((Context) this, cities[0]);
        }
        Map<String, String> map = new HashMap();
        map.put(this.mIndex, this.mCityName);
        TimeZoneUtils.saveTimeZoneMap(this, map);
        WidgetUtils.addWidget(DeskClockApplication.getDeskClockApplication(), getWidgetInstance(this.mTimeZone, "", this.mIndex, ""));
        updateWidget(this.mWidgetID);
    }

    public void updateSecondWidget() {
        Log.printf("updateSecondWidget mTimeZone= %s, mIndex= %s, mCityName= %s", this.mTimeZone, this.mIndex, this.mCityName);
        String[] cities = WidgetUtils.getCityIndex(DeskClockApplication.getDeskClockApplication(), this.mWidgetID);
        if (!(cities.length == 0 || TextUtils.isEmpty(cities[1]))) {
            TimeZoneUtils.updatePreFromWidget((Context) this, cities[1]);
        }
        Map<String, String> map = new HashMap();
        map.put(this.mIndex, this.mCityName);
        TimeZoneUtils.saveTimeZoneMap(this, map);
        WidgetUtils.addWidget(DeskClockApplication.getDeskClockApplication(), getWidgetInstance("", this.mTimeZone, "", this.mIndex));
        updateWidget(this.mWidgetID);
    }

    public static void startMiddleActivity(Context context, String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setFlags(402653184);
        intent.setComponent(new ComponentName(HwCustCoverAdapter.APP_PACKEGE, MiddleActivity.class.getName()));
        if (action != null) {
            intent.setAction(action);
        }
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }
}
