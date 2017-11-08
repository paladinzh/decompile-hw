package com.android.mms.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class MmsWidgetProvider extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int updateWidget : appWidgetIds) {
            updateWidget(context, updateWidget);
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.android.mms.intent.action.ACTION_NOTIFY_DATASET_CHANGED".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context, MmsWidgetProvider.class)), R.id.conversation_list);
            return;
        }
        super.onReceive(context, intent);
    }

    private static void updateWidget(Context context, int appWidgetId) {
        if (MLog.isLoggable("Mms_widget", 2)) {
            MLog.v("MmsWidgetProvider", "updateWidget appWidgetId: " + appWidgetId);
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        int unreadCount = MmsWidgetService.getMsgUnreadCount();
        remoteViews.setViewVisibility(R.id.widget_unread_count, unreadCount > 0 ? 0 : 8);
        if (unreadCount > 0) {
            remoteViews.setTextViewText(R.id.widget_unread_count, Integer.toString(unreadCount));
        }
        Intent intent = new Intent(context, MmsWidgetService.class);
        intent.putExtra("appWidgetId", appWidgetId);
        intent.setData(Uri.parse(intent.toUri(1)));
        remoteViews.setRemoteAdapter(appWidgetId, R.id.conversation_list, intent);
        remoteViews.setOnClickPendingIntent(R.id.widget_header, PendingIntent.getActivity(context, 0, new Intent(context, ConversationList.class), 134217728));
        Intent composeIntent = new Intent(context, ComposeMessageActivity.class);
        composeIntent.putExtra("fromWidget", true);
        composeIntent.setAction("android.intent.action.SENDTO");
        remoteViews.setOnClickPendingIntent(R.id.widget_compose, PendingIntent.getActivity(context, 0, composeIntent, 134217728));
        Intent msgIntent = new Intent();
        msgIntent.setType("vnd.android-dir/mms-sms");
        msgIntent.putExtra("fromWidget", true);
        msgIntent.setComponent(new ComponentName(context, ComposeMessageActivity.class));
        remoteViews.setPendingIntentTemplate(R.id.conversation_list, PendingIntent.getActivity(context, 0, msgIntent, 134217728));
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews);
    }

    public static void notifyDatasetChanged(Context context) {
        if (MLog.isLoggable("Mms_widget", 2)) {
            MLog.v("MmsWidgetProvider", "notifyDatasetChanged");
        }
        context.sendBroadcast(new Intent("com.android.mms.intent.action.ACTION_NOTIFY_DATASET_CHANGED"));
    }
}
