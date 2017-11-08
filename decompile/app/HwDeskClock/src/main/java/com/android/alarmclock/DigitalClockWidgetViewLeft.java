package com.android.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.RelativeLayout;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.util.FormatTime;
import com.android.util.Utils;
import java.util.TimeZone;

@RemoteView
public class DigitalClockWidgetViewLeft extends RelativeLayout {
    public static final String METHOD_TIMEZONE = "setmTimeZone";
    public static final String METHOD_WIDGETID = "setmWidgetId";
    private static final float ROBOTO_FONT_BOTTOM_PADDING_RATIO = 0.2f;
    private static final float ROBOTO_FONT_TOP_PADDING_RATIO = 0.18f;
    public static final int ROBOTO_LIGHT_FONT = 0;
    public static final int ROBOTO_REGULAR_FONT = 1;
    public static final int SIMPLE_CLOCK_MODE = 0;
    public static final int WORLD_CLOCK_MODE = 1;
    private boolean mAttached;
    private Context mContext;
    private TextView mDateTime;
    private int mFontType;
    private final Handler mHandler;
    private final BroadcastReceiver mIntentReceiver;
    private int mMode;
    private String mTimeZone;

    public DigitalClockWidgetViewLeft(Context context) {
        this(context, null);
    }

    public DigitalClockWidgetViewLeft(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DigitalClockWidgetViewLeft(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMode = 0;
        this.mFontType = 0;
        this.mHandler = new Handler();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                DigitalClockWidgetViewLeft.this.mHandler.post(new Runnable() {
                    public void run() {
                        DigitalClockWidgetViewLeft.this.updateTime();
                    }
                });
            }
        };
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.DigitalClock, defStyle, 0);
        this.mMode = a.getInt(0, 0);
        this.mFontType = a.getInt(1, 0);
        a.recycle();
    }

    public int getDateTimeId() {
        return R.id.digital_date_time;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDateTime = (TextView) findViewById(getDateTimeId());
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_SET");
            switch (this.mMode) {
                case 0:
                    filter.addAction("android.intent.action.TIME_TICK");
                    filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                    break;
                case 1:
                    filter.addAction("android.intent.action.TIME_TICK");
                    break;
            }
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            updateTime();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mAttached) {
            this.mAttached = false;
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
    }

    @RemotableViewMethod
    public void setmTimeZone(String mTimeZone) {
        this.mTimeZone = mTimeZone;
        updateTime();
    }

    private void updateTime() {
        this.mDateTime.setText(FormatTime.getDateString(this.mContext, FormatTime.getCalendar(TimeZone.getTimeZone(this.mTimeZone)).getTimeInMillis()));
    }

    public void initTextView(TextView view) {
        if (view != null) {
            if (!Utils.isExistCustomFont()) {
                Typeface theTypeface;
                if (Utils.isChineseLanguage()) {
                    theTypeface = Utils.getDefaultFont();
                } else if (this.mFontType == 1) {
                    theTypeface = Utils.getmRobotoRegularTypeface();
                } else {
                    theTypeface = Utils.getmRobotoLightTypeface();
                }
                view.setTypeface(theTypeface);
            }
            view.setIncludeFontPadding(false);
            view.setPadding(this.mPaddingLeft, (int) (view.getTextSize() * -0.18f), this.mPaddingRight, (int) (view.getTextSize() * -0.2f));
        }
    }

    public void changeViewColor(int color, int aporfulltime) {
        if (aporfulltime == 1 && this.mDateTime != null) {
            this.mDateTime.setTextColor(color);
        }
    }

    public void setAllVisibility(int visibility) {
        if (this.mDateTime != null) {
            this.mDateTime.setVisibility(visibility);
        }
    }
}
