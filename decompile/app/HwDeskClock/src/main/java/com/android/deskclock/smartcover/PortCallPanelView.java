package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.deskclock.smartcover.PortMetaballPath.Callback;

public class PortCallPanelView extends View implements Callback {
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
        this.mBallDst = 39.0f;
        this.mMetaball = new PortMetaballPath(getContext(), this);
        float density = getResources().getDisplayMetrics().density;
        this.mBallRadiusDp *= density;
        this.mCircleRadiusDp *= density;
        this.mBallDst *= density;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int halfWidth = w / 2;
        this.mMetaball.setPosition((float) halfWidth, (float) ((int) (((float) h) * 0.8f)), (((float) halfWidth) + this.mCircleRadiusDp) + this.mBallDst, (float) (h / 2), this.mCircleRadiusDp, this.mBallRadiusDp);
    }

    protected void onDraw(Canvas canvas) {
        this.mMetaball.draw(canvas);
    }

    public void startAnim() {
        this.mMetaball.start();
    }

    public void endAnim() {
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
}
