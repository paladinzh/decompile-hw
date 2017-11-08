package com.huawei.keyguard.cover.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews.RemoteView;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.util.HwLog;
import java.util.TimeZone;

@RemoteView
public class AnalogClockView extends View {
    private float m24Hour;
    private boolean mAttached;
    private boolean mChanged;
    private Drawable mDial;
    private int mDialHeight;
    private int mDialWidth;
    private Handler mHandler;
    private float mHour;
    private Drawable mHourHand;
    private Drawable mHourHandShadow;
    private final BroadcastReceiver mIntentReceiver;
    private Drawable mMinuteHand;
    private Drawable mMinuteHandShadow;
    private float mMinutes;
    private Paint mPaint;
    private int mPeriod;
    private float mSecond;
    private Drawable mSecondHand;
    private Drawable mSecondHandShadow;
    private Time mTime;
    private Message message;
    private Rect rect;
    private float scale;
    private boolean scaled;
    private int x;
    private int y;

    public void sendDelayedMessage() {
        if (this.message != null) {
            this.message = null;
        }
        this.message = new Message();
        this.message.what = 1;
        this.mHandler.sendMessageDelayed(this.message, (long) this.mPeriod);
    }

    public AnalogClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.scale = 1.0f;
        this.mPeriod = 1000;
        this.x = 0;
        this.y = 0;
        this.scaled = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        AnalogClockView.this.invalidate();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    HwLog.w("AnalogClockView", "onReceive, the intent is null!");
                    return;
                }
                String recvAction = intent.getAction();
                if (recvAction != null && "android.intent.action.TIMEZONE_CHANGED".equals(recvAction)) {
                    AnalogClockView.this.mTime = new Time(TimeZone.getTimeZone(intent.getStringExtra("time-zone")).getID());
                }
                AnalogClockView.this.onTimeChanged();
                AnalogClockView.this.invalidate();
            }
        };
        this.mHourHand = getResources().getDrawable(R$drawable.cover_clock_h);
        this.mMinuteHand = getResources().getDrawable(R$drawable.cover_clock_m);
        this.mSecondHand = getResources().getDrawable(R$drawable.cover_clock_s);
        this.mDial = getResources().getDrawable(R$drawable.cover_clock_bg);
        this.mHourHandShadow = getResources().getDrawable(R$drawable.cover_clock_h_shadow);
        this.mMinuteHandShadow = getResources().getDrawable(R$drawable.cover_clock_m_shadow);
        this.mSecondHandShadow = getResources().getDrawable(R$drawable.cover_clock_s_shadow);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-16777216);
        this.mPaint.setFakeBoldText(true);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mDialWidth = this.mDial.getIntrinsicWidth();
        this.mDialHeight = this.mDial.getIntrinsicHeight();
        this.rect = new Rect();
        this.mTime = new Time();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        }
        this.mTime = new Time();
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
        this.scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * this.scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * this.scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        this.x = availableWidth / 2;
        this.y = availableHeight / 2;
        if (availableWidth < this.mDialWidth || availableHeight < this.mDialHeight) {
            this.scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) this.mDialWidth), ((float) availableHeight) / ((float) this.mDialHeight));
        }
        this.rect.set(this.x - (this.mDialWidth / 2), this.y - (this.mDialHeight / 2), this.x + (this.mDialWidth / 2), this.y + (this.mDialHeight / 2));
        this.mChanged = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onTimeChanged();
        sendDelayedMessage();
        if (this.scaled) {
            canvas.save();
            canvas.scale(this.scale, this.scale, (float) this.x, (float) this.y);
        }
        if (this.mChanged) {
            this.mDial.setBounds(this.rect);
        }
        this.mDial.draw(canvas);
        canvas.save();
        canvas.restore();
        int pivotX = this.x;
        int pivotY = this.y;
        int pivotShadowY = this.y + 9;
        paint(canvas, this.mChanged, this.mHourHandShadow, 30.0f, this.mHour, true, pivotX, pivotShadowY, 0, 1342177280);
        paint(canvas, this.mChanged, this.mHourHand, 30.0f, this.mHour, true, pivotX, pivotY, 0, 1342177280);
        paint(canvas, this.mChanged, this.mMinuteHandShadow, 6.0f, this.mMinutes, true, pivotX, pivotShadowY, 0, 1342177280);
        paint(canvas, this.mChanged, this.mMinuteHand, 6.0f, this.mMinutes, true, pivotX, pivotY, 0, 1342177280);
        paint(canvas, this.mChanged, this.mSecondHandShadow, 6.0f, this.mSecond, true, pivotX, pivotShadowY, 0, 1342177280);
        paint(canvas, this.mChanged, this.mSecondHand, 6.0f, this.mSecond, true, pivotX, pivotY, 0, 1342177280);
        if (this.scaled) {
            canvas.restore();
        }
        if (this.mChanged) {
            this.mChanged = false;
        }
    }

    private void onTimeChanged() {
        this.mPeriod = 1000 - ((int) (System.currentTimeMillis() % 1000));
        this.mTime.setToNow();
        this.mSecond = (float) this.mTime.second;
        this.mMinutes = ((float) this.mTime.minute) + (this.mSecond / 60.0f);
        this.m24Hour = ((float) this.mTime.hour) + (this.mMinutes / 60.0f);
        this.mHour = getHour();
        this.mChanged = true;
        updateContentDescription(this.mTime);
    }

    private float getHour() {
        if (this.m24Hour >= 12.0f) {
            return this.m24Hour - 12.0f;
        }
        return this.m24Hour;
    }

    private void updateContentDescription(Time time) {
        setContentDescription(DateUtils.formatDateTime(this.mContext, time.toMillis(false), 129));
    }

    private void paint(Canvas canvas, boolean changed, Drawable hand, float degree, float number, boolean rotate, int pivotX, int pivotY, int shadowWidth, int shadowColor) {
        if (changed) {
            int w = hand.getIntrinsicWidth();
            int h = hand.getIntrinsicHeight();
            hand.setBounds(new Rect(pivotX - (w / 2), pivotY - (h / 2), (w / 2) + pivotX, (h / 2) + pivotY));
        }
        canvas.save();
        canvas.translate(0.0f, (float) shadowWidth);
        if (rotate) {
            canvas.rotate(number * degree, (float) pivotX, (float) pivotY);
        }
        hand.setColorFilter(shadowColor, Mode.SRC_IN);
        hand.draw(canvas);
        hand.clearColorFilter();
        canvas.restore();
        canvas.save();
        if (rotate) {
            canvas.rotate(number * degree, (float) pivotX, (float) pivotY);
        }
        hand.draw(canvas);
        canvas.restore();
    }
}
