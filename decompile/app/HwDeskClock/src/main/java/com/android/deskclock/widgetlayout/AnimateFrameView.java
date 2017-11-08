package com.android.deskclock.widgetlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;

public class AnimateFrameView extends FrameLayout {
    private int mAnimationHeight;
    private AnimateTextView mAnimationText;
    private int mAnimationWidth;
    private int mAppType;
    private LayoutParams mFrameLayoutParams;
    private int mOffsetX;
    private int mOffsetY;
    private int mOrientation;
    private float mStartX;
    private float mStartY;
    private Handler mainHandler;

    public AnimateFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimateFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAppType = 0;
        this.mainHandler = null;
        this.mAnimationWidth = 1080;
        this.mStartX = 0.0f;
        this.mOffsetX = 0;
        this.mAnimationHeight = 210;
        this.mStartY = 0.0f;
        this.mOffsetY = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SliderView, defStyle, 0);
        try {
            this.mOrientation = a.getInt(0, 0);
            a = context.obtainStyledAttributes(attrs, R$styleable.AnimateView, defStyle, 0);
            try {
                this.mAppType = a.getInt(0, 0);
                this.mFrameLayoutParams = new LayoutParams(-1, -1);
                LayoutInflater inflater = LayoutInflater.from(context);
                if (this.mOrientation == 0) {
                    if (1 == this.mAppType) {
                        this.mAnimationText = (AnimateTextView) inflater.inflate(R.layout.timer_animation, this, true).findViewById(R.id.timeranimationtv);
                        this.mAnimationText.setContentDescription(getContext().getResources().getString(R.string.tips_clock_closetimer));
                    } else if (this.mAppType == 0) {
                        this.mAnimationText = (AnimateTextView) inflater.inflate(R.layout.alarm_animation, this, true).findViewById(R.id.alarmanimationtv);
                        this.mAnimationText.setContentDescription(getContext().getResources().getString(R.string.tips_clock_closealarm));
                    }
                }
                this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.5f);
            } finally {
                a.recycle();
            }
        } finally {
            a.recycle();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case 0:
                if (this.mOrientation != 1) {
                    this.mStartX = getX() + eventX;
                    break;
                }
                this.mStartY = eventY;
                break;
            case 1:
                handleUpEvent(eventX, eventY);
                break;
            case 2:
                if (this.mOrientation != 1) {
                    this.mOffsetX = (int) (eventX - this.mStartX);
                    updateStart(this.mOffsetX, 0);
                    break;
                }
                this.mOffsetY = (int) (eventY - this.mStartY);
                updateStart(0, this.mOffsetY);
                break;
        }
        return true;
    }

    private void handleUpEvent(float eventX, float eventY) {
        if (this.mOrientation == 1) {
            this.mOffsetY = (int) (eventY - this.mStartY);
            if (Math.abs(this.mOffsetY) > this.mAnimationHeight) {
                this.mainHandler.obtainMessage(2).sendToTarget();
                return;
            } else {
                updateStart(0, 0);
                return;
            }
        }
        this.mOffsetX = (int) (eventX - this.mStartX);
        if (Math.abs(this.mOffsetX) > this.mAnimationWidth) {
            this.mainHandler.obtainMessage(2).sendToTarget();
        } else {
            updateStart(0, 0);
        }
    }

    private void updateStart(int x, int y) {
        this.mFrameLayoutParams.setMargins(x, y, 0, 0);
        this.mAnimationText.setLayoutParams(this.mFrameLayoutParams);
    }
}
