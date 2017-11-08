package com.android.alarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews.RemoteView;
import com.android.deskclock.R$styleable;
import com.android.util.ClockReporter;
import com.android.util.HwLog;

@RemoteView
public class DoubleClockView extends WorldAnalogClock {
    public static final int INDEX_FIRST_CITY = 0;
    public static final int INDEX_SECOND_CITY = 1;
    public static final String METHOD_TIMEZONE = "setmTimeZone";
    public static final String METHOD_WIDGETID = "setmWidgetId";
    private static final String TAG = "DoubleClockView";
    private int mIndex;
    private int mWidgetId;

    public DoubleClockView(Context context) {
        this(context, null);
    }

    public DoubleClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.WorldClock);
        try {
            this.mIndex = a.getInt(0, 0);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "DoubleClockView getInt fail", e);
        } finally {
            a.recycle();
        }
        initOnClick();
    }

    private void initOnClick() {
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                String action = null;
                if (DoubleClockView.this.mIndex == 0) {
                    action = "com.android.desk.change_first_city";
                } else if (DoubleClockView.this.mIndex == 1) {
                    action = "com.android.desk.change_second_city";
                }
                bundle.putInt("widget_id", DoubleClockView.this.mWidgetId);
                MiddleActivity.startMiddleActivity(DoubleClockView.this.mContext, action, bundle);
                ClockReporter.reportEventMessage(DoubleClockView.this.mContext, 70, "");
            }
        });
    }

    @RemotableViewMethod
    public void setmTimeZone(String mTimeZone) {
        this.mTimeZone = mTimeZone;
        onTimeChanged();
        invalidate();
    }

    @RemotableViewMethod
    public void setmWidgetId(int mWidgetId) {
        this.mWidgetId = mWidgetId;
    }
}
