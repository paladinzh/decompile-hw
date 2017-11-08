package com.android.deskclock.alarmclock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;

public class DayOfWeekLayout extends RelativeLayout {
    String dot;
    TextView mAmText;
    TextView mDayWeekText;

    public DayOfWeekLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAmText = (TextView) findViewById(R.id.am_digital);
        this.mDayWeekText = (TextView) findViewById(R.id.dayWeek_digital);
        this.dot = getContext().getString(R.string.day_concat);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        insideMeasure();
    }

    private void insideMeasure() {
        int width = getMeasuredWidth();
        if (width > 0 && this.mAmText != null && this.mDayWeekText != null) {
            float amWidth = getTextWidth(this.mAmText);
            float dayWeekWidth = getTextWidth(this.mDayWeekText);
            if (((int) (amWidth + dayWeekWidth)) <= width) {
                this.mAmText.setWidth((int) amWidth);
                this.mDayWeekText.setWidth((int) dayWeekWidth);
                return;
            }
            if (amWidth <= ((float) width) * 0.33333334f) {
                dayWeekWidth = ((float) width) - amWidth;
            } else if (dayWeekWidth <= ((float) width) * 0.6666666f) {
                amWidth = ((float) width) - dayWeekWidth;
            } else {
                dayWeekWidth = ((float) width) * 0.6666666f;
                amWidth = ((float) width) - dayWeekWidth;
            }
            this.mAmText.setWidth((int) amWidth);
            this.mDayWeekText.setWidth((int) dayWeekWidth);
        }
    }

    public float getTextWidth(TextView textView) {
        if (textView == null) {
            return 0.0f;
        }
        return textView.getPaint().measureText(textView.getText().toString());
    }

    public void updateText(Alarm alarm) {
        if (alarm != null) {
            String str;
            String label = alarm.getLabelOrDefault(DeskClockApplication.getDeskClockApplication()) + this.dot;
            if (alarm.daysOfWeekType != 3) {
                str = AlarmSetDialogManager.getRepeatTypeOfChina(DeskClockApplication.getDeskClockApplication(), alarm.daysOfWeekType);
            } else {
                str = AlarmSetDialogManager.toGogaleString(DeskClockApplication.getDeskClockApplication(), alarm.daysOfWeek, false);
            }
            if (this.mAmText != null) {
                this.mAmText.setTextDirection(5);
                this.mAmText.setText(label);
            }
            if (this.mDayWeekText != null) {
                this.mDayWeekText.setText(str);
            }
            insideMeasure();
        }
    }
}
