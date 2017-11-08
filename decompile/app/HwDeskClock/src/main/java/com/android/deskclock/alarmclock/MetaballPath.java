package com.android.deskclock.alarmclock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import java.util.ArrayList;
import java.util.List;

public class MetaballPath {
    public static final long ANIM_DELAY = 350;
    public static final long ANIM_DURATION = 900;
    private static final int MSG_REPEAT_ANIMATION = 101;
    private static final int MSG_REPEAT_DELAY = 200;
    private static final float POINT_DST_GAP = 2.0f;
    public static final int POINT_NUM = 4;
    private static final String TAG = "MetaballPath";
    private boolean isStopManual = false;
    AnimatorSet mAnimatorSet;
    public Paint mBallPaint = new Paint();
    Ball[] mBalls = new Ball[4];
    Callback mCallback;
    PointF mCenterBall = new PointF();
    PointF mCenterCircle = new PointF();
    float mCircleLineWidthScale = 1.0f;
    public Paint mCirclePaint = new Paint();
    Context mContext;
    private Handler mHandler = new BallHandler();
    public Interpolator mInterpolatorMove = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    float mMetaLimit;
    private float mPointGapInpx;
    float mRadiusBall;
    float mRadiusCircle;
    private Runnable mUpdateRunnable = new UpdateRunnable();

    public interface Callback {
        void onCircleLineWidthChange(float f);

        void onUpdate();
    }

