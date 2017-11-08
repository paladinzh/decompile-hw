package com.android.alarmclock;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.provider.Settings.Secure;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.deskclock.worldclock.City.WidgetColumns;
import com.android.util.Log;
import java.util.ArrayList;

public class WidgetUtils {
    private static ContentResolver mContentResolver;

    public static boolean addWidget(Context context, Widget widget) {
        initContentResolver(context);
        Cursor cursor = mContentResolver.query(WidgetColumns.CONTENT_URI, null, "widget_id=" + widget.queryWidgetID(), null, null);
        if (cursor == null) {
            return false;
        }
        if (cursor.moveToFirst()) {
            mContentResolver.update(WidgetColumns.CONTENT_URI, getContentValues(widget), "widget_id=" + widget.queryWidgetID(), null);
        } else {
            ContentValues values = getContentValues(widget);
            values.put("widget_id", Integer.valueOf(widget.queryWidgetID()));
            mContentResolver.insert(WidgetColumns.CONTENT_URI, values);
        }
        cursor.close();
        return true;
    }

    public static ArrayList<Integer> getWidgetIDList(Context context) {
        ArrayList<Integer> list = new ArrayList();
        Cursor cursor = context.getContentResolver().query(WidgetColumns.CONTENT_URI, new String[]{"widget_id"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndex("widget_id"))));
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public static String[] getCityIndex(Context context, int id) {
        String[] list = new String[2];
        initContentResolver(context);
        String[] project = new String[]{"first_index", "second_index"};
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor = mContentResolver.query(WidgetColumns.CONTENT_URI, project, "widget_id=?", selectionArgs, null);
        if (cursor == null) {
            return list;
        }
        try {
            if (cursor.moveToFirst()) {
                String first = cursor.getString(cursor.getColumnIndex("first_index"));
                String second = cursor.getString(cursor.getColumnIndex("second_index"));
                list[0] = first;
                list[1] = second;
            }
            cursor.close();
            return list;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Widget widget) {
        return widget.createCheckContentValues();
    }

    public static int deleteWidget(Context context, int id) {
        initContentResolver(context);
        return mContentResolver.delete(WidgetColumns.CONTENT_URI, "widget_id=" + id, null);
    }

    public static Widget getWidgetById(Context context, int id) {
        initContentResolver(context);
        Cursor cursor = mContentResolver.query(WidgetColumns.CONTENT_URI, null, "widget_id=" + id, null, null);
        Widget widget = null;
        if (cursor != null && cursor.moveToFirst()) {
            widget = new Widget(cursor);
        }
        if (cursor != null) {
            cursor.close();
        }
        return widget;
    }

    public static synchronized void initContentResolver(Context context) {
        synchronized (WidgetUtils.class) {
            if (mContentResolver == null) {
                try {
                    mContentResolver = context.createPackageContext(HwCustCoverAdapter.APP_PACKEGE, 3).getContentResolver();
                } catch (NameNotFoundException e) {
                    Log.e("WidgetUtils", "initContentResolver : NameNotFoundException = " + e.getMessage());
                }
            }
        }
    }

    public static void saveLaucherState(Context context, int state) {
        if (context != null) {
            try {
                Secure.putInt(context.getContentResolver(), "isNormal", state);
            } catch (Exception e) {
                Log.w("WidgetUtils", "saveLaucherState : " + e.getMessage());
            }
        }
    }

    public static int getLaucherState(Context context) {
        if (context == null) {
            return 1;
        }
        try {
            return Secure.getInt(context.getContentResolver(), "isNormal", 1);
        } catch (Exception e) {
            Log.w("WidgetUtils", "getLaucherState : " + e.getMessage());
            return 1;
        }
    }
}
