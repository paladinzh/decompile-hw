package com.android.deskclock.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.deskclock.R;
import java.util.ArrayList;
import java.util.List;

public class PanelPath {
    private Drawable bgNumber;
    private int centrballX;
    private int centtballY;
    private final int[] color = new int[]{-16777216, 0};
    private int dxintC = 18;
    boolean isAnimaShow = false;
    private AnimatorSet mAnimatorSet;
    private final Interpolator mBallInterpolator = new PathInterpolator(0.72f, 0.06f, 0.13f, 1.0f);
    private ValueAnimator mBounceAnimator;
    private PointF mBounceCtrlPt0;
    private PointF mBounceCtrlPt1;
    private float mBounceLevel;
    private Callback mCallback;
    private Ball mCenterBall;
    private Path mCenterPath;
    private PointF mControlDownPt;
    private PointF mControlUpPt;
    private Paint mCriPaint;
    private Ball mEndBall;
    private PointF mEndDownPt;
    private Path mEndPath;
    private PointF mEndPt;
    private PointF mEndUpPt;
    private final Interpolator mInterpolator = new PathInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
    private boolean mIsFirstBall;
    private Ball mMoveBall;
    private Path mMovePath;
    private Paint mPaint;
    private RadialGradient mRadialGradient;
    private RectF mRectOfOval;
    private int mRightDex = 3;
    private AnimatorSet mScaleAnimatorSet;
    private final float[] mScalePtsX = new float[]{1.0f, 1.09f, 0.95f, 1.0f};
    private final float[] mScalePtsY = new float[]{1.0f, 1.11f, 0.96f, 1.0f};
    private final float[] mScaleR = new float[]{1.0f, 1.15f, 1.0f};
    private PointF mStartDownPt;
    private PointF mStartPt;
    private PointF mStartUpPt;
    private final long[] mTimePtsX = new long[]{0, 333, 499, 799};
    private final long[] mTimePtsY = new long[]{0, 233, 433, 799};
    private final long[] mTimeTranslate = new long[]{333, 733};
    private final float[] position = new float[]{0.5f, 1.0f};
    private float radiusCenter;
    private Rect rightRect;

    public interface Callback {
        void onUpdateUI();
    }

    static class Ball {
        float cX;
        float cY;
        float rX;
        float rY;

        private void setCX(float cX) {
            this.cX = cX;
        }

        public void setCY(float cY) {
            this.cY = cY;
        }

        public void setRX(float rX) {
            this.rX = rX;
        }

        public void setRY(float rY) {
            this.rY = rY;
        }
    }

