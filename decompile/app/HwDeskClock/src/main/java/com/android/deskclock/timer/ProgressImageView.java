package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;

public class ProgressImageView extends View {
    private Drawable drawable;
    private boolean isReset;
    private Context mContext;
    private Paint mPaint;
    private float paintWidth;
    private RectF rectF;
    private float sweepAngle;
    private float width;

    public ProgressImageView(Context context) {
        this(context, null);
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public void setAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
    }

    private void initPaint() {
        if (DeskClockApplication.isBtvPadDevice()) {
            this.paintWidth = dp2Px(16.0f);
            this.rectF = new RectF(20.0f, 20.0f, this.width - 20.0f, this.width - 20.0f);
        } else {
            this.paintWidth = dp2Px(12.0f);
            this.rectF = new RectF(this.paintWidth / 2.0f, this.paintWidth / 2.0f, this.width - (this.paintWidth / 2.0f), this.width - (this.paintWidth / 2.0f));
        }
        this.mPaint = new Paint();
        this.mPaint.setColor(this.mContext.getResources().getColor(R.color.transparency_100_white));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.paintWidth);
        this.mPaint.setStyle(Style.STROKE);
        this.drawable = getResources().getDrawable(R.drawable.img_clock_timer_upperdial_processing);
    }

    protected void onMeasure(int arg0, int arg1) {
        super.onMeasure(arg0, arg1);
        this.width = (float) getMeasuredHeight();
        if (this.mPaint == null) {
            initPaint();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isReset) {
            canvas.drawARGB(0, 0, 0, 0);
            return;
        }
        canvas.drawArc(this.rectF, -90.0f, -this.sweepAngle, false, this.mPaint);
        int x = (this.mRight - this.mLeft) / 2;
        int y = (this.mBottom - this.mTop) / 2;
        int intrinsicWidth = this.drawable.getIntrinsicWidth();
        int intrinsicHeight = this.drawable.getIntrinsicHeight();
        this.drawable.setBounds(x - (intrinsicWidth / 2), y - (intrinsicHeight / 2), (intrinsicWidth / 2) + x, (intrinsicHeight / 2) + y);
        this.drawable.draw(canvas);
    }

    private float dp2Px(float dpValue) {
        return TypedValue.applyDimension(1, dpValue, this.mContext.getResources().getDisplayMetrics());
    }

    public void reset() {
        this.isReset = true;
        setAngle(0.0f);
        invalidate();
        this.isReset = false;
    }
}
