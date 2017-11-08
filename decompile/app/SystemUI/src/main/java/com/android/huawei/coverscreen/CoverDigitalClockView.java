package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.keyguard.R$drawable;

public class CoverDigitalClockView extends AbstractClockView {
    private static final int BG_DRAWABLE_ID = R$drawable.cover_digital_bg;
    private static final int[] FG_DRAWABLE_ID_ARRAY = new int[]{R$drawable.cover_digital_animate_point};
    private Drawable mAnimatePointDrawable;
    private int mAnimatePointSize;
    private float mMillisDegree;
    private Paint mPaint;

    public CoverDigitalClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverDigitalClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMillisDegree = 0.0f;
        this.mAnimatePointSize = 36;
        init(0, FG_DRAWABLE_ID_ARRAY);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-1);
        this.mPaint.setStrokeWidth(2.0f);
    }

    protected void updateRotateDegree() {
        this.mMillisDegree = ((float) (System.currentTimeMillis() % 60000)) * 0.006f;
    }

    protected long getMessageDelayMills() {
        return 17;
    }

    protected void setFgDrawable(int[] fgDrawableIdArray) {
        this.mAnimatePointDrawable = getDrawableById(fgDrawableIdArray[0]);
        this.mForgroundWidth = this.mAnimatePointDrawable.getIntrinsicWidth();
        this.mForgroundHeight = this.mAnimatePointDrawable.getIntrinsicHeight();
    }

    protected void setBgDrawable(int bgDrawableId) {
        this.mBgWidth = 500;
        this.mBgHeight = 500;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int radius = 230 - this.mAnimatePointSize;
        this.mForgroundBoundsRect.set(this.mPivotX - (this.mForgroundWidth / 2), this.mPivotY - radius, this.mPivotX + (this.mForgroundWidth / 2), (this.mPivotY + this.mForgroundHeight) - radius);
    }

    protected void paintBackground(Canvas canvas) {
    }

    protected void paintForground(Canvas canvas) {
        updateRotateDegree();
        drawAnimatePoint(canvas);
        drawDialPlate(canvas);
    }

    private void drawAnimatePoint(Canvas canvas) {
        canvas.save();
        this.mAnimatePointDrawable.setBounds(this.mForgroundBoundsRect);
        canvas.rotate(this.mMillisDegree, (float) this.mPivotX, (float) this.mPivotY);
        this.mAnimatePointDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawDialPlate(Canvas canvas) {
        canvas.save();
        long currentMillis = System.currentTimeMillis() % 60000;
        canvas.rotate((float) ((currentMillis / 500) * 3), (float) this.mPivotX, (float) this.mPivotY);
        float lengthChange = (((float) (currentMillis % 500)) * 2.0f) / 500.0f;
        for (int idx = -60; idx != 60; idx++) {
            double curRadian = Math.toRadians((double) (idx * 3));
            double sinRadian = Math.sin(curRadian);
            double cosRadian = Math.cos(curRadian);
            int startX = ((int) (196.0d * sinRadian)) + this.mPivotX;
            int startY = ((int) (-196.0d * cosRadian)) + this.mPivotY;
            float waveChange = 0.0f;
            if (Math.abs(idx) < 8) {
                float raiseBaseHeight = 16.0f * (1.0f - (((float) Math.abs(idx)) / 8.0f));
                if (idx <= 0) {
                    waveChange = raiseBaseHeight - lengthChange;
                } else {
                    waveChange = raiseBaseHeight + lengthChange;
                }
            }
            Canvas canvas2 = canvas;
            canvas2.drawLine((float) startX, (float) startY, (float) (((int) (((double) (230.0f + waveChange)) * sinRadian)) + this.mPivotX), (float) (((int) (((double) (-(230.0f + waveChange))) * cosRadian)) + this.mPivotY), this.mPaint);
        }
        canvas.restore();
    }
}