    public PanelPath(Context context, Callback callBack) {
        if (context == null) {
            Log.e("PanelPath", "context is null!");
            return;
        }
        this.mCallback = callBack;
        this.mBounceLevel = 0.0f;
        this.mIsFirstBall = true;
        this.mCenterBall = new Ball();
        this.mMoveBall = new Ball();
        this.mEndBall = new Ball();
        this.mRectOfOval = new RectF();
        this.mStartPt = new PointF();
        this.mEndPt = new PointF();
        this.mStartUpPt = new PointF();
        this.mStartDownPt = new PointF();
        this.mEndUpPt = new PointF();
        this.mEndDownPt = new PointF();
        this.mControlUpPt = new PointF();
        this.mControlDownPt = new PointF();
        this.mBounceCtrlPt0 = new PointF();
        this.mBounceCtrlPt1 = new PointF();
        this.mPaint = new Paint();
        this.mPaint.setColor(context.getResources().getColor(R.color.transparency_100_white));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.FILL);
        this.mCenterPath = new Path();
        this.mMovePath = new Path();
        this.mEndPath = new Path();
        this.mCriPaint = new Paint();
        this.mCriPaint.setARGB(75, 238, 238, 238);
        this.mCriPaint.setStyle(Style.FILL);
        this.rightRect = new Rect();
        this.bgNumber = context.getResources().getDrawable(R.drawable.img_clock_stopwatch_littledial_number);
        this.dxintC = (int) (context.getResources().getDisplayMetrics().density * 6.0f);
        this.mRightDex = (int) (context.getResources().getDisplayMetrics().density * 1.0f);
    }

    public void setPosition(int cX, int cY, float radiusCenterBall, float radiusMoveBall, float collRadius) {
        this.mCenterBall.setCX((float) cX);
        this.mCenterBall.setCY((float) cY);
        this.mCenterBall.setRX(radiusCenterBall);
        this.mCenterBall.setRY(radiusCenterBall);
        this.mRadialGradient = new RadialGradient((float) cX, (float) cY, 10.0f + radiusCenterBall, this.color, this.position, TileMode.CLAMP);
        this.mCriPaint.setShader(this.mRadialGradient);
        this.centrballX = cX;
        this.centtballY = cY;
        this.radiusCenter = radiusCenterBall;
        float sin45 = (float) Math.sin(0.7853981633974483d);
        float cos45 = (float) Math.cos(0.7853981633974483d);
        this.mStartPt.set(this.mCenterBall.cX + ((radiusCenterBall - radiusMoveBall) * cos45), this.mCenterBall.cY - ((radiusCenterBall - radiusMoveBall) * sin45));
        this.mEndPt.set(((float) cX) + (collRadius * sin45), ((float) cY) - (collRadius * cos45));
        this.mMoveBall.setCX(this.mStartPt.x);
        this.mMoveBall.setCY(this.mStartPt.y);
        this.mMoveBall.setRX(radiusMoveBall - ((float) this.dxintC));
        this.mMoveBall.setRY(radiusMoveBall - ((float) this.dxintC));
        this.mEndBall.setCX(this.mEndPt.x);
        this.mEndBall.setCY(this.mEndPt.y);
        this.mEndBall.setRX(radiusMoveBall - ((float) this.dxintC));
        this.mEndBall.setRY(radiusMoveBall - ((float) this.dxintC));
        float dx = this.mEndBall.rX + ((float) this.dxintC);
        this.rightRect.set((int) ((this.mEndBall.cX - dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cY - dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cX + dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cY + dx) + ((float) this.mRightDex)));
        this.bgNumber.setBounds(this.rightRect);
        initAnimation();
    }

    public void draw(Canvas canvas) {
        drawCenterBall(canvas);
        drawUpRightBall(canvas);
        drawMovingBall(canvas);
    }

    private void drawCenterBall(Canvas canvas) {
        if (this.isAnimaShow) {
            this.mCenterPath.reset();
            rect4Oval(this.mCenterBall.cX, this.mCenterBall.cY, this.mCenterBall.rX, this.mCenterBall.rY, this.mRectOfOval);
            this.mCenterPath.moveTo(this.mCenterBall.cX + this.mCenterBall.rX, this.mCenterBall.cY);
            this.mCenterPath.arcTo(this.mRectOfOval, 0.0f, 270.0f, true);
            if (this.mBounceAnimator == null || !this.mBounceAnimator.isRunning()) {
                this.mCenterPath.arcTo(this.mRectOfOval, 270.0f, 90.0f, false);
            } else {
                float yShift = (this.mCenterBall.rY * 0.55f) * ((this.mBounceLevel * 0.25f) + 1.0f);
                this.mBounceCtrlPt0.set(this.mCenterBall.cX + ((this.mCenterBall.rX * 0.55f) * ((this.mBounceLevel * 0.25f) + 1.0f)), this.mCenterBall.cY - this.mCenterBall.rY);
                this.mBounceCtrlPt1.set(this.mCenterBall.cX + this.mCenterBall.rX, this.mCenterBall.cY - yShift);
                this.mCenterPath.cubicTo(this.mBounceCtrlPt0.x, this.mBounceCtrlPt0.y, this.mBounceCtrlPt1.x, this.mBounceCtrlPt1.y, this.mCenterBall.cX + this.mCenterBall.rX, this.mCenterBall.cY);
            }
            this.mRadialGradient = new RadialGradient(this.mCenterBall.cX, this.mCenterBall.cY, this.mCenterBall.rX + 10.0f, this.color, this.position, TileMode.CLAMP);
            this.mCriPaint.setShader(this.mRadialGradient);
            canvas.drawCircle(this.mCenterBall.cX, this.mCenterBall.cY, this.mCenterBall.rX + 15.0f, this.mCriPaint);
            canvas.drawPath(this.mCenterPath, this.mPaint);
            return;
        }
        canvas.drawCircle((float) this.centrballX, (float) this.centtballY, this.radiusCenter + 15.0f, this.mCriPaint);
        canvas.drawCircle((float) this.centrballX, (float) this.centtballY, this.radiusCenter, this.mPaint);
    }

    private void drawUpRightBall(Canvas canvas) {
        if (!this.mIsFirstBall) {
            this.mEndPath.reset();
            rect4Oval(this.mEndBall.cX, this.mEndBall.cY, this.mEndBall.rX, this.mEndBall.rY, this.mRectOfOval);
            this.mEndPath.addOval(this.mRectOfOval, Direction.CCW);
            float dx = this.mEndBall.rX + ((float) this.dxintC);
            this.rightRect.set((int) ((this.mEndBall.cX - dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cY - dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cX + dx) + ((float) this.mRightDex)), (int) ((this.mEndBall.cY + dx) + ((float) this.mRightDex)));
            this.bgNumber.setBounds(this.rightRect);
            this.bgNumber.draw(canvas);
            canvas.drawPath(this.mEndPath, this.mPaint);
        }
    }

    private void drawMovingBall(Canvas canvas) {
        if (this.isAnimaShow) {
            this.mMovePath.reset();
            float totalDis = (float) Math.hypot((double) (this.mEndPt.x - this.mStartPt.x), (double) (this.mEndPt.y - this.mStartPt.y));
            float currDis = (float) Math.hypot((double) (this.mMoveBall.cX - this.mStartPt.x), (double) (this.mMoveBall.cY - this.mStartPt.y));
            if (!this.mIsFirstBall && currDis > totalDis - (this.mMoveBall.rX * 2.0f)) {
                buildMetaPath(this.mEndBall.cX, this.mEndBall.cY, this.mMoveBall.cX, this.mMoveBall.cY, this.mEndBall.rX, this.mMoveBall.rX, -2.356194490192345d);
            } else if (currDis < this.mMoveBall.rX * 2.5f) {
                buildMetaPath4Oval(this.mCenterBall.cX, this.mCenterBall.cY, this.mMoveBall.cX, this.mMoveBall.cY, this.mCenterBall.rX, this.mCenterBall.rY, this.mMoveBall.rX, this.mMoveBall.rX * 0.5f, 0.7853981633974483d);
            } else {
                rect4Oval(this.mMoveBall.cX, this.mMoveBall.cY, this.mMoveBall.rX, this.mMoveBall.rY, this.mRectOfOval);
                this.mMovePath.addOval(this.mRectOfOval, Direction.CCW);
            }
            boolean isBounceDis = this.mMoveBall.rX * 2.5f < currDis && currDis < this.mMoveBall.rX * 3.0f;
            if (!(!isBounceDis || this.mBounceAnimator == null || this.mBounceAnimator.isRunning())) {
                this.mBounceAnimator.start();
            }
            boolean isScaleDis = totalDis - currDis < this.mEndBall.rX * 2.0f && totalDis - currDis > this.mEndBall.rX;
            if (!(this.mIsFirstBall || !isScaleDis || this.mScaleAnimatorSet == null || this.mScaleAnimatorSet.isRunning())) {
                this.mScaleAnimatorSet.start();
            }
            canvas.drawPath(this.mMovePath, this.mPaint);
        }
    }

    private void buildMetaPath(float cx0, float cy0, float cx1, float cy1, float r0, float r1, double thetaOffset) {
        double d;
        float dist = (float) Math.hypot((double) (cx1 - cx0), (double) (cy1 - cy0));
        float metaDist = dist - (r0 - r1);
        float ratio = metaDist / (2.0f * r0) < 0.5f ? 0.0f : (metaDist / (2.0f * r0)) - 0.5f;
        double thetaShift = Math.acos((double) ((((r0 * r0) + (dist * dist)) - (r1 * r1)) / ((2.0f * r0) * dist)));
        double thetaUp0 = (((double) (1.0f - (1.0f * ratio))) * 1.5707963267948966d) + thetaOffset;
        double thetaDown0 = (((double) (1.0f - (1.0f * ratio))) * -1.5707963267948966d) + thetaOffset;
        double thetaUp1 = (((double) ((1.0f * ratio) + 1.0f)) * 1.5707963267948966d) + thetaOffset;
        double thetaDown1 = (((double) ((1.0f * ratio) + 1.0f)) * -1.5707963267948966d) + thetaOffset;
        if (thetaShift < 0.3d) {
            d = 0.3d;
        } else {
            d = thetaShift;
        }
        double thetaCtrlUp = d + thetaOffset;
        if (thetaShift < 0.3d) {
            thetaShift = 0.3d;
        }
        double thetaCtrldown = (-thetaShift) + thetaOffset;
        this.mStartUpPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaUp0))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaUp0))));
        this.mStartDownPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaDown0))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaDown0))));
        this.mEndUpPt.set((float) (((double) cx1) + (((double) r1) * Math.cos(thetaUp1))), (float) (((double) cy1) - (((double) r1) * Math.sin(thetaUp1))));
        this.mEndDownPt.set((float) (((double) cx1) + (((double) r1) * Math.cos(thetaDown1))), (float) (((double) cy1) - (((double) r1) * Math.sin(thetaDown1))));
        this.mControlUpPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaCtrlUp))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaCtrlUp))));
        this.mControlDownPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaCtrldown))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaCtrldown))));
        this.mMovePath.moveTo(this.mStartUpPt.x, this.mStartUpPt.y);
        if (metaDist < r0) {
            this.mMovePath.lineTo(this.mEndUpPt.x, this.mEndUpPt.y);
            rect4Oval(cx1, cy1, r1, r1, this.mRectOfOval);
            this.mMovePath.arcTo(this.mRectOfOval, -((float) Math.toDegrees(thetaUp1)), (float) Math.toDegrees((thetaUp1 - thetaOffset) * 2.0d), false);
            this.mMovePath.lineTo(this.mStartDownPt.x, this.mStartDownPt.y);
        } else {
            this.mMovePath.quadTo(this.mControlUpPt.x, this.mControlUpPt.y, this.mEndUpPt.x, this.mEndUpPt.y);
            rect4Oval(cx1, cy1, r1, r1, this.mRectOfOval);
            this.mMovePath.arcTo(this.mRectOfOval, -((float) Math.toDegrees(thetaUp1)), (float) Math.toDegrees((thetaUp1 - thetaOffset) * 2.0d), false);
            this.mMovePath.quadTo(this.mControlDownPt.x, this.mControlDownPt.y, this.mStartDownPt.x, this.mStartDownPt.y);
        }
        this.mMovePath.close();
    }

    private void buildMetaPath4Oval(float cx0, float cy0, float cx1, float cy1, float rx0, float ry0, float r1, float limitMeta, double thetaOffset) {
        float dist = (float) Math.hypot((double) (cx1 - cx0), (double) (cy1 - cy0));
        float r0 = (float) Math.hypot(((double) rx0) * Math.cos(thetaOffset), ((double) ry0) * Math.sin(thetaOffset));
        float metaDist = (dist - r0) + r1 < 0.0f ? 0.0f : (dist - r0) + r1;
        float ratio = metaDist / (this.mMoveBall.rX * 2.0f);
        double thetaShift = 2.0d * Math.asin((double) (r1 / (2.0f * r0)));
        double thetaUp0 = (Math.asin((double) (((1.0f + ratio) * r1) / (2.0f * r0))) * 2.0d) + thetaOffset;
        double thetaDown0 = (Math.asin((double) (((1.0f + ratio) * r1) / (2.0f * r0))) * -2.0d) + thetaOffset;
        double thetaUp1 = (((double) ratio) * 1.5707963267948966d) + thetaOffset;
        double thetaDown1 = (((double) ratio) * -1.5707963267948966d) + thetaOffset;
        if (metaDist < r1) {
            thetaShift = Math.acos((double) ((((r0 * r0) + (dist * dist)) - (r1 * r1)) / ((2.0f * r0) * dist)));
        } else if (metaDist > 2.0f * r1) {
            float rate4CtrlPt = ((dist - r0) - r1) / limitMeta;
            thetaShift *= (double) (1.0f - (1.2f * rate4CtrlPt));
            r0 += (0.4f * rate4CtrlPt) * limitMeta;
            thetaUp1 = (((double) (((limitMeta / r1) * rate4CtrlPt) + 1.0f)) * 1.5707963267948966d) + thetaOffset;
            thetaDown1 = (((double) (((rate4CtrlPt / r1) * rate4CtrlPt) + 1.0f)) * -1.5707963267948966d) + thetaOffset;
        }
        double thetaCtrlUp = thetaShift + thetaOffset;
        double thetaCtrldown = (-thetaShift) + thetaOffset;
        this.mStartUpPt.set((float) (((double) cx0) + (((double) rx0) * Math.cos(thetaUp0))), (float) (((double) cy0) - (((double) ry0) * Math.sin(thetaUp0))));
        this.mStartDownPt.set((float) (((double) cx0) + (((double) rx0) * Math.cos(thetaDown0))), (float) (((double) cy0) - (((double) ry0) * Math.sin(thetaDown0))));
        this.mEndUpPt.set((float) (((double) cx1) + (((double) r1) * Math.cos(thetaUp1))), (float) (((double) cy1) - (((double) r1) * Math.sin(thetaUp1))));
        this.mEndDownPt.set((float) (((double) cx1) + (((double) r1) * Math.cos(thetaDown1))), (float) (((double) cy1) - (((double) r1) * Math.sin(thetaDown1))));
        this.mControlUpPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaCtrlUp))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaCtrlUp))));
        this.mControlDownPt.set((float) (((double) cx0) + (((double) r0) * Math.cos(thetaCtrldown))), (float) (((double) cy0) - (((double) r0) * Math.sin(thetaCtrldown))));
        this.mMovePath.moveTo(this.mStartUpPt.x, this.mStartUpPt.y);
        this.mMovePath.quadTo(this.mControlUpPt.x, this.mControlUpPt.y, this.mEndUpPt.x, this.mEndUpPt.y);
        float radius;
        if (metaDist < r1) {
            radius = ((float) Math.hypot((double) (this.mEndUpPt.x - this.mEndDownPt.x), (double) (this.mEndUpPt.y - this.mEndDownPt.y))) / 2.0f;
            this.mMovePath.quadTo(((this.mEndUpPt.x + this.mEndDownPt.x) / 2.0f) + ((radius * ratio) * 2.0f), ((this.mEndUpPt.y + this.mEndDownPt.y) / 2.0f) - ((radius * ratio) * 2.0f), this.mEndDownPt.x, this.mEndDownPt.y);
        } else if (metaDist < 2.0f * r1) {
            radius = ((float) Math.hypot((double) (this.mEndUpPt.x - this.mEndDownPt.x), (double) (this.mEndUpPt.y - this.mEndDownPt.y))) / 2.0f;
            rect4Oval((this.mEndUpPt.x + this.mEndDownPt.x) / 2.0f, (this.mEndUpPt.y + this.mEndDownPt.y) / 2.0f, radius, radius, this.mRectOfOval);
            this.mMovePath.arcTo(this.mRectOfOval, -135.0f, 180.0f, false);
        } else {
            rect4Oval(cx1, cy1, r1, r1, this.mRectOfOval);
            this.mMovePath.arcTo(this.mRectOfOval, -((float) Math.toDegrees(thetaUp1)), (float) Math.toDegrees((thetaUp1 - thetaOffset) * 2.0d), false);
        }
        this.mMovePath.quadTo(this.mControlDownPt.x, this.mControlDownPt.y, this.mStartDownPt.x, this.mStartDownPt.y);
        this.mMovePath.close();
    }

    private void rect4Oval(float cX, float cY, float rX, float rY, RectF rect) {
        rect.set(cX - rX, cY - rY, cX + rX, cY + rY);
    }

    private void initAnimation() {
        int i;
        List<Animator> animators = new ArrayList();
        ValueAnimator[] xAnimators = new ValueAnimator[3];
        ValueAnimator[] yAnimators = new ValueAnimator[3];
        for (i = 0; i < 3; i++) {
            xAnimators[i] = ObjectAnimator.ofFloat(this.mCenterBall, "rX", new float[]{this.mScalePtsX[i] * this.mCenterBall.rX, this.mScalePtsX[i + 1] * this.mCenterBall.rX});
            xAnimators[i].setStartDelay(this.mTimePtsX[i]);
            xAnimators[i].setDuration(this.mTimePtsX[i + 1] - this.mTimePtsX[i]);
            xAnimators[i].setInterpolator(this.mInterpolator);
            yAnimators[i] = ObjectAnimator.ofFloat(this.mCenterBall, "rY", new float[]{this.mScalePtsY[i] * this.mCenterBall.rY, this.mScalePtsY[i + 1] * this.mCenterBall.rY});
            yAnimators[i].setStartDelay(this.mTimePtsY[i]);
            yAnimators[i].setDuration(this.mTimePtsY[i + 1] - this.mTimePtsY[i]);
            yAnimators[i].setInterpolator(this.mInterpolator);
            yAnimators[i].addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (PanelPath.this.mCallback != null) {
                        PanelPath.this.mCallback.onUpdateUI();
                    }
                }
            });
            animators.add(xAnimators[i]);
            animators.add(yAnimators[i]);
        }
        PropertyValuesHolder propertyX = PropertyValuesHolder.ofFloat("cX", new float[]{this.mStartPt.x, this.mEndPt.x});
        PropertyValuesHolder propertyY = PropertyValuesHolder.ofFloat("cY", new float[]{this.mStartPt.y, this.mEndPt.y});
        ValueAnimator moveAnimator = ObjectAnimator.ofPropertyValuesHolder(this.mMoveBall, new PropertyValuesHolder[]{propertyX, propertyY});
        moveAnimator.setStartDelay(this.mTimeTranslate[0]);
        moveAnimator.setDuration(this.mTimeTranslate[1] - this.mTimeTranslate[0]);
        moveAnimator.setInterpolator(this.mBallInterpolator);
        animators.add(moveAnimator);
        ValueAnimator[] destAnimator = new ValueAnimator[2];
        for (i = 0; i < 2; i++) {
            PropertyValuesHolder propRX = PropertyValuesHolder.ofFloat("rX", new float[]{this.mScaleR[i] * this.mEndBall.rX, this.mScaleR[i + 1] * this.mEndBall.rX});
            PropertyValuesHolder propRY = PropertyValuesHolder.ofFloat("rY", new float[]{this.mScaleR[i] * this.mEndBall.rY, this.mScaleR[i + 1] * this.mEndBall.rY});
            destAnimator[i] = ObjectAnimator.ofPropertyValuesHolder(this.mEndBall, new PropertyValuesHolder[]{propRX, propRY});
            destAnimator[i].setDuration(180);
            destAnimator[i].setInterpolator(this.mInterpolator);
            destAnimator[i].addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (PanelPath.this.mCallback != null) {
                        PanelPath.this.mCallback.onUpdateUI();
                    }
                }
            });
        }
        this.mScaleAnimatorSet = new AnimatorSet();
        this.mScaleAnimatorSet.playSequentially(new Animator[]{destAnimator[0], destAnimator[1]});
        this.mBounceAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mBounceAnimator.setDuration(400);
        this.mBounceAnimator.setInterpolator(new SimpleBoundInterpolator(2.25f));
        this.mBounceAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                PanelPath.this.mBounceLevel = ((Float) animation.getAnimatedValue()).floatValue() * (1.0f - (((float) animation.getCurrentPlayTime()) / ((float) animation.getDuration())));
                if (PanelPath.this.mScaleAnimatorSet != null && !PanelPath.this.mScaleAnimatorSet.isRunning() && PanelPath.this.mCallback != null) {
                    PanelPath.this.mCallback.onUpdateUI();
                }
            }
        });
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.playTogether(animators);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                PanelPath.this.mIsFirstBall = false;
                PanelPath.this.isAnimaShow = false;
            }
        });
    }

    public void start() {
        if (this.mAnimatorSet != null && !this.mAnimatorSet.isRunning() && this.mScaleAnimatorSet != null && !this.mScaleAnimatorSet.isRunning()) {
            this.isAnimaShow = true;
            this.mAnimatorSet.start();
        }
    }

    public void resetTO() {
        if (this.mAnimatorSet != null && this.mAnimatorSet.isRunning()) {
            this.mAnimatorSet.end();
        }
        reset();
        if (this.mCallback != null) {
            this.mCallback.onUpdateUI();
        }
    }

    private void reset() {
        this.mMoveBall.setCX(this.mStartPt.x);
        this.mMoveBall.setCY(this.mStartPt.y);
        this.mIsFirstBall = true;
        this.mBounceLevel = 0.0f;
        this.mCenterPath.reset();
        this.mMovePath.reset();
        this.mEndPath.reset();
    }

    public void setIsFirstBall(boolean istrue) {
        this.mIsFirstBall = istrue;
    }
}
