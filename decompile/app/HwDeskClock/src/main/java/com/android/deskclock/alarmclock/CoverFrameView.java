package com.android.deskclock.alarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;

public class CoverFrameView extends FrameLayout {
    private int mCoverAnimMarginTop;
    private CoverTextView mCoverTextView;
    private RelativeLayout mCoverTextViewLyt;
    private int mCoverWidth;
    private LayoutParams mFrameLayoutParams;
    private int mOffsetX;
    private int mOffsetY;
    private int mOrientation;
    private float mStartX;
    private float mStartY;
    private Handler mainHandler;

    public CoverFrameView(Context context) {
        this(context, null);
    }

    public CoverFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mainHandler = null;
        this.mCoverWidth = 1080;
        this.mStartX = 0.0f;
        this.mOffsetX = 0;
        this.mStartY = 0.0f;
        this.mOffsetY = 0;
        this.mCoverAnimMarginTop = 0;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.SliderView, defStyle, 0);
        try {
            this.mOrientation = typedArray.getInt(0, 0);
            this.mFrameLayoutParams = new LayoutParams(-1, -1);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = null;
            if (this.mOrientation == 0) {
                view = inflater.inflate(R.layout.cover_alarm_animation, this, true);
                this.mCoverWidth = (int) (((double) this.mCoverWidth) * 0.3d);
            } else if (this.mOrientation == 1) {
                view = inflater.inflate(R.layout.cover_alarm_animation_port, this, true);
                this.mCoverTextViewLyt = (RelativeLayout) view.findViewById(R.id.cover_anim);
                this.mCoverAnimMarginTop = getResources().getDimensionPixelSize(R.dimen.cover_anim_marginTop);
            }
            if (view != null) {
                this.mCoverTextView = (CoverTextView) view.findViewById(R.id.covertextanimation);
            }
        } finally {
            typedArray.recycle();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case 0:
                if (this.mOrientation != 1) {
                    if (this.mOrientation == 0) {
                        this.mStartX = getX() + eventX;
                        break;
                    }
                }
                this.mStartY = eventY;
                break;
                break;
            case 1:
                doUp(eventX, eventY);
                break;
            case 2:
                if (this.mOrientation != 1) {
                    if (this.mOrientation == 0) {
                        this.mOffsetX = (int) (eventX - this.mStartX);
                        updateStart(this.mOffsetX, 0);
                        break;
                    }
                }
                this.mOffsetY = (int) (eventY - this.mStartY);
                if (this.mOffsetY <= 0) {
                    updateStart(0, this.mOffsetY);
                    break;
                }
                break;
        }
        return true;
    }

    private void doUp(float eventX, float eventY) {
        if (this.mOrientation == 1) {
            this.mOffsetY = (int) (eventY - this.mStartY);
            if (this.mOffsetY <= (-this.mCoverAnimMarginTop)) {
                this.mainHandler.obtainMessage(2).sendToTarget();
            } else {
                updateStart(0, 0);
            }
        } else if (this.mOrientation == 0) {
            this.mOffsetX = (int) (eventX - this.mStartX);
            if (Math.abs(this.mOffsetX) > this.mCoverWidth) {
                this.mainHandler.obtainMessage(2).sendToTarget();
            } else {
                updateStart(0, 0);
            }
        }
    }

    private void updateStart(int x, int y) {
        if (this.mCoverTextViewLyt != null) {
            if (y < (-this.mCoverAnimMarginTop)) {
                y = -this.mCoverAnimMarginTop;
            }
            this.mFrameLayoutParams.topMargin = this.mCoverAnimMarginTop + y;
            this.mCoverTextViewLyt.setLayoutParams(this.mFrameLayoutParams);
            return;
        }
        this.mFrameLayoutParams.setMargins(x, y, 0, 0);
        this.mCoverTextView.setLayoutParams(this.mFrameLayoutParams);
    }

    public void setCoverViewWidth(int coverWidth) {
        this.mCoverWidth = coverWidth;
        this.mCoverWidth = (int) (((double) this.mCoverWidth) * 0.3d);
    }

    public void setMainHandler(Handler handler) {
        this.mainHandler = handler;
    }
}
