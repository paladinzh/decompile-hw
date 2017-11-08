package com.android.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews.RemoteView;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.util.Utils;
import java.util.Calendar;
import java.util.TimeZone;

@RemoteView
public class WorldAnalogClock extends View {
    public static final int BIG_DIAL_MODE = 0;
    public static final float DEGREE_ONE_HOUR = 30.0f;
    public static final float DEGREE_ONE_MINUTES = 6.0f;
    public static final float DEGREE_ONE_SECOND = 6.0f;
    private static final int REDRAW = 1;
    private static final int SECOND_SHADOWCOLOR = 637534208;
    private static final int SHADOWCOLOR = 1342177280;
    public static final int SMALL_DIAL_MODE = 1;
    private Boolean isAlert;
    private boolean mAttached;
    private Calendar mCalendar;
    private boolean mChanged;
    private Drawable mDial;
    private final int mDialHeight;
    private final int mDialWidth;
    private int mH;
    private final Handler mHandler;
    private float mHour;
    private Drawable mHourHand;
    private int mHourTemp;
    private final BroadcastReceiver mIntentReceiver;
    private Drawable mMinuteHand;
    private float mMinutes;
    private int mMode;
    private float mSecond;
    private Drawable mSecondHand;
    private int mShadowY;
    private Calendar mTime;
    public String mTimeZone;
    private int mW;
    private boolean mWhite;
    private float scale;

    public String getmTimeZone() {
        return this.mTimeZone;
    }

    public void setmTimeZone(String mTimeZone) {
        this.isAlert = Boolean.valueOf(false);
        if (mTimeZone == null) {
            this.mTimeZone = TimeZone.getDefault().getID();
        }
        this.mTimeZone = mTimeZone;
        onTimeChanged();
        invalidate();
    }

    public WorldAnalogClock(Context context) {
        this(context, null);
    }

