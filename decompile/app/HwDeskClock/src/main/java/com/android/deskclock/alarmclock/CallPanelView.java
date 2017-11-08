package com.android.deskclock.alarmclock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.deskclock.alarmclock.MetaballPath.Callback;

public class CallPanelView extends View implements Callback {
    private float mBallDst;
    private float mBallRadiusDp;
    private float mCircleRadiusDp;
    private boolean mIsUpdate;
    MetaballPath mMetaball;

    public CallPanelView(Context context) {
        this(context, null);
    }

    public CallPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBallRadiusDp = 4.0f;
        this.mCircleRadiusDp = 60.0f;
        this.mBallDst = 39.0f;
        this.mIsUpdate = false;
        this.mMetaball = new MetaballPath(getContext(), this);
        float density = getResources().getDisplayMetrics().density;
        this.mBallRadiusDp *= density;
        this.mCircleRadiusDp *= density;
        this.mBallDst *= density;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mCircleRadiusDp = (((float) h) * 0.5f) - 3.0f;
        this.mMetaball.setPosition(((float) w) / 2.0f, ((float) h) / 2.0f, ((((float) w) / 2.0f) + this.mCircleRadiusDp) + this.mBallDst, ((float) h) / 2.0f, this.mCircleRadiusDp, this.mBallRadiusDp);
    }

    protected void onDraw(Canvas canvas) {
        this.mMetaball.draw(canvas, this.mIsUpdate);
    }

    public void startAnim() {
        this.mIsUpdate = true;
        this.mMetaball.start();
    }

    public void endAnim() {
        this.mIsUpdate = false;
        this.mMetaball.stop();
    }

    public void onUpdate() {
        invalidate();
    }

    public void onCircleLineWidthChange(float scale) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            startAnim();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endAnim();
    }
}
