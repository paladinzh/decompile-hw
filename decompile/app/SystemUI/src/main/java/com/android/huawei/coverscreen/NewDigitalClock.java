package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.R$id;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

public class NewDigitalClock extends RelativeLayout {
    private TextView mAmPm;
    private TextView mDateView;
    protected final Handler mHandler;
    private TextView mHoursTime;
    private TextView mMinutesTime;

    public NewDigitalClock(Context context) {
        this(context, null);
    }

    public NewDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHandler = new Handler();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View v = findViewById(R$id.cover_hours_time_textview);
        if (v instanceof TextView) {
            this.mHoursTime = (TextView) v;
        }
        v = findViewById(R$id.cover_minutes_time_textview);
        if (v instanceof TextView) {
            this.mMinutesTime = (TextView) v;
        }
        v = findViewById(R$id.cover_am_pm_textview);
        if (v instanceof TextView) {
            this.mAmPm = (TextView) v;
        }
        v = findViewById(R$id.cover_date_time_textview);
        if (v instanceof TextView) {
            this.mDateView = (TextView) v;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i("NewDigitalClock", "onAttachedToWindow: " + hashCode());
        onTimeChanged();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i("NewDigitalClock", "onDetachedFromWindow: " + hashCode());
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected void onTimeChanged() {
        if (this.mHoursTime != null) {
            this.mHoursTime.setText(getHourTime());
        }
        if (this.mMinutesTime != null) {
            this.mMinutesTime.setText(getMinutesTime());
        }
        if (this.mAmPm != null) {
            this.mAmPm.setText(getAmPm());
        }
        if (this.mDateView != null) {
            this.mDateView.setText(getDateTime());
        }
    }

    private String getAmPm() {
        if (DateFormat.is24HourFormat(this.mContext)) {
            return null;
        }
        String ampmString;
        String[] ampm = new DateFormatSymbols().getAmPmStrings();
        String mAmString = ampm[0];
        String mPmString = ampm[1];
        if (Calendar.getInstance().get(9) == 0) {
            ampmString = mAmString;
        } else {
            ampmString = mPmString;
        }
        return ampmString;
    }

    private String getHourTime() {
        long sysTime = System.currentTimeMillis();
        if (DateFormat.is24HourFormat(this.mContext)) {
            return DateFormat.format("HH", sysTime).toString();
        }
        return DateFormat.format("hh", sysTime).toString();
    }

    private String getMinutesTime() {
        return DateFormat.format("mm", System.currentTimeMillis()).toString();
    }

    private String getDateTime() {
        return DateUtils.formatDateTime(getContext(), new Date().getTime(), 98330);
    }
}
