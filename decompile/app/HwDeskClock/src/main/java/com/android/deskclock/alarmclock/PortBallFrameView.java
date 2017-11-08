package com.android.deskclock.alarmclock;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.util.HwLog;

public class PortBallFrameView extends FrameLayout {
    private int mAnimationHeight;
    private int mAnimationWidth;
    private float mBallRadius;
    private PortCallPanelView mBooView;
    private LayoutParams mFrameLayoutParams;
    private float mStartY;
    private Handler mainHandler;

    public PortBallFrameView(Context context) {
        this(context, null);
    }

    public PortBallFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortBallFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mainHandler = null;
        this.mAnimationWidth = 1080;
        this.mAnimationHeight = 210;
        this.mStartY = 0.0f;
        this.mBallRadius = context.obtainStyledAttributes(attrs, R$styleable.PortBallFrameView, defStyle, 0).getDimension(0, getResources().getDisplayMetrics().density * 25.0f);
        this.mFrameLayoutParams = new LayoutParams(-1, -1);
        this.mBooView = (PortCallPanelView) LayoutInflater.from(context).inflate(R.layout.port_boll_animation_v, this, true).findViewById(R.id.port_boll_ani);
        this.mBooView.setCircleRadius(this.mBallRadius);
        this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.5f);
    }

    public void startTextViewAnimal() {
        if (this.mBooView != null) {
            this.mBooView.startAnim();
        }
    }

    public void stopTextViewAnimal() {
        if (this.mBooView != null) {
            this.mBooView.endAnim();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            HwLog.i("port", "start");
            startTextViewAnimal();
            return;
        }
        HwLog.i("port", "stop");
        stopTextViewAnimal();
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        HwLog.i("port", "onTouchEvent");
        switch (event.getAction()) {
            case 0:
                this.mStartY = eventY;
                stopTextViewAnimal();
                break;
            case 1:
                handleUpEvent(eventX, eventY);
                break;
            case 2:
                HwLog.i("port", "move");
                int offY = (int) (eventY - this.mStartY);
                if (offY < 0 && (-((((float) getHeight()) - (this.mBallRadius * 2.0f)) - 4.0f)) < ((float) offY)) {
                    updateStart(0, offY);
                    break;
                }
        }
        return true;
    }

    private void handleUpEvent(float eventX, float eventY) {
        isUpdate(eventY - this.mStartY, 100.0f);
    }

    private void isUpdate(float offsetDis, float animationDis) {
        if (offsetDis < (-animationDis)) {
            this.mainHandler.obtainMessage(2).sendToTarget();
            return;
        }
        updateStart(0, 0);
        startTextViewAnimal();
    }

    private void updateStart(int x, int y) {
        this.mFrameLayoutParams.setMargins(x, y, 0, 0);
        if (this.mBooView != null) {
            this.mBooView.setLayoutParams(this.mFrameLayoutParams);
        }
    }

    public void setCoverViewWidth(int animationWidth) {
        this.mAnimationWidth = animationWidth;
        this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.5f);
    }

    public void setMainHandler(Handler handler) {
        this.mainHandler = handler;
    }
}
