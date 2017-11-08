package com.huawei.keyguard.cover.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import java.util.Calendar;

public class CoverAnalogClockView extends AbstractAnalogClockView {
    private Calendar calendar;
    private float mHourDegree;
    private Drawable mHourHandDrawable;
    private Drawable mHourHandShadowDrawable;
    private int mMessageDelayMills;
    private float mMinuteDegree;
    private Drawable mMinuteHandDrawable;
    private Drawable mMinuteHandShadowDrawable;
    private float mSecondDegree;
    private Drawable mSecondHandDrawable;
    private Drawable mSecondHandShadowDrawable;

    public CoverAnalogClockView(Context context) {
        this(context, null);
    }

    public CoverAnalogClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverAnalogClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSecondDegree = 0.0f;
        this.mMinuteDegree = 0.0f;
        this.mHourDegree = 0.0f;
        this.mMessageDelayMills = 1000;
        this.mSecondHandDrawable = null;
        this.mMinuteHandDrawable = null;
        this.mHourHandDrawable = null;
        this.mSecondHandShadowDrawable = null;
        this.mMinuteHandShadowDrawable = null;
        this.mHourHandShadowDrawable = null;
        this.calendar = Calendar.getInstance();
    }

    protected void paintBackground(Canvas canvas) {
        if (this.mClockBackgroundDrawable != null) {
            canvas.save();
            if (this.mIsSizeChange) {
                this.mClockBackgroundDrawable.setBounds(this.mBackgroundBoundsRect);
            }
            this.mClockBackgroundDrawable.draw(canvas);
            canvas.restore();
        }
    }

    protected void paintForground(Canvas canvas) {
        updateRotateDegree();
        canvas.save();
        paintSingle(canvas, this.mHourHandShadowDrawable, this.mHourDegree);
        paintSingle(canvas, this.mHourHandDrawable, this.mHourDegree);
        paintSingle(canvas, this.mMinuteHandShadowDrawable, this.mMinuteDegree);
        paintSingle(canvas, this.mMinuteHandDrawable, this.mMinuteDegree);
        paintSingle(canvas, this.mSecondHandShadowDrawable, this.mSecondDegree);
        paintSingle(canvas, this.mSecondHandDrawable, this.mSecondDegree);
        canvas.restore();
    }

    protected void updateRotateDegree() {
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = this.calendar.get(10);
        int minute = this.calendar.get(12);
        int second = this.calendar.get(13);
        float exactMinute = ((float) minute) + (((float) second) / 60.0f);
        this.mSecondDegree = ((float) second) * 6.0f;
        this.mMinuteDegree = 6.0f * exactMinute;
        this.mHourDegree = (((float) hour) + (exactMinute / 60.0f)) * 30.0f;
    }

    protected long getMessageDelayMills() {
        this.mMessageDelayMills = 1000 - ((int) (this.calendar.getTimeInMillis() % 1000));
        return (long) this.mMessageDelayMills;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mForgroundBoundsRect.set(this.mPivotX - (this.mForgroundWidth / 2), this.mPivotY - (this.mForgroundHeight / 2), this.mPivotX + (this.mForgroundWidth / 2), this.mPivotY + (this.mForgroundHeight / 2));
    }

    private void paintSingle(Canvas canvas, Drawable drawable, float degree) {
        if (drawable != null) {
            canvas.save();
            drawable.setBounds(this.mForgroundBoundsRect);
            canvas.rotate(degree, (float) this.mPivotX, (float) this.mPivotY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    protected void setAnalogClockDrawable(Drawable[] drawableArray) {
        if (drawableArray.length != 0) {
            this.mHourHandShadowDrawable = drawableArray[0];
            this.mMinuteHandShadowDrawable = drawableArray[1];
            this.mSecondHandShadowDrawable = drawableArray[2];
            this.mHourHandDrawable = drawableArray[3];
            this.mMinuteHandDrawable = drawableArray[4];
            this.mSecondHandDrawable = drawableArray[5];
        }
        if (this.mSecondHandDrawable != null) {
            this.mForgroundWidth = this.mSecondHandDrawable.getIntrinsicWidth();
            this.mForgroundHeight = this.mSecondHandDrawable.getIntrinsicHeight();
        }
    }

    protected void setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        this.mBgWidth = this.mBackgroundWidth;
        this.mBgHeight = this.mBackgroundHeight;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
