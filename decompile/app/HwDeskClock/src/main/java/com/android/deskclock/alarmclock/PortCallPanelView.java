package com.android.deskclock.alarmclock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.alarmclock.WorldAnalogClock;
import com.android.deskclock.alarmclock.MetaballPath.Callback;

public class PortCallPanelView extends View implements Callback {
    public static final int DEFAUT_RADIUS = 25;
    private float mBallDst;
    private float mBallRadiusDp;
    private float mCircleRadiusDp;
    PortMetaballPath mMetaball;

    public PortCallPanelView(Context context) {
        this(context, null);
    }

    public PortCallPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBallRadiusDp = 4.0f;
        this.mCircleRadiusDp = 25.0f;
        this.mBallDst = WorldAnalogClock.DEGREE_ONE_HOUR;
        this.mMetaball = new PortMetaballPath(getContext(), this);
        float density = getResources().getDisplayMetrics().density;
        this.mBallRadiusDp *= density;
        this.mCircleRadiusDp *= density;
    }

    void setCircleRadius(float radius) {
        this.mCircleRadiusDp = radius;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int halfWidth = w / 2;
        this.mMetaball.setPosition((float) halfWidth, (((float) h) - this.mCircleRadiusDp) - 2.0f, (((float) halfWidth) + this.mCircleRadiusDp) - this.mBallDst, (float) (54 < h / 2 ? 54 : h / 2), this.mCircleRadiusDp, this.mBallRadiusDp);
    }

    protected void onDraw(Canvas canvas) {
        this.mMetaball.draw(canvas);
    }

    public void startAnim() {
        this.mMetaball.start();
    }

    public void onUpdate() {
        invalidate();
    }

    public void endAnim() {
        this.mMetaball.stop();
    }

    public void onCircleLineWidthChange(float scale) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            startAnim();
        }
    }
}
