package com.huawei.rcs.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.huawei.cspcommon.MLog;

public class RcsRoundProgressBar extends ProgressBar {
    private Paint paint;
    private int progress = 0;
    private int textColor = -1;
    private boolean textIsDisplayable = false;
    private float textSize = 30.0f;
    private Typeface textTypeface = Typeface.DEFAULT;

    public RcsRoundProgressBar(Context context) {
        super(context);
        initProcessBar(context, null);
    }

    public RcsRoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProcessBar(context, attrs);
    }

    public RcsRoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initProcessBar(context, attrs);
    }

    public RcsRoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initProcessBar(context, attrs);
    }

    private void initProcessBar(Context context, AttributeSet attrs) {
        this.textIsDisplayable = false;
        this.paint = new Paint();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setAntiAlias(true);
        this.paint.setColor(this.textColor);
        this.paint.setTextSize(this.textSize);
        this.paint.setTypeface(this.textTypeface);
        int percent = (int) ((((float) this.progress) / ((float) getMax())) * 100.0f);
        float centreX = (((float) getWidth()) / 2.0f) - (this.paint.measureText(percent + "%") / 2.0f);
        float centerY = (((float) getWidth()) / 2.0f) + (this.textSize / 2.0f);
        if (this.textIsDisplayable && percent >= 0) {
            canvas.drawText(percent + "%", centreX, centerY, this.paint);
        }
    }

    public synchronized int getMax() {
        return super.getMax();
    }

    public synchronized void setMax(int max) {
        MLog.i("RcsRoundProgressBar", "setmax:" + max);
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        super.setMax(max);
    }

    public int getProgress() {
        this.progress = super.getProgress();
        return this.progress;
    }

    public void setProgress(int progress) {
        MLog.i("RcsRoundProgressBar", "setProgress:" + progress);
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > getMax()) {
            progress = getMax();
        }
        super.setProgress(progress);
        this.progress = progress;
        this.textIsDisplayable = true;
        postInvalidate();
    }
}
