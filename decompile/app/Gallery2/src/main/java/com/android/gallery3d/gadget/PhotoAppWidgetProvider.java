package com.android.gallery3d.gadget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class PhotoAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WidgetProvider";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent = new Intent();
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra("appWidgetIds", appWidgetIds);
        intent.setClass(context, WidgetIntentService.class);
        context.startService(intent);
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        Intent intent = new Intent();
        intent.setAction("android.appwidget.action.APPWIDGET_DELETED");
        intent.putExtra("appWidgetIds", appWidgetIds);
        intent.setClass(context, WidgetIntentService.class);
        context.startService(intent);
        super.onDeleted(context, appWidgetIds);
    }
}
