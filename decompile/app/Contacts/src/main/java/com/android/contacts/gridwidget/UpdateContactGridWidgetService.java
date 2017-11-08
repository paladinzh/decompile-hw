package com.android.contacts.gridwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.android.contacts.hap.util.IntentServiceWithWakeLock;
import com.android.contacts.hap.util.ReflelctionConstant;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class UpdateContactGridWidgetService extends IntentServiceWithWakeLock {
    private static final String TAG = UpdateContactGridWidgetService.class.getSimpleName();

    public UpdateContactGridWidgetService() {
        super("UpdateContactGridWidgetService");
    }

    public void updateContactGridWidget(Context aContext, AppWidgetManager appWidgetManager, boolean aLocaleChanged, int action) {
        int[] lIds = appWidgetManager.getAppWidgetIds(new ComponentName(aContext, ContactWidgetProvider.class));
        Intent addGroupIntent = new Intent(aContext, DummyActivityForFavWidget.class);
        addGroupIntent.addFlags(268468224);
        addGroupIntent.setAction("com.huawei.android.ADDFROM_GROUP");
        PendingIntent pendingIntentGroup = PendingIntent.getActivity(aContext, 0, addGroupIntent, 134217728);
        Intent intent = new Intent(aContext, DummyActivityForFavWidget.class);
        intent.addFlags(268468224);
        intent.setAction("com.huawei.android.CLICK_DONE");
        PendingIntent pendingIntentSave = PendingIntent.getActivity(aContext, 0, intent, 134217728);
        Intent editFavouriteIntent = new Intent(aContext, DummyActivityForFavWidget.class);
        editFavouriteIntent.addFlags(268468224);
        editFavouriteIntent.setAction("com.huawei.android.ADD_FAVOURITE");
        PendingIntent pendingIntentFav = PendingIntent.getActivity(aContext, 0, editFavouriteIntent, 134217728);
        intent = new Intent(aContext, DummyActivityForFavWidget.class);
        intent.addFlags(268468224);
        PendingIntent pendingIntentForMultiselect = PendingIntent.getActivity(aContext, 0, intent, 134217728);
        for (int appWidgetId : lIds) {
            Intent intent2 = new Intent(aContext, ContactWidgetService.class);
            intent2.putExtra("appWidgetId", appWidgetId);
            intent2.putExtra("localeChanged", aLocaleChanged);
            Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int category = -5;
            String key = ReflelctionConstant.getAppWidgetHostCategory();
            if (!TextUtils.isEmpty(key)) {
                category = myOptions.getInt(key, -5);
            }
            boolean isKeyguard = category == ReflelctionConstant.getWidgetCategoryKeyguard();
            intent2.putExtra("isKeyguard", isKeyguard);
            intent2.setData(Uri.parse(intent2.toUri(1)));
            RemoteViews remoteViews = new RemoteViews(aContext.getPackageName(), isKeyguard ? R.layout.contact_widget_home_lock : R.layout.contact_widget_home);
            remoteViews.setOnClickPendingIntent(R.id.btnGroup, pendingIntentGroup);
            if (action == 0) {
                remoteViews.setViewVisibility(R.id.btnEdit, 0);
                remoteViews.setViewVisibility(R.id.btnDone, 8);
            } else {
                remoteViews.setOnClickPendingIntent(R.id.btnDone, pendingIntentSave);
            }
            if (action == 1) {
                remoteViews.setViewVisibility(R.id.btnDone, 0);
                remoteViews.setViewVisibility(R.id.btnEdit, 8);
            } else {
                remoteViews.setOnClickPendingIntent(R.id.btnEdit, pendingIntentFav);
            }
            remoteViews.setViewVisibility(R.id.intial_view, 8);
            remoteViews.setPendingIntentTemplate(R.id.GridView01, pendingIntentForMultiselect);
            remoteViews.setRemoteAdapter(R.id.GridView01, intent2);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.GridView01);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    protected void doWakefulWork(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String lAction = intent.getAction();
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "Service called with action :" + lAction);
            }
            boolean lLocaleChanged = intent.getBooleanExtra("localeChanged", false);
            Context lContext = getApplicationContext();
            AppWidgetManager lWidgetManager = AppWidgetManager.getInstance(lContext);
            if ("com.huawei.android.ADD_FAVOURITE".equals(lAction)) {
                updateContactGridWidget(lContext, lWidgetManager, lLocaleChanged, 1);
            } else if ("com.huawei.android.CLICK_DONE".equals(lAction)) {
                updateContactGridWidget(lContext, lWidgetManager, lLocaleChanged, 0);
            } else if ("com.android.contacts.favorites.updated".equals(lAction)) {
                SystemClock.sleep(500);
                updateContactGridWidget(lContext, lWidgetManager, lLocaleChanged, -1);
            }
        }
    }
}
