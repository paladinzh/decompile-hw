package com.android.deskclock.worldclock;

import android.app.Activity;
import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.alarmclock.WidgetUtils;
import com.android.alarmclock.WorldClockAppWidgetProvider;
import com.android.deskclock.R;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.deskclock.worldclock.City.LocationColumns;
import com.android.deskclock.worldclock.City.WidgetColumns;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class TimeZoneUtils {
    private static final String[] TIMEZONE_COLUMNS = new String[]{"unique_id", "name"};
    private static boolean bUpdating = false;
    private static ArrayList<String> cityList = null;
    private static String localCoutry = null;
    private static Map<String, String> localTimeZoneMap = null;
    private static Map<String, String> updateTimeZoneMap = null;

    private TimeZoneUtils() {
    }

    public static void startPickZoneActivity(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent("huawei.intent.action.ZONE_PICKER");
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startPickZoneActivity(Fragment fragment, int requestCode, Bundle bundle) {
        Intent intent = new Intent("huawei.intent.action.ZONE_PICKER");
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void registerUpdateUIBroadcast(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.desk.syncData");
        context.registerReceiver(receiver, filter, "com.android.deskclock.huawei.permission.SYNC_DATA", null);
    }

    public static void unRegisterUpdateUIBroadcast(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public static String getCityName(String cityAndCountry) {
        String city = cityAndCountry;
        if (TextUtils.isEmpty(cityAndCountry)) {
            return "";
        }
        int index = cityAndCountry.indexOf("(");
        if (index > 0) {
            city = cityAndCountry.substring(0, index).trim();
        }
        return city;
    }

    public static synchronized Map<String, String> getTimeZoneMap(Context context) {
        Map<String, String> all;
        synchronized (TimeZoneUtils.class) {
            all = Utils.getSharedPreferences(context, "timeZone.cfg", 0).getAll();
        }
        return all;
    }

    public static synchronized String getTimeZoneMapValue(Context context, String key) {
        String value;
        synchronized (TimeZoneUtils.class) {
            constructLocalMap(context);
            value = (String) localTimeZoneMap.get(key);
            if (value != null) {
                int index = value.lastIndexOf("_");
                if (index != -1) {
                    value = getLowVerName(context, Integer.parseInt(value.substring(index + 1).trim()));
                } else if (value.matches("^[0-9]+$")) {
                    value = getLowVerName(context, Integer.parseInt(value));
                }
            } else {
                Log.printf(" getTimeZoneMapValue do not get value  key=%s", key);
            }
        }
        return value;
    }

    public static synchronized void saveTimeZoneMap(Context context, Map<String, String> map) {
        synchronized (TimeZoneUtils.class) {
            constructLocalMap(context);
            localTimeZoneMap.putAll(map);
            Editor editor = Utils.getSharedPreferences(context, "timeZone.cfg", 0).edit();
            for (Entry<String, String> entry : map.entrySet()) {
                editor.putString((String) entry.getKey(), (String) entry.getValue());
            }
            editor.commit();
        }
    }

    public static void nSyncLoadLoadInitData(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                TimeZoneUtils.initLocalTimeZoneMap(context);
            }
        }).start();
    }

    public static synchronized void initLocalTimeZoneMap(Context context) {
        synchronized (TimeZoneUtils.class) {
            if (localTimeZoneMap == null) {
                localTimeZoneMap = getTimeZoneMap(context);
            }
        }
    }

    public static synchronized ArrayList<String> getExitCities(Context context) {
        ArrayList cities;
        synchronized (TimeZoneUtils.class) {
            initLocalTimeZoneMap(context);
            cities = new ArrayList();
            for (String city : localTimeZoneMap.keySet()) {
                cities.add(city);
            }
        }
        return cities;
    }

    public static synchronized void clearCityData() {
        synchronized (TimeZoneUtils.class) {
            if (localTimeZoneMap != null) {
                localTimeZoneMap.clear();
                localTimeZoneMap = null;
            }
            if (cityList != null) {
                cityList.clear();
                cityList = null;
            }
        }
    }

    private static String buildQuerySql(List<String> list, StringBuilder sqlCondition, String[] args) {
        int len = list.size();
        for (int i = 0; i < len - 1; i++) {
            sqlCondition.append(" unique_id=?").append(" OR ");
            args[i] = (String) list.get(i);
        }
        sqlCondition.append(" unique_id=?");
        args[len - 1] = (String) list.get(len - 1);
        return sqlCondition.toString();
    }

    public static synchronized void queryRemoteTimeZone(Context context, ArrayList<String> worldCityList) {
        synchronized (TimeZoneUtils.class) {
            setDataSync(context, 1);
            Log.printf("queryRemoteTimeZone query database", new Object[0]);
            StringBuilder stringBuilder = new StringBuilder(900);
            updateTimeZoneMap = new HashMap();
            int len = worldCityList.size();
            for (int i = 0; i < ((len + 30) - 1) / 30; i++) {
                int end;
                int start = i * 30;
                if (((i + 1) * 30) - len > 0) {
                    end = len;
                } else {
                    end = (i + 1) * 30;
                }
                List<String> list = worldCityList.subList(start, end);
                String[] sqlArgs = new String[list.size()];
                String sqlConString = buildQuerySql(list, stringBuilder, sqlArgs);
                try {
                    Cursor query = context.getContentResolver().query(Uri.parse("content://com.android.settings.timezonesprovider/timezones"), TIMEZONE_COLUMNS, sqlConString, sqlArgs, null);
                    if (query != null) {
                        Log.printf("query result count = %s", String.valueOf(query.getCount()));
                        while (query.moveToNext()) {
                            updateTimeZoneMap.put(query.getString(query.getColumnIndex(TIMEZONE_COLUMNS[0])), getCityName(query.getString(query.getColumnIndex(TIMEZONE_COLUMNS[1]))));
                        }
                        query.close();
                        stringBuilder.setLength(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (String key : worldCityList) {
                if (updateTimeZoneMap.containsKey(key)) {
                    Log.printf("key = %s, value=%s  update from settings", key, updateTimeZoneMap.get(key));
                } else {
                    String value = (String) localTimeZoneMap.get(key);
                    int index = value.lastIndexOf("_");
                    if (index != -1) {
                        updateTimeZoneMap.put(key, value.substring(index + 1).trim());
                    }
                    Log.printf("key = %s  update from local", key);
                }
            }
            saveTimeZoneMap(context, updateTimeZoneMap);
            updateTimeZoneMap.clear();
            updateTimeZoneMap = null;
            worldCityList.clear();
            setDataSync(context, 0);
            setTimeZoneUpdating(false);
            context.sendBroadcast(new Intent("com.android.desk.syncData"), "com.android.deskclock.huawei.permission.SYNC_DATA");
        }
    }

    private static synchronized void updatePref(Context context, String temp) {
        synchronized (TimeZoneUtils.class) {
            String id = Utils.getSharedPreferences(context, "setting_activity", 0).getString("home_time_index", "");
            if (TextUtils.isEmpty(id) || !id.equals(temp)) {
                if (localTimeZoneMap != null) {
                    localTimeZoneMap.remove(temp);
                }
                Editor editor = Utils.getSharedPreferences(context, "timeZone.cfg", 0).edit();
                editor.remove(temp);
                editor.commit();
                return;
            }
        }
    }

    public static void updateWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ArrayList<Integer> list = WidgetUtils.getWidgetIDList(context);
        if (list != null) {
            for (Integer intValue : list) {
                WorldClockAppWidgetProvider.updateAppWidget(context, appWidgetManager, intValue.intValue());
            }
        }
    }

    public static synchronized int getDataSync(Context context) {
        int i;
        synchronized (TimeZoneUtils.class) {
            i = Utils.getSharedPreferences(context, "config", 0).getInt("sync", 0);
        }
        return i;
    }

    public static synchronized void setDataSync(Context context, int state) {
        synchronized (TimeZoneUtils.class) {
            Editor editor = Utils.getSharedPreferences(context, "config", 0).edit();
            editor.putInt("sync", state);
            editor.commit();
        }
    }

    public static void constructLocalMap(Context context) {
        int state = getDataSync(context);
        initLocalTimeZoneMap(context);
        if (state == 1 && !getTimeZoneUpdating()) {
            context.sendBroadcast(new Intent("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED"), "com.android.deskclock.huawei.permission.SYNC_DATA");
        }
    }

    public static void worldPageUpdate(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("city_index", "c500");
        String[] selectionArgs = new String[]{"c500"};
        context.getContentResolver().update(LocationColumns.CONTENT_URI, contentValues, "city_index=?", selectionArgs);
    }

    public static ArrayList<String> getCityIndexList(Context context) {
        ArrayList<String> list = new ArrayList();
        Cursor cursor = context.getContentResolver().query(LocationColumns.CONTENT_URI, new String[]{"city_index"}, null, null, null);
        if (cursor == null) {
            return list;
        }
        while (cursor.moveToNext()) {
            String city = cursor.getString(cursor.getColumnIndex("city_index"));
            if (!("c500".equals(city) || "c501".equals(city) || "c502".equals(city))) {
                list.add(city);
            }
        }
        cursor.close();
        return list;
    }

    private static synchronized void initCityList(Context context) {
        synchronized (TimeZoneUtils.class) {
            try {
                for (String city : context.getResources().getStringArray(R.array.cities_names)) {
                    cityList.add(city);
                }
            } catch (OutOfMemoryError e) {
                Log.printfe("exception: %s", e.getMessage());
            }
        }
        return;
    }

    public static synchronized String getLowVerName(Context context, int cityIndex) {
        String str;
        synchronized (TimeZoneUtils.class) {
            String country = Locale.getDefault().getCountry();
            if (!(cityList == null || country.contains(localCoutry))) {
                cityList.clear();
                cityList = null;
                localCoutry = country;
            }
            if (cityList == null) {
                localCoutry = country;
                cityList = new ArrayList();
                initCityList(context);
            }
            Log.printf(" getLowVerName  get value  key=%s, value = %s", Integer.valueOf(cityIndex), cityList.get(cityIndex));
            str = (String) cityList.get(cityIndex);
        }
        return str;
    }

    public static synchronized boolean getTimeZoneUpdating() {
        boolean z;
        synchronized (TimeZoneUtils.class) {
            z = bUpdating;
        }
        return z;
    }

    public static synchronized void setTimeZoneUpdating(boolean bdoing) {
        synchronized (TimeZoneUtils.class) {
            bUpdating = bdoing;
        }
    }

    public static void updatePreFromWidget(Context context, String cityIndex) {
        try {
            Context appContext = context.createPackageContext(HwCustCoverAdapter.APP_PACKEGE, 3);
            String[] projection = new String[]{"city_index"};
            String[] selectionArgs = new String[]{cityIndex};
            Cursor cursor = appContext.getContentResolver().query(LocationColumns.CONTENT_URI, projection, "city_index=?", selectionArgs, null);
            boolean delete = true;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    delete = false;
                }
                cursor.close();
            }
            if (delete) {
                updatePref(appContext, cityIndex);
            }
        } catch (NameNotFoundException e) {
            Log.printfe("exception : %s", e.getMessage());
        }
    }

    public static void updatePreFromWidget(Context context, String[] cityIndexs) {
        try {
            Context appContext = context.createPackageContext(HwCustCoverAdapter.APP_PACKEGE, 3);
            String[] projection = new String[]{"city_index"};
            String[] selectionArgs = new String[]{cityIndexs[0], cityIndexs[1]};
            Cursor cursor = appContext.getContentResolver().query(LocationColumns.CONTENT_URI, projection, "city_index=? OR city_index=?", selectionArgs, null);
            boolean delIndex0 = true;
            boolean delIndex1 = true;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String index = cursor.getString(cursor.getColumnIndex("city_index"));
                    if (index.equals(cityIndexs[0])) {
                        delIndex0 = false;
                    }
                    if (index.equals(cityIndexs[1])) {
                        delIndex1 = false;
                    }
                }
                cursor.close();
            }
            if (delIndex0) {
                updatePref(appContext, cityIndexs[0]);
            }
            if (delIndex1) {
                updatePref(appContext, cityIndexs[1]);
            }
        } catch (NameNotFoundException e) {
            Log.printfe("exception : %s", e.getMessage());
        }
    }

    public static void updatePreFromWorldPage(Context context, String cityIndex) {
        try {
            Context appContext = context.createPackageContext(HwCustCoverAdapter.APP_PACKEGE, 3);
            Cursor cursor = appContext.getContentResolver().query(WidgetColumns.CONTENT_URI, new String[]{"_id"}, "first_index=? OR second_index=?", new String[]{cityIndex, cityIndex}, null);
            if (cursor != null && cursor.getCount() == 0) {
                updatePref(appContext, cityIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (NameNotFoundException e) {
            Log.printfe("exception : %s", e.getMessage());
        }
    }
}
