package com.huawei.systemmanager.spacecleanner.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.SimpleAnimatorListener;
import com.huawei.systemmanager.util.HwLog;

public class CompleteView extends View {
    private static final String TAG = "CompleteView";
    private AnimaController mAnima;
    private boolean mAnimaEnd;
    private Drawable mDrawable;

    private class AnimaController {
        private static final String PROPERTY_LEFT_LENGTH = "leftLength";
        private static final String PROPERTY_RIGHT_LENGTH = "rightLength";
        private static final String PROPERTY_SHAKE_ANGLE = "shake";
        private Path mPath;
        private float mShakeAngele;
        private Matrix mTempMatrix;
        private float[] mTempTickLeftTopPosition;
        private float mTickLeftLength;
        private float mTickRightLength;
        private final float[] sTickBottmPosistion;
        private final float[] sTickLeftTopPosition;

        private AnimaController() {
            this.sTickLeftTopPosition = new float[]{40.0f, 100.0f};
            this.sTickBottmPosistion = new float[]{88.0f, 152.0f};
            this.mTempTickLeftTopPosition = new float[2];
            this.mTempMatrix = new Matrix();
            this.mPath = new Path();
        }

        public void startAnima() {
            ValueAnimator animator = (ValueAnimator) AnimatorInflater.loadAnimator(CompleteView.this.getContext(), R.animator.space_clean_complete_icon_anima);
            animator.addListener(new SimpleAnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    CompleteView.this.mAnimaEnd = false;
                    HwLog.i(CompleteView.TAG, "onAnimationStart");
                }

                public void onAnimationCancel(Animator animation) {
                    CompleteView.this.mAnimaEnd = true;
                    HwLog.i(CompleteView.TAG, "onAnimationCancel");
                }

                public void onAnimationEnd(Animator animation) {
                    CompleteView.this.mAnimaEnd = true;
                    HwLog.i(CompleteView.TAG, "onAnimationEnd");
                }
            });
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object angele = animation.getAnimatedValue(AnimaController.PROPERTY_SHAKE_ANGLE);
                    if (angele != null) {
                        AnimaController.this.mShakeAngele = ((Float) angele).floatValue();
                    }
                    Object left = animation.getAnimatedValue(AnimaController.PROPERTY_LEFT_LENGTH);
                    if (left != null) {
                        AnimaController.this.mTickLeftLength = ((Float) left).floatValue();
                    }
                    Object right = animation.getAnimatedValue(AnimaController.PROPERTY_RIGHT_LENGTH);
                    if (right != null) {
                        AnimaController.this.mTickRightLength = ((Float) right).floatValue();
                    }
                    CompleteView.this.invalidate();
                }
            });
            animator.start();
        }

        public void doDraw(Canvas canvas) {
            canvas.save();
            this.mTempMatrix.reset();
            this.mTempMatrix.setRotate(this.mShakeAngele, this.sTickBottmPosistion[0], this.sTickBottmPosistion[1]);
            this.mTempMatrix.mapPoints(this.mTempTickLeftTopPosition, this.sTickLeftTopPosition);
            this.mPath.reset();
            this.mPath.addCircle(this.mTempTickLeftTopPosition[0], this.mTempTickLeftTopPosition[1], this.mTickLeftLength, Direction.CCW);
            this.mPath.addCircle(this.sTickBottmPosistion[0], this.sTickBottmPosistion[1], this.mTickRightLength, Direction.CCW);
            canvas.clipPath(this.mPath);
            canvas.rotate(this.mShakeAngele, this.sTickBottmPosistion[0], this.sTickBottmPosistion[1]);
            CompleteView.this.getDrawable().draw(canvas);
            canvas.restore();
        }
    }

    public CompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mAnimaEnd) {
            getDrawable().draw(canvas);
        } else if (this.mAnima != null) {
            this.mAnima.doDraw(canvas);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        setMeasuredDimension(d.getIntrinsicWidth(), d.getIntrinsicHeight());
    }

    private Drawable getDrawable() {
        if (this.mDrawable != null) {
            return this.mDrawable;
        }
        this.mDrawable = getContext().getDrawable(R.drawable.pic_safety);
        this.mDrawable.setBounds(0, 0, this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
        return this.mDrawable;
    }

    public void startAnima() {
        HwLog.i(TAG, "start anima called");
        if (this.mAnima != null) {
            HwLog.e(TAG, "startAnima error, mAnima!= null");
        }
        this.mAnima = new AnimaController();
        this.mAnima.startAnima();
        invalidate();
    }
}
