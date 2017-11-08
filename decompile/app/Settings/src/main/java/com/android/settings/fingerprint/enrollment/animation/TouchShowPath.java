package com.android.settings.fingerprint.enrollment.animation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class TouchShowPath {
    private double mAllDrawLength = 0.0d;
    private int mGroup;
    TouchShowPath mNext;
    private int mOrientation;
    private RectF mOutRectF;
    private Path mPainPath = new Path();
    private float mRadius;
    private int mStage;
    private float mStartAngle;
    private float mSweepAngle;
    private float mXLatitute;
    private float mYLatitute;

    public int getStage() {
        return this.mStage;
    }

    public int getGroup() {
        return this.mGroup;
    }

    public void setmRadius(float mRadius) {
        this.mRadius = AnimationUtil.getmScalePathData() * mRadius;
    }

    public void setmSweepAngle(float mSweepAngle) {
        if (mSweepAngle < 0.0f) {
            this.mOrientation = -1;
        } else {
            this.mOrientation = 1;
        }
        this.mSweepAngle = Math.abs(mSweepAngle);
    }

    public void setmStartAngle(float mStartAngle) {
        this.mStartAngle = mStartAngle;
    }

    public void setmXLatitute(float mXLatitute) {
        this.mXLatitute = (AnimationUtil.getmScalePathData() * mXLatitute) - 225.0f;
    }

    public void setmYLatitute(float mYLatitute) {
        this.mYLatitute = (AnimationUtil.getmScalePathData() * mYLatitute) - 155.0f;
    }

    public void setmStage(float mStage) {
        this.mStage = Math.round(mStage);
    }

    public void setmGroup(float mGroup) {
        this.mGroup = Math.round(mGroup);
    }

    public void calculateSquare() {
        this.mOutRectF = new RectF(this.mXLatitute - this.mRadius, this.mYLatitute - this.mRadius, this.mXLatitute + this.mRadius, this.mYLatitute + this.mRadius);
    }

    public void drawPath(Canvas canvas, double needDrawLength, Paint paint) {
        if (canvas != null && needDrawLength != 0.0d && this.mPainPath != null) {
            this.mPainPath.reset();
            if (needDrawLength < ((double) (this.mSweepAngle * this.mRadius))) {
                this.mPainPath.arcTo(this.mOutRectF, this.mStartAngle, (((float) needDrawLength) / this.mRadius) * ((float) this.mOrientation));
            } else if (needDrawLength >= ((double) (this.mSweepAngle * this.mRadius))) {
                this.mPainPath.arcTo(this.mOutRectF, this.mStartAngle, this.mSweepAngle * ((float) this.mOrientation));
            }
            canvas.drawPath(this.mPainPath, paint);
            if (this.mNext != null && needDrawLength >= ((double) (this.mSweepAngle * this.mRadius))) {
                this.mNext.drawPath(canvas, needDrawLength - ((double) (this.mSweepAngle * this.mRadius)), paint);
            }
        }
    }

    public double allDrawLength() {
        if (this.mAllDrawLength != 0.0d) {
            return this.mAllDrawLength;
        }
        if (this.mNext != null) {
            this.mAllDrawLength = ((double) (this.mSweepAngle * this.mRadius)) + this.mNext.allDrawLength();
        } else {
            this.mAllDrawLength = (double) (this.mSweepAngle * this.mRadius);
        }
        return this.mAllDrawLength;
    }

    public String toString() {
        return "TouchShowPath [sweepAngle=" + this.mSweepAngle + ", startAngle=" + this.mStartAngle + ", altitudeX=" + this.mXLatitute + ", altitudeY=" + this.mYLatitute + ", radius=" + this.mRadius + ", stage=" + this.mStage + ", group=" + this.mGroup + ", orientation=" + this.mOrientation + "]";
    }
}
