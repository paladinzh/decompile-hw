package com.huawei.systemmanager.spacecleanner.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.AnimaUtil;
import com.huawei.systemmanager.comm.anima.SimpleAnimatorListener;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CircleView;
import com.huawei.systemmanager.util.HwLog;

public class RoundCircleView extends CircleView {
    private static final float ANGLE_SPEED = 3.0f;
    private static final float ROTATION_START_ANGLE = 90.0f;
    private static final String TAG = "RoundCircleView";
    private IDrawStatus mCurState;
    private IDrawStatus mIdelState;
    private IDrawStatus mRotateState;
    private float mRotationAngle;

    private static class IDrawStatus {
        private IDrawStatus() {
        }

        public void onDrawBegin() {
        }

        public boolean onDraw(Canvas canvas) {
            return false;
        }
    }

    public RoundCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIdelState = new IDrawStatus() {
            private int mRotateAlpha = 255;

            public void onDrawBegin() {
                this.mRotateAlpha = 255;
                ValueAnimator animator = ValueAnimator.ofInt(new int[]{255, 0});
                animator.setDuration(500);
                animator.setInterpolator(AnimaUtil.AD_INTERPOLATOR);
                animator.addListener(new SimpleAnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                        AnonymousClass1.this.mRotateAlpha = 255;
                    }

                    public void onAnimationEnd(Animator animation) {
                        AnonymousClass1.this.mRotateAlpha = 0;
                    }
                });
                animator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Object value = animation.getAnimatedValue();
                        if (value == null) {
                            HwLog.w(RoundCircleView.TAG, "onAnimationUpdate value is null");
                        } else {
                            AnonymousClass1.this.mRotateAlpha = ((Integer) value).intValue();
                        }
                    }
                });
                animator.start();
            }

            public boolean onDraw(Canvas canvas) {
                RoundCircleView.this.drawBackground(canvas);
                if (this.mRotateAlpha <= 0) {
                    return false;
                }
                RoundCircleView roundCircleView = RoundCircleView.this;
                roundCircleView.mRotationAngle = roundCircleView.mRotationAngle + RoundCircleView.ANGLE_SPEED;
                canvas.save();
                canvas.rotate(RoundCircleView.this.mRotationAngle, (float) RoundCircleView.this.getCenterX(), (float) RoundCircleView.this.getCenterY());
                RoundCircleView.this.getProgressPaint().setAlpha(this.mRotateAlpha);
                RoundCircleView.this.drawProgress(canvas);
                canvas.rotate(90.0f, (float) RoundCircleView.this.getCenterX(), (float) RoundCircleView.this.getCenterY());
                RoundCircleView.this.getPonitPaint().setAlpha(this.mRotateAlpha);
                RoundCircleView.this.drawPoint(canvas);
                return true;
            }
        };
        this.mRotateState = new IDrawStatus() {
            public void onDrawBegin() {
                RoundCircleView.this.mRotationAngle = 90.0f;
                RoundCircleView.this.getPonitPaint().setAlpha(255);
                RoundCircleView.this.getProgressPaint().setAlpha(255);
            }

            public boolean onDraw(Canvas canvas) {
                RoundCircleView roundCircleView = RoundCircleView.this;
                roundCircleView.mRotationAngle = roundCircleView.mRotationAngle + RoundCircleView.ANGLE_SPEED;
                RoundCircleView.this.drawBackground(canvas);
                canvas.save();
                canvas.rotate(RoundCircleView.this.mRotationAngle, (float) RoundCircleView.this.getCenterX(), (float) RoundCircleView.this.getCenterY());
                RoundCircleView.this.drawProgress(canvas);
                canvas.rotate(90.0f, (float) RoundCircleView.this.getCenterX(), (float) RoundCircleView.this.getCenterY());
                RoundCircleView.this.drawPoint(canvas);
                canvas.restore();
                return true;
            }
        };
        setRound();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCurState != null && this.mCurState.onDraw(canvas)) {
            invalidate();
        }
    }

    public void setRound() {
        transState(this.mRotateState);
    }

    public void setIdel() {
        transState(this.mIdelState);
    }

    private void transState(IDrawStatus target) {
        if (this.mCurState != target) {
            this.mCurState = target;
            this.mCurState.onDrawBegin();
        }
        invalidate();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getProgressPaint().setShader(new SweepGradient((float) getCenterX(), (float) getCenterY(), new int[]{getResources().getColor(R.color.hwsystemmanager_white_color), getResources().getColor(R.color.hwsystemmanager_white_color), getResources().getColor(R.color.hwsystemmanager_white_alpha60_color)}, new float[]{0.0f, 0.66f, Utility.ALPHA_MAX}));
    }
}