    public WorldAnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldAnalogClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.scale = 1.0f;
        this.mTimeZone = TimeZone.getDefault().getID();
        this.isAlert = Boolean.valueOf(true);
        this.mMode = 0;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WorldAnalogClock.this.invalidate();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mHourTemp = -1;
        this.mWhite = false;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (WorldAnalogClock.this.mMode == 0 && "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                        String tz = intent.getStringExtra("time-zone");
                        if (tz != null) {
                            WorldAnalogClock.this.mTimeZone = tz;
                        } else {
                            return;
                        }
                    }
                    WorldAnalogClock.this.onTimeChanged();
                    WorldAnalogClock.this.invalidate();
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.AnalogClock, defStyle, 0);
        this.mMode = a.getInt(0, 0);
        a.recycle();
        if (this.isAlert.booleanValue()) {
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_light);
        } else {
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_light);
        }
        this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(this.mTimeZone));
        this.mDialWidth = this.mDial.getIntrinsicWidth();
        this.mDialHeight = this.mDial.getIntrinsicHeight();
        this.mW = this.mDial.getIntrinsicWidth();
        this.mH = this.mDial.getIntrinsicHeight();
        this.mTime = Calendar.getInstance();
    }

    public void sendDelayedMessage(long delayMillis) {
        Message message = new Message();
        message.what = 1;
        this.mHandler.sendMessageDelayed(message, delayMillis);
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
        this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(this.mTimeZone));
        onTimeChanged();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            this.mAttached = false;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mDialWidth) {
            hScale = ((float) widthSize) / ((float) this.mDialWidth);
        }
        if (heightMode != 0 && heightSize < this.mDialHeight) {
            vScale = ((float) heightSize) / ((float) this.mDialHeight);
        }
        float scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        sendRefreshMsg();
        boolean changed = this.mChanged;
        if (changed) {
            this.mChanged = false;
        }
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        Drawable dial = this.mDial;
        boolean scaled = false;
        if (availableWidth < this.mW || availableHeight < this.mH) {
            scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) this.mW), ((float) availableHeight) / ((float) this.mH));
            canvas.save();
            canvas.scale(this.scale, this.scale, (float) x, (float) y);
        }
        if (changed) {
            dial.setBounds(x - (this.mW / 2), y - (this.mH / 2), (this.mW / 2) + x, (this.mH / 2) + y);
        }
        dial.draw(canvas);
        int pivotX = x;
        int pivotY = y;
        paint(canvas, changed, this.mHourHand, DEGREE_ONE_HOUR, this.mHour, true, x, y, this.mShadowY, SHADOWCOLOR);
        paint(canvas, changed, this.mMinuteHand, 6.0f, this.mMinutes, true, x, y, this.mShadowY, SHADOWCOLOR);
        if (this.isAlert.booleanValue()) {
            paint(canvas, changed, this.mSecondHand, 6.0f, this.mSecond, true, x, y, this.mShadowY, SECOND_SHADOWCOLOR);
        }
        if (scaled) {
            canvas.restore();
        }
    }

    private void sendRefreshMsg() {
        this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(this.mTimeZone));
        int hour = this.mCalendar.get(11);
        int minute = this.mCalendar.get(12);
        int second = this.mCalendar.get(13);
        if (this.isAlert.booleanValue()) {
            sendDelayedMessage(1000);
        } else {
            sendDelayedMessage(60000);
        }
        this.mSecond = (float) second;
        this.mMinutes = ((float) minute) + (((float) second) / 60.0f);
        this.mHour = ((float) hour) + (this.mMinutes / 60.0f);
    }

    private void deleteThreeDrawable() {
        this.mSecondHand = null;
        deleteTwoDrawable();
    }

    private void deleteTwoDrawable() {
        this.mHourHand = null;
        this.mMinuteHand = null;
        this.mDial = null;
    }

    public void onTimeChanged() {
        this.mCalendar = Calendar.getInstance(TimeZone.getTimeZone(this.mTimeZone));
        int hour = this.mCalendar.get(11);
        int minute = this.mCalendar.get(12);
        int second = this.mCalendar.get(13);
        this.mTime.set(11, hour);
        this.mTime.set(12, minute);
        this.mTime.set(13, second);
        Utils.updateContentDescription(this, this.mTime);
        if (this.isAlert.booleanValue()) {
            onIsAlertTrueAction(hour);
        } else if (hour >= 18 || hour < 6) {
            if (-1 == this.mHourTemp) {
                this.mHourTemp = hour;
                this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_hourhand_black);
                this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_mintehand_black);
                this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_black);
                this.mWhite = false;
                this.mW = this.mDial.getIntrinsicWidth();
                this.mH = this.mDial.getIntrinsicHeight();
            } else if (this.mWhite) {
                deleteTwoDrawable();
                this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_hourhand_black);
                this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_mintehand_black);
                this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_black);
                this.mWhite = false;
                this.mW = this.mDial.getIntrinsicWidth();
                this.mH = this.mDial.getIntrinsicHeight();
            }
        } else if (-1 == this.mHourTemp) {
            this.mHourTemp = hour;
            this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_hourhand_light);
            this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_mintehand_light);
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_light);
            this.mWhite = true;
            this.mW = this.mDial.getIntrinsicWidth();
            this.mH = this.mDial.getIntrinsicHeight();
        } else if (!this.mWhite) {
            deleteTwoDrawable();
            this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_hourhand_light);
            this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_mintehand_light);
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_littledial_light);
            this.mWhite = true;
            this.mW = this.mDial.getIntrinsicWidth();
            this.mH = this.mDial.getIntrinsicHeight();
        }
        this.mSecond = (float) second;
        this.mMinutes = ((float) minute) + (((float) second) / 60.0f);
        this.mHour = ((float) hour) + (this.mMinutes / 60.0f);
        this.mChanged = true;
    }

    private void onIsAlertTrueAction(int hour) {
        if (hour >= 18 || hour < 6) {
            if (-1 == this.mHourTemp) {
                this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_hourhand_black);
                this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_minutehand_black);
                this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_black);
                this.mSecondHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_secondhand_black);
                this.mHourTemp = hour;
                this.mWhite = false;
                this.mW = this.mDial.getIntrinsicWidth();
                this.mH = this.mDial.getIntrinsicHeight();
            } else if (this.mWhite) {
                deleteThreeDrawable();
                this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_hourhand_black);
                this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_minutehand_black);
                this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_black);
                this.mSecondHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_secondhand_black);
                this.mWhite = false;
                this.mW = this.mDial.getIntrinsicWidth();
                this.mH = this.mDial.getIntrinsicHeight();
            }
        } else if (-1 == this.mHourTemp) {
            this.mHourTemp = hour;
            this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_hourhand_light);
            this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_minutehand_light);
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_light);
            this.mSecondHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_secondhand_light);
            this.mWhite = true;
            this.mW = this.mDial.getIntrinsicWidth();
            this.mH = this.mDial.getIntrinsicHeight();
        } else if (!this.mWhite) {
            deleteThreeDrawable();
            this.mHourHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_hourhand_light);
            this.mMinuteHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_minutehand_light);
            this.mDial = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_light);
            this.mSecondHand = getResources().getDrawable(R.drawable.img_clock_worldclock_dial_secondhand_light);
            this.mWhite = true;
            this.mW = this.mDial.getIntrinsicWidth();
            this.mH = this.mDial.getIntrinsicHeight();
        }
    }

    private void paint(Canvas canvas, boolean changed, Drawable hand, float degree, float number, boolean rotate, int pivotX, int pivotY, int shadowWidth, int shadowColor) {
        if (changed) {
            int w = hand.getIntrinsicWidth();
            int h = hand.getIntrinsicHeight();
            hand.setBounds(new Rect(pivotX - (w / 2), pivotY - (h / 2), (w / 2) + pivotX, (h / 2) + pivotY));
        }
        canvas.save();
        if (rotate) {
            canvas.rotate(number * degree, (float) pivotX, (float) pivotY);
        }
        hand.draw(canvas);
        canvas.restore();
    }
}
