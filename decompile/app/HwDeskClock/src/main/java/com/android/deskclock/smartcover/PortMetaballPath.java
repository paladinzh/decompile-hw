package com.android.deskclock.smartcover;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;
import java.util.List;

public class PortMetaballPath {
    private static PointF mCenterBall = new PointF();
    private static PointF mCenterCircle = new PointF();
    private int POINT_DST_GAP = 30;
    private boolean isInit = false;
    private AnimatorSet mAnimatorSet;
    private Paint mBallPaint = new Paint();
    Ball[] mBalls = new Ball[4];
    Callback mCallback;
    float mCircleLineWidthScale = 1.0f;
    private Paint mCirclePaint = new Paint();
    Context mContext;
    private Interpolator mInterpolatorMove = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    float mMetaLimit;
    float mRadiusBall;
    float mRadiusCircle;

    public interface Callback {
        void onCircleLineWidthChange(float f);

        void onUpdate();
    }

    class Ball {
        private Path mBallPath = new Path();
        PointF mControl = new PointF();
        float mCx;
        float mCy;
        boolean mDrawBezier;
        PointF mEnd = new PointF();
        float mFraction;
        float mFrom;
        float mRadius;
        RectF mRect = new RectF();
        PointF mStart = new PointF();
        float mTo;

        public Ball(float cy) {
            this.mCy = cy;
        }

        public void setCx(float cx) {
            float delta;
            this.mCx = cx;
            float dx = this.mCx - this.mFrom;
            float fraction = dx / PortMetaballPath.this.mMetaLimit;
            this.mFraction = dx / (this.mTo - this.mFrom);
            float fractionMeta = (dx - (PortMetaballPath.this.mRadiusBall * 2.0f)) / (PortMetaballPath.this.mMetaLimit - (PortMetaballPath.this.mRadiusBall * 2.0f));
            this.mDrawBezier = dx < PortMetaballPath.this.mMetaLimit;
            if (fractionMeta < 0.0f) {
                fractionMeta = 0.0f;
            }
            if (fractionMeta > 1.0f) {
                fractionMeta = 1.0f;
            }
            float hypotBall2Circle = (float) Math.hypot((double) (this.mCx - PortMetaballPath.mCenterCircle.x), (double) (this.mCy - PortMetaballPath.mCenterCircle.y));
            double deltaEnd = 2.5132741228718345d * ((double) fraction);
            this.mEnd.x = this.mCx + ((float) (((double) PortMetaballPath.this.mRadiusBall) * Math.cos(deltaEnd)));
            this.mEnd.y = this.mCy - ((float) (((double) PortMetaballPath.this.mRadiusBall) * Math.sin(deltaEnd)));
            float deltaStart = (float) Math.asin((double) ((PortMetaballPath.this.mRadiusBall * 2.0f) / PortMetaballPath.this.mRadiusCircle));
            if (dx < PortMetaballPath.this.mRadiusBall * 2.0f) {
                delta = deltaStart;
            } else {
                delta = deltaStart * (1.0f - (0.5f * fractionMeta));
            }
            this.mStart.x = PortMetaballPath.mCenterCircle.x + ((float) (((double) PortMetaballPath.this.mRadiusCircle) * Math.cos((double) delta)));
            this.mStart.y = PortMetaballPath.mCenterCircle.y - ((float) (((double) PortMetaballPath.this.mRadiusCircle) * Math.sin((double) delta)));
            float deltaControl = (float) (Math.asin((double) (PortMetaballPath.this.mRadiusBall / (PortMetaballPath.this.mRadiusCircle * 2.0f))) * 2.0d);
            float ex = 0.0f;
            float ey = 0.0f;
            if (dx < PortMetaballPath.this.mRadiusBall) {
                delta = (float) Math.acos((double) ((((hypotBall2Circle * hypotBall2Circle) + (PortMetaballPath.this.mRadiusCircle * PortMetaballPath.this.mRadiusCircle)) - (PortMetaballPath.this.mRadiusBall * PortMetaballPath.this.mRadiusBall)) / ((2.0f * hypotBall2Circle) * PortMetaballPath.this.mRadiusCircle)));
            } else if (dx < PortMetaballPath.this.mRadiusBall * 2.0f) {
                delta = deltaControl;
            } else {
                delta = deltaControl * (1.0f - fractionMeta);
                ex = (PortMetaballPath.this.mRadiusBall * 0.4f) * fractionMeta;
                ey = (PortMetaballPath.this.mRadiusBall * 0.5f) * fractionMeta;
            }
            this.mControl.x = (PortMetaballPath.mCenterCircle.x + ((float) (((double) PortMetaballPath.this.mRadiusCircle) * Math.cos((double) delta)))) + ex;
            this.mControl.y = (PortMetaballPath.mCenterCircle.y - ((float) (((double) PortMetaballPath.this.mRadiusCircle) * Math.sin((double) delta)))) + ey;
            this.mBallPath.reset();
            this.mBallPath.moveTo(this.mStart.x, this.mStart.y);
            this.mBallPath.quadTo(this.mControl.x, this.mControl.y, this.mEnd.x, this.mEnd.y);
            PortMetaballPath.this.circle2Rect(this.mCx, this.mCy, PortMetaballPath.this.mRadiusBall, this.mRect);
            this.mBallPath.arcTo(this.mRect, -((float) Math.toDegrees(deltaEnd)), (float) Math.toDegrees(2.0d * deltaEnd), false);
            this.mBallPath.quadTo(this.mControl.x, (PortMetaballPath.mCenterCircle.y * 2.0f) - this.mControl.y, this.mStart.x, (PortMetaballPath.mCenterCircle.y * 2.0f) - this.mStart.y);
            PortMetaballPath.this.circle2Rect(PortMetaballPath.mCenterCircle.x, PortMetaballPath.mCenterCircle.y, PortMetaballPath.this.mRadiusCircle, this.mRect);
            this.mBallPath.arcTo(this.mRect, (float) Math.toDegrees((double) deltaStart), -((float) Math.toDegrees((double) (2.0f * deltaStart))), false);
        }

