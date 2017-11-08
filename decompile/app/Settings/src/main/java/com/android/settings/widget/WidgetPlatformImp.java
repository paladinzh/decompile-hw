package com.android.settings.widget;

import android.content.Context;
import android.provider.Settings.Secure;
import android.widget.RemoteViews;

public class WidgetPlatformImp extends WidgetExtAbsBase {
    public long getValueofDataUsage(long value, int followAxis, ChartSweepView validAfterDynamic) {
        long retValue = value;
        if (followAxis == 1 && validAfterDynamic != null && value < validAfterDynamic.getValue()) {
            retValue = validAfterDynamic.getValue();
        }
        if (retValue > 107374182400000L) {
            return 107374182400000L;
        }
        return retValue;
    }

    public RemoteViews getView(Context context) {
        if ("disable".equals(Secure.getString(context.getContentResolver(), "location_providers_allowed"))) {
            return new RemoteViews(context.getPackageName(), 2130969256);
        }
        return new RemoteViews(context.getPackageName(), 2130969255);
    }
}
