package com.android.contacts.gridwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import com.android.contacts.util.HwLog;

public class ContactWidgetProvider extends AppWidgetProvider {
    private boolean mLocaleChanged = false;

    public void onEnabled(Context aContext) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactWidgetProvider", "Widget onEnabled()");
        }
        super.onEnabled(aContext);
    }

    public void onUpdate(Context aContext, AppWidgetManager aAppWidgetManager, int[] aAppWidgetIds) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactWidgetProvider", "Widget onUpdate()");
        }
        if (WidgetStatus.getWidgetStatus().isEditMode()) {
            startUpdateService("com.huawei.android.ADD_FAVOURITE", aContext, this.mLocaleChanged);
        } else {
            startUpdateService("com.android.contacts.favorites.updated", aContext, this.mLocaleChanged);
        }
        super.onUpdate(aContext, aAppWidgetManager, aAppWidgetIds);
    }

    public void onReceive(Context aContext, Intent aIntent) {
        String lAction = aIntent.getAction();
        if (HwLog.HWDBG) {
            HwLog.d("ContactWidgetProvider", "onReceive() is called!, Action: " + lAction);
        }
        super.onReceive(aContext, aIntent);
        startUpdateService(lAction, aContext, aIntent.getBooleanExtra("localeChanged", false));
    }

    void startUpdateService(String aAction, Context aContext, boolean aLocaleChanged) {
        Intent intent = new Intent(aContext, UpdateContactGridWidgetService.class);
        intent.setAction(aAction);
        intent.putExtra("localeChanged", aLocaleChanged);
        aContext.startService(intent);
    }

    public void onDisabled(Context aContext) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactWidgetProvider", "Entered onDisabled, last instance of widget is removed");
        }
        super.onDisabled(aContext);
    }
}
