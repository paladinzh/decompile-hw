package com.android.deskclock.worldclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.deskclock.R;
import java.util.Calendar;
import java.util.TimeZone;

public class ClockDValueTextView extends TextView {
    private boolean mAttached;
    private String mDot;
    private final BroadcastReceiver mIntentReceiver;
    public String mTimeZone;

    public void setmTimeZone(String timeZone) {
        this.mTimeZone = timeZone;
        setText(getDayString(getContext(), timeZone));
    }

    public ClockDValueTextView(Context context) {
        this(context, null);
    }

    public ClockDValueTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockDValueTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTimeZone = TimeZone.getDefault().getID();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    ClockDValueTextView.this.setText(ClockDValueTextView.this.getDayString(context, ClockDValueTextView.this.mTimeZone));
                }
            }
        };
        this.mDot = getResources().getString(R.string.day_concat);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, filter);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
    }

    public SpannableStringBuilder getDayString(Context mContext, String timeZone) {
        String minSeq;
        String hourSeq;
        Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        int s = Calendar.getInstance().get(6);
        int m = itemCalendar.get(6);
        int day = assignDay(Calendar.getInstance().get(1), itemCalendar.get(1), s, m);
        int dValue = TimeZone.getTimeZone(itemCalendar.getTimeZone().getID()).getOffset(System.currentTimeMillis()) - TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID()).getOffset(System.currentTimeMillis());
        int hours = (int) Math.abs(((long) dValue) / 3600000);
        int minutes = (int) Math.abs((((long) dValue) / 60000) % 60);
        if (hours == 0 || minutes == 0) {
            if (minutes == 0) {
                minSeq = "";
            } else {
                minSeq = mContext.getResources().getQuantityString(R.plurals.minutes, minutes, new Object[]{Integer.valueOf(minutes)});
            }
            if (hours == 0) {
                hourSeq = "";
            } else {
                hourSeq = mContext.getResources().getQuantityString(R.plurals.hours, hours, new Object[]{Integer.valueOf(hours)});
            }
        } else {
            minSeq = mContext.getResources().getQuantityString(R.plurals.m, minutes, new Object[]{Integer.valueOf(minutes)});
            hourSeq = mContext.getResources().getQuantityString(R.plurals.h, hours, new Object[]{Integer.valueOf(hours)}) + " ";
        }
        boolean hasDST = TimeZone.getTimeZone(timeZone).inDaylightTime(itemCalendar.getTime());
        String dst = mContext.getString(R.string.world_digital_dst_tv);
        String text1 = mContext.getString(day) + this.mDot;
        String text2 = "";
        if (dValue == 0) {
            if (hasDST) {
                text2 = dst;
            } else {
                text1 = mContext.getString(day);
            }
        }
        if (dValue < 0) {
            text2 = mContext.getString(R.string.lateGMT, new Object[]{hourSeq, minSeq});
            if (hasDST) {
                text2 = text2 + this.mDot + dst;
            }
        }
        if (dValue > 0) {
            text2 = mContext.getString(R.string.fastGMT, new Object[]{hourSeq, minSeq});
            if (hasDST) {
                text2 = text2 + this.mDot + dst;
            }
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text1 + text2);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(mContext.getColor(R.color.transparency_100_color)), 0, text1.length(), 34);
        return spannableStringBuilder;
    }

    private int assignDay(int currentYear, int timezoneYear, int s, int m) {
        if (currentYear > timezoneYear) {
            return R.string.yesterday;
        }
        if (currentYear < timezoneYear) {
            return R.string.tomorrow;
        }
        if (s > m) {
            return R.string.yesterday;
        }
        if (s < m) {
            return R.string.tomorrow;
        }
        return R.string.today;
    }
}
