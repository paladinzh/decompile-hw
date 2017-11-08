package com.android.gallery3d.gadget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViews;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;

public class WidgetIntentService extends IntentService {
    private static final String[] PROJECTION = new String[]{"bucket_id"};
    private static final String TAG = "WidgetIntentService";

    public WidgetIntentService() {
        super(TAG);
    }

    public WidgetIntentService(String name) {
        super(name);
    }

    private void updateView(int widgetId) {
        String bucketId = WidgetUtils.getSharedPrefer(getApplicationContext(), widgetId);
        GalleryLog.i(TAG, "bucketId =" + bucketId);
        if (bucketId != null) {
            setBucketIdToRemoteView(widgetId, bucketId);
        } else {
            setBackgroundToRemoteView(widgetId);
        }
    }

    private void setBucketIdToRemoteView(int widgetId, String bucketId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget_main);
        views.setInt(R.id.appwidget_view, "setViewWidgetId", widgetId);
        views.setString(R.id.appwidget_view, "setViewBucketId", bucketId);
        AppWidgetManager.getInstance(this).updateAppWidget(new int[]{widgetId}, views);
    }

    private void setBackgroundToRemoteView(int widgetId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget_main);
        views.setInt(R.id.appwidget_view, "setViewChooseAlbum", widgetId);
        AppWidgetManager.getInstance(this).updateAppWidget(new int[]{widgetId}, views);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int[] widgetIds = intent.getIntArrayExtra("appWidgetIds");
            if (widgetIds != null && widgetIds.length != 0) {
                String action = intent.getAction();
                if ("android.appwidget.action.APPWIDGET_UPDATE".equals(action)) {
                    for (int widgetId : widgetIds) {
                        updateView(widgetId);
                    }
                } else if ("android.appwidget.action.APPWIDGET_DELETED".equals(action)) {
                    for (int widgetId2 : widgetIds) {
                        WidgetUtils.delete(this, widgetId2);
                    }
                }
            }
        }
    }
}
