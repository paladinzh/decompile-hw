package com.android.deskclock.timer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.util.FormatTime;
import com.android.util.Utils;

public class PickedTime extends RelativeLayout {
    private CustomTextView mPickedHour;
    private TextView mPickedHourColon;
    private CustomTextView mPickedMinute;
    private TextView mPickedMinuteColon;
    private CustomTextView mPickedSecond;

    public PickedTime(Context context) {
        this(context, null);
    }

    public PickedTime(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickedTime(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.timer_picked_time, this, true);
        this.mPickedHour = (CustomTextView) findViewById(R.id.picked_hour);
        this.mPickedMinute = (CustomTextView) findViewById(R.id.picked_minute);
        this.mPickedSecond = (CustomTextView) findViewById(R.id.picked_second);
        this.mPickedHourColon = (TextView) findViewById(R.id.picked_hour_colon);
        this.mPickedMinuteColon = (TextView) findViewById(R.id.picked_minute_colon);
        String[] timePoint = Utils.getTimePoint(context);
        this.mPickedHourColon.setText(timePoint[0]);
        this.mPickedMinuteColon.setText(timePoint[1]);
        this.mPickedHour.setText(FormatTime.formatNumber(0));
        this.mPickedMinute.setText(FormatTime.formatNumber(0));
        this.mPickedSecond.setText(FormatTime.formatNumber(0));
    }

    public void setHourValue(int hour) {
        this.mPickedHour.setText(FormatTime.formatNumber(hour));
    }

    public void setMinuteValue(int min) {
        this.mPickedMinute.setText(FormatTime.formatNumber(min));
    }

    public void setSecondValue(int sec) {
        this.mPickedSecond.setText(FormatTime.formatNumber(sec));
    }
}
