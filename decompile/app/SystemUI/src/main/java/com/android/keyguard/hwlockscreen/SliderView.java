package com.android.keyguard.hwlockscreen;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.keyguard.util.HwLog;

public class SliderView extends RelativeLayout {
    private Callback mCallback;
    private OnConfirmListener mConfirmListener;
    private int mHeight;
    private int mLayoutDirection;
    private int mLeft;
    private int mMaxDistance;
    private ObjectAnimator mObjectAnimator;
    private Rect mRect;
    private int mRight;
    private RelativeLayout mSlideButton;
    private int mSlideButtonHeight;
    private int mSlideButtonWidth;
    private TextView mTvConfirm;
    private ViewDragHelper mViewDragHelper;

    public interface OnConfirmListener {
        void onConfirm();
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.mConfirmListener = onConfirmListener;
    }

    public SliderView(Context context) {
        this(context, null);
    }

    public SliderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCallback = new Callback() {
            public boolean tryCaptureView(View child, int pointerId) {
                return (child.getLeft() > 0 || child.getRight() < SliderView.this.mMaxDistance) && child == SliderView.this.mSlideButton;
            }

            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
            }

            public int clampViewPositionHorizontal(View child, int left, int dx) {
                int oldLeft = child.getLeft();
                int leftRange = left - SliderView.this.mLeft;
                if (leftRange > 0 && leftRange < SliderView.this.mMaxDistance) {
                    child.layout(left, (SliderView.this.mHeight - SliderView.this.mSlideButtonHeight) / 2, SliderView.this.mSlideButtonWidth + left, ((SliderView.this.mHeight - SliderView.this.mSlideButtonHeight) / 2) + SliderView.this.mSlideButtonHeight);
                }
                HwLog.i("SliderView", "button move to left:" + left + "leftRange:" + leftRange);
                return oldLeft;
            }

            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                int movedDistance = 0;
                int toStart = 0;
                int toEnd = 0;
                if (SliderView.this.mLayoutDirection == 0) {
                    movedDistance = releasedChild.getLeft() - SliderView.this.mLeft;
                    toStart = SliderView.this.mLeft;
                    toEnd = SliderView.this.mRight - SliderView.this.mSlideButtonWidth;
                } else if (SliderView.this.mLayoutDirection == 1) {
                    movedDistance = SliderView.this.mRight - releasedChild.getRight();
                    toStart = SliderView.this.mRight - SliderView.this.mSlideButtonWidth;
                    toEnd = SliderView.this.mLeft;
                }
                HwLog.i("SliderView", "onViewReleased LayoutDirection:" + SliderView.this.mLayoutDirection + " maxDistance:" + SliderView.this.mMaxDistance + " movedDistance:" + movedDistance + " toStart:" + toStart + " toEnd:" + toEnd);
                if (((double) movedDistance) >= ((double) SliderView.this.mMaxDistance) * 0.99d) {
                    SliderView.this.animToXToPostion(releasedChild, toEnd, 300);
                    if (SliderView.this.mConfirmListener != null) {
                        SliderView.this.mConfirmListener.onConfirm();
                        return;
                    }
                    return;
                }
                SliderView.this.animToXToPostion(releasedChild, toStart, 300);
            }

            public int getViewVerticalDragRange(View child) {
                return SliderView.this.mMaxDistance;
            }
        };
        this.mViewDragHelper = ViewDragHelper.create(this, this.mCallback);
    }

    public void computeScroll() {
        super.computeScroll();
        if (this.mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void animToXToPostion(final View view, int toX, long animationTime) {
        Property<View, Integer> layoutProperty = new Property<View, Integer>(Integer.class, "layout") {
            public Integer get(View object) {
                return Integer.valueOf(view.getLeft());
            }

            public void set(View object, Integer value) {
                object.layout(value.intValue(), (SliderView.this.mHeight - SliderView.this.mSlideButtonHeight) / 2, value.intValue() + object.getWidth(), ((SliderView.this.mHeight - SliderView.this.mSlideButtonHeight) / 2) + object.getHeight());
            }
        };
        if (this.mObjectAnimator != null && this.mObjectAnimator.isRunning()) {
            this.mObjectAnimator.cancel();
        }
        this.mObjectAnimator = ObjectAnimator.ofInt(view, layoutProperty, new int[]{view.getLeft(), toX});
        this.mObjectAnimator.setInterpolator(new AccelerateInterpolator());
        this.mObjectAnimator.setDuration(300);
        this.mObjectAnimator.start();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        HwLog.i("SliderView", "event action:" + event.getAction());
        if (event.getAction() == 2) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            HwLog.i("SliderView", "is in tvConfirm range:" + this.mRect.contains(x, y));
            if (!this.mRect.contains(x, y)) {
                event.setAction(3);
                this.mViewDragHelper.processTouchEvent(event);
                event.setAction(2);
                return true;
            }
        }
        this.mViewDragHelper.processTouchEvent(event);
        return true;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTvConfirm = (TextView) getChildAt(0);
        this.mSlideButton = (RelativeLayout) getChildAt(1);
        ImageView arrow2 = (ImageView) this.mSlideButton.getChildAt(1);
        initArrowAnimation((ImageView) this.mSlideButton.getChildAt(0), 1.0f, 0.5f);
        initArrowAnimation(arrow2, 0.5f, 1.0f);
    }

    private void initArrowAnimation(View view, float start, float end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", new float[]{start, end});
        animator.setDuration(300);
        animator.setRepeatMode(2);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mLayoutDirection = getLayoutDirection();
        this.mSlideButtonWidth = this.mSlideButton.getWidth();
        int witdh = this.mTvConfirm.getWidth();
        this.mHeight = this.mTvConfirm.getHeight();
        this.mSlideButtonHeight = this.mSlideButton.getHeight();
        this.mLeft = this.mTvConfirm.getLeft();
        this.mRight = this.mTvConfirm.getRight();
        this.mMaxDistance = witdh - this.mSlideButtonWidth;
        this.mRect = new Rect(this.mLeft, this.mTvConfirm.getTop(), this.mRight, this.mTvConfirm.getBottom());
        HwLog.i("SliderView", "TvConfirm getleft():" + this.mLeft + " SlideButton.getWidth():" + this.mSlideButtonWidth + " TvConfirm.getWidth:" + witdh);
    }
}
