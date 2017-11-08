package com.android.deskclock.alarmclock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.view.animation.AccelerateInterpolator;
import com.android.deskclock.alarmclock.MetaballPath.Callback;
import java.util.ArrayList;
import java.util.List;

public class PortMetaballPath extends MetaballPath {
    private static final int BALL_END = 250;
    private static final String TAG = "PortMetaballPath";
    private int POINT_DST_GAP = 10;
    private boolean isInit = false;

    class Ball extends com.android.deskclock.alarmclock.MetaballPath.Ball {
        public Ball(float cy) {
            super(cy);
        }

        public void draw(Canvas canvas) {
            if (this.mDrawBezier) {
                canvas.save();
                canvas.rotate(-90.0f, PortMetaballPath.this.mCenterCircle.x, PortMetaballPath.this.mCenterCircle.y);
                drawBezier(canvas);
                canvas.restore();
                return;
            }
            canvas.save();
            canvas.rotate(-90.0f, PortMetaballPath.this.mCenterCircle.x, PortMetaballPath.this.mCenterCircle.y);
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, PortMetaballPath.this.mBallPaint);
            canvas.restore();
        }
    }

    public PortMetaballPath(Context context, Callback callback) {
        super(context, callback);
        dp2px();
        for (int i = 0; i < this.mBalls.length; i++) {
            this.mBalls[i] = new Ball(0.0f);
        }
    }

    public void setPosition(float sx, float sy, float ex, float ey, float rCircle, float rBall) {
        if (!this.isInit) {
            this.isInit = true;
            this.mCenterCircle.set(sx, sy);
            this.mCenterBall.set(ex, ey);
            this.mRadiusCircle = rCircle;
            this.mRadiusBall = rBall;
            this.mMetaLimit = this.mRadiusBall * 3.0f;
            for (com.android.deskclock.alarmclock.MetaballPath.Ball ball : this.mBalls) {
                ball.mCy = sy;
            }
            initAnimation();
        }
    }

    public void start() {
        if (this.mAnimatorSet != null && !this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.start();
        }
    }

    public void stop() {
        if (this.mAnimatorSet != null && this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.end();
            this.mAnimatorSet.cancel();
        }
    }

    public void draw(Canvas canvas) {
        draw(canvas, true);
    }

    public void drawCircle(Canvas canvas) {
        this.mCirclePaint.setStrokeWidth(this.mCircleLineWidthScale * 2.0f);
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, this.mRadiusCircle, this.mCirclePaint);
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, 4.0f, this.mBallPaint);
    }

    private void dp2px() {
        if (this.mContext != null) {
            this.POINT_DST_GAP = (int) (((float) this.POINT_DST_GAP) * this.mContext.getResources().getDisplayMetrics().density);
        }
    }

    private void initAnimation() {
        List<Animator> animators = new ArrayList();
        ValueAnimator[] mMoveAnimator = new ValueAnimator[4];
        ValueAnimator[] mScaleAnimator = new ValueAnimator[4];
        for (int i = 0; i < 4; i++) {
            this.mBalls[i].setMoveRange((this.mCenterCircle.x + this.mRadiusCircle) - this.mRadiusBall, (this.mCenterBall.x - ((float) (this.POINT_DST_GAP * i))) + 250.0f);
            mMoveAnimator[i] = ObjectAnimator.ofFloat(this.mBalls[i], "Cx", new float[]{this.mBalls[i].getFrom(), this.mBalls[i].getTo()});
            mMoveAnimator[i].setDuration(900);
            mMoveAnimator[i].setStartDelay(((long) i) * 350);
            mMoveAnimator[i].setInterpolator(this.mInterpolatorMove);
            final int index = i;
            mMoveAnimator[i].addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (PortMetaballPath.this.mCallback != null) {
                        PortMetaballPath.this.mCallback.onUpdate();
                    }
                    PortMetaballPath.this.onBallAnimationStart(index);
                }
            });
            mScaleAnimator[i] = ObjectAnimator.ofFloat(this.mBalls[i], "Radius", new float[]{this.mRadiusBall, 0.0f});
            mScaleAnimator[i].setDuration(900);
            mScaleAnimator[i].setStartDelay(((long) i) * 350);
            mScaleAnimator[i].setInterpolator(new AccelerateInterpolator());
            animators.add(mMoveAnimator[i]);
            animators.add(mScaleAnimator[i]);
        }
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                PortMetaballPath.this.mAnimatorSet.start();
            }
        });
        this.mAnimatorSet.setStartDelay(200);
        this.mAnimatorSet.playTogether(animators);
    }
}
