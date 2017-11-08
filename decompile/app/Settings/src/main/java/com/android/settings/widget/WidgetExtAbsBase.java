package com.android.settings.widget;

import android.content.Context;
import android.widget.RemoteViews;

abstract class WidgetExtAbsBase {
    WidgetExtAbsBase() {
    }

    public long getValueofDataUsage(long value, int followAxis, ChartSweepView validAfterDynamic) {
        return 0;
    }

    public RemoteViews getView(Context context) {
        return null;
    }
}
