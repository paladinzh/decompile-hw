package com.android.alarmclock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import com.android.deskclock.R;

@RemoteView
public class DigitalClockWidgetViewRight extends DigitalClockWidgetViewLeft {
    public DigitalClockWidgetViewRight(Context context) {
        this(context, null);
    }

    public DigitalClockWidgetViewRight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DigitalClockWidgetViewRight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getDateTimeId() {
        return R.id.digital_date_timeright;
    }
}
