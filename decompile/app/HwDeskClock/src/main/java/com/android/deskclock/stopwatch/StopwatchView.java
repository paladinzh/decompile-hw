package com.android.deskclock.stopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.deskclock.stopwatch.PanelPath.Callback;

public class StopwatchView extends View implements Callback {
    private PanelPath mPanelPath = new PanelPath(getContext(), this);
    private float mRadiusCenterBall = 80.0f;
    private float mRadiusMoveBall = 20.0f;

    public StopwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float ratio = getResources().getDisplayMetrics().density;
        this.mRadiusCenterBall *= ratio;
        this.mRadiusMoveBall *= ratio;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPanelPath.draw(canvas);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mPanelPath.setPosition(w / 2, h / 2, this.mRadiusCenterBall, this.mRadiusMoveBall, ((float) w) / 2.0f);
    }

    public void onUpdateUI() {
        invalidate();
    }
}