        public void setRadius(float radius) {
            this.mRadius = radius;
        }

        public void setMoveRange(float from, float to) {
            this.mFrom = from;
            this.mTo = to;
        }

        public float getFrom() {
            return this.mFrom;
        }

        public float getTo() {
            return this.mTo;
        }

        public void draw(Canvas canvas) {
            if (this.mDrawBezier) {
                canvas.save();
                canvas.rotate(-90.0f, PortMetaballPath.mCenterCircle.x, PortMetaballPath.mCenterCircle.y);
                drawBezier(canvas);
                canvas.restore();
                return;
            }
            canvas.save();
            canvas.rotate(-90.0f, PortMetaballPath.mCenterCircle.x, PortMetaballPath.mCenterCircle.y);
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, PortMetaballPath.this.mBallPaint);
            canvas.restore();
        }

        private void drawBezier(Canvas canvas) {
            canvas.drawPath(this.mBallPath, PortMetaballPath.this.mBallPaint);
        }

        public float getFraction() {
            return this.mFraction;
        }
    }

    public PortMetaballPath(Context context, Callback callback) {
        if (context == null) {
            Log.w("PortMetaballPath", "context is null");
            return;
        }
        this.mContext = context;
        this.mCallback = callback;
        dp2px();
        for (int i = 0; i < this.mBalls.length; i++) {
            this.mBalls[i] = new Ball(0.0f);
        }
        this.mCirclePaint.setStyle(Style.STROKE);
        this.mCirclePaint.setColor(-1);
        this.mCirclePaint.setAntiAlias(true);
        this.mBallPaint.setStyle(Style.FILL);
        this.mBallPaint.setColor(-1);
        this.mBallPaint.setAntiAlias(true);
    }

    public void setPosition(float sx, float sy, float ex, float ey, float rCircle, float rBall) {
        if (!this.isInit) {
            this.isInit = true;
            mCenterCircle.set(sx, sy);
            mCenterBall.set(ex, ey);
            this.mRadiusCircle = rCircle;
            this.mRadiusBall = rBall;
            this.mMetaLimit = this.mRadiusBall * 3.0f;
            for (Ball ball : this.mBalls) {
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
        for (Ball b : this.mBalls) {
            b.draw(canvas);
        }
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas) {
        this.mCirclePaint.setStrokeWidth(this.mCircleLineWidthScale * 2.0f);
        canvas.drawCircle(mCenterCircle.x, mCenterCircle.y, this.mRadiusCircle, this.mCirclePaint);
        canvas.drawCircle(mCenterCircle.x, mCenterCircle.y, 4.0f, this.mBallPaint);
    }

    private void circle2Rect(float cx, float cy, float r, RectF rect) {
        rect.set(cx - r, cy - r, cx + r, cy + r);
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
            this.mBalls[i].setMoveRange((mCenterCircle.x + this.mRadiusCircle) - this.mRadiusBall, (mCenterBall.x - ((float) (this.POINT_DST_GAP * i))) + 250.0f);
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

    private void onBallAnimationStart(int index) {
        if (index == 0) {
            this.mCircleLineWidthScale = 2.0f;
        }
        if (index == 3) {
            this.mCircleLineWidthScale = 2.0f - this.mBalls[3].getFraction();
        }
        if (this.mCallback != null) {
            this.mCallback.onCircleLineWidthChange(this.mCircleLineWidthScale);
        }
    }
}
