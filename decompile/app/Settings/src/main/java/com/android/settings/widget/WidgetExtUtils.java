package com.android.settings.widget;

import android.content.Context;
import android.widget.RemoteViews;

public class WidgetExtUtils {
    public static long getValueofDataUsage(long value, int followAxis, ChartSweepView validAfterDynamic) {
        long retValue = value;
        if (validAfterDynamic != null) {
            return new WidgetPlatformImp().getValueofDataUsage(value, followAxis, validAfterDynamic);
        }
        return retValue;
    }

    public static RemoteViews getView(Context context) {
        if (context != null) {
            return new WidgetPlatformImp().getView(context);
        }
        return null;
    }
}