    private class AnimatorSetListener extends AnimatorListenerAdapter {
        private AnimatorSetListener() {
        }

        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            MetaballPath.this.doUpdate();
        }

        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (MetaballPath.this.mHandler != null) {
                MetaballPath.this.mHandler.removeMessages(MetaballPath.MSG_REPEAT_ANIMATION);
                if (!MetaballPath.this.isStopManual) {
                    MetaballPath.this.mHandler.sendEmptyMessageDelayed(MetaballPath.MSG_REPEAT_ANIMATION, 200);
                }
            }
        }
    }

    public class Ball {
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
            float fraction = dx / MetaballPath.this.mMetaLimit;
            this.mFraction = dx / (this.mTo - this.mFrom);
            float fractionMeta = (dx - (MetaballPath.this.mRadiusBall * MetaballPath.POINT_DST_GAP)) / (MetaballPath.this.mMetaLimit - (MetaballPath.this.mRadiusBall * MetaballPath.POINT_DST_GAP));
            this.mDrawBezier = dx < MetaballPath.this.mMetaLimit;
            if (fractionMeta < 0.0f) {
                fractionMeta = 0.0f;
            }
            if (fractionMeta > 1.0f) {
                fractionMeta = 1.0f;
            }
            float hypotBall2Circle = (float) Math.hypot((double) (this.mCx - MetaballPath.this.mCenterCircle.x), (double) (this.mCy - MetaballPath.this.mCenterCircle.y));
            double deltaEnd = 2.5132741228718345d * ((double) fraction);
            this.mEnd.x = this.mCx + ((float) (((double) MetaballPath.this.mRadiusBall) * Math.cos(deltaEnd)));
            this.mEnd.y = this.mCy - ((float) (((double) MetaballPath.this.mRadiusBall) * Math.sin(deltaEnd)));
            float deltaStart = (float) Math.asin((double) ((MetaballPath.this.mRadiusBall * MetaballPath.POINT_DST_GAP) / MetaballPath.this.mRadiusCircle));
            if (dx < MetaballPath.this.mRadiusBall * MetaballPath.POINT_DST_GAP) {
                delta = deltaStart;
            } else {
                delta = deltaStart * (1.0f - (0.5f * fractionMeta));
            }
            this.mStart.x = MetaballPath.this.mCenterCircle.x + ((float) (((double) MetaballPath.this.mRadiusCircle) * Math.cos((double) delta)));
            this.mStart.y = MetaballPath.this.mCenterCircle.y - ((float) (((double) MetaballPath.this.mRadiusCircle) * Math.sin((double) delta)));
            float deltaControl = (float) (Math.asin((double) (MetaballPath.this.mRadiusBall / (MetaballPath.this.mRadiusCircle * MetaballPath.POINT_DST_GAP))) * 2.0d);
            float ex = 0.0f;
            float ey = 0.0f;
            if (dx < MetaballPath.this.mRadiusBall) {
                delta = (float) Math.acos((double) ((((hypotBall2Circle * hypotBall2Circle) + (MetaballPath.this.mRadiusCircle * MetaballPath.this.mRadiusCircle)) - (MetaballPath.this.mRadiusBall * MetaballPath.this.mRadiusBall)) / ((MetaballPath.POINT_DST_GAP * hypotBall2Circle) * MetaballPath.this.mRadiusCircle)));
            } else if (dx < MetaballPath.this.mRadiusBall * MetaballPath.POINT_DST_GAP) {
                delta = deltaControl;
            } else {
                delta = deltaControl * (1.0f - fractionMeta);
                ex = (MetaballPath.this.mRadiusBall * 0.4f) * fractionMeta;
                ey = (MetaballPath.this.mRadiusBall * 0.5f) * fractionMeta;
            }
            this.mControl.x = (MetaballPath.this.mCenterCircle.x + ((float) (((double) MetaballPath.this.mRadiusCircle) * Math.cos((double) delta)))) + ex;
            this.mControl.y = (MetaballPath.this.mCenterCircle.y - ((float) (((double) MetaballPath.this.mRadiusCircle) * Math.sin((double) delta)))) + ey;
            this.mBallPath.reset();
            this.mBallPath.moveTo(this.mStart.x, this.mStart.y);
            this.mBallPath.quadTo(this.mControl.x, this.mControl.y, this.mEnd.x, this.mEnd.y);
            MetaballPath.this.circle2Rect(this.mCx, this.mCy, MetaballPath.this.mRadiusBall, this.mRect);
            this.mBallPath.arcTo(this.mRect, -((float) Math.toDegrees(deltaEnd)), (float) Math.toDegrees(2.0d * deltaEnd), false);
            this.mBallPath.quadTo(this.mControl.x, (MetaballPath.this.mCenterCircle.y * MetaballPath.POINT_DST_GAP) - this.mControl.y, this.mStart.x, (MetaballPath.this.mCenterCircle.y * MetaballPath.POINT_DST_GAP) - this.mStart.y);
            MetaballPath.this.circle2Rect(MetaballPath.this.mCenterCircle.x, MetaballPath.this.mCenterCircle.y, MetaballPath.this.mRadiusCircle, this.mRect);
            this.mBallPath.arcTo(this.mRect, (float) Math.toDegrees((double) deltaStart), -((float) Math.toDegrees((double) (MetaballPath.POINT_DST_GAP * deltaStart))), false);
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
                drawBezier(canvas);
                canvas.save();
                canvas.translate(MetaballPath.this.mCenterCircle.x * MetaballPath.POINT_DST_GAP, 0.0f);
                canvas.scale(-1.0f, 1.0f);
                drawBezier(canvas);
                canvas.restore();
                return;
            }
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, MetaballPath.this.mBallPaint);
            canvas.save();
            canvas.translate(MetaballPath.this.mCenterCircle.x * MetaballPath.POINT_DST_GAP, 0.0f);
            canvas.scale(-1.0f, 1.0f);
            canvas.drawCircle(this.mCx, this.mCy, this.mRadius, MetaballPath.this.mBallPaint);
            canvas.restore();
        }

        public void drawBezier(Canvas canvas) {
            canvas.drawPath(this.mBallPath, MetaballPath.this.mBallPaint);
        }

        public float getFraction() {
            return this.mFraction;
        }
    }

    private class BallHandler extends Handler {
        private BallHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MetaballPath.MSG_REPEAT_ANIMATION /*101*/:
                    MetaballPath.this.start();
                    return;
                default:
                    return;
            }
        }
    }

    private class MoveAnimatorListener extends AnimatorListenerAdapter {
        private int mIndex;

        public MoveAnimatorListener(int index) {
            this.mIndex = index;
        }

        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            MetaballPath.this.onBallAnimationStart(this.mIndex);
        }
    }

    public class UpdateRunnable implements Runnable {
        public void run() {
            MetaballPath.this.doUpdate();
        }
    }

    public MetaballPath(Context context, Callback callback) {
        if (context == null) {
            Log.w(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.mCallback = callback;
        for (int i = 0; i < this.mBalls.length; i++) {
            this.mBalls[i] = new Ball(0.0f);
        }
        this.mCirclePaint.setStyle(Style.STROKE);
        this.mCirclePaint.setColor(-1);
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setStrokeWidth(4.0f);
        this.mBallPaint.setStyle(Style.FILL);
        this.mBallPaint.setColor(-1);
        this.mBallPaint.setAntiAlias(true);
    }

    public void setPosition(float sx, float sy, float ex, float ey, float rCircle, float rBall) {
        this.mCenterCircle.set(sx, sy);
        this.mCenterBall.set(ex, ey);
        this.mRadiusCircle = rCircle;
        this.mRadiusBall = rBall;
        this.mMetaLimit = this.mRadiusBall * 3.0f;
        for (Ball ball : this.mBalls) {
            ball.mCy = sy;
        }
        initAnimation();
    }

    public void start() {
        this.isStopManual = false;
        if (this.mAnimatorSet != null && !this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.start();
        }
    }

    public void stop() {
        this.isStopManual = true;
        if (this.mCallback != null) {
            this.mCallback.onUpdate();
        }
        if (this.mHandler != null) {
            this.mHandler.removeMessages(MSG_REPEAT_ANIMATION);
        }
        if (this.mAnimatorSet != null) {
            this.mAnimatorSet.end();
        }
    }

    public void draw(Canvas canvas, boolean isUpdate) {
        if (isUpdate) {
            for (Ball b : this.mBalls) {
                b.draw(canvas);
            }
        }
        drawCircle(canvas);
    }

    public void drawCircle(Canvas canvas) {
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, this.mRadiusCircle, this.mCirclePaint);
        canvas.drawCircle(this.mCenterCircle.x, this.mCenterCircle.y, 4.0f, this.mBallPaint);
    }

    private void circle2Rect(float cx, float cy, float r, RectF rect) {
        rect.set(cx - r, cy - r, cx + r, cy + r);
    }

    private float dp2px(float dp) {
        if (this.mContext == null) {
            return -1.0f;
        }
        return this.mContext.getResources().getDisplayMetrics().density * dp;
    }

    private void initAnimation() {
        List<Animator> animators = new ArrayList();
        ValueAnimator[] mMoveAnimator = new ValueAnimator[4];
        ValueAnimator[] mScaleAnimator = new ValueAnimator[4];
        this.mPointGapInpx = dp2px(POINT_DST_GAP);
        for (int i = 0; i < 4; i++) {
            this.mBalls[i].setMoveRange((this.mCenterCircle.x + this.mRadiusCircle) - this.mRadiusBall, this.mCenterBall.x - (((float) i) * this.mPointGapInpx));
            mMoveAnimator[i] = ObjectAnimator.ofFloat(this.mBalls[i], "Cx", new float[]{this.mBalls[i].getFrom(), this.mBalls[i].getTo()});
            mMoveAnimator[i].setDuration(900);
            mMoveAnimator[i].setStartDelay(((long) i) * 350);
            mMoveAnimator[i].setInterpolator(this.mInterpolatorMove);
            mMoveAnimator[i].addListener(new MoveAnimatorListener(i));
            mScaleAnimator[i] = ObjectAnimator.ofFloat(this.mBalls[i], "Radius", new float[]{this.mRadiusBall, 0.0f});
            mScaleAnimator[i].setDuration(900);
            mScaleAnimator[i].setStartDelay(((long) i) * 350);
            mScaleAnimator[i].setInterpolator(new AccelerateInterpolator());
            animators.add(mMoveAnimator[i]);
            animators.add(mScaleAnimator[i]);
        }
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.addListener(new AnimatorSetListener());
        this.mAnimatorSet.playTogether(animators);
    }

    private void doUpdate() {
        if (this.mCallback != null) {
            this.mCallback.onUpdate();
        }
        if (this.mAnimatorSet != null && this.mAnimatorSet.isRunning()) {
            this.mHandler.postDelayed(this.mUpdateRunnable, 60);
        }
    }

    public void onBallAnimationStart(int index) {
        if (index == 0) {
            this.mCircleLineWidthScale = POINT_DST_GAP;
        }
        if (index == 3) {
            this.mCircleLineWidthScale = POINT_DST_GAP - this.mBalls[3].getFraction();
        }
        if (this.mCallback != null) {
            this.mCallback.onCircleLineWidthChange(this.mCircleLineWidthScale);
        }
    }
}
