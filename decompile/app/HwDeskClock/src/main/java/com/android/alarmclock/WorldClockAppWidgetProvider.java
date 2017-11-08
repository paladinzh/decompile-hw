package com.android.alarmclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.android.deskclock.R;
import com.android.deskclock.worldclock.TimeZoneUtils;

public class WorldClockAppWidgetProvider extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        WidgetUtils.deleteWidget(context, appWidgetIds[0]);
        String[] cityIndexs = WidgetUtils.getCityIndex(context, appWidgetIds[0]);
        if (!(cityIndexs.length == 0 || TextUtils.isEmpty(cityIndexs[0]) || TextUtils.isEmpty(cityIndexs[1]))) {
            TimeZoneUtils.updatePreFromWidget(context, cityIndexs);
        }
        super.onDeleted(context, appWidgetIds);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Widget widget = WidgetUtils.getWidgetById(context, appWidgetId);
        if (widget != null) {
            String firstTimeZone = widget.getmFirstTimeZone();
            String secondTimeZone = widget.getmSecondTimeZone();
            String firstIndex = widget.getmFirstIndex();
            String secondIndex = widget.getmSecondIndex();
            if (firstTimeZone != null && secondTimeZone != null && !"".equals(firstTimeZone) && !"".equals(secondTimeZone)) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.worldclock_appwidget);
                setClock(context, firstTimeZone, views, R.id.firstWorldClock, R.id.firstCityNames, firstIndex, appWidgetId);
                views.setString(R.id.world_digital_clock, "setmTimeZone", firstTimeZone);
                setClock(context, secondTimeZone, views, R.id.secondWorldClock, R.id.secondCityNames, secondIndex, appWidgetId);
                views.setString(R.id.world_digital_clockright, "setmTimeZone", secondTimeZone);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    public static String getDisplayName(Context context, String index) {
        if (index == null) {
            return "";
        }
        return TimeZoneUtils.getTimeZoneMapValue(context, index);
    }

    private static void setClock(Context context, String timezone, RemoteViews views, int clockViewId, int clockCityNames, String index, int widgetId) {
        views.setString(clockViewId, "setmTimeZone", timezone);
        views.setInt(clockViewId, "setmWidgetId", widgetId);
        views.setTextViewText(clockCityNames, getDisplayName(context, index));
    }
}
