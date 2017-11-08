package com.android.deskclock.alarmclock;

import android.content.Context;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;

public class CalendarPreference extends Preference {
    private Callback mCallBack = null;
    private boolean mClear = false;
    TimePicker timepick = null;

    public interface Callback {
        int getHour();

        int getMinute();

        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    public CalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarPreference(Context context) {
        super(context, null);
    }

    protected View onCreateView(ViewGroup parent) {
        View layout = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.prefer_calendar, parent, false);
        this.timepick = (TimePicker) layout.findViewById(R.id.alarm_timePicker);
        this.timepick.setCurrentHour(Integer.valueOf(this.mCallBack.getHour()));
        this.timepick.setCurrentMinute(Integer.valueOf(this.mCallBack.getMinute()));
        return layout;
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.timepick = (TimePicker) view.findViewById(R.id.alarm_timePicker);
        if (this.timepick != null) {
            this.timepick.setIs24HourView(Boolean.valueOf(DateFormat.is24HourFormat(DeskClockApplication.getDeskClockApplication())));
            this.timepick.setOnTimeChangedListener(new OnTimeChangedListener() {
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    if (CalendarPreference.this.mCallBack != null) {
                        CalendarPreference.this.mCallBack.onTimeChanged(view, hourOfDay, minute);
                    }
                }
            });
            this.timepick.setCurrentHour(Integer.valueOf(this.mCallBack.getHour()));
            this.timepick.setCurrentMinute(Integer.valueOf(this.mCallBack.getMinute()));
            if (this.mClear) {
                this.timepick.clearFocus();
                this.mClear = false;
            }
        }
    }

    public void setCallback(Callback callBack) {
        this.mCallBack = callBack;
    }

    public void clearFocus() {
        this.mClear = true;
    }

    public void updateCalendarMode() {
        if (this.timepick != null) {
            this.timepick.setIs24HourView(Boolean.valueOf(DateFormat.is24HourFormat(DeskClockApplication.getDeskClockApplication())));
        }
    }
}
