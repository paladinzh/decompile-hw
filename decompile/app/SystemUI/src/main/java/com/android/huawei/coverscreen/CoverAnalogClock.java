package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import java.util.Calendar;

public class CoverAnalogClock extends AbstractClockView {
    private float mHourDegree;
    private Drawable mHourHandDrawable;
    private int mMessageDelayMills;
    private float mMinuteDegree;
    private Drawable mMinuteHandDrawable;
    private float mSecondDegree;
    private Drawable mSecondHandDrawable;

    public CoverAnalogClock(Context context) {
        this(context, null);
    }

    public CoverAnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverAnalogClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSecondDegree = 0.0f;
        this.mMinuteDegree = 0.0f;
        this.mHourDegree = 0.0f;
        this.mMessageDelayMills = 1000;
        this.mSecondHandDrawable = null;
        this.mMinuteHandDrawable = null;
        this.mHourHandDrawable = null;
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
        paintSingle(canvas, this.mHourHandDrawable, this.mHourDegree);
        paintSingle(canvas, this.mMinuteHandDrawable, this.mMinuteDegree);
        paintSingle(canvas, this.mSecondHandDrawable, this.mSecondDegree);
        canvas.restore();
    }

    protected void updateRotateDegree() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(10);
        int minute = calendar.get(12);
        int second = calendar.get(13);
        float exactMinute = ((float) minute) + (((float) second) / 60.0f);
        this.mSecondDegree = ((float) second) * 6.0f;
        this.mMinuteDegree = 6.0f * exactMinute;
        this.mHourDegree = (((float) hour) + (exactMinute / 60.0f)) * 30.0f;
    }

    protected long getMessageDelayMills() {
        this.mMessageDelayMills = 1000 - ((int) (System.currentTimeMillis() % 1000));
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

    protected void setFgDrawable(int[] fgDrawableIdArray) {
        this.mHourHandDrawable = getDrawableById(fgDrawableIdArray[0]);
        this.mMinuteHandDrawable = getDrawableById(fgDrawableIdArray[1]);
        this.mSecondHandDrawable = getDrawableById(fgDrawableIdArray[2]);
        this.mForgroundWidth = this.mSecondHandDrawable.getIntrinsicWidth();
        this.mForgroundHeight = this.mSecondHandDrawable.getIntrinsicHeight();
    }

    protected void setBgDrawable(int bgDrawableId) {
        super.setBgDrawable(bgDrawableId);
        this.mBgWidth = this.mBackgroundWidth;
        this.mBgHeight = this.mBackgroundHeight;
    }
}
